package ar.edu.itba.paw.services;

import ar.edu.itba.paw.models.PackTag;

import ar.edu.itba.paw.models.PackSortOption;

import ar.edu.itba.paw.models.Pack;
import ar.edu.itba.paw.persistence.PackDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PackServiceImpl implements PackService {

    private final PackDao packDao;

    @Autowired
    public PackServiceImpl(final PackDao packDao) {
        this.packDao = packDao;
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
    public void setActive(Long id, boolean active) {
        packDao.setActive(id, active);
    }

    @Override
    public Optional<Pack> findImageByPackId(Long id) {
        return packDao.findImageByPackId(id);
    }
}
