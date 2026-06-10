package ar.edu.itba.paw.services.pack;

import ar.edu.itba.paw.models.pack.Pack;
import ar.edu.itba.paw.models.pack.FavoriteToggleException;
import ar.edu.itba.paw.persistence.PackFavoriteDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PackFavoriteServiceImpl implements PackFavoriteService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PackFavoriteServiceImpl.class);

    private final PackFavoriteDao packFavoriteDao;
    private final PackService packService;

    @Autowired
    public PackFavoriteServiceImpl(final PackFavoriteDao packFavoriteDao,
            @Lazy final PackService packService) {
        this.packFavoriteDao = packFavoriteDao;
        this.packService = packService;
    }

    @Transactional(readOnly = true)
    @Override
    public List<Pack> listActiveFavoritePacks(final long clientUserId, final int page, final int pageSize) {
        final int safeSize = Math.max(1, Math.min(pageSize, 48));
        return packFavoriteDao.findActiveFavoritePacksForClient(clientUserId, page, safeSize);
    }

    @Transactional(readOnly = true)
    @Override
    public int countActiveFavoritePacks(final long clientUserId) {
        return packFavoriteDao.countActiveFavoritePacksForClient(clientUserId);
    }

    @Transactional(readOnly = true)
    @Override
    public boolean isFavorite(final long clientUserId, final long packId) {
        return packFavoriteDao.exists(clientUserId, packId);
    }

    @Transactional
    @Override
    public void toggleFavorite(final long clientUserId, final long packId) throws FavoriteToggleException {
        if (packFavoriteDao.exists(clientUserId, packId)) {
            packFavoriteDao.delete(clientUserId, packId);
            LOGGER.info("User {} removed pack {} from favorites", clientUserId, packId);
            return;
        }
        final Pack pack = packService.findById(packId).orElseThrow(() -> {
            LOGGER.warn("Favorite toggle rejected: pack not found packId={} clientUserId={}", packId, clientUserId);
            return new FavoriteToggleException(FavoriteToggleException.Reason.PACK_NOT_FOUND, "Pack not found: " + packId);
        });
        if (!Boolean.TRUE.equals(pack.getActive()) || Boolean.TRUE.equals(pack.getDeleted())) {
            LOGGER.warn("Favorite toggle rejected: pack unavailable packId={} clientUserId={}", packId, clientUserId);
            throw new FavoriteToggleException(FavoriteToggleException.Reason.PACK_UNAVAILABLE, "Pack is not available for favorites: " + packId);
        }
        // Do not allow favoriting packs that are part of an auction
        if (pack.getAuction() != null) {
            LOGGER.warn("Favorite toggle rejected: pack is an auction packId={} clientUserId={}", packId, clientUserId);
            throw new FavoriteToggleException(FavoriteToggleException.Reason.PACK_UNAVAILABLE, "Pack is an auction: " + packId);
        }
        packFavoriteDao.insert(clientUserId, packId);
        LOGGER.info("User {} added pack {} to favorites", clientUserId, packId);
    }
}
