package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.models.auction.Auction;
import ar.edu.itba.paw.models.auction.AuctionSortOption;
import ar.edu.itba.paw.models.pack.Pack;
import ar.edu.itba.paw.models.pack.PackTag;
import org.springframework.stereotype.Repository;
import org.springframework.context.annotation.Primary;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
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

    private void appendFilterJoinsAndConditions(StringBuilder hql, List<Object> params, String query, List<PackTag> tags, String city, List<String> timeRanges, boolean requirePositiveStock) {
        hql.append("FROM Auction a JOIN FETCH a.pack p ");
        // Note: As Pack is not yet mapped with Commerce in this snippet, we assume standard properties.
        // But we need commerce to filter by city and query. We can join Commerce if mapped, or use subqueries.
        // Assuming we can join commerce:
        // For now, let's use standard JPQL based on the assumption that Pack has a commerce mapped, or we just join the Commerce entity explicitly.
        hql.append(", ar.edu.itba.paw.models.user.Commerce c WHERE p.commerceId = c.id ");

        if (tags != null && !tags.isEmpty()) {
            hql.append("AND EXISTS (SELECT 1 FROM p.tags t WHERE t IN (?").append(params.size() + 1).append(")) ");
            params.add(tags);
        }

        hql.append("AND a.status = 'ACTIVE' AND a.endTime > ?").append(params.size() + 1).append(" ");
        hql.append("AND p.active = true AND p.deleted = false ");
        params.add(LocalDateTime.now(ZoneOffset.UTC));

        if (requirePositiveStock) {
            hql.append("AND p.stock > 0 ");
        }

        if (query != null && !query.isBlank()) {
            final String escapedQuery = query.trim()
                    .replace("\\", "\\\\")
                    .replace("%", "\\%")
                    .replace("_", "\\_");
            final String pattern = "%" + escapedQuery + "%";
            hql.append("AND (LOWER(p.title) LIKE LOWER(?").append(params.size() + 1).append(") ESCAPE '\\' OR LOWER(c.commercialName) LIKE LOWER(?").append(params.size() + 2).append(") ESCAPE '\\') ");
            params.add(pattern);
            params.add(pattern);
        }

        if (city != null && !city.isBlank()) {
            hql.append("AND c.city = ?").append(params.size() + 1).append(" ");
            params.add(city);
        }

        if (timeRanges != null && !timeRanges.isEmpty()) {
            List<String> timeConditions = new ArrayList<>();
            for (String range : timeRanges) {
                switch (range) {
                    case "morning":
                        timeConditions.add("CAST(SUBSTRING(c.openingTime, 1, LOCATE(':', c.openingTime) - 1) AS int) < 12");
                        break;
                    case "afternoon":
                        timeConditions.add("(CAST(SUBSTRING(c.openingTime, 1, LOCATE(':', c.openingTime) - 1) AS int) >= 12 AND CAST(SUBSTRING(c.openingTime, 1, LOCATE(':', c.openingTime) - 1) AS int) < 17)");
                        break;
                    case "evening":
                        timeConditions.add("CAST(SUBSTRING(c.openingTime, 1, LOCATE(':', c.openingTime) - 1) AS int) >= 17");
                        break;
                }
            }
            if (!timeConditions.isEmpty()) {
                hql.append("AND (").append(String.join(" OR ", timeConditions)).append(") ");
            }
        }
    }

    @Override
    public List<Auction> filterAuctions(final String query, final List<PackTag> tags,
                                        final String city, final List<String> timeRanges,
                                        final AuctionSortOption sort,
                                        final int page, final int pageSize,
                                        final boolean requirePositiveStock) {
        StringBuilder hql = new StringBuilder("SELECT a ");
        List<Object> params = new ArrayList<>();
        appendFilterJoinsAndConditions(hql, params, query, tags, city, timeRanges, requirePositiveStock);

        if (tags != null && !tags.isEmpty()) {
            // Need all tags to match, not just one.
            hql = new StringBuilder("SELECT a FROM Auction a JOIN a.pack p, ar.edu.itba.paw.models.user.Commerce c JOIN p.tags t WHERE p.commerceId = c.id ");
            hql.append("AND a.status = 'ACTIVE' AND a.endTime > ?1 AND p.active = true AND p.deleted = false ");
            params.clear();
            params.add(LocalDateTime.now(ZoneOffset.UTC));
            if (requirePositiveStock) hql.append("AND p.stock > 0 ");
            // Append other query/city/time conditions here for tags
            if (query != null && !query.isBlank()) {
                final String pattern = "%" + query.trim().replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_") + "%";
                hql.append("AND (LOWER(p.title) LIKE LOWER(?2) ESCAPE '\\' OR LOWER(c.commercialName) LIKE LOWER(?3) ESCAPE '\\') ");
                params.add(pattern);
                params.add(pattern);
            }
            if (city != null && !city.isBlank()) {
                hql.append("AND c.city = ?").append(params.size() + 1).append(" ");
                params.add(city);
            }
            // Group by to enforce ALL tags
            hql.append("AND t IN (?").append(params.size() + 1).append(") GROUP BY a HAVING COUNT(DISTINCT t) = ?").append(params.size() + 2).append(" ");
            params.add(tags);
            params.add((long) tags.size());
        }

        AuctionSortOption safeSort = sort != null ? sort : AuctionSortOption.TIME_REMAINING_ASC;
        // Map sort to HQL
        switch (safeSort) {
            case TIME_REMAINING_ASC: hql.append("ORDER BY a.endTime ASC "); break;
            case TIME_REMAINING_DESC: hql.append("ORDER BY a.endTime DESC "); break;
            case PRICE_ASC: hql.append("ORDER BY COALESCE(a.currentBid, a.initialPrice) ASC "); break;
            case PRICE_DESC: hql.append("ORDER BY COALESCE(a.currentBid, a.initialPrice) DESC "); break;
        }

        TypedQuery<Auction> typedQuery = em.createQuery(hql.toString(), Auction.class);
        for (int i = 0; i < params.size(); i++) {
            typedQuery.setParameter(i + 1, params.get(i));
        }
        typedQuery.setMaxResults(pageSize);
        typedQuery.setFirstResult((page - 1) * pageSize);

        List<Auction> auctions = typedQuery.getResultList();
        for (final Auction auction : auctions) {
            if (auction.getPack() != null) {
                auction.getPack().getCommerceId();
            }
        }
        return auctions;
    }

    @Override
    public int countFilteredAuctions(final String query, final List<PackTag> tags,
                                     final String city, final List<String> timeRanges,
                                     final boolean requirePositiveStock) {
        StringBuilder hql = new StringBuilder("SELECT COUNT(DISTINCT a.id) ");
        List<Object> params = new ArrayList<>();
        appendFilterJoinsAndConditions(hql, params, query, tags, city, timeRanges, requirePositiveStock);

        if (tags != null && !tags.isEmpty()) {
            hql = new StringBuilder("SELECT COUNT(DISTINCT a.id) FROM Auction a JOIN a.pack p, ar.edu.itba.paw.models.user.Commerce c JOIN p.tags t WHERE p.commerceId = c.id ");
            hql.append("AND a.status = 'ACTIVE' AND a.endTime > ?1 AND p.active = true AND p.deleted = false ");
            params.clear();
            params.add(LocalDateTime.now(ZoneOffset.UTC));
            if (requirePositiveStock) hql.append("AND p.stock > 0 ");
            if (query != null && !query.isBlank()) {
                final String pattern = "%" + query.trim().replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_") + "%";
                hql.append("AND (LOWER(p.title) LIKE LOWER(?2) ESCAPE '\\' OR LOWER(c.commercialName) LIKE LOWER(?3) ESCAPE '\\') ");
                params.add(pattern);
                params.add(pattern);
            }
            if (city != null && !city.isBlank()) {
                hql.append("AND c.city = ?").append(params.size() + 1).append(" ");
                params.add(city);
            }
            // For count, JPA can't count grouped easily, we might need a subquery for exact tag match
            hql.append("AND (SELECT COUNT(DISTINCT t2) FROM p.tags t2 WHERE t2 IN (?").append(params.size() + 1).append(")) = ?").append(params.size() + 2).append(" ");
            params.add(tags);
            params.add((long) tags.size());
        }

        TypedQuery<Long> typedQuery = em.createQuery(hql.toString(), Long.class);
        for (int i = 0; i < params.size(); i++) {
            typedQuery.setParameter(i + 1, params.get(i));
        }
        Long count = typedQuery.getSingleResult();
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
        hql.append("FROM Auction a JOIN a.pack p, ar.edu.itba.paw.models.user.Commerce c WHERE p.commerceId = c.id ");
        hql.append("AND p.deleted = false ");
        hql.append("AND a.id IN (SELECT b.auctionId FROM Bid b WHERE b.clientId = ?").append(params.size() + 1).append(") ");
        params.add(clientId);

        if (status != null) {
            hql.append("AND a.status = ?").append(params.size() + 1).append(" ");
            params.add(status);
        }

        if (query != null && !query.isBlank()) {
            final String escaped = query.trim().replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
            final String pattern = "%" + escaped + "%";
            hql.append("AND (LOWER(p.title) LIKE LOWER(?").append(params.size() + 1).append(") ESCAPE '\\' OR LOWER(p.description) LIKE LOWER(?").append(params.size() + 2).append(") ESCAPE '\\' OR LOWER(c.commercialName) LIKE LOWER(?").append(params.size() + 3).append(") ESCAPE '\\') ");
            params.add(pattern);
            params.add(pattern);
            params.add(pattern);
        }
    }

    @Override
    public List<Auction> filterParticipatedAuctions(final long clientId, final Auction.Status status, final String query, final int page, final int pageSize) {
        // We do 1+1 query for participated auctions to sort by latest bid
        StringBuilder idHql = new StringBuilder("SELECT a.id, MAX(b.timestamp) ");
        idHql.append("FROM Auction a JOIN a.pack p, ar.edu.itba.paw.models.user.Commerce c, Bid b WHERE p.commerceId = c.id AND a.id = b.auctionId ");
        idHql.append("AND p.deleted = false AND b.clientId = ?1 ");
        
        List<Object> params = new ArrayList<>();
        params.add(clientId);

        if (status != null) {
            idHql.append("AND a.status = ?2 ");
            params.add(status);
        }

        if (query != null && !query.isBlank()) {
            final String escaped = query.trim().replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
            final String pattern = "%" + escaped + "%";
            idHql.append("AND (LOWER(p.title) LIKE LOWER(?3) ESCAPE '\\' OR LOWER(p.description) LIKE LOWER(?4) ESCAPE '\\' OR LOWER(c.commercialName) LIKE LOWER(?5) ESCAPE '\\') ");
            params.add(pattern);
            params.add(pattern);
            params.add(pattern);
        }
        idHql.append("GROUP BY a.id ORDER BY MAX(b.timestamp) DESC");

        TypedQuery<Object[]> typedQuery = em.createQuery(idHql.toString(), Object[].class);
        for (int i = 0; i < params.size(); i++) {
            typedQuery.setParameter(i + 1, params.get(i));
        }
        typedQuery.setMaxResults(pageSize);
        typedQuery.setFirstResult((page - 1) * pageSize);

        List<Object[]> results = typedQuery.getResultList();
        if (results.isEmpty()) return new ArrayList<>();

        List<Long> ids = new ArrayList<>();
        for (Object[] row : results) {
            ids.add((Long) row[0]);
        }

        List<Auction> auctions = em.createQuery("SELECT a FROM Auction a JOIN FETCH a.pack p WHERE a.id IN :ids", Auction.class)
                .setParameter("ids", ids)
                .getResultList();

        // Sort in memory to preserve order from MAX(b.timestamp)
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
