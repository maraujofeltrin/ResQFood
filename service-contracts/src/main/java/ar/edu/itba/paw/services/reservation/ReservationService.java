package ar.edu.itba.paw.services.reservation;

import ar.edu.itba.paw.models.reservation.Reservation;
import ar.edu.itba.paw.services.pack.DirectReservationCheck;

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

    /**
     * Validates a direct (non-auction) reservation for an active pack.
     */
    DirectReservationCheck checkDirectPackReservation(long packId, int quantity);

    Reservation confirmPickup(final Long id);

    /**
     * Rejects a RESERVED reservation for a commerce-owned pack.
     * Changes status to CANCELED, restores pack stock and notifies the client.
     *
     * @param reservationId  reservation id to reject
     * @param commerceUserId authenticated commerce user id
     * @return the updated Reservation in CANCELED status
     * @throws IllegalArgumentException if ids are invalid or reservation doesn't belong to commerce
     * @throws IllegalStateException    if reservation cannot be rejected in its current state
     */
    Reservation rejectReservationForCommerce(Long reservationId, Long commerceUserId);

    /**
     * Validates a pickup code belongs to a RESERVED reservation owned by the given commerce,
     * then confirms pickup (status → PAID, sets pickupConfirmationDate).
     *
     * @param pickupCode     the 5-char alphanumeric code shown by the client
     * @param commerceUserId the user-id of the authenticated commerce
     * @return success with confirmation, or a typed failure
     */
    PickupByCodeResult confirmPickupByCode(String pickupCode, Long commerceUserId);
}
