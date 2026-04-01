package ar.edu.itba.paw.services;

import ar.edu.itba.paw.models.Reservation;
import ar.edu.itba.paw.models.User;
import ar.edu.itba.paw.persistence.ReservationDao;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    public ReservationServiceImpl(final UserService userService, final ClientService clientService,
            final ReservationDao reservationDao) {
        this.userService = userService;
        this.clientService = clientService;
        this.reservationDao = reservationDao;
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

        return reservationDao.createReservation(
                user.getId(),
                packId,
                now,
                lineTotal,
                Reservation.Status.RESERVED,
                UUID.randomUUID().toString(),
                null,
                quantity,
                pickupWindow);
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
