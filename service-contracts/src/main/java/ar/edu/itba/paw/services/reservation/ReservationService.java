package ar.edu.itba.paw.services.reservation;

import ar.edu.itba.paw.models.reservation.AlreadyUsedTokenStatus;
import ar.edu.itba.paw.models.reservation.PickupByCodeError;
import ar.edu.itba.paw.models.reservation.Reservation;
import ar.edu.itba.paw.models.reservation.ReservationRejectionError;
import ar.edu.itba.paw.models.reservation.ReservationToken;
import ar.edu.itba.paw.models.reservation.ReservationTokenActionError;
import ar.edu.itba.paw.services.pack.DirectReservationCheck;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ReservationService {

    Reservation createReservation(long packId, long userId, int quantity, double unitPrice, String pickupWindow,
            boolean isAuction);

    Optional<Reservation> findById(final Long id);

    Optional<Reservation> findByIdWithDetails(final Long id);

    List<Reservation> findByPackId(final Long packId, int page, int pageSize);
    int countByPackId(Long packId);

    String computePickupDateStr(Reservation reservation);

    /**
     * Validates a direct (non-auction) reservation for an active pack.
     */
    DirectReservationCheck checkDirectPackReservation(long packId, int quantity);

    Reservation confirmPickup(final Long id);

    /**
     * Rejects a RESERVED reservation without using exceptions for expected failures.
     */
    ReservationServiceResult<ReservationRejectionError> tryRejectReservation(Long reservationId);

    /**
     * Core rejection: validates state, restores stock, marks CANCELED, notifies client.
     *
     * @param reservationId reservation to reject
     * @return the updated Reservation in CANCELED status
     * @throws ar.edu.itba.paw.models.reservation.ReservationCreationException if rejection fails
     */
    Reservation rejectReservation(Long reservationId);

    /**
     * Validates a pickup code belongs to a RESERVED reservation owned by the given commerce,
     * then confirms pickup (status -> PAID, sets pickupConfirmationDate).
     *
     * @param pickupCode     the 5-char alphanumeric code shown by the client
     * @param commerceUserId the user-id of the authenticated commerce
     * @return success with confirmation, or a typed failure
     */
    ReservationServiceResult<PickupByCodeError> confirmPickupByCode(String pickupCode, Long commerceUserId);

    List<Reservation> filterReservations(Long commerceId, Long customerId, String query, Reservation.Status status,
            boolean excludeAuctionPacks, int page, int pageSize);

    int countFilteredReservations(Long commerceId, Long customerId, String query, Reservation.Status status,
            boolean excludeAuctionPacks);

    boolean hasActiveReservation(Long packId, Long customerId);

    boolean hasPaidReservationWithCommerce(Long customerId, Long commerceId);

    List<Object[]> countPaidReservationsPerDay(Long commerceId, LocalDateTime from, LocalDateTime to);

    int countPaidReservationsInPeriod(Long commerceId, LocalDateTime from, LocalDateTime to);

    BigDecimal sumRevenueInPeriod(Long commerceId, LocalDateTime from, LocalDateTime to);

    Optional<Long> findBestSellingPackId(Long commerceId, LocalDateTime from, LocalDateTime to);

    long countByStatusInPeriod(Long commerceId, Reservation.Status status, LocalDateTime from, LocalDateTime to);

    BigDecimal averageTicketInPeriod(Long commerceId, LocalDateTime from, LocalDateTime to);

    long countUniqueClientsInPeriod(Long commerceId, LocalDateTime from, LocalDateTime to);

    List<Object[]> findTopSellingPacks(Long commerceId, LocalDateTime from, LocalDateTime to, int limit);

    List<Object[]> findTopClientsByPaidReservations(Long commerceId, LocalDateTime from, LocalDateTime to, int limit);

    long countNewClientsInPeriod(Long commerceId, LocalDateTime from, LocalDateTime to);

    enum TokenValidationResult {
        SUCCESS,
        ALREADY_USED,
        EXPIRED,
        NOT_FOUND
    }

    /**
     * Validates a reservation token for display (GET confirm pages) without consuming it.
     */
    TokenValidationResult validateToken(String token, ReservationToken.Action action);

    Optional<Long> findReservationIdByToken(String token);

    Optional<AlreadyUsedTokenStatus> getAlreadyUsedTokenStatus(String token);

    /**
     * Valida un token ACCEPT, verifica el pickup code, marca el token como usado
     * y confirma la entrega de la reserva en una sola transacción.
     */
    ReservationServiceResult<ReservationTokenActionError> acceptByToken(String token, String pickupCode);

    /**
     * Valida un token REJECT, lo marca como usado y rechaza la reserva
     * (restaura stock, marca CANCELED, notifica al cliente).
     */
    ReservationServiceResult<ReservationTokenActionError> rejectByToken(String token);
}
