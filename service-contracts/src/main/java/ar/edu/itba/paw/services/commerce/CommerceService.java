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
}
