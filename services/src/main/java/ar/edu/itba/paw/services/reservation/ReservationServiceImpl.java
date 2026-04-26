package ar.edu.itba.paw.services.reservation;

import ar.edu.itba.paw.models.auction.Auction;
import ar.edu.itba.paw.models.pack.Pack;
import ar.edu.itba.paw.models.reservation.PickupByCodeError;
import ar.edu.itba.paw.models.reservation.Reservation;
import ar.edu.itba.paw.models.reservation.ReservationToken;
import ar.edu.itba.paw.models.user.User;
import ar.edu.itba.paw.models.user.Commerce;
import ar.edu.itba.paw.persistence.AuctionDao;
import ar.edu.itba.paw.persistence.PackDao;
import ar.edu.itba.paw.persistence.ReservationDao;
import ar.edu.itba.paw.persistence.ReservationTokenDao;
import ar.edu.itba.paw.services.commerce.CommerceService;
import ar.edu.itba.paw.services.pack.DirectReservationCheck;
import ar.edu.itba.paw.services.user.ClientService;
import ar.edu.itba.paw.services.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

@Service
public class ReservationServiceImpl implements ReservationService {

    private static final int PICKUP_WINDOW_MAX_LEN = 512;

    private static final int PICKUP_CODE_LEN = 5;

    private static final String PICKUP_CODE_ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    private static final int PICKUP_CODE_MAX_ATTEMPTS = 64;

    private static final DateTimeFormatter DATE_ONLY_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static final SecureRandom PICKUP_CODE_RANDOM = new SecureRandom();

    private final UserService userService;
    private final ClientService clientService;
    private final ReservationDao reservationDao;
    private final ReservationTokenDao reservationTokenDao;
    private final PackDao packDao;
    private final CommerceService commerceService;
    private final ReservationMailService reservationMailService;
    private final AuctionDao auctionDao;
    private final ZoneId displayZone;

    @Autowired
    public ReservationServiceImpl(final UserService userService,
            final ClientService clientService,
            final ReservationDao reservationDao,
            final ReservationTokenDao reservationTokenDao,
            final PackDao packDao,
            final ReservationMailService reservationMailService,
            final CommerceService commerceService,
            final AuctionDao auctionDao,
            final ZoneId displayZone) {
        this.userService = userService;
        this.clientService = clientService;
        this.reservationDao = reservationDao;
        this.reservationTokenDao = reservationTokenDao;
        this.packDao = packDao;
        this.reservationMailService = reservationMailService;
        this.commerceService = commerceService;
        this.auctionDao = auctionDao;
        this.displayZone = displayZone;
    }

    @Transactional
    @Override
    public Reservation createReservation(final long packId, final long userId, final int quantity,
            final double unitPrice,
            final String pickupWindow, final String baseUrl) {
        if (quantity < 1) {
            throw new IllegalArgumentException("quantity must be >= 1");
        }
        if (pickupWindow != null && pickupWindow.length() > PICKUP_WINDOW_MAX_LEN) {
            throw new IllegalArgumentException("pickup_window must be at most " + PICKUP_WINDOW_MAX_LEN + " characters");
        }

        final User user = userService.findById(userId)
                .orElseThrow(() -> new IllegalStateException("User not found for id: " + userId));
        if (user.getRole() != User.Role.CLIENT) {
            throw new IllegalStateException("Only CLIENT users can create reservations");
        }
        clientService.findByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("Client profile not found for user id: " + userId));

        if (!packDao.decrementStock(packId, quantity)) {
            throw new IllegalStateException("Could not decrement stock for pack: " + packId);
        }

        final LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        final double lineTotal = unitPrice * quantity;

        final Reservation reservation = reservationDao.createReservation(
                user.getId(),
                packId,
                now,
                lineTotal,
                Reservation.Status.RESERVED,
                pickUniquePickupCode(),
                null,
                quantity,
                pickupWindow);

        final Long commerceId = packDao.findById(packId)
                .orElseThrow(() -> new IllegalStateException("Pack not found after stock update: " + packId))
                .getCommerceId();
        final User commerceUser = userService.findById(commerceId)
                .orElseThrow(() -> new IllegalStateException("Commerce user not found for id: " + commerceId));
        final String commerceEmail = commerceUser.getEmail();
        final Locale commerceLocale = commerceUser.getLocale();

        final String pickupDateStr = computePickupDateStr(reservation);
        final boolean auctionReservation = baseUrl == null || baseUrl.trim().isEmpty();

