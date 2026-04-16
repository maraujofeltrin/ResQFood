package ar.edu.itba.paw.services;

import ar.edu.itba.paw.models.Reservation;
import ar.edu.itba.paw.models.User;
import ar.edu.itba.paw.models.Commerce;
import ar.edu.itba.paw.persistence.PackDao;
import ar.edu.itba.paw.persistence.ReservationDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
import java.util.Optional;

@Service
public class ReservationServiceImpl implements ReservationService {

    private static final int PICKUP_WINDOW_MAX_LEN = 512;

    private static final int PICKUP_CODE_LEN = 5;

    private static final String PICKUP_CODE_ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    private static final int PICKUP_CODE_MAX_ATTEMPTS = 64;

    private static final DateTimeFormatter DATE_ONLY_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static final SecureRandom PICKUP_CODE_RANDOM = new SecureRandom();

    /** Contraseña temporal hasta contar con registro/login propio ({@code users.password} NOT NULL). */
    private static final String RESERVATION_USER_PLACEHOLDER_PASSWORD = "__RESERVATION_PENDING_PASSWORD__";

    private final UserService userService;
    private final ClientService clientService;
    private final ReservationDao reservationDao;
    private final PackDao packDao;
    private final CommerceService commerceService;
    private final ReservationMailService reservationMailService;
    private final ZoneId displayZone;

    @Autowired
    public ReservationServiceImpl(final UserService userService, final ClientService clientService,
            final ReservationDao reservationDao,
            final PackDao packDao,
            final ReservationMailService reservationMailService,
            final CommerceService commerceService,
            @Value("${app.display-zone:}") final String displayZone) {
        this.userService = userService;
        this.clientService = clientService;
        this.reservationDao = reservationDao;
        this.packDao = packDao;
        this.reservationMailService = reservationMailService;
        this.commerceService = commerceService;
        this.displayZone = (displayZone == null || displayZone.trim().isEmpty())
                ? ZoneId.of("America/Argentina/Buenos_Aires")
                : ZoneId.of(displayZone.trim());
    }

    @Transactional
    @Override
    public Reservation createReservation(final long packId, final String email, final String firstName,
            final String lastName, final String phone, final int quantity, final double unitPrice,
            final String pickupWindow, final String baseUrl) {
        if (quantity < 1) {
            throw new IllegalArgumentException("quantity must be >= 1");
        }
        if (pickupWindow != null && pickupWindow.length() > PICKUP_WINDOW_MAX_LEN) {
            throw new IllegalArgumentException("pickup_window must be at most " + PICKUP_WINDOW_MAX_LEN + " characters");
        }

        final String displayName = firstName + " " + lastName;
        final User user = userService.findByEmail(email).orElseGet(() -> userService.createUser(email,
                RESERVATION_USER_PLACEHOLDER_PASSWORD, displayName, phone, User.Role.CLIENT));

        clientService.findByUserId(user.getId()).orElseGet(() -> clientService.createClient(user.getId(), firstName,
                lastName, null));

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
        final String commerceEmail = userService.findById(commerceId)
            .map(User::getEmail)
            .orElseThrow(() -> new IllegalStateException("Commerce user not found for id: " + commerceId));

        final String pickupDateStr = computePickupDateStr(reservation);

        reservationMailService.sendReservationRequestToCommerce(reservation, commerceEmail, baseUrl, pickupDateStr);
        reservationMailService.sendReservationCodeToClient(reservation, user.getEmail(), pickupDateStr);

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
            final ar.edu.itba.paw.models.Pack pack = packDao.findById(reservation.getPackId()).orElse(null);
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

    @Override
    public Reservation confirmPickup(final Long id) {
        return reservationDao.confirmPickup(id, java.time.LocalDateTime.now());
    }
}
