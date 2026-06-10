package ar.edu.itba.paw.services.commerce;

import ar.edu.itba.paw.models.user.Commerce;
import ar.edu.itba.paw.models.user.CommerceFavoriteToggleException;
import ar.edu.itba.paw.models.user.User;
import ar.edu.itba.paw.persistence.CommerceDao;
import ar.edu.itba.paw.persistence.CommerceFavoriteDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CommerceFavoriteServiceImpl implements CommerceFavoriteService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CommerceFavoriteServiceImpl.class);

    private final CommerceFavoriteDao commerceFavoriteDao;
    private final CommerceDao commerceDao;

    @Autowired
    public CommerceFavoriteServiceImpl(final CommerceFavoriteDao commerceFavoriteDao,
            final CommerceDao commerceDao) {
        this.commerceFavoriteDao = commerceFavoriteDao;
        this.commerceDao = commerceDao;
    }

    @Transactional(readOnly = true)
    @Override
    public List<Commerce> listFavoriteCommerces(final long clientUserId, final int page, final int pageSize) {
        final int safeSize = Math.max(1, Math.min(pageSize, 48));
        return commerceFavoriteDao.findFavoriteCommercesForClient(clientUserId, page, safeSize);
    }

    @Transactional(readOnly = true)
    @Override
    public int countFavoriteCommerces(final long clientUserId) {
        return commerceFavoriteDao.countFavoriteCommercesForClient(clientUserId);
    }

    @Transactional(readOnly = true)
    @Override
    public boolean isFavorite(final long clientUserId, final long commerceId) {
        return commerceFavoriteDao.exists(clientUserId, commerceId);
    }

    @Transactional
    @Override
    public void toggleFavorite(final long clientUserId, final long commerceId) throws CommerceFavoriteToggleException {
        if (commerceFavoriteDao.exists(clientUserId, commerceId)) {
            commerceFavoriteDao.delete(clientUserId, commerceId);
            LOGGER.info("User {} removed commerce {} from favorites", clientUserId, commerceId);
            return;
        }
        commerceDao.findByUserId(commerceId).orElseThrow(() -> {
            LOGGER.warn("Commerce favorite toggle rejected: commerce not found commerceId={} clientUserId={}", commerceId, clientUserId);
            return new CommerceFavoriteToggleException(
                    CommerceFavoriteToggleException.Reason.COMMERCE_NOT_FOUND,
                    "Commerce not found: " + commerceId);
        });
        commerceFavoriteDao.insert(clientUserId, commerceId);
        LOGGER.info("User {} added commerce {} to favorites", clientUserId, commerceId);
    }

    @Transactional(readOnly = true)
    @Override
    public List<User> findFavoritingClients(final long commerceId) {
        return commerceFavoriteDao.findFavoritingClientsByCommerce(commerceId);
    }
}
