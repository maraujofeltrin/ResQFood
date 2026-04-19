package ar.edu.itba.paw.services;

import ar.edu.itba.paw.models.Pack;
import ar.edu.itba.paw.models.PackSortOption;
import ar.edu.itba.paw.models.PackTag;

import java.util.List;
import java.util.Optional;

public interface PackService {
    Pack createPack(Long commerceId, String title, String description, Double originalPrice, Double finalPrice, Integer stock, List<PackTag> tags, byte[] imageData, String imageContentType);
    Optional<Pack> findById(Long id);
    List<Pack> findAll();
    List<Pack> findActive();
    List<Pack> searchPacks(String query);
    List<Pack> findActiveByTags(List<PackTag> tags);
    List<Pack> searchPacksWithTags(String query, List<PackTag> tags);

    List<Pack> findActive(PackSortOption sort);
    List<Pack> searchPacks(String query, PackSortOption sort);
    List<Pack> findActiveByTags(List<PackTag> tags, PackSortOption sort);
    List<Pack> searchPacksWithTags(String query, List<PackTag> tags, PackSortOption sort);
    Pack update(Pack pack);
    void setActive(Long id, boolean active);
    Optional<Pack> findImageByPackId(Long id);

    List<Pack> filterPacks(String query, List<PackTag> tags, String city,
                           List<String> timeRanges, PackSortOption sort);
}
