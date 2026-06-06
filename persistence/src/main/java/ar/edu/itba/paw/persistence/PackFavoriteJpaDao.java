package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.models.pack.ClientPackFavorite;
import ar.edu.itba.paw.models.pack.ClientPackFavoriteId;
import ar.edu.itba.paw.models.pack.Pack;
import ar.edu.itba.paw.models.user.Client;
import ar.edu.itba.paw.persistence.util.Pagination;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Pack favorites are mapped by {@link ClientPackFavorite} on {@code client_pack_favorites}.
 * List queries use a two-step pattern (pack ids, then fetch) to preserve order and avoid fetch pagination issues.
 */
@Primary
@Repository
public class PackFavoriteJpaDao implements PackFavoriteDao {

    private static final String ACTIVE_FAVORITE_FILTER =
            " f.client.userId = :cid AND p.active = true AND p.deleted = false";

    @PersistenceContext
    private EntityManager em;

    @Override
    public List<Pack> findActiveFavoritePacksForClient(final long clientId, final int page, final int pageSize) {
        final int safePage = Math.max(1, page);
        final int safeSize = Math.max(1, pageSize);

        final List<Long> ids = queryActiveFavoritePackIds(clientId, safePage, safeSize);
        if (ids.isEmpty()) {
            return Collections.emptyList();
        }

        final List<Pack> packs = em.createQuery(
                        "SELECT p FROM Pack p JOIN FETCH p.commerce WHERE p.id IN :ids ORDER BY "
                                + buildPackIdPositionOrderByClause(ids),
                        Pack.class)
                .setParameter("ids", ids)
                .getResultList();

        // Tags fetched separately: DISTINCT + LEFT JOIN FETCH tags + ORDER BY CASE is rejected by PostgreSQL.
        em.createQuery(
                        "SELECT DISTINCT p FROM Pack p LEFT JOIN FETCH p.tags WHERE p.id IN :ids",
                        Pack.class)
                .setParameter("ids", ids)
                .getResultList();

        return packs;
    }

    @Override
    public int countActiveFavoritePacksForClient(final long clientId) {
        final Long count = em.createQuery(
                        "SELECT COUNT(f) FROM ClientPackFavorite f JOIN f.pack p WHERE" + ACTIVE_FAVORITE_FILTER,
                        Long.class)
                .setParameter("cid", clientId)
                .getSingleResult();
        return count != null ? count.intValue() : 0;
    }

    @Override
    public boolean exists(final long clientId, final long packId) {
        final Long count = em.createQuery(
                        "SELECT COUNT(f) FROM ClientPackFavorite f"
                                + " WHERE f.client.userId = :cid AND f.pack.id = :pid",
                        Long.class)
                .setParameter("cid", clientId)
                .setParameter("pid", packId)
                .getSingleResult();
        return count != null && count.longValue() > 0L;
    }

    @Override
    public void insert(final long clientId, final long packId) {
        final ClientPackFavorite favorite = new ClientPackFavorite(
                em.getReference(Client.class, clientId),
                em.getReference(Pack.class, packId),
                LocalDateTime.now(ZoneOffset.UTC));
        em.persist(favorite);
        em.flush();
    }

    @Override
    public void delete(final long clientId, final long packId) {
        final ClientPackFavorite favorite = em.find(
                ClientPackFavorite.class, new ClientPackFavoriteId(clientId, packId));
        if (favorite != null) {
            em.remove(favorite);
            em.flush();
        }
    }

    @Override
    public List<Long> findClientIdsByPack(final long packId) {
        return em.createQuery(
                        "SELECT f.client.userId FROM ClientPackFavorite f WHERE f.pack.id = :pid",
                        Long.class)
                .setParameter("pid", packId)
                .getResultList();
    }

    private List<Long> queryActiveFavoritePackIds(final long clientId, final int page, final int pageSize) {
        final javax.persistence.Query idQuery = em.createNativeQuery(
                "SELECT f.pack_id FROM client_pack_favorites f "
                        + "INNER JOIN packs p ON p.id = f.pack_id "
                        + "WHERE f.client_id = :cid AND p.active = TRUE AND p.deleted = FALSE "
                        + "ORDER BY f.created_at DESC");
        idQuery.setParameter("cid", clientId);
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

    private String buildPackIdPositionOrderByClause(final List<Long> ids) {
        final StringBuilder orderBy = new StringBuilder("CASE p.id ");
        for (int index = 0; index < ids.size(); index++) {
            orderBy.append("WHEN ").append(ids.get(index).longValue()).append(" THEN ").append(index).append(' ');
        }
        orderBy.append("ELSE ").append(ids.size()).append(" END");
        return orderBy.toString();
    }
}
