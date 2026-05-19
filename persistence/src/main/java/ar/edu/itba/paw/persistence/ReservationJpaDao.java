package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.models.reservation.Reservation;
import ar.edu.itba.paw.persistence.util.LikePatternSupport;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.ArrayList;

@Primary
@Repository("reservationJpaDao")
public class ReservationJpaDao implements ReservationDao {

    @PersistenceContext
    private EntityManager em;

    @Override
    public Reservation createReservation(final Long customerId, final Long packId, final LocalDateTime reservationDate,
            final Double finalPrice, final Reservation.Status status, final String pickupCode,
            final LocalDateTime pickupConfirmationDate, final Integer quantity, final String pickupWindow) {
        final Reservation r = new Reservation(null, customerId, packId, reservationDate, finalPrice, status, pickupCode, pickupConfirmationDate, quantity == null ? 1 : quantity, pickupWindow);
        em.persist(r);
        return r;
    }

    @Override
    public Optional<Reservation> findById(final Long id) {
        return Optional.ofNullable(em.find(Reservation.class, id));
    }

    @Override
    public List<Reservation> findByCustomerId(final Long customerId) {
        return em.createQuery("FROM Reservation r WHERE r.customerId = :customerId", Reservation.class)
                .setParameter("customerId", customerId)
                .getResultList();
    }

    @Override
    public List<Reservation> findByCommerceId(final Long commerceId) {
        return em.createQuery("SELECT r FROM Reservation r JOIN r.pack p WHERE p.commerceId = :commerceId", Reservation.class)
                .setParameter("commerceId", commerceId)
                .getResultList();
    }

    @Override
    public List<Reservation> findByPackId(final Long packId) {
        return em.createQuery("FROM Reservation r WHERE r.packId = :packId", Reservation.class)
                .setParameter("packId", packId)
                .getResultList();
    }

    @Override
    public Reservation updateStatus(final Long id, final Reservation.Status status) {
        final Reservation r = em.find(Reservation.class, id);
        if (r != null) {
            em.createQuery("UPDATE Reservation r SET r.status = :status WHERE r.id = :id")
                .setParameter("status", status)
                .setParameter("id", id)
                .executeUpdate();
            em.refresh(r);
        } else {
            throw new IllegalStateException("Reservation not found: " + id);
        }
        return r;
    }

    @Override
    public Reservation confirmPickup(final Long id, final LocalDateTime pickupConfirmationDate) {
        final Reservation r = em.find(Reservation.class, id);
        if (r != null) {
            em.createQuery("UPDATE Reservation r SET r.status = :status, r.pickupConfirmationDate = :date WHERE r.id = :id")
                .setParameter("status", Reservation.Status.PAID)
                .setParameter("date", pickupConfirmationDate)
                .setParameter("id", id)
                .executeUpdate();
            em.refresh(r);
        } else {
            throw new IllegalStateException("Reservation not found: " + id);
        }
        return r;
    }

    @Override
    public Optional<Reservation> findByPickupCode(final String pickupCode) {
        return em.createQuery("FROM Reservation r WHERE r.pickupCode = :code", Reservation.class)
            .setParameter("code", pickupCode)
            .getResultList()
            .stream()
            .findFirst();
    }