        if (auctionReservation) {
            reservationMailService.sendAuctionWinnerCodeToClient(reservation, user.getEmail(), pickupDateStr,
                user.getLocale());
            reservationMailService.sendAuctionWinnerCodeToCommerce(reservation, commerceEmail, pickupDateStr,
                commerceLocale);
        } else {
            final String acceptToken = UUID.randomUUID().toString();
            final String rejectToken = UUID.randomUUID().toString();
            final LocalDateTime tokenCreatedAt = LocalDateTime.now(ZoneOffset.UTC);
            final LocalDateTime tokenExpiresAt = tokenCreatedAt.plusHours(48);

            reservationTokenDao.create(acceptToken, reservation.getId(), ReservationToken.Action.ACCEPT,
                    tokenCreatedAt, tokenExpiresAt);
            reservationTokenDao.create(rejectToken, reservation.getId(), ReservationToken.Action.REJECT,
                    tokenCreatedAt, tokenExpiresAt);

            reservationMailService.sendReservationRequestToCommerce(reservation, commerceEmail, baseUrl,
                acceptToken, rejectToken, pickupDateStr, commerceLocale);
            reservationMailService.sendReservationCodeToClient(reservation, user.getEmail(), pickupDateStr,
                user.getLocale());
        }

        return reservation;
    }

    private String pickUniquePickupCode() {
        for (int attempt = 0; attempt < PICKUP_CODE_MAX_ATTEMPTS; attempt++) {
            final String code = generatePickupCode();
            if (reservationDao.findByPickupCode(code).isEmpty()) {
                return code;
            }
        }
        throw new IllegalStateException("Could not allocate unique pickup code after " + PICKUP_CODE_MAX_ATTEMPTS
                + " attempts");
    }

    private static String generatePickupCode() {
        final StringBuilder sb = new StringBuilder(PICKUP_CODE_LEN);
        for (int i = 0; i < PICKUP_CODE_LEN; i++) {
            sb.append(PICKUP_CODE_ALPHABET.charAt(PICKUP_CODE_RANDOM.nextInt(PICKUP_CODE_ALPHABET.length())));
        }
        return sb.toString();
    }

    @Override
    public Optional<Reservation> findById(final Long id) {
        return reservationDao.findById(id);
    }

    @Override
    public List<Reservation> findByCustomerId(final Long customerId) {
        if (customerId == null) {
            return Collections.emptyList();
        }
        return reservationDao.findByCustomerId(customerId);
    }

    @Override
    public List<Reservation> findByCommerceId(final Long commerceId) {
        if (commerceId == null) {
            return Collections.emptyList();
        }
        return reservationDao.findByCommerceId(commerceId);
    }

    @Override
    public void validateReservationBelongsToCommerce(final Long reservationId, final Long commerceUserId) {
        if (reservationId == null || commerceUserId == null) {
            throw new IllegalArgumentException("INVALID_PARAMS");
        }

        final Reservation reservation = reservationDao.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("RESERVATION_NOT_FOUND"));
        
        if (reservation.getPackId() == null) {
            throw new IllegalArgumentException("PACK_NOT_FOUND");
        }

        final boolean isOwned = packDao.findById(reservation.getPackId())
                .map(pack -> commerceUserId.equals(pack.getCommerceId()))
                .orElse(false);
        
        if (!isOwned) {
            throw new IllegalArgumentException("WRONG_COMMERCE");
        }
    }

    @Override
    public String computePickupDateStr(final Reservation reservation) {
        if (reservation == null || reservation.getReservationDate() == null) {
            return "-";
        }

        LocalDate pickupDate;
        try {
            pickupDate = ZonedDateTime.of(reservation.getReservationDate(), ZoneOffset.UTC)
                    .withZoneSameInstant(displayZone)
                    .toLocalDate();
        } catch (final Exception e) {
            pickupDate = reservation.getReservationDate().toLocalDate();
        }

        try {
            final ar.edu.itba.paw.models.pack.Pack pack = packDao.findById(reservation.getPackId()).orElse(null);
            if (pack != null) {
                final Long commerceId = pack.getCommerceId();
                if (commerceId != null) {
                    final Optional<Commerce> maybeCommerce = commerceService.findByUserId(commerceId);
                    if (maybeCommerce.isPresent()) {
                        final Commerce commerce = maybeCommerce.get();
                        final String closing = commerce.getClosingTime();
                        if (closing != null) {
                            try {
                                final LocalTime closeT = LocalTime.parse(closing);
                                final LocalTime resTime = ZonedDateTime.of(reservation.getReservationDate(), ZoneOffset.UTC)
                                        .withZoneSameInstant(displayZone)
                                        .toLocalTime();
                                if (resTime.isAfter(closeT) || resTime.equals(closeT)) {
                                    pickupDate = pickupDate.plusDays(1);
                                }
                            } catch (final Exception e) {
                                // parse error, fallback to same-day
                            }
                        }
                    }
                }
            }
        } catch (final Exception e) {
            // any issue, fallback to same-day
        }

        return pickupDate.format(DATE_ONLY_FORMATTER);
    }

    @Transactional
    @Override
    public Reservation confirmPickup(final Long id) {
        final Reservation reservation = reservationDao.findById(id)
                .orElseThrow(() -> new IllegalStateException("Reservation not found: " + id));
        if (reservation.getStatus() != Reservation.Status.RESERVED) {
            throw new IllegalStateException("Cannot confirm pickup: reservation status is " + reservation.getStatus());
        }
        return reservationDao.confirmPickup(id, LocalDateTime.now(ZoneOffset.UTC));
    }

    @Transactional
    @Override
    public Reservation rejectReservationForCommerce(final Long reservationId, final Long commerceUserId) {
        validateReservationBelongsToCommerce(reservationId, commerceUserId);
        return rejectReservation(reservationId);
    }

    @Transactional
    @Override
    public Reservation rejectReservation(final Long reservationId) {
        final Reservation reservation = reservationDao.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("RESERVATION_NOT_FOUND"));

        final Reservation.Status status = reservation.getStatus();
        if (status == Reservation.Status.CANCELED) {
            throw new IllegalStateException("ALREADY_CANCELED");
        }
        if (status == Reservation.Status.PAID) {
            throw new IllegalStateException("ALREADY_COMPLETED");
        }
        if (status != Reservation.Status.RESERVED) {
            throw new IllegalStateException("INVALID_STATUS");
        }

        if (reservation.getPackId() == null) {
            throw new IllegalStateException("PACK_NOT_FOUND");
        }

        final int quantity = reservation.getQuantity() == null ? 1 : reservation.getQuantity();
        if (!packDao.incrementStock(reservation.getPackId(), quantity)) {
            throw new IllegalStateException("STOCK_RESTORE_FAILED");
        }

        final Reservation canceledReservation = reservationDao.updateStatus(reservation.getId(), Reservation.Status.CANCELED);
        final User clientUser = userService.findById(canceledReservation.getCustomerId())
                .orElseThrow(() -> new IllegalStateException("Customer user not found for reservation id: "
                        + canceledReservation.getId()));
        final String clientEmail = clientUser.getEmail();

        reservationMailService.sendReservationRejectedToClient(canceledReservation, clientEmail,
            clientUser.getLocale());
        return canceledReservation;
    }

    @Override
    public DirectReservationCheck checkDirectPackReservation(final long packId, final int quantity) {
        final Optional<Pack> packOpt = packDao.findById(packId)
                .filter(p -> Boolean.TRUE.equals(p.getActive()));
        if (packOpt.isEmpty()) {
            return DirectReservationCheck.blocked(DirectReservationCheck.Outcome.PACK_UNAVAILABLE, null);
        }
        final Pack pack = packOpt.get();
        final Optional<Auction> auctionForReserve = auctionDao.findByPackId(packId);
        if (auctionForReserve.isPresent()) {
            final Auction a = auctionForReserve.get();
            if (a.getStatus() == Auction.Status.ACTIVE) {
                return DirectReservationCheck.blocked(DirectReservationCheck.Outcome.AUCTION_ACTIVE, pack);
            }
            if (a.getStatus() == Auction.Status.FINISHED) {
                return DirectReservationCheck.blocked(DirectReservationCheck.Outcome.AUCTION_ENDED_NO_DIRECT, pack);
            }
        }
        if (pack.getStock() != null && quantity > pack.getStock().intValue()) {
            return DirectReservationCheck.blocked(DirectReservationCheck.Outcome.QUANTITY_EXCEEDS_STOCK, pack);
        }
        final Double finalPrice = pack.getFinalPrice();
        if (finalPrice == null) {
            return DirectReservationCheck.blocked(DirectReservationCheck.Outcome.MISSING_FINAL_PRICE, pack);
        }
        return DirectReservationCheck.ok(pack, finalPrice);
    }

    @Transactional
    @Override
    public PickupByCodeResult confirmPickupByCode(final String pickupCode, final Long commerceUserId) {
        if (pickupCode == null || pickupCode.isBlank()) {
            return PickupByCodeResult.failure(PickupByCodeError.EMPTY);
        }

        final String normalizedCode = pickupCode.trim().toUpperCase(java.util.Locale.ROOT);

        final Optional<Reservation> resOpt = reservationDao.findByPickupCode(normalizedCode);
        if (resOpt.isEmpty()) {
            return PickupByCodeResult.failure(PickupByCodeError.NOT_FOUND);
        }
        final Reservation reservation = resOpt.get();

        final Reservation.Status status = reservation.getStatus();
        if (status == Reservation.Status.PAID) {
            return PickupByCodeResult.failure(PickupByCodeError.ALREADY_COMPLETED);
        }
        if (status == Reservation.Status.CANCELED) {
            return PickupByCodeResult.failure(PickupByCodeError.ALREADY_CANCELED);
        }
        if (status != Reservation.Status.RESERVED) {
            return PickupByCodeResult.failure(PickupByCodeError.NOT_FOUND);
        }

        final Long packId = reservation.getPackId();
        if (packId == null) {
            return PickupByCodeResult.failure(PickupByCodeError.NOT_FOUND);
        }
        final Long packCommerceId = packDao.findById(packId)
                .map(Pack::getCommerceId)
                .orElse(null);
        if (packCommerceId == null || !packCommerceId.equals(commerceUserId)) {
            return PickupByCodeResult.failure(PickupByCodeError.WRONG_COMMERCE);
        }

        final Reservation confirmed = reservationDao.confirmPickup(reservation.getId(),
                LocalDateTime.now(ZoneOffset.UTC));
        return PickupByCodeResult.success(confirmed);
    }
}
