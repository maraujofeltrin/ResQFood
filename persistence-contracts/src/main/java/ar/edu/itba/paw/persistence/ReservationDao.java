package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.models.Reservation;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ReservationDao {
    Reservation createReservation(final Long customerId, final Long packId, final LocalDateTime reservationDate,
            final Double finalPrice, final Reservation.Status status, final String pickupCode,
            final LocalDateTime pickupConfirmationDate, final Integer quantity, final String pickupWindow);

    Optional<Reservation> findById(final Long id);

    List<Reservation> findByCustomerId(final Long customerId);

    List<Reservation> findByPackId(final Long packId);

    Reservation updateStatus(final Long id, final Reservation.Status status);

    Optional<Reservation> findByPickupCode(final String pickupCode);

    Reservation confirmPickup(final Long id, final java.time.LocalDateTime pickupConfirmationDate);
}
