package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.models.pack.Pack;
import ar.edu.itba.paw.persistence.util.Pagination;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Favorites are stored in {@code client_pack_favorites} without a dedicated JPA entity.
 * Native SQL is intentional for this join table; see {@link #findActiveFavoritePacksForClient}.
 */
@Primary
@Repository
public class PackFavoriteJpaDao implements PackFavoriteDao {

    private static final Logger LOGGER = LoggerFactory.getLogger(PackFavoriteJpaDao.class);

    @PersistenceContext
    private EntityManager em;

    @Override
    public List<Pack> findActiveFavoritePacksForClient(final long clientId, final int page, final int pageSize) {
        final int safePage = Math.max(1, page);
        final int safeSize = Math.max(1, pageSize);

        final javax.persistence.Query idQuery = em.createNativeQuery("SELECT f.pack_id FROM client_pack_favorites f INNER JOIN packs p ON p.id = f.pack_id WHERE f.client_id = :cid AND p.active = TRUE AND p.deleted = FALSE ORDER BY f.created_at DESC");
        idQuery.setParameter("cid", clientId);
        idQuery.setFirstResult(Pagination.offset(safePage, safeSize));
        idQuery.setMaxResults(safeSize);
        final List<?> rawIds = idQuery.getResultList();
        final List<Long> ids = new ArrayList<>();
        for (final Object o : rawIds) {
            if (o instanceof Number) {
                ids.add(((Number) o).longValue());
            } else {
                ids.add(Long.parseLong(o.toString()));
            }
        }

        if (ids.isEmpty()) {
            return Collections.emptyList();
        }

        final List<Pack> packs = em.createQuery("SELECT DISTINCT p FROM Pack p LEFT JOIN FETCH p.tags WHERE p.id IN :ids", Pack.class)
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
        final javax.persistence.Query q = em.createNativeQuery("SELECT COUNT(*) FROM client_pack_favorites f INNER JOIN packs p ON p.id = f.pack_id WHERE f.client_id = :cid AND p.active = TRUE AND p.deleted = FALSE");
        q.setParameter("cid", clientId);
        final Object res = q.getSingleResult();
        if (res instanceof Number) {
            return ((Number) res).intValue();
        }
        return Integer.parseInt(res.toString());
    }

    @Override
    public boolean exists(final long clientId, final long packId) {
        final javax.persistence.Query q = em.createNativeQuery("SELECT COUNT(*) FROM client_pack_favorites WHERE client_id = :cid AND pack_id = :pid");
        q.setParameter("cid", clientId);
        q.setParameter("pid", packId);
        final Object res = q.getSingleResult();
        if (res instanceof Number) {
            return ((Number) res).intValue() > 0;
        }
        return Integer.parseInt(res.toString()) > 0;
    }

    @Override
    public void insert(final long clientId, final long packId) {
        em.createNativeQuery("INSERT INTO client_pack_favorites (client_id, pack_id) VALUES (:cid, :pid)")
                .setParameter("cid", clientId)
                .setParameter("pid", packId)
                .executeUpdate();
    }

    @Override
    public void delete(final long clientId, final long packId) {
        em.createNativeQuery("DELETE FROM client_pack_favorites WHERE client_id = :cid AND pack_id = :pid")
                .setParameter("cid", clientId)
                .setParameter("pid", packId)
                .executeUpdate();
    }
}