    private void appendReservationFilters(StringBuilder hql, List<Object> params, Long commerceId, Long customerId, String query, Reservation.Status status, boolean excludeAuctionPacks) {
        hql.append("FROM Reservation r ");
        final boolean needsPack = commerceId != null || (query != null && !query.isBlank()) || excludeAuctionPacks;
        final boolean needsCommerce = commerceId != null || (query != null && !query.isBlank());
        if (needsPack) {
            hql.append("LEFT JOIN r.pack p ");
        }
        if (needsCommerce) {
            hql.append("JOIN Commerce c ON p.commerceId = c.userId ");
        }
        hql.append("WHERE 1=1 ");

        if (commerceId != null) {
            hql.append("AND p.commerceId = ?").append(params.size() + 1).append(" ");
            params.add(commerceId);
        }
        if (customerId != null) {
            hql.append("AND r.customerId = ?").append(params.size() + 1).append(" ");
            params.add(customerId);
        }
        if (status != null) {
            hql.append("AND r.status = ?").append(params.size() + 1).append(" ");
            params.add(status);
        }

        final Optional<String> searchPattern = LikePatternSupport.escapeAndWrap(query);
        if (searchPattern.isPresent()) {
            final String pattern = searchPattern.get();
            final String titleParam = "?" + (params.size() + 1);
            final String descriptionParam = "?" + (params.size() + 2);
            hql.append("AND (");
            LikePatternSupport.appendEscapedLike(hql, "p.title", titleParam);
            hql.append(" OR ");
            LikePatternSupport.appendEscapedLike(hql, "p.description", descriptionParam);
            params.add(pattern);
            params.add(pattern);

            if (commerceId != null) {
                final String clientParam = "?" + (params.size() + 1);
                hql.append(" OR EXISTS (SELECT 1 FROM Client cl WHERE cl.userId = r.customerId AND ");
                LikePatternSupport.appendEscapedLike(hql, "CONCAT(cl.name, ' ', cl.lastName)", clientParam);
                hql.append(") ");
                params.add(pattern);
            } else if (customerId != null) {
                final String commerceParam = "?" + (params.size() + 1);
                hql.append(" OR ");
                LikePatternSupport.appendEscapedLike(hql, "c.commercialName", commerceParam);
                params.add(pattern);
            }
            hql.append(") ");
        }

        if (excludeAuctionPacks) {
            hql.append("AND NOT EXISTS (SELECT 1 FROM Auction a WHERE a.pack.id = p.id) ");
        }
    }

    @Override
    public List<Reservation> filterReservations(Long commerceId, Long customerId, String query, Reservation.Status status, boolean excludeAuctionPacks, int page, int pageSize) {
        StringBuilder hql = new StringBuilder("SELECT r ");
        List<Object> params = new ArrayList<>();
        appendReservationFilters(hql, params, commerceId, customerId, query, status, excludeAuctionPacks);
        hql.append("ORDER BY r.reservationDate DESC ");

        TypedQuery<Reservation> typedQuery = em.createQuery(hql.toString(), Reservation.class);
        for (int i = 0; i < params.size(); i++) {
            typedQuery.setParameter(i + 1, params.get(i));
        }
        typedQuery.setMaxResults(pageSize);
        typedQuery.setFirstResult((page - 1) * pageSize);

        return typedQuery.getResultList();
    }

    @Override
    public int countFilteredReservations(Long commerceId, Long customerId, String query, Reservation.Status status, boolean excludeAuctionPacks) {
        StringBuilder hql = new StringBuilder("SELECT COUNT(r.id) ");
        List<Object> params = new ArrayList<>();
        appendReservationFilters(hql, params, commerceId, customerId, query, status, excludeAuctionPacks);

        TypedQuery<Long> typedQuery = em.createQuery(hql.toString(), Long.class);
        for (int i = 0; i < params.size(); i++) {
            typedQuery.setParameter(i + 1, params.get(i));
        }
        Long count = typedQuery.getSingleResult();
        return count != null ? count.intValue() : 0;
    }

    @Override
    public boolean hasActiveReservation(Long packId, Long customerId) {
        final Number count = em.createQuery("SELECT COUNT(r.id) FROM Reservation r WHERE r.packId = :packId AND r.customerId = :customerId AND r.status = :status", Number.class)
                .setParameter("packId", packId)
                .setParameter("customerId", customerId)
                .setParameter("status", Reservation.Status.RESERVED)
                .getSingleResult();
        return count != null && count.intValue() > 0;
    }

    @Override
    public boolean hasPaidReservationWithCommerce(final Long customerId, final Long commerceId) {
        final Number count = em.createQuery("SELECT COUNT(r.id) FROM Reservation r JOIN r.pack p WHERE r.customerId = :customerId AND p.commerceId = :commerceId AND r.status = :status", Number.class)
                .setParameter("customerId", customerId)
                .setParameter("commerceId", commerceId)
                .setParameter("status", Reservation.Status.PAID)
                .getSingleResult();
        return count != null && count.intValue() > 0;
    }

