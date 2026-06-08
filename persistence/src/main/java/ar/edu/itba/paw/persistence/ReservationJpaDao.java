package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.models.pack.Pack;
import ar.edu.itba.paw.models.reservation.Reservation;
import ar.edu.itba.paw.models.user.Client;
import ar.edu.itba.paw.persistence.util.JpqlQuerySupport;
import ar.edu.itba.paw.persistence.util.LikePatternSupport;
import ar.edu.itba.paw.persistence.util.Pagination;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Primary
@Repository("reservationJpaDao")
public class ReservationJpaDao implements ReservationDao {

    @PersistenceContext
    private EntityManager em;

    @Override
    public Reservation createReservation(final Long customerId, final Long packId, final LocalDateTime reservationDate,
            final Double finalPrice, final Reservation.Status status, final String pickupCode,
            final LocalDateTime pickupConfirmationDate, final Integer quantity, final String pickupWindow) {
        final Client customer = em.getReference(Client.class, customerId);
        final Pack pack = em.getReference(Pack.class, packId);
        final Reservation r = new Reservation(null, customer, pack, reservationDate, finalPrice, status, pickupCode, pickupConfirmationDate, quantity == null ? 1 : quantity, pickupWindow);
        em.persist(r);
        return r;
    }

    @Override
    public Optional<Reservation> findById(final Long id) {
        return Optional.ofNullable(em.find(Reservation.class, id));
    }

    @Override
    public Optional<Reservation> findByIdWithDetails(final Long id) {
        return em.createQuery(
                "SELECT r FROM Reservation r "
                        + "JOIN FETCH r.customer "
                        + "JOIN FETCH r.pack p "
                        + "LEFT JOIN FETCH p.image "
                        + "JOIN FETCH p.commerce "
                        + "LEFT JOIN FETCH p.auction "
                        + "WHERE r.id = :id",
                Reservation.class)
                .setParameter("id", id)
                .getResultList()
                .stream()
                .findFirst();
    }

    @Override
    public List<Reservation> findByCustomerId(final Long customerId) {
        return em.createQuery("FROM Reservation r WHERE r.customer.userId = :customerId", Reservation.class)
                .setParameter("customerId", customerId)
                .getResultList();
    }

    @Override
    public List<Reservation> findByCommerceId(final Long commerceId) {
        return em.createQuery("SELECT r FROM Reservation r JOIN r.pack p WHERE p.commerce.userId = :commerceId", Reservation.class)
                .setParameter("commerceId", commerceId)
                .getResultList();
    }

    @Override
    public List<Reservation> findByPackId(final Long packId) {
        return em.createQuery(
                "FROM Reservation r JOIN FETCH r.customer WHERE r.pack.id = :packId ORDER BY r.reservationDate DESC",
                Reservation.class)
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
        return em.createQuery(
                "SELECT r FROM Reservation r "
                        + "JOIN FETCH r.customer "
                        + "JOIN FETCH r.pack p "
                        + "LEFT JOIN FETCH p.image "
                        + "JOIN FETCH p.commerce "
                        + "LEFT JOIN FETCH p.auction "
                        + "WHERE r.pickupCode = :code",
                Reservation.class)
            .setParameter("code", pickupCode)
            .setMaxResults(1)
            .getResultList()
            .stream()
            .findFirst();
    }

