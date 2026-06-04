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
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

        @SuppressWarnings("unchecked")
        final List<Long> ids = em.createQuery(
                        "SELECT f.pack.id FROM ClientPackFavorite f JOIN f.pack p WHERE"
                                + ACTIVE_FAVORITE_FILTER
                                + " ORDER BY f.createdAt DESC",
                        Long.class)
                .setParameter("cid", clientId)
                .setFirstResult(Pagination.offset(safePage, safeSize))
                .setMaxResults(safeSize)
                .getResultList();

        if (ids.isEmpty()) {
            return Collections.emptyList();
        }

        final List<Pack> packs = em.createQuery(
                        "SELECT DISTINCT p FROM Pack p LEFT JOIN FETCH p.tags JOIN FETCH p.commerce WHERE p.id IN :ids",
                        Pack.class)
                .setParameter("ids", ids)
                .getResultList();

        final Map<Long, Integer> positions = new HashMap<>();
        for (int i = 0; i < ids.size(); i++) {
            positions.put(ids.get(i), Integer.valueOf(i));
        }
        packs.sort(Comparator.comparingInt(pack -> positions.getOrDefault(pack.getId(), Integer.MAX_VALUE)));
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
}
