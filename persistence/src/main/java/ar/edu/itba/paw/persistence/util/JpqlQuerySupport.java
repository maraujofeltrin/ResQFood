package ar.edu.itba.paw.persistence.util;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.Map;

/**
 * Shared helpers for binding named JPQL parameters.
 */
public final class JpqlQuerySupport {

    private JpqlQuerySupport() {
    }

    public static <T> TypedQuery<T> createQuery(final EntityManager em, final String jpql,
                                                final Map<String, Object> params, final Class<T> resultType) {
        final TypedQuery<T> query = em.createQuery(jpql, resultType);
        for (final Map.Entry<String, Object> entry : params.entrySet()) {
            query.setParameter(entry.getKey(), entry.getValue());
        }
        return query;
    }
}