    @Override
    public int countPaidReservationsInPeriod(final Long commerceId,
                                          final LocalDateTime periodStart,
                                          final LocalDateTime periodEnd) {
        final Number count = em.createQuery("SELECT COUNT(r.id) FROM Reservation r JOIN r.pack p WHERE p.commerceId = :commerceId AND r.status = :status AND r.pickupConfirmationDate >= :start AND r.pickupConfirmationDate < :end", Number.class)
                .setParameter("commerceId", commerceId)
                .setParameter("status", Reservation.Status.PAID)
                .setParameter("start", periodStart)
                .setParameter("end", periodEnd)
                .getSingleResult();
        return count != null ? count.intValue() : 0;
    }

    @Override
    public List<Object[]> countPaidReservationsPerDay(final Long commerceId, final LocalDateTime from,
            final LocalDateTime to) {
        // Use native SQL to group by date to avoid HQL function typing issues across dialects
        final String sql = "SELECT DATE(r.reservation_date) AS day, COUNT(r.*) AS cnt "
            + "FROM reservations r JOIN packs p ON p.id = r.pack_id "
            + "WHERE p.commerce_id = :commerceId AND r.status = 'PAID' "
            + "AND r.reservation_date >= :start AND r.reservation_date < :end "
            + "GROUP BY DATE(r.reservation_date) ORDER BY day ASC";

        final javax.persistence.Query q = em.createNativeQuery(sql);
        q.setParameter("commerceId", commerceId);
        q.setParameter("start", java.sql.Timestamp.valueOf(from));
        q.setParameter("end", java.sql.Timestamp.valueOf(to));

        final List<Object[]> results = q.getResultList();
        return results;
    }

    @Override
    public BigDecimal sumRevenueInPeriod(final Long commerceId, final LocalDateTime from,
            final LocalDateTime to) {
        final Double sum = em.createQuery("SELECT SUM(r.finalPrice) FROM Reservation r JOIN r.pack p WHERE p.commerceId = :commerceId AND r.status = 'PAID' AND r.pickupConfirmationDate >= :start AND r.pickupConfirmationDate < :end", Double.class)
                .setParameter("commerceId", commerceId)
                .setParameter("start", from)
                .setParameter("end", to)
                .getSingleResult();
        return sum == null ? BigDecimal.ZERO : BigDecimal.valueOf(sum);
    }

    @Override
    public Optional<Long> findBestSellingPackId(final Long commerceId, final LocalDateTime from,
            final LocalDateTime to) {
        return em.createQuery("SELECT r.packId FROM Reservation r JOIN r.pack p WHERE p.commerceId = :commerceId AND r.status = 'PAID' AND r.pickupConfirmationDate >= :start AND r.pickupConfirmationDate < :end GROUP BY r.packId ORDER BY COUNT(r.id) DESC", Long.class)
            .setParameter("commerceId", commerceId)
            .setParameter("start", from)
            .setParameter("end", to)
            .setMaxResults(1)
            .getResultList()
            .stream()
            .findFirst();
    }

    @Override
    public long countByStatusInPeriod(final Long commerceId, final Reservation.Status status,
            final LocalDateTime from, final LocalDateTime to) {
        final Number count = em.createQuery("SELECT COUNT(r.id) FROM Reservation r JOIN r.pack p WHERE p.commerceId = :commerceId AND r.status = :status AND r.reservationDate >= :start AND r.reservationDate < :end", Number.class)
                .setParameter("commerceId", commerceId)
                .setParameter("status", status)
                .setParameter("start", from)
                .setParameter("end", to)
                .getSingleResult();
        return count != null ? count.longValue() : 0L;
    }

    @Override
    public long countCanceledReservationsInPeriod(final Long commerceId, final LocalDateTime from,
            final LocalDateTime to) {
        final Number count = em.createQuery("SELECT COUNT(r.id) FROM Reservation r JOIN r.pack p WHERE p.commerceId = :commerceId AND r.status = :status AND r.reservationDate >= :start AND r.reservationDate < :end", Number.class)
                .setParameter("commerceId", commerceId)
                .setParameter("status", Reservation.Status.CANCELED)
                .setParameter("start", from)
                .setParameter("end", to)
                .getSingleResult();
        return count != null ? count.longValue() : 0L;
    }