    private void appendReservationFilters(final StringBuilder jpql, final Map<String, Object> params,
            final Long commerceId, final Long customerId, final String query, final Reservation.Status status,
            final boolean excludeAuctionPacks) {
        jpql.append("FROM Reservation r ");
        final boolean needsPack = commerceId != null || (query != null && !query.isBlank()) || excludeAuctionPacks;
        final boolean needsCommerce = commerceId != null || (query != null && !query.isBlank());
        if (needsPack) {
            jpql.append("LEFT JOIN r.pack p ");
        }
        if (needsCommerce) {
            jpql.append("JOIN p.commerce c ");
        }
        jpql.append("WHERE 1=1 ");

        if (commerceId != null) {
            jpql.append("AND p.commerce.userId = :commerceId ");
            params.put("commerceId", commerceId);
        }
        if (customerId != null) {
            jpql.append("AND r.customer.userId = :customerId ");
            params.put("customerId", customerId);
        }
        if (status != null) {
            jpql.append("AND r.status = :status ");
            params.put("status", status);
        }

        final Optional<String> searchPattern = LikePatternSupport.escapeAndWrap(query);
        if (searchPattern.isPresent()) {
            final String pattern = searchPattern.get();
            jpql.append("AND (");
            LikePatternSupport.appendEscapedLike(jpql, "p.title", ":search");
            jpql.append(" OR ");
            LikePatternSupport.appendEscapedLike(jpql, "p.description", ":search");
            params.put("search", pattern);

            if (commerceId != null) {
                jpql.append(" OR EXISTS (SELECT 1 FROM Client cl WHERE cl.userId = r.customer.userId AND ");
                LikePatternSupport.appendEscapedLike(jpql, "CONCAT(cl.name, ' ', cl.lastName)", ":search");
                jpql.append(") ");
            } else if (customerId != null) {
                jpql.append(" OR ");
                LikePatternSupport.appendEscapedLike(jpql, "c.commercialName", ":search");
            }
            jpql.append(") ");
        }

        if (excludeAuctionPacks) {
            jpql.append("AND NOT EXISTS (SELECT 1 FROM Auction a WHERE a.pack.id = p.id) ");
        }
    }

    private void appendReservationFiltersNative(final StringBuilder sql, final Map<String, Object> params,
            final Long commerceId, final Long customerId, final String query, final Reservation.Status status,
            final boolean excludeAuctionPacks) {
        final boolean needsPack = commerceId != null || (query != null && !query.isBlank()) || excludeAuctionPacks;
        final boolean needsCommerce = commerceId != null || (query != null && !query.isBlank());
        if (needsPack) {
            sql.append("LEFT JOIN packs p ON p.id = r.pack_id ");
        }
        if (needsCommerce) {
            sql.append("JOIN commerces c ON c.user_id = p.commerce_id ");
        }
        sql.append("WHERE 1=1 ");

        if (commerceId != null) {
            sql.append("AND p.commerce_id = :commerceId ");
            params.put("commerceId", commerceId);
        }
        if (customerId != null) {
            sql.append("AND r.customer_id = :customerId ");
            params.put("customerId", customerId);
        }
        if (status != null) {
            sql.append("AND r.status = :status ");
            params.put("status", status.name());
        }

        final Optional<String> searchPattern = LikePatternSupport.escapeAndWrap(query);
        if (searchPattern.isPresent()) {
            final String pattern = searchPattern.get();
            sql.append("AND (");
            LikePatternSupport.appendEscapedLikeNative(sql, "p.title", ":search");
            sql.append(" OR ");
            LikePatternSupport.appendEscapedLikeNative(sql, "p.description", ":search");
            params.put("search", pattern);

            if (commerceId != null) {
                sql.append(" OR EXISTS (SELECT 1 FROM clients cl WHERE cl.user_id = r.customer_id AND ");
                LikePatternSupport.appendEscapedLikeNative(sql, "(cl.name || ' ' || cl.last_name)", ":search");
                sql.append(") ");
            } else if (customerId != null) {
                sql.append(" OR ");
                LikePatternSupport.appendEscapedLikeNative(sql, "c.commercial_name", ":search");
            }
            sql.append(") ");
        }

        if (excludeAuctionPacks) {
            sql.append("AND NOT EXISTS (SELECT 1 FROM auctions a WHERE a.pack_id = p.id) ");
        }
    }

    private List<Long> queryReservationIds(final Long commerceId, final Long customerId, final String query,
            final Reservation.Status status, final boolean excludeAuctionPacks, final int page, final int pageSize) {
        final StringBuilder sql = new StringBuilder("SELECT r.id FROM reservations r ");
        final Map<String, Object> params = new LinkedHashMap<>();
        appendReservationFiltersNative(sql, params, commerceId, customerId, query, status, excludeAuctionPacks);
        sql.append("ORDER BY r.reservation_date DESC, r.id DESC");

        final javax.persistence.Query idQuery = em.createNativeQuery(sql.toString());
        for (final Map.Entry<String, Object> entry : params.entrySet()) {
            idQuery.setParameter(entry.getKey(), entry.getValue());
        }
        idQuery.setFirstResult(Pagination.offset(page, pageSize));
        idQuery.setMaxResults(pageSize);
        return parseLongIds(idQuery.getResultList());
    }

