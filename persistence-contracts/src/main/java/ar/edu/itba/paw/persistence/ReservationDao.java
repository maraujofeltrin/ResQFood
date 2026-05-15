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

    int countPaidReservationsInPeriod(Long commerceId, LocalDateTime periodStart, LocalDateTime periodEnd);

    List<Object[]> countPaidReservationsPerDay(Long commerceId, LocalDateTime from, LocalDateTime to);

    BigDecimal sumRevenueInPeriod(Long commerceId, LocalDateTime from, LocalDateTime to);

    Optional<Long> findBestSellingPackId(Long commerceId, LocalDateTime from, LocalDateTime to);

    long countByStatusInPeriod(Long commerceId, Reservation.Status status, LocalDateTime from, LocalDateTime to);

    long countCanceledReservationsInPeriod(Long commerceId, LocalDateTime from, LocalDateTime to);

    BigDecimal averageTicketInPeriod(Long commerceId, LocalDateTime from, LocalDateTime to);

    long countUniqueClientsInPeriod(Long commerceId, LocalDateTime from, LocalDateTime to);

    List<Object[]> findTopSellingPacks(Long commerceId, LocalDateTime from, LocalDateTime to, int limit);

    List<Object[]> findTopClientsByPaidReservations(Long commerceId, LocalDateTime from, LocalDateTime to, int limit);

    long countNewClientsInPeriod(Long commerceId, LocalDateTime from, LocalDateTime to);
}
