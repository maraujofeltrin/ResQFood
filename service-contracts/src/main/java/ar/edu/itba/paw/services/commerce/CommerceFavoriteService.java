package ar.edu.itba.paw.services.commerce;

import ar.edu.itba.paw.models.user.Commerce;
import ar.edu.itba.paw.models.user.User;

import java.util.List;

public interface CommerceFavoriteService {

    List<Commerce> listFavoriteCommerces(long clientUserId, int page, int pageSize);

    int countFavoriteCommerces(long clientUserId);

    boolean isFavorite(long clientUserId, long commerceId);

    /**
     * @throws IllegalArgumentException if the commerce is not found
     */
    void toggleFavorite(long clientUserId, long commerceId);

    List<User> findFavoritingClients(long commerceId);
}
