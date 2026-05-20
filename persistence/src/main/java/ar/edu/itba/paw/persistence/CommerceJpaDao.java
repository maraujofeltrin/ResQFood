package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.models.pack.Municipality;
import ar.edu.itba.paw.models.user.Commerce;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Optional;
import java.util.List;

@Primary
@Repository
public class CommerceJpaDao implements CommerceDao {

    @PersistenceContext
    private EntityManager em;

    @Override
    public Commerce createCommerce(final Long userId, final String commercialName, final Commerce.Category category,
            final String street, final Integer streetNumber, final Municipality city, final String province,
            final String postalCode, final String openingTime, final String closingTime) {
        final Commerce commerce = new Commerce(userId, commercialName, category, street, streetNumber, city, province, postalCode, openingTime, closingTime);
        em.persist(commerce);
        return commerce;
    }

    @Override
    public Optional<Commerce> findByUserId(final Long userId) {
        return Optional.ofNullable(em.find(Commerce.class, userId));
    }

    @Override
    public Commerce update(final Commerce commerce) {
        return em.merge(commerce);
    }

    @Override
    public List<Commerce> filterCommerces(String query, String cityFilter, int page, int pageSize) {
        final StringBuilder jpql = new StringBuilder("SELECT c FROM Commerce c LEFT JOIN CommerceReview r ON c.userId = r.commerceUserId");
        final java.util.Map<String, Object> params = new java.util.HashMap<>();
        final List<String> conditions = new java.util.ArrayList<>();

        if (query != null && !query.trim().isEmpty()) {
            conditions.add("LOWER(c.commercialName) LIKE :query");
            params.put("query", "%" + query.trim().toLowerCase() + "%");
        }

        if (cityFilter != null && !cityFilter.trim().isEmpty()) {
            final Municipality m = Municipality.fromCityName(cityFilter);
            if (m != null) {
                conditions.add("c.city = :city");
                params.put("city", m);
            }
        }

        if (!conditions.isEmpty()) {
            jpql.append(" WHERE ").append(String.join(" AND ", conditions));
        }

        jpql.append(" GROUP BY c ORDER BY COALESCE(AVG(r.rating), 0.0) DESC, c.commercialName ASC");

        final javax.persistence.TypedQuery<Commerce> q = em.createQuery(jpql.toString(), Commerce.class);
        for (final java.util.Map.Entry<String, Object> entry : params.entrySet()) {
            q.setParameter(entry.getKey(), entry.getValue());
        }

        return q.setFirstResult((page - 1) * pageSize)
                .setMaxResults(pageSize)
                .getResultList();
    }

    @Override
    public int countFilteredCommerces(String query, String cityFilter) {
        final javax.persistence.criteria.CriteriaBuilder cb = em.getCriteriaBuilder();
        final javax.persistence.criteria.CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        final javax.persistence.criteria.Root<Commerce> root = cq.from(Commerce.class);

        cq.select(cb.count(root));
        cq.where(buildPredicates(cb, root, query, cityFilter));

        return em.createQuery(cq).getSingleResult().intValue();
    }

    private javax.persistence.criteria.Predicate[] buildPredicates(
            final javax.persistence.criteria.CriteriaBuilder cb,
            final javax.persistence.criteria.Root<Commerce> root,
            final String query,
            final String cityFilter) {
        final java.util.List<javax.persistence.criteria.Predicate> predicates = new java.util.ArrayList<>();

        if (query != null && !query.trim().isEmpty()) {
            predicates.add(cb.like(cb.lower(root.get("commercialName")), "%" + query.trim().toLowerCase() + "%"));
        }

        if (cityFilter != null && !cityFilter.trim().isEmpty()) {
            final Municipality m = Municipality.fromCityName(cityFilter);
            if (m != null) {
                predicates.add(cb.equal(root.get("city"), m));
            }
        }

        return predicates.toArray(new javax.persistence.criteria.Predicate[0]);
    }
}
