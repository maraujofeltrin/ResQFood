package ar.edu.itba.paw.services;

import ar.edu.itba.paw.models.Reservation;
import ar.edu.itba.paw.models.User;
import ar.edu.itba.paw.persistence.PackDao;
import ar.edu.itba.paw.persistence.ReservationDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

@Service
public class ReservationServiceImpl implements ReservationService {

    private static final int PICKUP_WINDOW_MAX_LEN = 512;

    /** Contraseña temporal hasta contar con registro/login propio ({@code users.password} NOT NULL). */
    private static final String RESERVATION_USER_PLACEHOLDER_PASSWORD = "__RESERVATION_PENDING_PASSWORD__";

    private final UserService userService;
    private final ClientService clientService;
    private final ReservationDao reservationDao;
    private final PackDao packDao;
    private final ReservationMailService reservationMailService;
    private final String appBaseUrl;

    @Autowired
    public ReservationServiceImpl(final UserService userService, final ClientService clientService,
            final ReservationDao reservationDao,
            final PackDao packDao,
            final ReservationMailService reservationMailService,
            @Value("${app.base-url}") final String appBaseUrl) {
        this.userService = userService;
        this.clientService = clientService;
        this.reservationDao = reservationDao;
        this.packDao = packDao;
        this.reservationMailService = reservationMailService;
        this.appBaseUrl = appBaseUrl;
    }

    @Override
    public Reservation createReservation(final long packId, final String email, final String firstName,
            final String lastName, final String phone, final int quantity, final double unitPrice,
            final String pickupWindow) {
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

        final LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        final double lineTotal = unitPrice * quantity;

        final Reservation reservation = reservationDao.createReservation(
                user.getId(),
                packId,
                now,
                lineTotal,
                Reservation.Status.RESERVED,
                UUID.randomUUID().toString(),
                null,
                quantity,
                pickupWindow);

        final Long commerceId = packDao.findById(packId)
            .orElseThrow(() -> new IllegalStateException("Pack not found: " + packId))
            .getCommerceId();
        final String commerceEmail = userService.findById(commerceId)
            .map(User::getEmail)
            .orElseThrow(() -> new IllegalStateException("Commerce user not found for id: " + commerceId));

        reservationMailService.sendReservationRequestToCommerce(reservation, commerceEmail, appBaseUrl);
        reservationMailService.sendReservationCodeToClient(reservation, user.getEmail());

        return reservation;
    }

    @Override
    public Optional<Reservation> findById(final Long id) {
        return reservationDao.findById(id);
    }

    @Override
    public Reservation confirmPickup(final Long id) {
        return reservationDao.confirmPickup(id, java.time.LocalDateTime.now());
    }
}
