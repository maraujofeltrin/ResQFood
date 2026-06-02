package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.models.reservation.Reservation;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ReservationDao {
    Reservation createReservation(final Long customerId, final Long packId, final LocalDateTime reservationDate,
            final Double finalPrice, final Reservation.Status status, final String pickupCode,
            final LocalDateTime pickupConfirmationDate, final Integer quantity, final String pickupWindow);

    Optional<Reservation> findById(final Long id);

    Optional<Reservation> findByIdWithDetails(final Long id);

    List<Reservation> findByCustomerId(final Long customerId);

    List<Reservation> findByCommerceId(final Long commerceId);

    List<Reservation> findByPackId(final Long packId);

    Reservation updateStatus(final Long id, final Reservation.Status status);

    Optional<Reservation> findByPickupCode(final String pickupCode);

    Reservation confirmPickup(final Long id, final java.time.LocalDateTime pickupConfirmationDate);

    List<Reservation> filterReservations(Long commerceId, Long customerId, String query, Reservation.Status status,
            boolean excludeAuctionPacks, int page, int pageSize);

    int countFilteredReservations(Long commerceId, Long customerId, String query, Reservation.Status status,
            boolean excludeAuctionPacks);

    boolean hasActiveReservation(Long packId, Long customerId);

    boolean hasPaidReservationWithCommerce(Long customerId, Long commerceId);

    /**
     * Counts PAID reservations whose scheduled pickup ({@code pickupConfirmationDate}) falls in {@code [periodStart, periodEnd)}.
     */
    int countPaidReservationsInPeriod(Long commerceId, LocalDateTime periodStart, LocalDateTime periodEnd);

    /**
     * Daily volume of PAID reservations grouped by {@code reservation_date} (when the reservation was created),
     * not by scheduled pickup. Period bounds apply to {@code reservation_date}.
     */
    List<Object[]> countPaidReservationsPerDay(Long commerceId, LocalDateTime from, LocalDateTime to);

    /**
     * Sums {@code finalPrice} for PAID reservations whose scheduled pickup ({@code pickupConfirmationDate})
     * falls in {@code [from, to)}.
     */
    BigDecimal sumRevenueInPeriod(Long commerceId, LocalDateTime from, LocalDateTime to);

    /**
     * Pack with the most PAID reservations in the period, ranked by scheduled pickup ({@code pickupConfirmationDate}).
     */
    Optional<Long> findBestSellingPackId(Long commerceId, LocalDateTime from, LocalDateTime to);

    /**
     * Counts reservations with the given status whose {@code reservation_date} (creation time) falls in {@code [from, to)}.
     */
    long countByStatusInPeriod(Long commerceId, Reservation.Status status, LocalDateTime from, LocalDateTime to);

    /**
     * Counts CANCELED reservations whose {@code reservation_date} falls in {@code [from, to)}.
     */
    long countCanceledReservationsInPeriod(Long commerceId, LocalDateTime from, LocalDateTime to);

    /**
     * Average ticket for PAID reservations whose scheduled pickup ({@code pickupConfirmationDate}) falls in {@code [from, to)}.
     */
    BigDecimal averageTicketInPeriod(Long commerceId, LocalDateTime from, LocalDateTime to);

    /**
     * Distinct clients with at least one PAID reservation whose scheduled pickup falls in {@code [from, to)}.
     */
    long countUniqueClientsInPeriod(Long commerceId, LocalDateTime from, LocalDateTime to);

    /**
     * Top packs by units sold (PAID, period on {@code pickupConfirmationDate}).
     */
    List<Object[]> findTopSellingPacks(Long commerceId, LocalDateTime from, LocalDateTime to, int limit);

    /**
     * Top clients by PAID reservation count (period on {@code pickupConfirmationDate}).
     */
    List<Object[]> findTopClientsByPaidReservations(Long commerceId, LocalDateTime from, LocalDateTime to, int limit);

    /**
     * Clients whose first PAID pickup in the commerce occurs in {@code [from, to)} (uses {@code pickupConfirmationDate}).
     */
    long countNewClientsInPeriod(Long commerceId, LocalDateTime from, LocalDateTime to);
}
