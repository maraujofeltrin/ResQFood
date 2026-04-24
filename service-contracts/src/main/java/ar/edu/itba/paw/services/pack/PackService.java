package ar.edu.itba.paw.services.pack;

import ar.edu.itba.paw.models.pack.Pack;
import ar.edu.itba.paw.models.pack.PackSortOption;
import ar.edu.itba.paw.models.pack.PackTag;
import ar.edu.itba.paw.services.commerce.CommercePackAccess;

import java.util.List;
import java.util.Optional;

public interface PackService {
    Pack createPack(Long commerceId, String title, String description, Double originalPrice, Double finalPrice, Integer stock, List<PackTag> tags, byte[] imageData, String imageContentType);
    Optional<Pack> findById(Long id);
    List<Pack> findAll();
    List<Pack> findByCommerceId(Long commerceId);
    Pack update(Pack pack);
    void deletePack(Long id);
    Optional<Pack> findImageByPackId(Long id);
    void updateImage(Long packId, byte[] imageData, String imageContentType);

    /**
     * Direct-sale pack owned by the commerce, not soft-deleted, and not under an auction.
     */
    CommercePackAccess resolvePackForDirectEdit(long packId, long commerceUserId);

    List<Pack> filterPacks(String query, List<PackTag> tags, String city,
                           List<String> timeRanges, PackSortOption sort);
}
