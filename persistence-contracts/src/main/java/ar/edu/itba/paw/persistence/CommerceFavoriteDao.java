package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.models.user.Commerce;
import ar.edu.itba.paw.models.user.User;

import java.util.List;

public interface CommerceFavoriteDao {

    /**
     * Favorite commerces for client, most recently added first.
     */
    List<Commerce> findFavoriteCommercesForClient(long clientId, int page, int pageSize);

    int countFavoriteCommercesForClient(long clientId);

    boolean exists(long clientId, long commerceId);

    void insert(long clientId, long commerceId);

    void delete(long clientId, long commerceId);

    List<User> findFavoritingClientsByCommerce(long commerceId);
}
