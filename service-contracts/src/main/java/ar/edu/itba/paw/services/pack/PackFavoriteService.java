package ar.edu.itba.paw.services.pack;

import ar.edu.itba.paw.models.pack.Pack;

import java.util.List;

public interface PackFavoriteService {

    /**
     * First page of active favorites (e.g. carousel), ordered by most recent save first.
     */
    List<Pack> listActiveFavoritePacks(long clientUserId, int limit);

    List<Pack> listActiveFavoritePacks(long clientUserId, int page, int pageSize);

    int countActiveFavoritePacks(long clientUserId);

    boolean isFavorite(long clientUserId, long packId);

    void toggleFavorite(long clientUserId, long packId);
}
