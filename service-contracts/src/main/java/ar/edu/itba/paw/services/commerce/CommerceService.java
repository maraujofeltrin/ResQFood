package ar.edu.itba.paw.services.commerce;

import ar.edu.itba.paw.models.pack.Municipality;
import ar.edu.itba.paw.models.user.Commerce;

import java.util.List;
import java.util.Optional;

public interface CommerceService {
    Optional<Commerce> findByUserId(final Long userId);

    /**
     * Actualiza categoría, dirección y horarios del comercio. El nombre comercial no se modifica.
     */
    void updateProfileFields(long userId, Commerce.Category category, String street, Integer streetNumber,
            Municipality city, String province, String postalCode, String openingTime, String closingTime);

    java.util.List<Commerce> filterCommerces(String query, String cityFilter, Commerce.Category categoryFilter, int page, int pageSize);

    int countFilteredCommerces(String query, String cityFilter, Commerce.Category categoryFilter);

    Commerce createCommerce(final Long userId, final String commercialName, final Commerce.Category category,
            final String street, final Integer streetNumber, final Municipality city, final String province,
            final String postalCode, final String openingTime, final String closingTime);

    Commerce update(final Commerce commerce);
}
