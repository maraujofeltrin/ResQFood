package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.models.auction.Auction;
import ar.edu.itba.paw.models.auction.AuctionSortOption;
import ar.edu.itba.paw.models.pack.Municipality;
import ar.edu.itba.paw.models.pack.Pack;
import ar.edu.itba.paw.models.pack.PackTag;
import ar.edu.itba.paw.persistence.util.JpqlQuerySupport;
import ar.edu.itba.paw.persistence.util.LikePatternSupport;
import ar.edu.itba.paw.persistence.util.OpeningTimeFilterJpql;
import ar.edu.itba.paw.persistence.util.OpeningTimeFilterSql;
import ar.edu.itba.paw.persistence.util.Pagination;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Primary
@Repository("auctionJpaDao")
public class AuctionJpaDao implements AuctionDao {

    @PersistenceContext
    private EntityManager em;

    @Override
    public Auction createAuction(final long packId, final double initialPrice, final double minBidIncrement, final LocalDateTime endTime) {
        final Auction a = new Auction(null, em.getReference(Pack.class, packId), initialPrice, minBidIncrement, null, null, endTime, Auction.Status.ACTIVE, LocalDateTime.now(ZoneOffset.UTC));
        em.persist(a);
        return a;
    }

    @Override
    public Optional<Auction> findById(final long id) {
        return em.createQuery(
                        "SELECT a FROM Auction a JOIN FETCH a.pack p JOIN FETCH p.commerce WHERE a.id = :id",
                        Auction.class)
            .setParameter("id", id)
            .getResultList()
            .stream()
            .findFirst();
    }

    @Override
    public Optional<Auction> findByPackId(final long packId) {
        return em.createQuery(
                        "SELECT a FROM Auction a JOIN FETCH a.pack p JOIN FETCH p.commerce WHERE p.id = :packId",
                        Auction.class)
            .setParameter("packId", packId)
            .getResultList()
            .stream()
            .findFirst();
    }

    private void appendCityFilter(final StringBuilder jpql, final Map<String, Object> params, final String city) {
        final Municipality municipality = Municipality.fromCityName(city);
        if (municipality != null) {
            jpql.append("AND c.city = :city ");
            params.put("city", municipality);
        }
    }

    private void appendQueryFilter(final StringBuilder jpql, final Map<String, Object> params, final String query) {
        final Optional<String> pattern = LikePatternSupport.escapeAndWrap(query);
        if (pattern.isEmpty()) {
            return;
        }
        jpql.append("AND (");
        LikePatternSupport.appendEscapedLike(jpql, "p.title", ":search");
        jpql.append(" OR ");
        LikePatternSupport.appendEscapedLike(jpql, "c.commercialName", ":search");
        jpql.append(") ");
        params.put("search", pattern.get());
    }

    private void appendActiveAuctionFilters(final StringBuilder jpql, final Map<String, Object> params,
                                            final boolean requirePositiveStock) {
        jpql.append("AND a.status = :activeStatus AND a.endTime > :now ");
        jpql.append("AND p.active = true AND p.deleted = false ");
        params.put("activeStatus", Auction.Status.ACTIVE);
        params.put("now", LocalDateTime.now(ZoneOffset.UTC));
        if (requirePositiveStock) {
            jpql.append("AND p.stock > 0 ");
        }
    }

    private void appendFilterFromClause(final StringBuilder jpql, final boolean withTags) {
        jpql.append("FROM Auction a JOIN a.pack p JOIN p.commerce c ");
        if (withTags) {
            jpql.append("JOIN p.tags t ");
        }
        jpql.append("WHERE 1=1 ");
    }

    private void appendCommonCatalogFilters(final StringBuilder jpql, final Map<String, Object> params, final String query,
                                            final String city, final List<String> timeRanges,
                                            final boolean requirePositiveStock, final Long commerceUserId) {
        appendActiveAuctionFilters(jpql, params, requirePositiveStock);
        if (commerceUserId != null) {
            jpql.append("AND p.commerce.userId = :commerceUserId ");
            params.put("commerceUserId", commerceUserId);
        }
        appendQueryFilter(jpql, params, query);
        appendCityFilter(jpql, params, city);
        OpeningTimeFilterJpql.appendTimeRangeConditions(jpql, "c.openingTime", timeRanges);
    }

