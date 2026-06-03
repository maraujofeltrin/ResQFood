package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.models.user.Commerce;
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
 * Favorites are stored in {@code client_commerce_favorites} without a dedicated JPA entity.
 * Native SQL is intentional for this join table; see {@link #findFavoriteCommercesForClient}.
 */
@Primary
@Repository
public class CommerceFavoriteJpaDao implements CommerceFavoriteDao {

    private static final Logger LOGGER = LoggerFactory.getLogger(CommerceFavoriteJpaDao.class);

    @PersistenceContext
    private EntityManager em;

    @Override
    public List<Commerce> findFavoriteCommercesForClient(final long clientId, final int page, final int pageSize) {
        final int safePage = Math.max(1, page);
        final int safeSize = Math.max(1, pageSize);

        final javax.persistence.Query idQuery = em.createNativeQuery(
                "SELECT f.commerce_id FROM client_commerce_favorites f " +
                "INNER JOIN commerces c ON c.user_id = f.commerce_id " +
                "WHERE f.client_id = :cid ORDER BY f.created_at DESC");
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

        final List<Commerce> commerces = em.createQuery(
                        "SELECT c FROM Commerce c JOIN FETCH c.user WHERE c.userId IN :ids", Commerce.class)
                .setParameter("ids", ids)
                .getResultList();

        final Map<Long, Integer> positions = new HashMap<>();
        for (int i = 0; i < ids.size(); i++) {
            positions.put(ids.get(i), Integer.valueOf(i));
        }
        commerces.sort(Comparator.comparingInt(c -> positions.getOrDefault(c.getUserId(), Integer.MAX_VALUE)));
        return commerces;
    }

    @Override
    public int countFavoriteCommercesForClient(final long clientId) {
        final javax.persistence.Query q = em.createNativeQuery(
                "SELECT COUNT(*) FROM client_commerce_favorites WHERE client_id = :cid");
        q.setParameter("cid", clientId);
        final Object res = q.getSingleResult();
        if (res instanceof Number) {
            return ((Number) res).intValue();
        }
        return Integer.parseInt(res.toString());
    }

    @Override
    public boolean exists(final long clientId, final long commerceId) {
        final javax.persistence.Query q = em.createNativeQuery(
                "SELECT COUNT(*) FROM client_commerce_favorites WHERE client_id = :cid AND commerce_id = :comId");
        q.setParameter("cid", clientId);
        q.setParameter("comId", commerceId);
        final Object res = q.getSingleResult();
        if (res instanceof Number) {
            return ((Number) res).intValue() > 0;
        }
        return Integer.parseInt(res.toString()) > 0;
    }

    @Override
    public void insert(final long clientId, final long commerceId) {
        em.createNativeQuery(
                "INSERT INTO client_commerce_favorites (client_id, commerce_id) VALUES (:cid, :comId)")
                .setParameter("cid", clientId)
                .setParameter("comId", commerceId)
                .executeUpdate();
    }

    @Override
    public void delete(final long clientId, final long commerceId) {
        em.createNativeQuery(
                "DELETE FROM client_commerce_favorites WHERE client_id = :cid AND commerce_id = :comId")
                .setParameter("cid", clientId)
                .setParameter("comId", commerceId)
                .executeUpdate();
    }

    @Override
    public List<Long> findClientIdsByCommerce(final long commerceId) {
        final javax.persistence.Query q = em.createNativeQuery(
                "SELECT client_id FROM client_commerce_favorites WHERE commerce_id = :cid");
        q.setParameter("cid", commerceId);
        final List<?> rawIds = q.getResultList();
        final List<Long> ids = new ArrayList<>();
        for (final Object o : rawIds) {
            if (o instanceof Number) {
                ids.add(((Number) o).longValue());
            } else {
                ids.add(Long.parseLong(o.toString()));
            }
        }
        return ids;
    }
}
