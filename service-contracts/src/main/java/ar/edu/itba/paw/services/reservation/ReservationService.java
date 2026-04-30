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

    List<Reservation> findByPackId(final Long packId);

    String computePickupDateStr(Reservation reservation);

    void validateReservationBelongsToCommerce(Long reservationId, Long commerceUserId);

    /**
     * Validates a direct (non-auction) reservation for an active pack.
     */
    DirectReservationCheck checkDirectPackReservation(long packId, int quantity);

    Reservation confirmPickup(final Long id);

    /**
     * Rejects a RESERVED reservation for a commerce-owned pack without using exceptions
     * for expected failures.
     */
    ReservationRejectionResult tryRejectReservationForCommerce(Long reservationId, Long commerceUserId);

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
     * Core rejection: validates state, restores stock, marks CANCELED, notifies client.
     * Does NOT check commerce ownership — caller must verify beforehand.
     *
     * @param reservationId reservation to reject
     * @return the updated Reservation in CANCELED status
     * @throws IllegalStateException if reservation cannot be rejected in its current state
     */
    Reservation rejectReservation(Long reservationId);

    /**
     * Consumes an ACCEPT token after validating commerce ownership and pickup code,
     * then confirms pickup.
     */
    ReservationTokenActionResult acceptReservationTokenWithPickupCode(
            String token, String pickupCode, Long commerceUserId);

    /**
     * Consumes a REJECT token after validating commerce ownership,
     * then rejects the reservation (stock restore + status + email).
     */
    ReservationTokenActionResult rejectReservationToken(String token, Long commerceUserId);

    /**
     * Validates a pickup code belongs to a RESERVED reservation owned by the given commerce,
     * then confirms pickup (status → PAID, sets pickupConfirmationDate).
     *
     * @param pickupCode     the 5-char alphanumeric code shown by the client
     * @param commerceUserId the user-id of the authenticated commerce
     * @return success with confirmation, or a typed failure
     */
    PickupByCodeResult confirmPickupByCode(String pickupCode, Long commerceUserId);

    List<Reservation> filterReservations(Long commerceId, Long customerId, String query, Reservation.Status status,
            boolean excludeAuctionPacks, int page, int pageSize);

    int countFilteredReservations(Long commerceId, Long customerId, String query, Reservation.Status status,
            boolean excludeAuctionPacks);

    boolean hasActiveReservation(Long packId, Long customerId);

}
