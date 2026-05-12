package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.models.auction.Auction;
import ar.edu.itba.paw.models.pack.Municipality;
import ar.edu.itba.paw.models.pack.Pack;
import ar.edu.itba.paw.models.pack.PackSortOption;
import ar.edu.itba.paw.models.pack.PackTag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Locale;

@Primary
@Repository
public class PackJpaDao implements PackDao {

    private static final Logger LOGGER = LoggerFactory.getLogger(PackJpaDao.class);

    @PersistenceContext
    private EntityManager em;

    @Override
    public Pack createPack(final Long commerceId, final String title, final String description,
                           final Double originalPrice, final Double finalPrice, final Integer stock,
                           final List<PackTag> tags, final Long imageId) {
        final Pack pack = new Pack(null, commerceId, title, description, originalPrice, finalPrice, stock, true, false,
                new ArrayList<>(), imageId);
        pack.setTags(tags != null ? new ArrayList<>(tags) : new ArrayList<>());
        em.persist(pack);
        return pack;
    }

    @Override
    public Optional<Pack> findById(final Long id) {
        return Optional.ofNullable(em.find(Pack.class, id));
    }

    @Override
    public List<Pack> findAll() {
        return em.createQuery("FROM Pack p", Pack.class).getResultList();
    }

    @Override
    public List<Pack> findByCommerceId(final Long commerceId) {
        return em.createQuery("FROM Pack p WHERE p.commerceId = :cid AND p.deleted = false ORDER BY p.id DESC", Pack.class)
                .setParameter("cid", commerceId)
                .getResultList();
    }

    @Override
    public Pack update(final Pack pack) {
        return pack;
    }

    @Override
    public void setActive(final Long id, final boolean active) {
        final int updated = em.createQuery("UPDATE Pack p SET p.active = :active WHERE p.id = :id")
                .setParameter("active", active)
                .setParameter("id", id)
                .executeUpdate();
        if (updated == 0) {
            LOGGER.warn("setActive: no packs row matched for id {} (active={})", id, Boolean.valueOf(active));
        }
    }

    @Override
    public void softDelete(final Long id) {
        final int updated = em.createQuery("UPDATE Pack p SET p.deleted = true WHERE p.id = :id")
                .setParameter("id", id)
                .executeUpdate();
        if (updated == 0) {
            LOGGER.warn("softDelete: no packs row matched for id {}", id);
        }
    }

    @Override
    public boolean decrementStock(final long packId, final int quantity) {
        if (quantity < 1) {
            return false;
        }
        final int updated = em.createQuery("UPDATE Pack p SET p.stock = p.stock - :qty WHERE p.id = :id AND p.stock >= :qty")
                .setParameter("qty", quantity)
                .setParameter("id", packId)
                .executeUpdate();
        if (updated != 1) {
            LOGGER.debug("decrementStock: expected exactly one updated row, got {} (packId={}, quantity={})",
                    Integer.valueOf(updated), Long.valueOf(packId), Integer.valueOf(quantity));
        }
        return updated == 1;
    }

    @Override
    public boolean incrementStock(final long packId, final int quantity) {
        if (quantity < 1) {
            return false;
        }
        final int updated = em.createQuery("UPDATE Pack p SET p.stock = p.stock + :qty WHERE p.id = :id")
                .setParameter("qty", quantity)
                .setParameter("id", packId)
                .executeUpdate();
        if (updated != 1) {
            LOGGER.debug("incrementStock: expected exactly one updated row, got {} (packId={}, quantity={})",
                    Integer.valueOf(updated), Long.valueOf(packId), Integer.valueOf(quantity));
        }
        return updated == 1;
    }

    @Override
    public List<Pack> filterPacks(final String query, final List<PackTag> tags, final String city,
                                  final List<String> timeRanges, final PackSortOption sort,
                                  final int page, final int pageSize, final boolean requirePositiveStock) {
        final List<Long> ids = queryPackIds(query, tags, city, timeRanges, sort, page, pageSize, requirePositiveStock);
        if (ids.isEmpty()) {
            return Collections.emptyList();
        }
        final List<Pack> packs = em.createQuery("SELECT DISTINCT p FROM Pack p LEFT JOIN FETCH p.tags WHERE p.id IN :ids",
                        Pack.class)
                .setParameter("ids", ids)
                .getResultList();
        final Map<Long, Integer> positions = new java.util.HashMap<>();
        for (int index = 0; index < ids.size(); index++) {
            positions.put(ids.get(index), Integer.valueOf(index));
        }
        packs.sort(Comparator.comparingInt(pack -> positions.getOrDefault(pack.getId(), Integer.MAX_VALUE)));
        return packs;
    }

    @Override
    public int countFilteredPacks(final String query, final List<PackTag> tags, final String city,
                                  final List<String> timeRanges, final boolean requirePositiveStock) {
        final StringBuilder jpql = new StringBuilder("SELECT COUNT(p.id) FROM Pack p, Commerce c WHERE c.userId = p.commerceId AND p.active = true AND p.deleted = false AND NOT EXISTS (SELECT a.id FROM Auction a WHERE a.pack.id = p.id AND a.status = :activeStatus)");
        final Map<String, Object> params = new LinkedHashMap<>();
        params.put("activeStatus", Auction.Status.ACTIVE);
        appendOptionalFilters(jpql, params, query, tags, city, timeRanges, requirePositiveStock);
        final Number count = (Number) createQuery(jpql.toString(), params, Long.class).getSingleResult();
        return count == null ? 0 : count.intValue();
    }