    private List<Long> parseLongIds(final List<?> rawIds) {
        final List<Long> ids = new ArrayList<>(rawIds.size());
        for (final Object rawId : rawIds) {
            if (rawId instanceof Number) {
                ids.add(((Number) rawId).longValue());
            } else {
                ids.add(Long.parseLong(rawId.toString()));
            }
        }
        return ids;
    }

    @Override
    public List<Reservation> filterReservations(final Long commerceId, final Long customerId, final String query,
            final Reservation.Status status, final boolean excludeAuctionPacks, final int page, final int pageSize) {
        final List<Long> ids = queryReservationIds(commerceId, customerId, query, status, excludeAuctionPacks, page,
                pageSize);
        if (ids.isEmpty()) {
            return Collections.emptyList();
        }

        return em.createQuery(
                "SELECT r FROM Reservation r "
                        + "JOIN FETCH r.customer "
                        + "JOIN FETCH r.pack p "
                        + "LEFT JOIN FETCH p.image "
                        + "JOIN FETCH p.commerce "
                        + "LEFT JOIN FETCH p.auction "
                        + "WHERE r.id IN :ids "
                        + "ORDER BY r.reservationDate DESC, r.id DESC",
                Reservation.class)
                .setParameter("ids", ids)
                .getResultList();
    }

    @Override
    public int countFilteredReservations(final Long commerceId, final Long customerId, final String query,
            final Reservation.Status status, final boolean excludeAuctionPacks) {
        final StringBuilder jpql = new StringBuilder("SELECT COUNT(r.id) ");
        final Map<String, Object> params = new LinkedHashMap<>();
        appendReservationFilters(jpql, params, commerceId, customerId, query, status, excludeAuctionPacks);

        final Long count = JpqlQuerySupport.createQuery(em, jpql.toString(), params, Long.class).getSingleResult();
        return count != null ? count.intValue() : 0;
    }

    @Override
    public boolean hasActiveReservation(final Long packId, final Long customerId) {
        final Number count = em.createQuery("SELECT COUNT(r.id) FROM Reservation r WHERE r.pack.id = :packId AND r.customer.userId = :customerId AND r.status = :status", Number.class)
                .setParameter("packId", packId)
                .setParameter("customerId", customerId)
                .setParameter("status", Reservation.Status.RESERVED)
                .getSingleResult();
        return count != null && count.intValue() > 0;
    }

    @Override
    public boolean hasPaidReservationWithCommerce(final Long customerId, final Long commerceId) {
        final Number count = em.createQuery("SELECT COUNT(r.id) FROM Reservation r JOIN r.pack p WHERE r.customer.userId = :customerId AND p.commerce.userId = :commerceId AND r.status = :status", Number.class)
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
        final Number count = em.createQuery("SELECT COUNT(r.id) FROM Reservation r JOIN r.pack p WHERE p.commerce.userId = :commerceId AND r.status = :status AND r.pickupConfirmationDate >= :start AND r.pickupConfirmationDate < :end", Number.class)
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
        return em.createQuery(
                "SELECT FUNCTION('date_trunc', 'day', r.reservationDate), COUNT(r.id) "
                + "FROM Reservation r JOIN r.pack p "
                + "WHERE p.commerce.userId = :commerceId AND r.status = :status "
                + "AND r.reservationDate >= :start AND r.reservationDate < :end "
                + "GROUP BY FUNCTION('date_trunc', 'day', r.reservationDate) "
                + "ORDER BY FUNCTION('date_trunc', 'day', r.reservationDate) ASC",
                Object[].class)
                .setParameter("commerceId", commerceId)
                .setParameter("status", Reservation.Status.PAID)
                .setParameter("start", from)
                .setParameter("end", to)
                .getResultList();
    }

    @Override
    public BigDecimal sumRevenueInPeriod(final Long commerceId, final LocalDateTime from,
            final LocalDateTime to) {
        final Double sum = em.createQuery("SELECT SUM(r.finalPrice) FROM Reservation r JOIN r.pack p WHERE p.commerce.userId = :commerceId AND r.status = :status AND r.pickupConfirmationDate >= :start AND r.pickupConfirmationDate < :end", Double.class)
                .setParameter("commerceId", commerceId)
                .setParameter("status", Reservation.Status.PAID)
                .setParameter("start", from)
                .setParameter("end", to)
                .getSingleResult();
        return sum == null ? BigDecimal.ZERO : BigDecimal.valueOf(sum);
    }

