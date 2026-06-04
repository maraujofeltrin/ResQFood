package ar.edu.itba.paw.services.reservation;

import ar.edu.itba.paw.models.auction.Auction;
import ar.edu.itba.paw.models.pack.Pack;
import ar.edu.itba.paw.models.reservation.PickupByCodeError;
import ar.edu.itba.paw.models.reservation.Reservation;
import ar.edu.itba.paw.models.reservation.ReservationRejectionError;
import ar.edu.itba.paw.models.reservation.ReservationToken;
import ar.edu.itba.paw.models.user.User;
import ar.edu.itba.paw.persistence.AuctionDao;
import ar.edu.itba.paw.persistence.PackDao;
import ar.edu.itba.paw.persistence.ReservationDao;
import ar.edu.itba.paw.persistence.ReservationTokenDao;
import ar.edu.itba.paw.services.notification.NotificationService;
import ar.edu.itba.paw.services.pack.DirectReservationCheck;
import ar.edu.itba.paw.services.user.ClientService;
import ar.edu.itba.paw.services.user.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ReservationServiceImpl implements ReservationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReservationServiceImpl.class);

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
    private final NotificationService notificationService;
    private final AuctionDao auctionDao;
    private final ZoneId displayZone;

    @Autowired
    public ReservationServiceImpl(final UserService userService,
            final ClientService clientService,
            final ReservationDao reservationDao,
            final ReservationTokenDao reservationTokenDao,
            final PackDao packDao,
            final NotificationService notificationService,
            final AuctionDao auctionDao,
            final ZoneId displayZone) {
        this.userService = userService;
        this.clientService = clientService;
        this.reservationDao = reservationDao;
        this.reservationTokenDao = reservationTokenDao;
        this.packDao = packDao;
        this.notificationService = notificationService;
        this.auctionDao = auctionDao;
        this.displayZone = displayZone;
    }

    @Transactional
    @Override
    public Reservation createReservation(final long packId, final long userId, final int quantity,
            final double unitPrice,
            final String pickupWindow, final boolean isAuction) {
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

        final Pack pack = packDao.findById(packId)
                .orElseThrow(() -> new IllegalStateException("Pack not found after stock update: " + packId));
        final Long commerceId = pack.getCommerceId();
        final User commerceUser = userService.findById(commerceId)
                .orElseThrow(() -> new IllegalStateException("Commerce user not found for id: " + commerceId));
        final String commerceEmail = commerceUser.getEmail();
        final java.util.Locale commerceLocale = commerceUser.getLocale();

        final Reservation persisted = reservationDao.createReservation(
                user.getId(),
                packId,
                now,
                lineTotal,
                Reservation.Status.RESERVED,
                pickUniquePickupCode(),
                null,
                quantity,
                pickupWindow);

        final Reservation reservation = reservationDao.findByIdWithDetails(persisted.getId())
                .orElseThrow(() -> new IllegalStateException("Reservation not found: " + persisted.getId()));
        final String pickupDateStr = computePickupDateStr(reservation);

        if (isAuction) {
            notificationService.notifyAuctionWinnerForClient(reservation, user.getEmail(), pickupDateStr,
                user.getLocale());
            notificationService.notifyAuctionWinnerForCommerce(reservation, commerceEmail, pickupDateStr,
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

            notificationService.notifyReservationRequested(reservation, commerceEmail,
                acceptToken, rejectToken, pickupDateStr, commerceLocale);
            notificationService.notifyReservationCodeIssued(reservation, user.getEmail(), pickupDateStr,
                user.getLocale());
        }

        LOGGER.info("Reservation created: reservationId={}, packId={}, userId={}", reservation.getId(), packId, userId);
        return reservation;
    }

    private String pickUniquePickupCode() {
        for (int attempt = 0; attempt < PICKUP_CODE_MAX_ATTEMPTS; attempt++) {
            final String code = generatePickupCode();
            if (reservationDao.findByPickupCode(code).isEmpty()) {
                return code;
            }
        }
        LOGGER.error("Could not allocate unique pickup code after {} attempts", PICKUP_CODE_MAX_ATTEMPTS);
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

    @Transactional(readOnly = true)
    @Override
    public Optional<Reservation> findById(final Long id) {
        return reservationDao.findById(id);
    }

    @Transactional(readOnly = true)
    @Override
    public List<Reservation> findByCustomerId(final Long customerId) {
        if (customerId == null) {
            return Collections.emptyList();
        }
        return reservationDao.findByCustomerId(customerId);
    }

    @Transactional(readOnly = true)
    @Override
    public List<Reservation> findByCommerceId(final Long commerceId) {
        if (commerceId == null) {
            return Collections.emptyList();
        }
        return reservationDao.findByCommerceId(commerceId);
    }

    @Transactional(readOnly = true)
    @Override
    public List<Reservation> findByPackId(final Long packId) {
        if (packId == null) {
            return Collections.emptyList();
        }
        return reservationDao.findByPackId(packId);
    }



    @Transactional(readOnly = true)
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
            LOGGER.debug("computePickupDateStr: timezone conversion fallback reservationId={}", reservation.getId(), e);
            pickupDate = reservation.getReservationDate().toLocalDate();
        }

        try {
            final Pack pack = reservation.getPack();
            if (pack != null && pack.getCommerce() != null) {
                final String closing = pack.getCommerce().getClosingTime();
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
                        LOGGER.debug("computePickupDateStr: closing time parse fallback reservationId={} packId={}",
                                reservation.getId(), pack.getId(), e);
                    }
                }
            }
        } catch (final Exception e) {
            LOGGER.debug("computePickupDateStr: pack/commerce access fallback reservationId={}",
                    reservation.getId(), e);
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
        final Reservation confirmed = reservationDao.confirmPickup(id, LocalDateTime.now(ZoneOffset.UTC));
        LOGGER.info("Pickup confirmed for reservationId={}", id);
        return confirmed;
    }

    @Transactional
    @Override
    public ReservationServiceResult<ReservationRejectionError> tryRejectReservation(final Long reservationId) {
        return rejectReservationInternal(reservationId);
    }

    @Transactional
    @Override
    public Reservation rejectReservation(final Long reservationId) {
        final ReservationServiceResult<ReservationRejectionError> result = rejectReservationInternal(reservationId);
        return result.reservation().orElseThrow(() -> toRejectionException(result));
    }

    private ReservationServiceResult<ReservationRejectionError> rejectReservationInternal(final Long reservationId) {
        final Reservation reservation = reservationDao.findById(reservationId)
                .orElse(null);
        if (reservation == null) {
            LOGGER.warn("Failed to reject reservation: reservationId={}, error={}", reservationId, ReservationRejectionError.RESERVATION_NOT_FOUND);
            return ReservationServiceResult.failure(ReservationRejectionError.RESERVATION_NOT_FOUND);
        }

        final Reservation.Status status = reservation.getStatus();
        if (status == Reservation.Status.CANCELED) {
            LOGGER.warn("Failed to reject reservation: reservationId={}, error={}", reservationId, ReservationRejectionError.ALREADY_CANCELED);
            return ReservationServiceResult.failure(ReservationRejectionError.ALREADY_CANCELED);
        }
        if (status == Reservation.Status.PAID) {
            LOGGER.warn("Failed to reject reservation: reservationId={}, error={}", reservationId, ReservationRejectionError.ALREADY_COMPLETED);
            return ReservationServiceResult.failure(ReservationRejectionError.ALREADY_COMPLETED);
        }
        if (status != Reservation.Status.RESERVED) {
            LOGGER.warn("Failed to reject reservation: reservationId={}, error={}", reservationId, ReservationRejectionError.INVALID_STATUS);
            return ReservationServiceResult.failure(ReservationRejectionError.INVALID_STATUS);
        }

        if (reservation.getPack() == null) {
            LOGGER.warn("Failed to reject reservation: reservationId={}, error={}", reservationId, ReservationRejectionError.PACK_NOT_FOUND);
            return ReservationServiceResult.failure(ReservationRejectionError.PACK_NOT_FOUND);
        }

        final int quantity = reservation.getQuantity() == null ? 1 : reservation.getQuantity();
        if (!packDao.incrementStock(reservation.getPack().getId(), quantity)) {
            LOGGER.warn("Failed to reject reservation: reservationId={}, error={}", reservationId, ReservationRejectionError.STOCK_RESTORE_FAILED);
            return ReservationServiceResult.failure(ReservationRejectionError.STOCK_RESTORE_FAILED);
        }

        reservationDao.updateStatus(reservation.getId(), Reservation.Status.CANCELED);
        final Reservation canceledReservation = reservationDao.findByIdWithDetails(reservation.getId())
                .orElseThrow(() -> new IllegalStateException("Reservation not found after cancel: " + reservationId));
        final User clientUser = userService.findById(canceledReservation.getCustomer().getUserId())
                .orElseThrow(() -> new IllegalStateException("Customer user not found for reservation id: "
                        + canceledReservation.getId()));
        final String clientEmail = clientUser.getEmail();

        notificationService.notifyReservationRejected(canceledReservation, clientEmail,
            clientUser.getLocale());
        LOGGER.info("Reservation canceled: reservationId={}", reservationId);
        return ReservationServiceResult.success(canceledReservation);
    }

    private RuntimeException toRejectionException(final ReservationServiceResult<ReservationRejectionError> result) {
        final ReservationRejectionError error = result.error()
                .orElse(ReservationRejectionError.INVALID_STATUS);
        switch (error) {
            case INVALID_PARAMS:
            case RESERVATION_NOT_FOUND:
            case PACK_NOT_FOUND:
                return new IllegalArgumentException(error.name());
            case ALREADY_CANCELED:
            case ALREADY_COMPLETED:
            case INVALID_STATUS:
            case STOCK_RESTORE_FAILED:
            default:
                return new IllegalStateException(error.name());
        }
    }

    @Transactional(readOnly = true)
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
    public ReservationServiceResult<PickupByCodeError> confirmPickupByCode(final String pickupCode, final Long commerceUserId) {
        if (pickupCode == null || pickupCode.isBlank()) {
            return ReservationServiceResult.failure(PickupByCodeError.EMPTY);
        }

        final String normalizedCode = pickupCode.trim().toUpperCase(java.util.Locale.ROOT);

        final Optional<Reservation> resOpt = reservationDao.findByPickupCode(normalizedCode);
        if (resOpt.isEmpty()) {
            return ReservationServiceResult.failure(PickupByCodeError.NOT_FOUND);
        }
        final Reservation reservation = resOpt.get();

        final Reservation.Status status = reservation.getStatus();
        if (status == Reservation.Status.PAID) {
            return ReservationServiceResult.failure(PickupByCodeError.ALREADY_COMPLETED);
        }
        if (status == Reservation.Status.CANCELED) {
            return ReservationServiceResult.failure(PickupByCodeError.ALREADY_CANCELED);
        }
        if (status != Reservation.Status.RESERVED) {
            return ReservationServiceResult.failure(PickupByCodeError.NOT_FOUND);
        }

        final Long packId = reservation.getPack().getId();
        if (packId == null) {
            return ReservationServiceResult.failure(PickupByCodeError.NOT_FOUND);
        }
        final Long packCommerceId = reservation.getPack().getCommerce() != null
                ? reservation.getPack().getCommerce().getUserId()
                : null;
        if (packCommerceId == null || !packCommerceId.equals(commerceUserId)) {
            return ReservationServiceResult.failure(PickupByCodeError.WRONG_COMMERCE);
        }

        confirmPickup(reservation.getId());
        final Reservation confirmed = reservationDao.findByIdWithDetails(reservation.getId())
                .orElseThrow(() -> new IllegalStateException("Reservation not found after pickup: " + reservation.getId()));
        return ReservationServiceResult.success(confirmed);
    }

    @Transactional(readOnly = true)
    @Override
    public List<Reservation> filterReservations(Long commerceId, Long customerId, String query,
            Reservation.Status status, boolean excludeAuctionPacks, int page, int pageSize) {
        return reservationDao.filterReservations(commerceId, customerId, query, status, excludeAuctionPacks, page, pageSize);
    }

    @Transactional(readOnly = true)
    @Override
    public int countFilteredReservations(Long commerceId, Long customerId, String query,
            Reservation.Status status, boolean excludeAuctionPacks) {
        return reservationDao.countFilteredReservations(commerceId, customerId, query, status, excludeAuctionPacks);
    }

    @Transactional(readOnly = true)
    @Override
    public boolean hasActiveReservation(Long packId, Long customerId) {
        return reservationDao.hasActiveReservation(packId, customerId);
    }

}