    @Override
    public List<Pack> filterCommercePacks(final Long commerceId, final Boolean hasAuction, final int page, final int pageSize) {
        final StringBuilder jpql = new StringBuilder("SELECT p FROM Pack p WHERE p.commerceId = :cid AND p.deleted = false");
        final Map<String, Object> params = new LinkedHashMap<>();
        params.put("cid", commerceId);
        if (hasAuction != null) {
            if (hasAuction.booleanValue()) {
                jpql.append(" AND EXISTS (SELECT a.id FROM Auction a WHERE a.pack.id = p.id)");
            } else {
                jpql.append(" AND NOT EXISTS (SELECT a.id FROM Auction a WHERE a.pack.id = p.id)");
            }
        }
        jpql.append(" ORDER BY p.id DESC");
        return createQuery(jpql.toString(), params, Pack.class)
                .setFirstResult(Math.max(0, page - 1) * pageSize)
                .setMaxResults(pageSize)
                .getResultList();
    }

    @Override
    public int countCommercePacks(final Long commerceId, final Boolean hasAuction) {
        final StringBuilder jpql = new StringBuilder("SELECT COUNT(p.id) FROM Pack p WHERE p.commerceId = :cid AND p.deleted = false");
        final Map<String, Object> params = new LinkedHashMap<>();
        params.put("cid", commerceId);
        if (hasAuction != null) {
            if (hasAuction.booleanValue()) {
                jpql.append(" AND EXISTS (SELECT a.id FROM Auction a WHERE a.pack.id = p.id)");
            } else {
                jpql.append(" AND NOT EXISTS (SELECT a.id FROM Auction a WHERE a.pack.id = p.id)");
            }
        }
        final Number count = (Number) createQuery(jpql.toString(), params, Long.class).getSingleResult();
        return count == null ? 0 : count.intValue();
    }

    private List<Long> queryPackIds(final String query, final List<PackTag> tags, final String city,
                                    final List<String> timeRanges, final PackSortOption sort,
                                    final int page, final int pageSize, final boolean requirePositiveStock) {
        final StringBuilder jpql = new StringBuilder("SELECT p.id FROM Pack p, Commerce c WHERE c.userId = p.commerceId AND p.active = true AND p.deleted = false AND NOT EXISTS (SELECT a.id FROM Auction a WHERE a.pack.id = p.id AND a.status = :activeStatus)");
        final Map<String, Object> params = new LinkedHashMap<>();
        params.put("activeStatus", Auction.Status.ACTIVE);
        appendOptionalFilters(jpql, params, query, tags, city, timeRanges, requirePositiveStock);
        jpql.append(" ORDER BY ").append(toOrderByClause(sort));
        return createQuery(jpql.toString(), params, Long.class)
                .setFirstResult(Math.max(0, page - 1) * pageSize)
                .setMaxResults(pageSize)
                .getResultList();
    }

    private void appendOptionalFilters(final StringBuilder jpql, final Map<String, Object> params,
                                       final String query, final List<PackTag> tags, final String city,
                                       final List<String> timeRanges, final boolean requirePositiveStock) {
        if (requirePositiveStock) {
            jpql.append(" AND p.stock > 0");
        }
        if (query != null && !query.isBlank()) {
            final String normalized = "%" + query.trim().toLowerCase(Locale.ROOT) + "%";
            jpql.append(" AND (LOWER(p.title) LIKE :search OR LOWER(c.commercialName) LIKE :search)");
            params.put("search", normalized);
        }
        final Municipality municipality = Municipality.fromCityName(city);
        if (municipality != null) {
            jpql.append(" AND c.city = :city");
            params.put("city", municipality);
        }
        final List<String> timePredicates = new ArrayList<>();
        if (timeRanges != null) {
            for (final String range : timeRanges) {
                if ("morning".equals(range)) {
                    timePredicates.add("c.openingTime < '12:00'");
                } else if ("afternoon".equals(range)) {
                    timePredicates.add("c.openingTime >= '12:00' AND c.openingTime < '17:00'");
                } else if ("evening".equals(range)) {
                    timePredicates.add("c.openingTime >= '17:00'");
                }
            }
        }
        if (!timePredicates.isEmpty()) {
            jpql.append(" AND (").append(String.join(" OR ", timePredicates)).append(")");
        }
        if (tags != null && !tags.isEmpty()) {
            // Each required tag must be present, so we add one MEMBER OF predicate per tag instead of grouping.
            for (int index = 0; index < tags.size(); index++) {
                final String paramName = "tag" + index;
                jpql.append(" AND :").append(paramName).append(" MEMBER OF p.tags");
                params.put(paramName, tags.get(index));
            }
        }
    }

    private <T> javax.persistence.TypedQuery<T> createQuery(final String jpql, final Map<String, Object> params,
                                                            final Class<T> resultType) {
        final javax.persistence.TypedQuery<T> query = em.createQuery(jpql, resultType);
        for (final Map.Entry<String, Object> entry : params.entrySet()) {
            query.setParameter(entry.getKey(), entry.getValue());
        }
        return query;
    }

    private String toOrderByClause(final PackSortOption sort) {
        final PackSortOption safeSort = sort != null ? sort : PackSortOption.DATE_DESC;
        switch (safeSort) {
            case TITLE_ASC:
                return "p.title ASC, p.id DESC";
            case PRICE_ASC:
                return "p.finalPrice ASC, p.id DESC";
            case PRICE_DESC:
                return "p.finalPrice DESC, p.id DESC";
            case DISCOUNT_DESC:
                return "(p.originalPrice - p.finalPrice) DESC, p.id DESC";
            case DATE_DESC:
            default:
                return "p.id DESC";
        }
    }
}