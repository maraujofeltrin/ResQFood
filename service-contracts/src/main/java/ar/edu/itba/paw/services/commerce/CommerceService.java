package ar.edu.itba.paw.services.commerce;

import ar.edu.itba.paw.models.user.Commerce;
import ar.edu.itba.paw.models.pack.Municipality;
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
     * Active catalog offers (direct-sale packs and live auctions) visible on the public commerce profile.
     * The caller must ensure the commerce exists; {@code packPage} is clamped to the valid range.
     */
    CommercePublicOffers getPublicOffers(long commerceUserId, int packPage, int packPageSize);
}
