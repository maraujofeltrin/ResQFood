package ar.edu.itba.paw.services.pack;

import ar.edu.itba.paw.models.pack.Pack;
import ar.edu.itba.paw.models.pack.PackDirectEditException;
import ar.edu.itba.paw.models.pack.PackSortOption;
import ar.edu.itba.paw.models.pack.PackTag;
import ar.edu.itba.paw.persistence.PackDao;
import ar.edu.itba.paw.persistence.PackFavoriteDao;
import ar.edu.itba.paw.services.auction.AuctionService;
import ar.edu.itba.paw.services.commerce.CommerceFavoriteService;
import ar.edu.itba.paw.services.image.ImageService;
import ar.edu.itba.paw.services.notification.NotificationService;
import ar.edu.itba.paw.services.reservation.ReservationService;
import org.springframework.context.annotation.Lazy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class PackServiceImpl implements PackService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PackServiceImpl.class);

    private final PackDao packDao;
    private final PackFavoriteDao packFavoriteDao;
    private final ImageService imageService;
    private final AuctionService auctionService;
    private final ReservationService reservationService;
    private final NotificationService notificationService;
    private final CommerceFavoriteService commerceFavoriteService;

    @Autowired
    public PackServiceImpl(final PackDao packDao, final PackFavoriteDao packFavoriteDao,
            final ImageService imageService,
            @Lazy final AuctionService auctionService,
            @Lazy final ReservationService reservationService,
            final NotificationService notificationService,
            final CommerceFavoriteService commerceFavoriteService) {
        this.packDao = packDao;
        this.packFavoriteDao = packFavoriteDao;
        this.imageService = imageService;
        this.auctionService = auctionService;
        this.reservationService = reservationService;
        this.notificationService = notificationService;
        this.commerceFavoriteService = commerceFavoriteService;
    }

    @Transactional
    @Override
    public Pack createPack(Long commerceId, String title, String description, Double originalPrice,
                           Double finalPrice, Integer stock, List<PackTag> tags,
                           Long imageId) {
        final Pack createdPack = packDao.createPack(commerceId, title, description, originalPrice, finalPrice, stock, tags, imageId);
        LOGGER.info("Pack created: packId={}, commerceId={}", createdPack.getId(), commerceId);
        notificationService.notifyPackPublished(createdPack,
                commerceFavoriteService.findFavoritingClients(createdPack.getCommerceId()));
        return createdPack;
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<Pack> findById(Long id) {
        return packDao.findById(id);
    }

    @Transactional
    @Override
    public Pack update(Pack pack) {
        return packDao.update(pack);
    }

    @Transactional
    @Override
    public void deletePack(final long packId) {
        requirePackForDirectEdit(packId, PackDirectEditException.ForbiddenAction.DELETE);
        packDao.softDelete(packId);
        LOGGER.info("Pack soft-deleted: packId={}", packId);
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<Pack> findVisibleForDetail(final Long packId, final Long viewerUserId) {
        final Optional<Pack> packOpt = packDao.findById(packId);
        if (packOpt.isEmpty()) {
            return Optional.empty();
        }

        final Pack pack = packOpt.get();
        if (Boolean.TRUE.equals(pack.getDeleted())) {
            return Optional.empty();
        }
        if (Boolean.TRUE.equals(pack.getActive())) {
            return Optional.of(pack);
        }
        if (viewerUserId != null && (viewerUserId.equals(pack.getCommerceId())
                || reservationService.hasActiveReservation(packId, viewerUserId)
                || hasParticipatedInPackAuction(packId, viewerUserId))) {
            return Optional.of(pack);
        }
        return Optional.empty();
    }

    private boolean hasParticipatedInPackAuction(final Long packId, final Long viewerUserId) {
        return auctionService.findByPackId(packId)
                .map(auction -> auctionService.hasClientBidOnAuction(auction.getId(), viewerUserId))
                .orElse(false);
    }

    @Transactional
    @Override
    public Pack updatePack(final long packId, final String title, final String description,
            final Double originalPrice, final Double finalPrice, final Integer stock, final List<PackTag> tags,
            final Long imageId) {
        final Pack packToUpdate = requirePackForDirectEdit(packId, PackDirectEditException.ForbiddenAction.EDIT);
        final int oldStock = packToUpdate.getStock();

        packToUpdate.setTitle(title);
        packToUpdate.setDescription(description);
        packToUpdate.setOriginalPrice(originalPrice);
        packToUpdate.setFinalPrice(finalPrice);
        packToUpdate.setStock(stock);
        packToUpdate.setTags(tags != null ? tags : java.util.Collections.emptyList());

        if (imageId != null) {
            packToUpdate.setImage(imageService.getImage(imageId)
                    .orElseThrow(() -> new NoSuchElementException("Image not found: " + imageId)));
        }

        final Pack updatedPack = packDao.update(packToUpdate);
        LOGGER.info("Pack updated: packId={}", packId);
        if (oldStock == 0 && updatedPack.getStock() > 0) {
            notificationService.notifyPackRestocked(updatedPack,
                    packFavoriteDao.findFavoritingClientsByPack(updatedPack.getId()));
        }
        return updatedPack;
    }

    @Transactional(readOnly = true)
    @Override
    public List<Pack> filterPacks(final String query, final List<PackTag> tags,
                                  final String city, final List<String> timeRanges,
                                  final PackSortOption sort,
                                  final int page, final int pageSize,
                                  final boolean requirePositiveStock, final Long commerceUserId) {
        return packDao.filterPacks(query, tags, city, timeRanges, sort, page, pageSize, requirePositiveStock,
                commerceUserId);
    }

    @Transactional(readOnly = true)
    @Override
    public int countFilteredPacks(final String query, final List<PackTag> tags,
                                  final String city, final List<String> timeRanges,
                                  final boolean requirePositiveStock, final Long commerceUserId) {
        return packDao.countFilteredPacks(query, tags, city, timeRanges, requirePositiveStock, commerceUserId);
    }

    @Transactional(readOnly = true)
    @Override
    public Pack resolvePackForDirectEdit(final long packId) {
        return requirePackForDirectEdit(packId, PackDirectEditException.ForbiddenAction.EDIT);
    }

    private Pack requirePackForDirectEdit(final long packId,
            final PackDirectEditException.ForbiddenAction forbiddenAction) {
        final Optional<Pack> packOpt = packDao.findById(packId);
        if (packOpt.isEmpty() || Boolean.TRUE.equals(packOpt.get().getDeleted())) {
            LOGGER.warn("Pack not available for direct edit: packId={}", packId);
            throw new PackDirectEditException(PackDirectEditException.Reason.NOT_FOUND,
                    "Pack not found or deleted: " + packId);
        }
        if (auctionService.findByPackId(packId).isPresent()) {
            LOGGER.warn("Pack tied to auction cannot be edited directly: packId={}", packId);
            throw new PackDirectEditException(PackDirectEditException.Reason.FORBIDDEN_AUCTION, forbiddenAction,
                    "Pack is tied to an auction: " + packId);
        }
        return packOpt.get();
    }

    @Transactional(readOnly = true)
    @Override
    public List<Pack> filterCommercePacks(Long commerceId, Boolean hasAuction, int page, int pageSize) {
        return packDao.filterCommercePacks(commerceId, hasAuction, page, pageSize);
    }

    @Transactional(readOnly = true)
    @Override
    public int countCommercePacks(Long commerceId, Boolean hasAuction) {
        return packDao.countCommercePacks(commerceId, hasAuction);
    }

    @Transactional(readOnly = true)
    @Override
    public List<Pack> getPublicOffersByCommerce(final Long commerceUserId, final int page, final int pageSize) {
        final int safePage = page < 1 ? 1 : page;
        final int safePageSize = pageSize < 1 ? 12 : pageSize;
        return packDao.findPublicOffersByCommerce(commerceUserId, safePage, safePageSize);
    }

    @Transactional(readOnly = true)
    @Override
    public int countPublicOffersByCommerce(final Long commerceUserId) {
        return packDao.countPublicOffersByCommerce(commerceUserId);
    }

    @Transactional
    @Override
    public boolean decrementStock(final long packId, final int quantity) {
        return packDao.decrementStock(packId, quantity);
    }

    @Transactional
    @Override
    public boolean incrementStock(final long packId, final int quantity) {
        return packDao.incrementStock(packId, quantity);
    }
}
