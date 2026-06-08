package ar.edu.itba.paw.services.commerce;

import ar.edu.itba.paw.models.pack.Municipality;
import ar.edu.itba.paw.models.pack.Pack;
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

    /**
     * Active catalog offers (direct-sale packs with stock or packs with an ACTIVE auction) for the public
     * commerce profile, ordered by {@code pack.id DESC}. Each returned {@link Pack} has its {@code auction}
     * relation hydrated (null when the pack is direct-sale only).
     */
    List<Pack> getPublicOffers(long commerceUserId, int page, int pageSize);

    int countPublicOffers(long commerceUserId);

    Commerce createCommerce(final Long userId, final String commercialName, final Commerce.Category category,
            final String street, final Integer streetNumber, final Municipality city, final String province,
            final String postalCode, final String openingTime, final String closingTime);

    Commerce update(final Commerce commerce);
}
