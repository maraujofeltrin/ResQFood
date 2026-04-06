package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.models.Pack;
import java.util.Optional;
import java.util.List;

import ar.edu.itba.paw.models.PackTag;

public interface PackDao {
    Pack createPack(Long commerceId, String title, String description, Double originalPrice, Double finalPrice, Integer stock, List<PackTag> tags, byte[] imageData, String imageContentType);
    Optional<Pack> findById(Long id);
    List<Pack> findAll();
    List<Pack> findActive();
    List<Pack> searchPacks(String query);
    List<Pack> findActiveByTags(List<PackTag> tags);
    List<Pack> searchPacksWithTags(String query, List<PackTag> tags);
    Pack update(Pack pack);
    void setActive(Long id, boolean active);
    Optional<Pack> findImageByPackId(Long id);
}
