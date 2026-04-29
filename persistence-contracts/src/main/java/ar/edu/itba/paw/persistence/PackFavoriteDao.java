package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.models.pack.Pack;

import java.util.List;

public interface PackFavoriteDao {

    /**
     * Active, non-deleted favorite packs, most recently added first.
     */
    List<Pack> findActiveFavoritePacksForClient(long clientId, int page, int pageSize);

    int countActiveFavoritePacksForClient(long clientId);

    boolean exists(long clientId, long packId);

    void insert(long clientId, long packId);

    void delete(long clientId, long packId);
}
