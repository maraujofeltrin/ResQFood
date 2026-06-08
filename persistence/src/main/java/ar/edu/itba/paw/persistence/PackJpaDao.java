package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.models.auction.Auction;
import ar.edu.itba.paw.models.image.Image;
import ar.edu.itba.paw.models.pack.Municipality;
import ar.edu.itba.paw.models.pack.Pack;
import ar.edu.itba.paw.models.user.Commerce;
import ar.edu.itba.paw.models.pack.PackSortOption;
import ar.edu.itba.paw.models.pack.PackTag;
import ar.edu.itba.paw.persistence.util.JpqlQuerySupport;
import ar.edu.itba.paw.persistence.util.LikePatternSupport;
import ar.edu.itba.paw.persistence.util.OpeningTimeFilterJpql;
import ar.edu.itba.paw.persistence.util.OpeningTimeFilterSql;
import ar.edu.itba.paw.persistence.util.Pagination;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
        final Commerce commerce = em.getReference(Commerce.class, commerceId);
        final Image image = imageId != null ? em.getReference(Image.class, imageId) : null;
        final Pack pack = new Pack(null, commerce, title, description, originalPrice, finalPrice, stock, true, false,
                new ArrayList<>(), image);
        pack.setTags(tags != null ? new ArrayList<>(tags) : new ArrayList<>());
        em.persist(pack);
        return pack;
    }

    @Override
    public Optional<Pack> findById(final Long id) {
        return em.createQuery(
                        "SELECT DISTINCT p FROM Pack p LEFT JOIN FETCH p.tags JOIN FETCH p.commerce "
                                + "LEFT JOIN FETCH p.auction WHERE p.id = :id",
                        Pack.class)
                .setParameter("id", id)
                .getResultList()
                .stream()
                .findFirst();
    }

    @Override
    public List<Pack> findAll() {
        return em.createQuery("FROM Pack p", Pack.class).getResultList();
    }

    @Override
    public List<Pack> findByCommerceId(final Long commerceId) {
        return em.createQuery("FROM Pack p WHERE p.commerce.userId = :cid AND p.deleted = false ORDER BY p.id DESC", Pack.class)
                .setParameter("cid", commerceId)
                .getResultList();
    }

    @Override
    public Pack update(final Pack pack) {
        return em.merge(pack);
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
                                  final int page, final int pageSize, final boolean requirePositiveStock,
                                  final Long commerceUserId) {
        final List<Long> ids = queryPackIds(query, tags, city, timeRanges, sort, page, pageSize,
                requirePositiveStock, commerceUserId);
        if (ids.isEmpty()) {
            return Collections.emptyList();
        }
        final List<Pack> packs = em.createQuery(
                        "SELECT DISTINCT p FROM Pack p LEFT JOIN FETCH p.tags JOIN FETCH p.commerce "
                                + "LEFT JOIN FETCH p.auction "
                                + "WHERE p.id IN :ids ORDER BY " + toOrderByClause(sort),
                        Pack.class)
                .setParameter("ids", ids)
                .getResultList();
        return packs;
    }

    @Override
    public int countFilteredPacks(final String query, final List<PackTag> tags, final String city,
                                  final List<String> timeRanges, final boolean requirePositiveStock,
                                  final Long commerceUserId) {
        final StringBuilder jpql = new StringBuilder("SELECT COUNT(p.id) FROM Pack p JOIN p.commerce c WHERE p.active = true AND p.deleted = false AND NOT EXISTS (SELECT a.id FROM Auction a WHERE a.pack.id = p.id AND a.status = :activeStatus)");
        final Map<String, Object> params = new LinkedHashMap<>();
        params.put("activeStatus", Auction.Status.ACTIVE);
        appendOptionalFilters(jpql, params, query, tags, city, timeRanges, requirePositiveStock, commerceUserId);
        final Number count = (Number) JpqlQuerySupport.createQuery(em, jpql.toString(), params, Long.class).getSingleResult();
        return count == null ? 0 : count.intValue();
    }

    @Override
    public List<Pack> filterCommercePacks(final Long commerceId, final Boolean hasAuction, final int page, final int pageSize) {
        final StringBuilder jpql = new StringBuilder(
                "SELECT p FROM Pack p LEFT JOIN FETCH p.auction "
                        + "WHERE p.commerce.userId = :cid AND p.deleted = false");
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
        return JpqlQuerySupport.createQuery(em, jpql.toString(), params, Pack.class)
                .setFirstResult(Pagination.offset(page, pageSize))
                .setMaxResults(pageSize)
                .getResultList();
    }

    @Override
    public List<Pack> findPublicOffersByCommerce(final Long commerceUserId, final int page, final int pageSize) {
        final String idSql =
                "SELECT p.id FROM packs p "
                        + "WHERE p.commerce_id = :cid AND p.active = TRUE AND p.deleted = FALSE "
                        + "AND ( "
                        + "  (p.stock > 0 AND NOT EXISTS (SELECT 1 FROM auctions a WHERE a.pack_id = p.id AND a.status = :activeStatus)) "
                        + "  OR EXISTS (SELECT 1 FROM auctions a WHERE a.pack_id = p.id AND a.status = :activeStatus) "
                        + ") "
                        + "ORDER BY p.id DESC";
        final Query idQuery = em.createNativeQuery(idSql);
        idQuery.setParameter("cid", commerceUserId);
        idQuery.setParameter("activeStatus", Auction.Status.ACTIVE.name());
        idQuery.setFirstResult(Pagination.offset(page, pageSize));
        idQuery.setMaxResults(pageSize);

        final List<?> rawIds = idQuery.getResultList();
        if (rawIds.isEmpty()) {
            return Collections.emptyList();
        }
        final List<Long> ids = new ArrayList<>(rawIds.size());
        for (final Object rawId : rawIds) {
            ids.add(rawId instanceof Number ? ((Number) rawId).longValue() : Long.parseLong(rawId.toString()));
        }

        return em.createQuery(
                        "SELECT DISTINCT p FROM Pack p "
                                + "JOIN FETCH p.commerce "
                                + "LEFT JOIN FETCH p.auction "
                                + "WHERE p.id IN :ids "
                                + "ORDER BY p.id DESC",
                        Pack.class)
                .setParameter("ids", ids)
                .getResultList();
    }

    @Override
    public int countPublicOffersByCommerce(final Long commerceUserId) {
        final Number count = (Number) em.createQuery(
                        "SELECT COUNT(p.id) FROM Pack p "
                                + "WHERE p.commerce.userId = :cid AND p.active = true AND p.deleted = false "
                                + "AND ( "
                                + "  (p.stock > 0 AND NOT EXISTS (SELECT a.id FROM Auction a WHERE a.pack = p AND a.status = :activeStatus)) "
                                + "  OR EXISTS (SELECT a.id FROM Auction a WHERE a.pack = p AND a.status = :activeStatus) "
                                + ")")
                .setParameter("cid", commerceUserId)
                .setParameter("activeStatus", Auction.Status.ACTIVE)
                .getSingleResult();
        return count == null ? 0 : count.intValue();
    }

    @Override
    public int countCommercePacks(final Long commerceId, final Boolean hasAuction) {
        final StringBuilder jpql = new StringBuilder("SELECT COUNT(p.id) FROM Pack p WHERE p.commerce.userId = :cid AND p.deleted = false");
        final Map<String, Object> params = new LinkedHashMap<>();
        params.put("cid", commerceId);
        if (hasAuction != null) {
            if (hasAuction.booleanValue()) {
                jpql.append(" AND EXISTS (SELECT a.id FROM Auction a WHERE a.pack.id = p.id)");
            } else {
                jpql.append(" AND NOT EXISTS (SELECT a.id FROM Auction a WHERE a.pack.id = p.id)");
            }
        }
        final Number count = (Number) JpqlQuerySupport.createQuery(em, jpql.toString(), params, Long.class).getSingleResult();
        return count == null ? 0 : count.intValue();
    }

    private List<Long> queryPackIds(final String query, final List<PackTag> tags, final String city,
                                    final List<String> timeRanges, final PackSortOption sort,
                                    final int page, final int pageSize, final boolean requirePositiveStock,
                                    final Long commerceUserId) {
        final StringBuilder sql = new StringBuilder(
                "SELECT p.id FROM packs p INNER JOIN commerces c ON c.user_id = p.commerce_id "
                        + "WHERE p.active = TRUE AND p.deleted = FALSE "
                        + "AND NOT EXISTS (SELECT 1 FROM auctions a WHERE a.pack_id = p.id AND a.status = :activeStatus)");
        final Map<String, Object> params = new LinkedHashMap<>();
        params.put("activeStatus", Auction.Status.ACTIVE.name());
        appendOptionalFiltersNative(sql, params, query, tags, city, timeRanges, requirePositiveStock, commerceUserId);
        sql.append(" ORDER BY ").append(toNativeOrderByClause(sort));

        final Query idQuery = em.createNativeQuery(sql.toString());
        for (final Map.Entry<String, Object> entry : params.entrySet()) {
            idQuery.setParameter(entry.getKey(), entry.getValue());
        }
        idQuery.setFirstResult(Pagination.offset(page, pageSize));
        idQuery.setMaxResults(pageSize);

        final List<?> rawIds = idQuery.getResultList();
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

    private void appendOptionalFiltersNative(final StringBuilder sql, final Map<String, Object> params,
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

    private void appendOptionalFilters(final StringBuilder jpql, final Map<String, Object> params,
                                       final String query, final List<PackTag> tags, final String city,
                                       final List<String> timeRanges, final boolean requirePositiveStock,
                                       final Long commerceUserId) {
        if (commerceUserId != null) {
            jpql.append(" AND p.commerce.userId = :commerceUserId");
            params.put("commerceUserId", commerceUserId);
        }
        if (requirePositiveStock) {
            jpql.append(" AND p.stock > 0");
        }
        final Optional<String> searchPattern = LikePatternSupport.escapeAndWrap(query);
        if (searchPattern.isPresent()) {
            jpql.append(" AND (");
            LikePatternSupport.appendEscapedLike(jpql, "p.title", ":search");
            jpql.append(" OR ");
            LikePatternSupport.appendEscapedLike(jpql, "c.commercialName", ":search");
            jpql.append(")");
            params.put("search", searchPattern.get());
        }
        final Municipality municipality = Municipality.fromCityName(city);
        if (municipality != null) {
            jpql.append(" AND c.city = :city");
            params.put("city", municipality);
        }
        OpeningTimeFilterJpql.appendTimeRangeConditions(jpql, "c.openingTime", timeRanges);
        if (tags != null && !tags.isEmpty()) {
            // Each required tag must be present, so we add one MEMBER OF predicate per tag instead of grouping.
            for (int index = 0; index < tags.size(); index++) {
                final String paramName = "tag" + index;
                jpql.append(" AND :").append(paramName).append(" MEMBER OF p.tags");
                params.put(paramName, tags.get(index));
            }
        }
    }

    private String toNativeOrderByClause(final PackSortOption sort) {
        final PackSortOption safeSort = sort != null ? sort : PackSortOption.DATE_DESC;
        return safeSort.getOrderByClause().replace("packs.", "p.");
    }

    private String toOrderByClause(final PackSortOption sort) {
        return toNativeOrderByClause(sort)
                .replace("final_price", "finalPrice")
                .replace("original_price", "originalPrice");
    }
}