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

    void validateReservationBelongsToCommerce(Long reservationId, Long commerceUserId);

    Reservation confirmPickup(final Long id);

    /**
     * Validates a pickup code belongs to a RESERVED reservation owned by the given commerce,
     * then confirms pickup (status → PAID, sets pickupConfirmationDate).
     *
     * @param pickupCode      the 5-char alphanumeric code shown by the client
     * @param commerceUserId  the user-id of the authenticated commerce
     * @return the confirmed Reservation
     * @throws IllegalArgumentException if code is blank, not found, or doesn't belong to commerce
     * @throws IllegalStateException    if reservation is not in RESERVED status
     */
    Reservation confirmPickupByCode(String pickupCode, Long commerceUserId);
}
