package ar.edu.itba.paw.services.pack;

import ar.edu.itba.paw.models.pack.PackTag;

import ar.edu.itba.paw.models.pack.PackSortOption;

import ar.edu.itba.paw.models.pack.Pack;
import ar.edu.itba.paw.persistence.ImageDao;
import ar.edu.itba.paw.persistence.PackDao;
import ar.edu.itba.paw.services.auction.AuctionService;
import ar.edu.itba.paw.services.reservation.ReservationService;
import ar.edu.itba.paw.services.commerce.CommercePackAccess;
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
    private final ImageDao imageDao;
    private final AuctionService auctionService;
    private final ReservationService reservationService;

    @Autowired
    public PackServiceImpl(final PackDao packDao, final ImageDao imageDao, final AuctionService auctionService,
            final ReservationService reservationService) {
        this.packDao = packDao;
        this.imageDao = imageDao;
        this.auctionService = auctionService;
        this.reservationService = reservationService;
    }

    @Transactional
    @Override
    public Pack createPack(Long commerceId, String title, String description, Double originalPrice,
                           Double finalPrice, Integer stock, List<PackTag> tags,
                           Long imageId) {
        final Pack createdPack = packDao.createPack(commerceId, title, description, originalPrice, finalPrice, stock, tags, imageId);
        LOGGER.info("Pack created: packId={}, commerceId={}", createdPack.getId(), commerceId);
        return createdPack;
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<Pack> findById(Long id) {
        return packDao.findById(id);
    }

    @Override
    public List<Pack> findAll() {
        return packDao.findAll();
    }

    @Override
    public List<Pack> findByCommerceId(Long commerceId) {
        return packDao.findByCommerceId(commerceId);
    }

    @Transactional
    @Override
    public Pack update(Pack pack) {
        return packDao.update(pack);
    }

    @Transactional
    @Override
    public CommercePackAccess deletePack(long packId) {
        CommercePackAccess access = resolvePackForDirectEdit(packId);
        if (!(access instanceof CommercePackAccess.Granted)) {
            LOGGER.warn("Failed to soft-delete pack: packId={}, accessType={}", packId, access.getClass().getSimpleName());
            return access;
        }
        packDao.softDelete(packId);
        LOGGER.info("Pack soft-deleted: packId={}", packId);
        return access;
    }

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
                .map(auction -> auctionService.getBidHistory(auction.getId()).stream()
                        .anyMatch(bid -> viewerUserId.equals(bid.getClient().getUserId())))
                .orElse(false);
    }

    @Transactional
    @Override
    public CommercePackAccess updatePack(long packId, String title, String description, Double originalPrice,
                           Double finalPrice, Integer stock, List<PackTag> tags,
                           Long imageId) {
        CommercePackAccess access = resolvePackForDirectEdit(packId);
        if (!(access instanceof CommercePackAccess.Granted)) {
            LOGGER.warn("Failed to update pack: packId={}, accessType={}", packId, access.getClass().getSimpleName());
            return access;
        }
        final Pack packToUpdate = ((CommercePackAccess.Granted) access).pack();

        packToUpdate.setTitle(title);
        packToUpdate.setDescription(description);
        packToUpdate.setOriginalPrice(originalPrice);
        packToUpdate.setFinalPrice(finalPrice);
        packToUpdate.setStock(stock);
        packToUpdate.setTags(tags != null ? tags : java.util.Collections.emptyList());

        if (imageId != null) {
            packToUpdate.setImage(imageDao.getImage(imageId)
                    .orElseThrow(() -> new NoSuchElementException("Image not found: " + imageId)));
        }

        final Pack updatedPack = packDao.update(packToUpdate);
        LOGGER.info("Pack updated: packId={}", packId);
        return new CommercePackAccess.Granted(updatedPack);
    }

    @Override
    public List<Pack> filterPacks(final String query, final List<PackTag> tags,
                                  final String city, final List<String> timeRanges,
                                  final PackSortOption sort,
                                  final int page, final int pageSize,
                                  final boolean requirePositiveStock, final Long commerceUserId) {
        return packDao.filterPacks(query, tags, city, timeRanges, sort, page, pageSize, requirePositiveStock,
                commerceUserId);
    }

    @Override
    public int countFilteredPacks(final String query, final List<PackTag> tags,
                                  final String city, final List<String> timeRanges,
                                  final boolean requirePositiveStock, final Long commerceUserId) {
        return packDao.countFilteredPacks(query, tags, city, timeRanges, requirePositiveStock, commerceUserId);
    }

    @Override
    public CommercePackAccess resolvePackForDirectEdit(final long packId) {
        final Optional<Pack> packOpt = packDao.findById(packId);
        if (packOpt.isEmpty() || Boolean.TRUE.equals(packOpt.get().getDeleted())) {
            return new CommercePackAccess.NotFound();
        }
        if (auctionService.findByPackId(packId).isPresent()) {
            return new CommercePackAccess.ForbiddenAuction();
        }
        return new CommercePackAccess.Granted(packOpt.get());
    }

    @Override
    public List<Pack> filterCommercePacks(Long commerceId, Boolean hasAuction, int page, int pageSize) {
        return packDao.filterCommercePacks(commerceId, hasAuction, page, pageSize);
    }

    @Override
    public int countCommercePacks(Long commerceId, Boolean hasAuction) {
        return packDao.countCommercePacks(commerceId, hasAuction);
    }
}