    private void appendAllTagsCountFilter(final StringBuilder jpql, final Map<String, Object> params, final List<PackTag> tags) {
        jpql.append("AND (SELECT COUNT(DISTINCT t2) FROM Pack p2 JOIN p2.tags t2 WHERE p2.id = p.id AND t2 IN (:tags)) = :requiredTagCount ");
        params.put("tags", tags);
        params.put("requiredTagCount", Long.valueOf(tags.size()));
    }

    private void appendCatalogFiltersNative(final StringBuilder sql, final Map<String, Object> params,
                                            final String query, final List<PackTag> tags, final String city,
                                            final List<String> timeRanges, final boolean requirePositiveStock,
                                            final Long commerceUserId) {
        if (commerceUserId != null) {
            sql.append(" AND p.commerce_id = :commerceUserId");
            params.put("commerceUserId", commerceUserId);
        }
        if (requirePositiveStock) {
            sql.append(" AND p.stock > 0");
        }
        final Optional<String> searchPattern = LikePatternSupport.escapeAndWrap(query);
        if (searchPattern.isPresent()) {
            sql.append(" AND (");
            LikePatternSupport.appendEscapedLikeNative(sql, "p.title", ":search");
            sql.append(" OR ");
            LikePatternSupport.appendEscapedLikeNative(sql, "c.commercial_name", ":search");
            sql.append(")");
            params.put("search", searchPattern.get());
        }
        final Municipality municipality = Municipality.fromCityName(city);
        if (municipality != null) {
            sql.append(" AND c.city = :city");
            params.put("city", municipality.getCityName());
        }
        OpeningTimeFilterSql.appendTimeRangeConditions(sql, "c.opening_time", timeRanges);
        if (tags != null && !tags.isEmpty()) {
            for (int index = 0; index < tags.size(); index++) {
                final String paramName = "tag" + index;
                sql.append(" AND EXISTS (SELECT 1 FROM pack_tags pt WHERE pt.pack_id = p.id AND pt.tag = :")
                        .append(paramName).append(")");
                params.put(paramName, tags.get(index).name());
            }
        }
    }

    private void appendParticipatedSearchFiltersNative(final StringBuilder sql, final Map<String, Object> params,
                                                         final String query) {
        final Optional<String> searchPattern = LikePatternSupport.escapeAndWrap(query);
        if (searchPattern.isEmpty()) {
            return;
        }
        sql.append(" AND (");
        LikePatternSupport.appendEscapedLikeNative(sql, "p.title", ":search");
        sql.append(" OR ");
        LikePatternSupport.appendEscapedLikeNative(sql, "p.description", ":search");
        sql.append(" OR ");
        LikePatternSupport.appendEscapedLikeNative(sql, "c.commercial_name", ":search");
        sql.append(")");
        params.put("search", searchPattern.get());
    }

    private String toNativeOrderByClause(final AuctionSortOption sort) {
        final AuctionSortOption safeSort = sort != null ? sort : AuctionSortOption.TIME_REMAINING_ASC;
        return safeSort.getOrderByClause();
    }

