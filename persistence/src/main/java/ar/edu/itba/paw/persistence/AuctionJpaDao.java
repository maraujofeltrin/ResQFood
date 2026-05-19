package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.models.auction.Auction;
import ar.edu.itba.paw.models.auction.AuctionSortOption;
import ar.edu.itba.paw.models.pack.Municipality;
import ar.edu.itba.paw.models.pack.Pack;
import ar.edu.itba.paw.models.pack.PackTag;
import ar.edu.itba.paw.persistence.util.LikePatternSupport;
import ar.edu.itba.paw.persistence.util.OpeningTimeFilterJpql;
import org.springframework.stereotype.Repository;
import org.springframework.context.annotation.Primary;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
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

    private void appendCityFilter(final StringBuilder hql, final List<Object> params, final String city) {
        final Municipality municipality = Municipality.fromCityName(city);
        if (municipality != null) {
            hql.append("AND c.city = ?").append(params.size() + 1).append(" ");
            params.add(municipality);
        }
    }

    private void appendQueryFilter(final StringBuilder hql, final List<Object> params, final String query) {
        final Optional<String> pattern = LikePatternSupport.escapeAndWrap(query);
        if (pattern.isEmpty()) {
            return;
        }
        final String search = pattern.get();
        final String titleParam = "?" + (params.size() + 1);
        final String commerceParam = "?" + (params.size() + 2);
        hql.append("AND (");
        LikePatternSupport.appendEscapedLike(hql, "p.title", titleParam);
        hql.append(" OR ");
        LikePatternSupport.appendEscapedLike(hql, "c.commercialName", commerceParam);
        hql.append(") ");
        params.add(search);
        params.add(search);
    }

    private void appendActiveAuctionFilters(final StringBuilder hql, final List<Object> params,
                                            final boolean requirePositiveStock) {
        hql.append("AND a.status = 'ACTIVE' AND a.endTime > ?").append(params.size() + 1).append(" ");
        hql.append("AND p.active = true AND p.deleted = false ");
        params.add(LocalDateTime.now(ZoneOffset.UTC));
        if (requirePositiveStock) {
            hql.append("AND p.stock > 0 ");
        }
    }

    private void appendFilterFromClause(final StringBuilder hql, final boolean withTags) {
        hql.append("FROM Auction a JOIN a.pack p JOIN Commerce c ON p.commerceId = c.userId ");
        if (withTags) {
            hql.append("JOIN p.tags t ");
        }
        hql.append("WHERE 1=1 ");
    }

    private void appendCommonCatalogFilters(final StringBuilder hql, final List<Object> params, final String query,
                                            final String city, final List<String> timeRanges,
                                            final boolean requirePositiveStock) {
        appendActiveAuctionFilters(hql, params, requirePositiveStock);
        appendQueryFilter(hql, params, query);
        appendCityFilter(hql, params, city);
        OpeningTimeFilterJpql.appendTimeRangeConditions(hql, "c.openingTime", timeRanges);
    }

    private void appendAllTagsFilter(final StringBuilder hql, final List<Object> params, final List<PackTag> tags) {
        hql.append("AND t IN (?").append(params.size() + 1).append(") ");
        params.add(tags);
        hql.append("GROUP BY a.id HAVING COUNT(DISTINCT t) = ?").append(params.size() + 1).append(" ");
        params.add((long) tags.size());
    }

    private void appendAllTagsCountFilter(final StringBuilder hql, final List<Object> params, final List<PackTag> tags) {
        hql.append("AND (SELECT COUNT(DISTINCT t2) FROM Pack p2 JOIN p2.tags t2 WHERE p2.id = p.id AND t2 IN (?")
                .append(params.size() + 1).append(")) = ?").append(params.size() + 2).append(" ");
        params.add(tags);
        params.add((long) tags.size());
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
                                       final int page, final int pageSize, final boolean requirePositiveStock) {
        final boolean withTags = tags != null && !tags.isEmpty();
        final StringBuilder hql = new StringBuilder("SELECT a.id ");
        final List<Object> params = new ArrayList<>();
        appendFilterFromClause(hql, withTags);
        appendCommonCatalogFilters(hql, params, query, city, timeRanges, requirePositiveStock);
        if (withTags) {
            appendAllTagsFilter(hql, params, tags);
        }
        hql.append("ORDER BY ").append(toOrderByClause(sort, withTags));

        final TypedQuery<Long> typedQuery = em.createQuery(hql.toString(), Long.class);
        for (int i = 0; i < params.size(); i++) {
            typedQuery.setParameter(i + 1, params.get(i));
        }
        typedQuery.setMaxResults(pageSize);
        typedQuery.setFirstResult(Math.max(0, page - 1) * pageSize);
        return typedQuery.getResultList();
    }

    @Override
    public List<Auction> filterAuctions(final String query, final List<PackTag> tags,
                                        final String city, final List<String> timeRanges,
                                        final AuctionSortOption sort,
                                        final int page, final int pageSize,
                                        final boolean requirePositiveStock) {
        final List<Long> ids = queryAuctionIds(query, tags, city, timeRanges, sort, page, pageSize, requirePositiveStock);
        if (ids.isEmpty()) {
            return Collections.emptyList();
        }

        final List<Auction> auctions = em.createQuery(
                        "SELECT a FROM Auction a JOIN FETCH a.pack p WHERE a.id IN :ids", Auction.class)
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
                                     final boolean requirePositiveStock) {
        final boolean withTags = tags != null && !tags.isEmpty();
        final StringBuilder hql = new StringBuilder("SELECT COUNT(DISTINCT a.id) ");
        final List<Object> params = new ArrayList<>();
        appendFilterFromClause(hql, withTags);
        appendCommonCatalogFilters(hql, params, query, city, timeRanges, requirePositiveStock);
        if (withTags) {
            appendAllTagsCountFilter(hql, params, tags);
        }

        final TypedQuery<Long> typedQuery = em.createQuery(hql.toString(), Long.class);
        for (int i = 0; i < params.size(); i++) {
            typedQuery.setParameter(i + 1, params.get(i));
        }
        final Long count = typedQuery.getSingleResult();
        return count != null ? count.intValue() : 0;
    }

    @Override
    public List<Auction> findByCommerceId(final long commerceId) {
        return em.createQuery("SELECT a FROM Auction a JOIN FETCH a.pack p WHERE p.commerceId = :commerceId AND p.deleted = false ORDER BY a.createdAt DESC", Auction.class)
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
        return em.createQuery("SELECT a FROM Auction a JOIN FETCH a.pack p WHERE a.status = 'ACTIVE' AND a.endTime <= :now AND p.deleted = false", Auction.class)
                .setParameter("now", LocalDateTime.now(ZoneOffset.UTC))
                .getResultList();
    }

    private void appendParticipatedConditions(StringBuilder hql, List<Object> params, long clientId, Auction.Status status, String query) {
        hql.append("FROM Auction a JOIN a.pack p JOIN Commerce c ON p.commerceId = c.userId WHERE p.deleted = false ");
        hql.append("AND a.id IN (SELECT b.auctionId FROM Bid b WHERE b.clientId = ?").append(params.size() + 1).append(") ");
        params.add(clientId);

        if (status != null) {
            hql.append("AND a.status = ?").append(params.size() + 1).append(" ");
            params.add(status);
        }

        final Optional<String> pattern = LikePatternSupport.escapeAndWrap(query);
        if (pattern.isPresent()) {
            final String search = pattern.get();
            final String titleParam = "?" + (params.size() + 1);
            final String descriptionParam = "?" + (params.size() + 2);
            final String commerceParam = "?" + (params.size() + 3);
            hql.append("AND (");
            LikePatternSupport.appendEscapedLike(hql, "p.title", titleParam);
            hql.append(" OR ");
            LikePatternSupport.appendEscapedLike(hql, "p.description", descriptionParam);
            hql.append(" OR ");
            LikePatternSupport.appendEscapedLike(hql, "c.commercialName", commerceParam);
            hql.append(") ");
            params.add(search);
            params.add(search);
            params.add(search);
        }
    }

    @Override
    public List<Auction> filterParticipatedAuctions(final long clientId, final Auction.Status status, final String query, final int page, final int pageSize) {
        StringBuilder idHql = new StringBuilder("SELECT a.id, MAX(b.timestamp) ");
        idHql.append("FROM Auction a JOIN a.pack p JOIN Commerce c ON p.commerceId = c.userId, Bid b WHERE a.id = b.auctionId ");
        idHql.append("AND p.deleted = false AND b.clientId = ?1 ");

        List<Object> params = new ArrayList<>();
        params.add(clientId);

        if (status != null) {
            idHql.append("AND a.status = ?2 ");
            params.add(status);
        }

        final Optional<String> pattern = LikePatternSupport.escapeAndWrap(query);
        if (pattern.isPresent()) {
            final String search = pattern.get();
            final String titleParam = "?" + (params.size() + 1);
            final String descriptionParam = "?" + (params.size() + 2);
            final String commerceParam = "?" + (params.size() + 3);
            idHql.append("AND (");
            LikePatternSupport.appendEscapedLike(idHql, "p.title", titleParam);
            idHql.append(" OR ");
            LikePatternSupport.appendEscapedLike(idHql, "p.description", descriptionParam);
            idHql.append(" OR ");
            LikePatternSupport.appendEscapedLike(idHql, "c.commercialName", commerceParam);
            idHql.append(") ");
            params.add(search);
            params.add(search);
            params.add(search);
        }
        idHql.append("GROUP BY a.id ORDER BY MAX(b.timestamp) DESC");

        TypedQuery<Object[]> typedQuery = em.createQuery(idHql.toString(), Object[].class);
        for (int i = 0; i < params.size(); i++) {
            typedQuery.setParameter(i + 1, params.get(i));
        }
        typedQuery.setMaxResults(pageSize);
        typedQuery.setFirstResult((page - 1) * pageSize);

        List<Object[]> results = typedQuery.getResultList();
        if (results.isEmpty()) {
            return new ArrayList<>();
        }

        List<Long> ids = new ArrayList<>();
        for (Object[] row : results) {
            ids.add((Long) row[0]);
        }

        List<Auction> auctions = em.createQuery("SELECT a FROM Auction a JOIN FETCH a.pack p WHERE a.id IN :ids", Auction.class)
                .setParameter("ids", ids)
                .getResultList();

        auctions.sort((a1, a2) -> Integer.compare(ids.indexOf(a1.getId()), ids.indexOf(a2.getId())));
        return auctions;
    }

    @Override
    public int countParticipatedAuctions(final long clientId, final Auction.Status status, final String query) {
        StringBuilder hql = new StringBuilder("SELECT COUNT(DISTINCT a.id) ");
        List<Object> params = new ArrayList<>();
        appendParticipatedConditions(hql, params, clientId, status, query);

        TypedQuery<Long> typedQuery = em.createQuery(hql.toString(), Long.class);
        for (int i = 0; i < params.size(); i++) {
            typedQuery.setParameter(i + 1, params.get(i));
        }
        Long count = typedQuery.getSingleResult();
        return count != null ? count.intValue() : 0;
    }
}
