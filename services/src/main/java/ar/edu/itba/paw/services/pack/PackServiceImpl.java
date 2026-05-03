package ar.edu.itba.paw.services.pack;

import ar.edu.itba.paw.models.pack.PackTag;

import ar.edu.itba.paw.models.pack.PackSortOption;

import ar.edu.itba.paw.models.pack.Pack;
import ar.edu.itba.paw.persistence.PackDao;
import ar.edu.itba.paw.services.auction.AuctionService;
import ar.edu.itba.paw.services.commerce.CommercePackAccess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class PackServiceImpl implements PackService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PackServiceImpl.class);

    private final PackDao packDao;
    private final AuctionService auctionService;

    @Autowired
    public PackServiceImpl(final PackDao packDao, final AuctionService auctionService) {
        this.packDao = packDao;
        this.auctionService = auctionService;
    }

    @Transactional
    @Override
    public Pack createPack(Long commerceId, String title, String description, Double originalPrice,
                           Double finalPrice, Integer stock, List<PackTag> tags,
                           Long imageId) {
        return packDao.createPack(commerceId, title, description, originalPrice, finalPrice, stock, tags, imageId);
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
    public CommercePackAccess deletePack(long packId, long commerceUserId) {
        CommercePackAccess access = resolvePackForDirectEdit(packId, commerceUserId);
        if (!(access instanceof CommercePackAccess.Granted)) {
            return access;
        }
        packDao.softDelete(packId);
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
        if (viewerUserId != null && viewerUserId.equals(pack.getCommerceId())) {
            return Optional.of(pack);
        }
        return Optional.empty();
    }

    @Transactional
    @Override
    public CommercePackAccess updatePack(long packId, long commerceUserId, String title, String description, Double originalPrice,
                           Double finalPrice, Integer stock, List<PackTag> tags,
                           Long imageId) {
        CommercePackAccess access = resolvePackForDirectEdit(packId, commerceUserId);
        if (!(access instanceof CommercePackAccess.Granted)) {
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
            packToUpdate.setImageId(imageId);
        }

        return new CommercePackAccess.Granted(packDao.update(packToUpdate));
    }

    @Override
    public List<Pack> filterPacks(final String query, final List<PackTag> tags,
                                  final String city, final List<String> timeRanges,
                                  final PackSortOption sort,
                                  final int page, final int pageSize,
                                  final boolean requirePositiveStock) {
        return packDao.filterPacks(query, tags, city, timeRanges, sort, page, pageSize, requirePositiveStock);
    }

    @Override
    public int countFilteredPacks(final String query, final List<PackTag> tags,
                                  final String city, final List<String> timeRanges,
                                  final boolean requirePositiveStock) {
        return packDao.countFilteredPacks(query, tags, city, timeRanges, requirePositiveStock);
    }

    @Override
    public CommercePackAccess resolvePackForDirectEdit(final long packId, final long commerceUserId) {
        final Optional<Pack> packOpt = packDao.findById(packId);
        if (packOpt.isEmpty() || packOpt.get().getCommerceId() == null
                || packOpt.get().getCommerceId() != commerceUserId
                || Boolean.TRUE.equals(packOpt.get().getDeleted())) {
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