    @Override
    public BigDecimal averageTicketInPeriod(final Long commerceId, final LocalDateTime from,
            final LocalDateTime to) {
        final Double avg = em.createQuery("SELECT AVG(r.finalPrice) FROM Reservation r JOIN r.pack p WHERE p.commerceId = :commerceId AND r.status = :status AND r.pickupConfirmationDate >= :start AND r.pickupConfirmationDate < :end", Double.class)
                .setParameter("commerceId", commerceId)
                .setParameter("status", Reservation.Status.PAID)
                .setParameter("start", from)
                .setParameter("end", to)
                .getSingleResult();
        return avg == null ? BigDecimal.ZERO : BigDecimal.valueOf(avg);
    }

    @Override
    public long countUniqueClientsInPeriod(final Long commerceId, final LocalDateTime from,
            final LocalDateTime to) {
        final Number count = em.createQuery("SELECT COUNT(DISTINCT r.customerId) FROM Reservation r JOIN r.pack p WHERE p.commerceId = :commerceId AND r.status = :status AND r.pickupConfirmationDate >= :start AND r.pickupConfirmationDate < :end", Number.class)
                .setParameter("commerceId", commerceId)
                .setParameter("status", Reservation.Status.PAID)
                .setParameter("start", from)
                .setParameter("end", to)
                .getSingleResult();
        return count != null ? count.longValue() : 0L;
    }

    @Override
    public List<Object[]> findTopSellingPacks(final Long commerceId, final LocalDateTime from,
            final LocalDateTime to, final int limit) {
        final List<Object[]> results = em.createQuery("SELECT r.packId, SUM(r.quantity) as unitsSold FROM Reservation r JOIN r.pack p WHERE p.commerceId = :commerceId AND r.status = :status AND r.pickupConfirmationDate >= :start AND r.pickupConfirmationDate < :end GROUP BY r.packId ORDER BY unitsSold DESC", Object[].class)
                .setParameter("commerceId", commerceId)
                .setParameter("status", Reservation.Status.PAID)
                .setParameter("start", from)
                .setParameter("end", to)
                .setMaxResults(limit)
                .getResultList();
        return results;
    }

    @Override
    public List<Object[]> findTopClientsByPaidReservations(final Long commerceId, final LocalDateTime from,
            final LocalDateTime to, final int limit) {
        final List<Object[]> results = em.createQuery("SELECT r.customerId, COUNT(r.id) as reservationCount FROM Reservation r JOIN r.pack p WHERE p.commerceId = :commerceId AND r.status = :status AND r.pickupConfirmationDate >= :start AND r.pickupConfirmationDate < :end GROUP BY r.customerId ORDER BY reservationCount DESC", Object[].class)
                .setParameter("commerceId", commerceId)
                .setParameter("status", Reservation.Status.PAID)
                .setParameter("start", from)
                .setParameter("end", to)
                .setMaxResults(limit)
                .getResultList();
        return results;
    }

    @Override
    public long countNewClientsInPeriod(final Long commerceId, final LocalDateTime from,
            final LocalDateTime to) {
        final Number count = em.createQuery("SELECT COUNT(DISTINCT r.customerId) FROM Reservation r JOIN r.pack p WHERE p.commerceId = :commerceId AND r.status = :status AND r.pickupConfirmationDate >= :start AND r.pickupConfirmationDate < :end AND NOT EXISTS (SELECT 1 FROM Reservation r2 WHERE r2.customerId = r.customerId AND r2.pickupConfirmationDate < :start)", Number.class)
                .setParameter("commerceId", commerceId)
                .setParameter("status", Reservation.Status.PAID)
                .setParameter("start", from)
                .setParameter("end", to)
                .getSingleResult();
        return count != null ? count.longValue() : 0L;
    }
}