    private String toJpqlOrderByClause(final AuctionSortOption sort) {
        return toNativeOrderByClause(sort)
                .replace("end_time", "endTime")
                .replace("current_bid", "currentBid")
                .replace("initial_price", "initialPrice");
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

    private List<Long> queryAuctionIds(final String query, final List<PackTag> tags, final String city,
                                       final List<String> timeRanges, final AuctionSortOption sort,
                                       final int page, final int pageSize, final boolean requirePositiveStock,
                                       final Long commerceUserId) {
        final StringBuilder sql = new StringBuilder(
                "SELECT a.id FROM auctions a "
                        + "INNER JOIN packs p ON p.id = a.pack_id "
                        + "INNER JOIN commerces c ON c.user_id = p.commerce_id "
                        + "WHERE a.status = :activeStatus AND a.end_time > :now "
                        + "AND p.active = TRUE AND p.deleted = FALSE");
        final Map<String, Object> params = new LinkedHashMap<>();
        params.put("activeStatus", Auction.Status.ACTIVE.name());
        params.put("now", LocalDateTime.now(ZoneOffset.UTC));
        appendCatalogFiltersNative(sql, params, query, tags, city, timeRanges, requirePositiveStock, commerceUserId);
        sql.append(" ORDER BY ").append(toNativeOrderByClause(sort));

        final Query idQuery = em.createNativeQuery(sql.toString());
        for (final Map.Entry<String, Object> entry : params.entrySet()) {
            idQuery.setParameter(entry.getKey(), entry.getValue());
        }
        idQuery.setFirstResult(Pagination.offset(page, pageSize));
        idQuery.setMaxResults(pageSize);
        return parseLongIds(idQuery.getResultList());
    }

    @Override
    public List<Auction> filterAuctions(final String query, final List<PackTag> tags,
                                        final String city, final List<String> timeRanges,
                                        final AuctionSortOption sort,
                                        final int page, final int pageSize,
                                        final boolean requirePositiveStock,
                                        final Long commerceUserId) {
        final List<Long> ids = queryAuctionIds(query, tags, city, timeRanges, sort, page, pageSize,
                requirePositiveStock, commerceUserId);
        if (ids.isEmpty()) {
            return Collections.emptyList();
        }

        return em.createQuery(
                        "SELECT a FROM Auction a JOIN FETCH a.pack p JOIN FETCH p.commerce "
                                + "WHERE a.id IN :ids ORDER BY " + toJpqlOrderByClause(sort),
                        Auction.class)
                .setParameter("ids", ids)
                .getResultList();
    }

    @Override
    public int countFilteredAuctions(final String query, final List<PackTag> tags,
                                     final String city, final List<String> timeRanges,
                                     final boolean requirePositiveStock,
                                     final Long commerceUserId) {
        final boolean withTags = tags != null && !tags.isEmpty();
        final StringBuilder jpql = new StringBuilder("SELECT COUNT(DISTINCT a.id) ");
        final Map<String, Object> params = new LinkedHashMap<>();
        appendFilterFromClause(jpql, withTags);
        appendCommonCatalogFilters(jpql, params, query, city, timeRanges, requirePositiveStock, commerceUserId);
        if (withTags) {
            appendAllTagsCountFilter(jpql, params, tags);
        }

        final Long count = JpqlQuerySupport.createQuery(em, jpql.toString(), params, Long.class).getSingleResult();
        return count != null ? count.intValue() : 0;
    }

    @Override
    public List<Auction> findByCommerceId(final long commerceId) {
        return em.createQuery("SELECT a FROM Auction a JOIN FETCH a.pack p WHERE p.commerce.userId = :commerceId AND p.deleted = false ORDER BY a.createdAt DESC", Auction.class)
                .setParameter("commerceId", commerceId)
                .getResultList();
    }

    @Override
    public List<Auction> findByStatus(final Auction.Status status) {
        return em.createQuery("SELECT a FROM Auction a JOIN FETCH a.pack p WHERE a.status = :status AND p.deleted = false ORDER BY a.endTime ASC", Auction.class)
                .setParameter("status", status)
                .getResultList();
    }

    @Override
    public void updateStatus(final long auctionId, final Auction.Status status) {
        em.createQuery("UPDATE Auction a SET a.status = :status WHERE a.id = :auctionId")
            .setParameter("status", status)
            .setParameter("auctionId", auctionId)
            .executeUpdate();
    }

    @Override
    public void updateCurrentBid(final long auctionId, final double amount, final long bidderId) {
        em.createQuery("UPDATE Auction a SET a.currentBid = :amount, a.currentBidderId = :bidderId WHERE a.id = :auctionId")
            .setParameter("amount", amount)
            .setParameter("bidderId", bidderId)
            .setParameter("auctionId", auctionId)
            .executeUpdate();
    }

    @Override
    public List<Auction> findExpiredActive() {
        return em.createQuery("SELECT a FROM Auction a JOIN FETCH a.pack p JOIN FETCH p.commerce WHERE a.status = :activeStatus AND a.endTime <= :now AND p.deleted = false", Auction.class)
                .setParameter("activeStatus", Auction.Status.ACTIVE)
                .setParameter("now", LocalDateTime.now(ZoneOffset.UTC))
                .getResultList();
    }

    private void appendParticipatedConditions(final StringBuilder jpql, final Map<String, Object> params,
                                              final long clientId, final Auction.Status status, final String query) {
        jpql.append("FROM Auction a JOIN a.pack p JOIN p.commerce c WHERE p.deleted = false ");
        jpql.append("AND a.id IN (SELECT b.auction.id FROM Bid b WHERE b.client.userId = :clientId) ");
        params.put("clientId", Long.valueOf(clientId));

        if (status != null) {
            jpql.append("AND a.status = :auctionStatus ");
            params.put("auctionStatus", status);
        }

        final Optional<String> pattern = LikePatternSupport.escapeAndWrap(query);
        if (pattern.isPresent()) {
            jpql.append("AND (");
            LikePatternSupport.appendEscapedLike(jpql, "p.title", ":search");
            jpql.append(" OR ");
            LikePatternSupport.appendEscapedLike(jpql, "p.description", ":search");
            jpql.append(" OR ");
            LikePatternSupport.appendEscapedLike(jpql, "c.commercialName", ":search");
            jpql.append(") ");
            params.put("search", pattern.get());
        }
    }

    private String buildIdPositionOrderByClause(final List<Long> ids) {
        final StringBuilder orderBy = new StringBuilder("CASE a.id ");
        for (int index = 0; index < ids.size(); index++) {
            orderBy.append("WHEN ").append(ids.get(index).longValue()).append(" THEN ").append(index).append(' ');
        }
        orderBy.append("ELSE ").append(ids.size()).append(" END");
        return orderBy.toString();
    }

    @Override
    public List<Auction> filterParticipatedAuctions(final long clientId, final Auction.Status status, final String query, final int page, final int pageSize) {
        final StringBuilder sql = new StringBuilder(
                "SELECT a.id FROM auctions a "
                        + "INNER JOIN packs p ON p.id = a.pack_id "
                        + "INNER JOIN commerces c ON c.user_id = p.commerce_id "
                        + "INNER JOIN bids b ON b.auction_id = a.id AND b.client_id = :clientId "
                        + "WHERE p.deleted = FALSE");
        final Map<String, Object> params = new LinkedHashMap<>();
        params.put("clientId", Long.valueOf(clientId));

        if (status != null) {
            sql.append(" AND a.status = :auctionStatus");
            params.put("auctionStatus", status.name());
        }
        appendParticipatedSearchFiltersNative(sql, params, query);
        sql.append(" GROUP BY a.id ORDER BY MAX(b.timestamp) DESC");

        final Query idQuery = em.createNativeQuery(sql.toString());
        for (final Map.Entry<String, Object> entry : params.entrySet()) {
            idQuery.setParameter(entry.getKey(), entry.getValue());
        }
        idQuery.setFirstResult(Pagination.offset(page, pageSize));
        idQuery.setMaxResults(pageSize);

        final List<Long> ids = parseLongIds(idQuery.getResultList());
        if (ids.isEmpty()) {
            return Collections.emptyList();
        }

        return em.createQuery(
                        "SELECT a FROM Auction a JOIN FETCH a.pack p LEFT JOIN FETCH p.image JOIN FETCH p.commerce "
                                + "WHERE a.id IN :ids ORDER BY " + buildIdPositionOrderByClause(ids),
                        Auction.class)
                .setParameter("ids", ids)
                .getResultList();
    }

    @Override
    public int countParticipatedAuctions(final long clientId, final Auction.Status status, final String query) {
        final StringBuilder jpql = new StringBuilder("SELECT COUNT(DISTINCT a.id) ");
        final Map<String, Object> params = new LinkedHashMap<>();
        appendParticipatedConditions(jpql, params, clientId, status, query);

        final Long count = JpqlQuerySupport.createQuery(em, jpql.toString(), params, Long.class).getSingleResult();
        return count != null ? count.intValue() : 0;
    }

}