    @Override
    public Optional<Long> findBestSellingPackId(final Long commerceId, final LocalDateTime from,
            final LocalDateTime to) {
        return em.createQuery("SELECT r.pack.id FROM Reservation r JOIN r.pack p WHERE p.commerce.userId = :commerceId AND r.status = :status AND r.pickupConfirmationDate >= :start AND r.pickupConfirmationDate < :end GROUP BY r.pack.id ORDER BY COUNT(r.id) DESC", Long.class)
            .setParameter("commerceId", commerceId)
            .setParameter("status", Reservation.Status.PAID)
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
        final Number count = em.createQuery("SELECT COUNT(r.id) FROM Reservation r JOIN r.pack p WHERE p.commerce.userId = :commerceId AND r.status = :status AND r.reservationDate >= :start AND r.reservationDate < :end", Number.class)
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
        final Number count = em.createQuery("SELECT COUNT(r.id) FROM Reservation r JOIN r.pack p WHERE p.commerce.userId = :commerceId AND r.status = :status AND r.reservationDate >= :start AND r.reservationDate < :end", Number.class)
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
        final Double avg = em.createQuery("SELECT AVG(r.finalPrice) FROM Reservation r JOIN r.pack p WHERE p.commerce.userId = :commerceId AND r.status = :status AND r.pickupConfirmationDate >= :start AND r.pickupConfirmationDate < :end", Double.class)
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
        final Number count = em.createQuery("SELECT COUNT(DISTINCT r.customer.userId) FROM Reservation r JOIN r.pack p WHERE p.commerce.userId = :commerceId AND r.status = :status AND r.pickupConfirmationDate >= :start AND r.pickupConfirmationDate < :end", Number.class)
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
        return em.createQuery("SELECT r.pack.id, SUM(r.quantity) as unitsSold FROM Reservation r JOIN r.pack p WHERE p.commerce.userId = :commerceId AND r.status = :status AND r.pickupConfirmationDate >= :start AND r.pickupConfirmationDate < :end GROUP BY r.pack.id ORDER BY unitsSold DESC", Object[].class)
                .setParameter("commerceId", commerceId)
                .setParameter("status", Reservation.Status.PAID)
                .setParameter("start", from)
                .setParameter("end", to)
                .setMaxResults(limit)
                .getResultList();
    }

    @Override
    public List<Object[]> findTopClientsByPaidReservations(final Long commerceId, final LocalDateTime from,
            final LocalDateTime to, final int limit) {
        return em.createQuery("SELECT r.customer.userId, COUNT(r.id) as reservationCount FROM Reservation r JOIN r.pack p WHERE p.commerce.userId = :commerceId AND r.status = :status AND r.pickupConfirmationDate >= :start AND r.pickupConfirmationDate < :end GROUP BY r.customer.userId ORDER BY reservationCount DESC", Object[].class)
                .setParameter("commerceId", commerceId)
                .setParameter("status", Reservation.Status.PAID)
                .setParameter("start", from)
                .setParameter("end", to)
                .setMaxResults(limit)
                .getResultList();
    }

    @Override
    public long countNewClientsInPeriod(final Long commerceId, final LocalDateTime from,
            final LocalDateTime to) {
        final Number count = em.createQuery("SELECT COUNT(DISTINCT r.customer.userId) FROM Reservation r JOIN r.pack p WHERE p.commerce.userId = :commerceId AND r.status = :status AND r.pickupConfirmationDate >= :start AND r.pickupConfirmationDate < :end AND NOT EXISTS (SELECT 1 FROM Reservation r2 WHERE r2.customer.userId = r.customer.userId AND r2.pickupConfirmationDate < :start)", Number.class)
                .setParameter("commerceId", commerceId)
                .setParameter("status", Reservation.Status.PAID)
                .setParameter("start", from)
                .setParameter("end", to)
                .getSingleResult();
        return count != null ? count.longValue() : 0L;
    }
}
