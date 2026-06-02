package ar.edu.itba.paw.services.security;

/**
 * Resource ownership checks for method-level authorization via SpEL ({@code @own} bean).
 */
public interface OwnershipService {

    /**
     * @throws OwnershipResourceNotFoundException if the pack does not exist or is deleted
     */
    boolean canWritePack(long packId, long currentUserId);

    /**
     * @throws OwnershipResourceNotFoundException if the auction or its pack does not exist
     */
    boolean canWriteAuction(long auctionId, long currentUserId);

    /**
     * Commerce-only write access to a reservation (e.g. reject from dashboard).
     *
     * @throws OwnershipResourceNotFoundException if the reservation or its pack does not exist
     */
    boolean canWriteReservation(long reservationId, long currentUserId);

    /**
     * Commerce-only write access via reservation token (accept/reject links).
     *
     * @throws OwnershipResourceNotFoundException if the token, reservation or pack does not exist
     */
    boolean canWriteToken(String token, long currentUserId);

    /**
     * @throws OwnershipResourceNotFoundException if the notification does not exist
     */
    boolean canWriteNotification(long notificationId, long currentUserId);
}
