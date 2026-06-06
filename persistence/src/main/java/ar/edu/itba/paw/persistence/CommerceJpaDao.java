package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.models.pack.Municipality;
import ar.edu.itba.paw.models.user.Commerce;
import ar.edu.itba.paw.models.user.User;
import ar.edu.itba.paw.persistence.util.Pagination;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Primary
@Repository
public class CommerceJpaDao implements CommerceDao {

    @PersistenceContext
    private EntityManager em;

    @Override
    public Commerce createCommerce(final Long userId, final String commercialName, final Commerce.Category category,
            final String street, final Integer streetNumber, final Municipality city, final String province,
            final String postalCode, final String openingTime, final String closingTime) {
        final User user = em.getReference(User.class, userId);
        final Commerce commerce = new Commerce(user, commercialName, category, street, streetNumber, city, province,
                postalCode, openingTime, closingTime);
        em.persist(commerce);
        return commerce;
    }

    @Override
    public Optional<Commerce> findByUserId(final Long userId) {
        return em.createQuery(
                        "SELECT c FROM Commerce c JOIN FETCH c.user WHERE c.userId = :userId", Commerce.class)
                .setParameter("userId", userId)
                .getResultList()
                .stream()
                .findFirst();
    }

    @Override
    public Commerce update(final Commerce commerce) {
        return em.merge(commerce);
    }

    @Override
    public List<Commerce> filterCommerces(String query, String cityFilter, Commerce.Category categoryFilter, int page, int pageSize) {
        final List<Long> ids = queryCommerceIds(query, cityFilter, categoryFilter, page, pageSize);
        if (ids.isEmpty()) {
            return Collections.emptyList();
        }

        return em.createQuery(
                        "SELECT c FROM Commerce c JOIN FETCH c.user WHERE c.userId IN :ids ORDER BY "
                                + buildCommerceUserIdPositionOrderByClause(ids),
                        Commerce.class)
                .setParameter("ids", ids)
                .getResultList();
    }

    @Override
    public int countFilteredCommerces(String query, String cityFilter, Commerce.Category categoryFilter) {
        final javax.persistence.criteria.CriteriaBuilder cb = em.getCriteriaBuilder();
        final javax.persistence.criteria.CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        final javax.persistence.criteria.Root<Commerce> root = cq.from(Commerce.class);

        cq.select(cb.count(root));
        cq.where(buildPredicates(cb, root, query, cityFilter, categoryFilter));

        return em.createQuery(cq).getSingleResult().intValue();
    }

    private List<Long> queryCommerceIds(final String query, final String cityFilter,
            final Commerce.Category categoryFilter, final int page, final int pageSize) {
        final StringBuilder sql = new StringBuilder(
                "SELECT c.user_id FROM commerces c "
                        + "LEFT JOIN commerce_reviews r ON c.user_id = r.commerce_user_id");
        final Map<String, Object> params = new LinkedHashMap<>();
        final List<String> conditions = new ArrayList<>();

        if (query != null && !query.trim().isEmpty()) {
            conditions.add("LOWER(c.commercial_name) LIKE :query");
            params.put("query", "%" + query.trim().toLowerCase() + "%");
        }

        if (cityFilter != null && !cityFilter.trim().isEmpty()) {
            final Municipality m = Municipality.fromCityName(cityFilter);
            if (m != null) {
                conditions.add("c.city = :city");
                params.put("city", m.getCityName());
            }
        }

        if (categoryFilter != null) {
            conditions.add("c.category = :category");
            params.put("category", categoryFilter.name());
        }

        if (!conditions.isEmpty()) {
            sql.append(" WHERE ").append(String.join(" AND ", conditions));
        }

        sql.append(" GROUP BY c.user_id, c.commercial_name "
                + "ORDER BY COALESCE(AVG(r.rating), 0) DESC, c.commercial_name ASC");

        final javax.persistence.Query idQuery = em.createNativeQuery(sql.toString());
        for (final Map.Entry<String, Object> entry : params.entrySet()) {
            idQuery.setParameter(entry.getKey(), entry.getValue());
        }

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

    private String buildCommerceUserIdPositionOrderByClause(final List<Long> ids) {
        final StringBuilder orderBy = new StringBuilder("CASE c.userId ");
        for (int index = 0; index < ids.size(); index++) {
            orderBy.append("WHEN ").append(ids.get(index).longValue()).append(" THEN ").append(index).append(' ');
        }
        orderBy.append("ELSE ").append(ids.size()).append(" END");
        return orderBy.toString();
    }

    private javax.persistence.criteria.Predicate[] buildPredicates(
            final javax.persistence.criteria.CriteriaBuilder cb,
            final javax.persistence.criteria.Root<Commerce> root,
            final String query,
            final String cityFilter,
            final Commerce.Category categoryFilter) {
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

        if (categoryFilter != null) {
            predicates.add(cb.equal(root.get("category"), categoryFilter));
        }

        return predicates.toArray(new javax.persistence.criteria.Predicate[0]);
    }
}
