package ar.edu.itba.paw.services.commerce;

import ar.edu.itba.paw.models.user.Commerce;
import java.util.Optional;

public interface CommerceService {
    Optional<Commerce> findByUserId(final Long userId);
}
