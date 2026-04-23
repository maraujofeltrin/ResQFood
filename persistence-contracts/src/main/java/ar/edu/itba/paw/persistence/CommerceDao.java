package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.models.user.Commerce;
import java.util.Optional;

public interface CommerceDao {
    Commerce createCommerce(final Long userId, final String commercialName, final Commerce.Category category,
            final String street, final Integer streetNumber, final String city, final String province,
            final String postalCode, final String openingTime, final String closingTime);

    Optional<Commerce> findByUserId(final Long userId);

    Commerce update(final Commerce commerce);
}

