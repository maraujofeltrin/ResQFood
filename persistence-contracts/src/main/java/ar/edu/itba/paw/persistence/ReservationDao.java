package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.models.reservation.Reservation;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ReservationDao {
    Reservation createReservation(final Long customerId, final Long packId, final LocalDateTime reservationDate,
            final Double finalPrice, final Reservation.Status status, final String pickupCode,
            final LocalDateTime pickupConfirmationDate, final Integer quantity, final String pickupWindow);

    Optional<Reservation> findById(final Long id);

    List<Reservation> findByCustomerId(final Long customerId);

    List<Reservation> findByCommerceId(final Long commerceId);

    List<Reservation> findByPackId(final Long packId);

    Reservation updateStatus(final Long id, final Reservation.Status status);

    Optional<Reservation> findByPickupCode(final String pickupCode);

    Reservation confirmPickup(final Long id, final java.time.LocalDateTime pickupConfirmationDate);

    List<Reservation> filterReservations(Long commerceId, Long customerId, String query, Reservation.Status status, int page, int pageSize);

    int countFilteredReservations(Long commerceId, Long customerId, String query, Reservation.Status status);

    boolean hasActiveReservation(Long packId, Long customerId);

    boolean hasPaidReservationWithCommerce(Long customerId, Long commerceId);
}
