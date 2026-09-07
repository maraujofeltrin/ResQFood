package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.models.user.ClientCommerceFavorite;
import ar.edu.itba.paw.models.user.ClientCommerceFavoriteId;
import ar.edu.itba.paw.models.user.Client;
import ar.edu.itba.paw.models.user.Commerce;
import ar.edu.itba.paw.models.user.User;
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
 * Commerce favorites are mapped by {@link ClientCommerceFavorite} on {@code client_commerce_favorites}.
 * List queries use a two-step pattern (commerce ids, then fetch) to preserve order and avoid fetch pagination issues.
 */
@Primary
@Repository
public class CommerceFavoriteJpaDao implements CommerceFavoriteDao {

    @PersistenceContext
    private EntityManager em;

    @Override
    public List<Commerce> findFavoriteCommercesForClient(final long clientId, final int page, final int pageSize) {
        final int safePage = Math.max(1, page);
        final int safeSize = Math.max(1, pageSize);

        final List<Long> ids = queryFavoriteCommerceIds(clientId, safePage, safeSize);
        if (ids.isEmpty()) {
            return Collections.emptyList();
        }

        final List<Commerce> commerces = em.createQuery(
                        "SELECT c FROM Commerce c JOIN FETCH c.user WHERE c.userId IN :ids ORDER BY "
                                + buildCommerceIdPositionOrderByClause(ids),
                        Commerce.class)
                .setParameter("ids", ids)
                .getResultList();

        return commerces;
    }

    @Override
    public int countFavoriteCommercesForClient(final long clientId) {
        final Long count = em.createQuery(
                        "SELECT COUNT(f) FROM ClientCommerceFavorite f WHERE f.client.userId = :cid",
                        Long.class)
                .setParameter("cid", clientId)
                .getSingleResult();
        return count != null ? count.intValue() : 0;
    }

    @Override
    public boolean exists(final long clientId, final long commerceId) {
        final Long count = em.createQuery(
                        "SELECT COUNT(f) FROM ClientCommerceFavorite f"
                                + " WHERE f.client.userId = :cid AND f.commerce.userId = :comId",
                        Long.class)
                .setParameter("cid", clientId)
                .setParameter("comId", commerceId)
                .getSingleResult();
        return count != null && count.longValue() > 0L;
    }

    @Override
    public void insert(final long clientId, final long commerceId) {
        final ClientCommerceFavorite favorite = new ClientCommerceFavorite(
                em.getReference(Client.class, clientId),
                em.getReference(Commerce.class, commerceId),
                LocalDateTime.now(ZoneOffset.UTC));
        em.persist(favorite);
        em.flush();
    }

    @Override
    public void delete(final long clientId, final long commerceId) {
        final ClientCommerceFavorite favorite = em.find(
                ClientCommerceFavorite.class, new ClientCommerceFavoriteId(clientId, commerceId));
        if (favorite != null) {
            em.remove(favorite);
            em.flush();
        }
    }

    @Override
    public List<User> findFavoritingClientsByCommerce(final long commerceId) {
        return em.createQuery(
                        "SELECT f.client.user FROM ClientCommerceFavorite f WHERE f.commerce.userId = :comId",
                        User.class)
                .setParameter("comId", commerceId)
                .getResultList();
    }

    private List<Long> queryFavoriteCommerceIds(final long clientId, final int page, final int pageSize) {
        final javax.persistence.Query idQuery = em.createNativeQuery(
                "SELECT f.commerce_id FROM client_commerce_favorites f "
                        + "INNER JOIN commerces c ON c.user_id = f.commerce_id "
                        + "WHERE f.client_id = :cid "
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

    private String buildCommerceIdPositionOrderByClause(final List<Long> ids) {
        final StringBuilder orderBy = new StringBuilder("CASE c.userId ");
        for (int index = 0; index < ids.size(); index++) {
            orderBy.append("WHEN ").append(ids.get(index).longValue()).append(" THEN ").append(index).append(' ');
        }
        orderBy.append("ELSE ").append(ids.size()).append(" END");
        return orderBy.toString();
    }
}
