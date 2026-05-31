package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.models.auction.Auction;
import ar.edu.itba.paw.models.auction.AuctionSortOption;
import ar.edu.itba.paw.models.pack.Municipality;
import ar.edu.itba.paw.models.pack.Pack;
import ar.edu.itba.paw.models.pack.PackTag;
import ar.edu.itba.paw.persistence.util.JpqlQuerySupport;
import ar.edu.itba.paw.persistence.util.LikePatternSupport;
import ar.edu.itba.paw.persistence.util.OpeningTimeFilterJpql;
import ar.edu.itba.paw.persistence.util.Pagination;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
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
        return em.createQuery("SELECT a FROM Auction a JOIN FETCH a.pack p WHERE a.id = :id", Auction.class)
            .setParameter("id", id)
            .getResultList()
            .stream()
            .findFirst();
    }

    @Override
    public Optional<Auction> findByPackId(final long packId) {
        return em.createQuery("SELECT a FROM Auction a JOIN FETCH a.pack p WHERE p.id = :packId", Auction.class)
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

    private void appendAllTagsFilter(final StringBuilder jpql, final Map<String, Object> params, final List<PackTag> tags) {
        jpql.append("AND t IN (:tags) ");
        params.put("tags", tags);
        jpql.append("GROUP BY a.id HAVING COUNT(DISTINCT t) = :requiredTagCount ");
        params.put("requiredTagCount", Long.valueOf(tags.size()));
    }

    private void appendAllTagsCountFilter(final StringBuilder jpql, final Map<String, Object> params, final List<PackTag> tags) {
        jpql.append("AND (SELECT COUNT(DISTINCT t2) FROM Pack p2 JOIN p2.tags t2 WHERE p2.id = p.id AND t2 IN (:tags)) = :requiredTagCount ");
        params.put("tags", tags);
        params.put("requiredTagCount", Long.valueOf(tags.size()));
    }

    private String toOrderByClause(final AuctionSortOption sort, final boolean groupedByAuctionId) {
        final AuctionSortOption safeSort = sort != null ? sort : AuctionSortOption.TIME_REMAINING_ASC;
        if (groupedByAuctionId) {
            switch (safeSort) {
                case TIME_REMAINING_DESC:
                    return "MIN(a.endTime) DESC";
                case PRICE_ASC:
                    return "MIN(COALESCE(a.currentBid, a.initialPrice)) ASC";
                case PRICE_DESC:
                    return "MIN(COALESCE(a.currentBid, a.initialPrice)) DESC";
                case TIME_REMAINING_ASC:
                default:
                    return "MIN(a.endTime) ASC";
            }
        }
        switch (safeSort) {
            case TIME_REMAINING_DESC:
                return "a.endTime DESC";
            case PRICE_ASC:
                return "COALESCE(a.currentBid, a.initialPrice) ASC";
            case PRICE_DESC:
                return "COALESCE(a.currentBid, a.initialPrice) DESC";
            case TIME_REMAINING_ASC:
            default:
                return "a.endTime ASC";
        }
    }

    private List<Long> queryAuctionIds(final String query, final List<PackTag> tags, final String city,
                                       final List<String> timeRanges, final AuctionSortOption sort,
                                       final int page, final int pageSize, final boolean requirePositiveStock,
                                       final Long commerceUserId) {
        final boolean withTags = tags != null && !tags.isEmpty();
        final StringBuilder jpql = new StringBuilder("SELECT a.id ");
        final Map<String, Object> params = new LinkedHashMap<>();
        appendFilterFromClause(jpql, withTags);
        appendCommonCatalogFilters(jpql, params, query, city, timeRanges, requirePositiveStock, commerceUserId);
        if (withTags) {
            appendAllTagsFilter(jpql, params, tags);
        }
        jpql.append("ORDER BY ").append(toOrderByClause(sort, withTags));

        return JpqlQuerySupport.createQuery(em, jpql.toString(), params, Long.class)
                .setMaxResults(pageSize)
                .setFirstResult(Pagination.offset(page, pageSize))
                .getResultList();
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

        final List<Auction> auctions = em.createQuery(
                        "SELECT a FROM Auction a JOIN FETCH a.pack p JOIN FETCH p.commerce WHERE a.id IN :ids",
                        Auction.class)
                .setParameter("ids", ids)
                .getResultList();

        final Map<Long, Integer> positions = new HashMap<>();
        for (int index = 0; index < ids.size(); index++) {
            positions.put(ids.get(index), Integer.valueOf(index));
        }
        auctions.sort(Comparator.comparingInt(auction -> positions.getOrDefault(auction.getId(), Integer.MAX_VALUE)));
        return auctions;
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
        return em.createQuery("SELECT a FROM Auction a JOIN FETCH a.pack p WHERE a.status = :activeStatus AND a.endTime <= :now AND p.deleted = false", Auction.class)
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

    @Override
    public List<Auction> filterParticipatedAuctions(final long clientId, final Auction.Status status, final String query, final int page, final int pageSize) {
        final StringBuilder idJpql = new StringBuilder("SELECT a.id, MAX(b.timestamp) ");
        idJpql.append("FROM Auction a JOIN a.pack p JOIN p.commerce c, Bid b WHERE a.id = b.auction.id ");
        idJpql.append("AND p.deleted = false AND b.client.userId = :clientId ");

        final Map<String, Object> params = new LinkedHashMap<>();
        params.put("clientId", Long.valueOf(clientId));

        if (status != null) {
            idJpql.append("AND a.status = :auctionStatus ");
            params.put("auctionStatus", status);
        }

        final Optional<String> pattern = LikePatternSupport.escapeAndWrap(query);
        if (pattern.isPresent()) {
            idJpql.append("AND (");
            LikePatternSupport.appendEscapedLike(idJpql, "p.title", ":search");
            idJpql.append(" OR ");
            LikePatternSupport.appendEscapedLike(idJpql, "p.description", ":search");
            idJpql.append(" OR ");
            LikePatternSupport.appendEscapedLike(idJpql, "c.commercialName", ":search");
            idJpql.append(") ");
            params.put("search", pattern.get());
        }
        idJpql.append("GROUP BY a.id ORDER BY MAX(b.timestamp) DESC");

        final List<Object[]> results = JpqlQuerySupport.createQuery(em, idJpql.toString(), params, Object[].class)
                .setMaxResults(pageSize)
                .setFirstResult(Pagination.offset(page, pageSize))
                .getResultList();
        if (results.isEmpty()) {
            return new ArrayList<>();
        }

        final List<Long> ids = new ArrayList<>();
        for (final Object[] row : results) {
            ids.add((Long) row[0]);
        }

        final List<Auction> auctions = em.createQuery("SELECT a FROM Auction a JOIN FETCH a.pack p WHERE a.id IN :ids", Auction.class)
                .setParameter("ids", ids)
                .getResultList();

        auctions.sort((a1, a2) -> Integer.compare(ids.indexOf(a1.getId()), ids.indexOf(a2.getId())));
        return auctions;
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
