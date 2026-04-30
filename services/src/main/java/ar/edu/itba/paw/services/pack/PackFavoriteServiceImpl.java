package ar.edu.itba.paw.services.pack;

import ar.edu.itba.paw.models.pack.Pack;
import ar.edu.itba.paw.persistence.PackDao;
import ar.edu.itba.paw.persistence.PackFavoriteDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PackFavoriteServiceImpl implements PackFavoriteService {

    private final PackFavoriteDao packFavoriteDao;
    private final PackDao packDao;

    @Autowired
    public PackFavoriteServiceImpl(final PackFavoriteDao packFavoriteDao, final PackDao packDao) {
        this.packFavoriteDao = packFavoriteDao;
        this.packDao = packDao;
    }

    @Override
    public List<Pack> listActiveFavoritePacks(final long clientUserId, final int limit) {
        final int safeMax = Math.max(1, Math.min(limit, 48));
        return packFavoriteDao.findActiveFavoritePacksForClient(clientUserId, 1, safeMax);
    }

    @Override
    public List<Pack> listActiveFavoritePacks(final long clientUserId, final int page, final int pageSize) {
        final int safeSize = Math.max(1, Math.min(pageSize, 48));
        return packFavoriteDao.findActiveFavoritePacksForClient(clientUserId, page, safeSize);
    }

    @Override
    public int countActiveFavoritePacks(final long clientUserId) {
        return packFavoriteDao.countActiveFavoritePacksForClient(clientUserId);
    }

    @Override
    public boolean isFavorite(final long clientUserId, final long packId) {
        return packFavoriteDao.exists(clientUserId, packId);
    }

    @Transactional
    @Override
    public void toggleFavorite(final long clientUserId, final long packId) {
        if (packFavoriteDao.exists(clientUserId, packId)) {
            packFavoriteDao.delete(clientUserId, packId);
            return;
        }
        final Pack pack = packDao.findById(packId).orElseThrow(() -> new IllegalArgumentException("Pack not found: " + packId));
        if (!Boolean.TRUE.equals(pack.getActive()) || Boolean.TRUE.equals(pack.getDeleted())) {
            throw new IllegalArgumentException("Pack is not available for favorites: " + packId);
        }
        packFavoriteDao.insert(clientUserId, packId);
    }
}
