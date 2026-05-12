package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.models.pack.Municipality;
import ar.edu.itba.paw.models.user.Commerce;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
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
}
