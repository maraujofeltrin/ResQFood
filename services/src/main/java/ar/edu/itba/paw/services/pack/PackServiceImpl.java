package ar.edu.itba.paw.services.pack;

import ar.edu.itba.paw.models.pack.PackTag;

import ar.edu.itba.paw.models.pack.PackSortOption;

import ar.edu.itba.paw.models.pack.Pack;
import ar.edu.itba.paw.persistence.PackDao;
import ar.edu.itba.paw.services.auction.AuctionService;
import ar.edu.itba.paw.services.commerce.CommercePackAccess;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PackServiceImpl implements PackService {

    private final PackDao packDao;
    private final AuctionService auctionService;

    @Autowired
    public PackServiceImpl(final PackDao packDao, final AuctionService auctionService) {
        this.packDao = packDao;
        this.auctionService = auctionService;
    }

    @Override
    public Pack createPack(Long commerceId, String title, String description, Double originalPrice,
                           Double finalPrice, Integer stock, List<PackTag> tags,
                           byte[] imageData, String imageContentType) {
        return packDao.createPack(commerceId, title, description, originalPrice, finalPrice, stock, tags, imageData, imageContentType);
    }

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

    @Override
    public List<Pack> findActive() {
        return packDao.findActive();
    }

    @Override
    public List<Pack> searchPacks(String query) {
        return packDao.searchPacks(query);
    }

    @Override
    public List<Pack> findActiveByTags(final List<PackTag> tags) {
        return packDao.findActiveByTags(tags);
    }

    @Override
    public List<Pack> searchPacksWithTags(final String query, final List<PackTag> tags) {
        return packDao.searchPacksWithTags(query, tags);
    }

    @Override
    public List<Pack> findActive(PackSortOption sort) {
        return packDao.findActive(sort);
    }

    @Override
    public List<Pack> searchPacks(String query, PackSortOption sort) {
        return packDao.searchPacks(query, sort);
    }

    @Override
    public List<Pack> findActiveByTags(List<PackTag> tags, PackSortOption sort) {
        return packDao.findActiveByTags(tags, sort);
    }

    @Override
    public List<Pack> searchPacksWithTags(String query, List<PackTag> tags, PackSortOption sort) {
        return packDao.searchPacksWithTags(query, tags, sort);
    }

    @Override
    public Pack update(Pack pack) {
        return packDao.update(pack);
    }

    @Override
    public void deletePack(Long id) {
        packDao.softDelete(id);
    }

    @Override
    public Optional<Pack> findImageByPackId(Long id) {
        return packDao.findImageByPackId(id);
    }

    @Override
    public void updateImage(Long packId, byte[] imageData, String imageContentType) {
        packDao.updateImage(packId, imageData, imageContentType);
    }

    @Override
    public List<Pack> filterPacks(final String query, final List<PackTag> tags,
                                  final String city, final List<String> timeRanges,
                                  final PackSortOption sort) {
        return packDao.filterPacks(query, tags, city, timeRanges, sort);
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
}
