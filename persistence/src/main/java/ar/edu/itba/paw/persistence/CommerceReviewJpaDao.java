package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.models.user.Client;
import ar.edu.itba.paw.models.user.Commerce;
import ar.edu.itba.paw.models.user.CommerceReview;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import ar.edu.itba.paw.persistence.util.Pagination;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

@Primary
@Repository
public class CommerceReviewJpaDao implements CommerceReviewDao {

    @PersistenceContext
    private EntityManager em;

    @Override
    public CommerceReview createReview(Long commerceUserId, Long clientUserId, Integer rating, String body) {
        final LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        final Commerce commerce = em.getReference(Commerce.class, commerceUserId);
        final Client client = em.getReference(Client.class, clientUserId);
        final CommerceReview review = new CommerceReview(null, commerce, client, rating, body, now, now);
        em.persist(review);
        return review;
    }

    @Override
    public CommerceReview updateReview(Long id, Integer rating, String body) {
        em.createQuery("UPDATE CommerceReview r SET r.rating = :rating, r.body = :body, r.updatedAt = :updatedAt WHERE r.id = :id")
                .setParameter("rating", rating)
                .setParameter("body", body)
                .setParameter("updatedAt", LocalDateTime.now())
                .setParameter("id", id)
                .executeUpdate();
        final CommerceReview review = em.find(CommerceReview.class, id);
        if (review != null) {
            em.flush();
            em.refresh(review);
        }
        return review;
    }

    @Override
    public Optional<CommerceReview> findByClientAndCommerce(Long clientUserId, Long commerceUserId) {
        return em.createQuery(
                "FROM CommerceReview r WHERE r.client.userId = :client AND r.commerce.userId = :commerce",
                CommerceReview.class)
                .setParameter("client", clientUserId)
                .setParameter("commerce", commerceUserId)
                .getResultList()
                .stream()
                .findFirst();
    }

    @Override
    public List<CommerceReview> findByCommerceId(Long commerceUserId, int page, int pageSize) {
        return em.createQuery(
                "FROM CommerceReview r WHERE r.commerce.userId = :commerce ORDER BY r.createdAt DESC",
                CommerceReview.class)
                .setParameter("commerce", commerceUserId)
                .setFirstResult(Pagination.offset(page, pageSize))
                .setMaxResults(pageSize)
                .getResultList();
    }

    @Override
    public int countByCommerceId(Long commerceUserId) {
        Number count = em.createQuery(
                "SELECT COUNT(r) FROM CommerceReview r WHERE r.commerce.userId = :commerce", Number.class)
                .setParameter("commerce", commerceUserId)
                .getSingleResult();
        return count != null ? count.intValue() : 0;
    }

    @Override
    public Double averageRatingByCommerceId(Long commerceUserId) {
        return em.createQuery(
                "SELECT AVG(r.rating) FROM CommerceReview r WHERE r.commerce.userId = :commerce", Double.class)
                .setParameter("commerce", commerceUserId)
                .getSingleResult();
    }
}
