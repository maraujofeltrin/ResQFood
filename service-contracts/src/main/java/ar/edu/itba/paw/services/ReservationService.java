package ar.edu.itba.paw.services;

import ar.edu.itba.paw.models.Reservation;

import java.util.List;
import java.util.Optional;

public interface ReservationService {

    Reservation createReservation(long packId, long userId, int quantity, double unitPrice, String pickupWindow,
            String baseUrl);

    Optional<Reservation> findById(final Long id);

    List<Reservation> findByCustomerId(final Long customerId);

    List<Reservation> findByCommerceId(final Long commerceId);

    String computePickupDateStr(Reservation reservation);

    Reservation confirmPickup(final Long id);
}
