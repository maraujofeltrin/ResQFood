package ar.edu.itba.paw.services.pack;

import ar.edu.itba.paw.models.pack.Pack;
import ar.edu.itba.paw.models.pack.PackSortOption;
import ar.edu.itba.paw.models.pack.PackTag;
import ar.edu.itba.paw.services.commerce.CommercePackAccess;

import java.util.List;
import java.util.Optional;

public interface PackService {
    Pack createPack(Long commerceId, String title, String description, Double originalPrice, Double finalPrice, Integer stock, List<PackTag> tags, Long imageId);
    Optional<Pack> findById(Long id);
    List<Pack> findAll();
    List<Pack> findByCommerceId(Long commerceId);
    Pack update(Pack pack);
    CommercePackAccess deletePack(long packId, long commerceUserId);

    /**
     * Returns a pack visible on its public detail page.
     * Inactive packs are visible only to their owning commerce.
     */
    Optional<Pack> findVisibleForDetail(Long packId, Long viewerUserId);


    /**
     * Updates an existing pack. If imageData is provided, it updates the image too.
     */
    CommercePackAccess updatePack(long packId, long commerceUserId, String title, String description, Double originalPrice,
                    Double finalPrice, Integer stock, List<PackTag> tags,
                    Long imageId);

    /**
     * Direct-sale pack owned by the commerce, not soft-deleted, and not under an auction.
     */
    CommercePackAccess resolvePackForDirectEdit(long packId, long commerceUserId);

    List<Pack> filterPacks(String query, List<PackTag> tags, String city,
                           List<String> timeRanges, PackSortOption sort,
                           int page, int pageSize, boolean requirePositiveStock, Long commerceUserId);

    int countFilteredPacks(String query, List<PackTag> tags, String city, List<String> timeRanges,
                           boolean requirePositiveStock, Long commerceUserId);

    List<Pack> filterCommercePacks(Long commerceId, Boolean hasAuction, int page, int pageSize);

    int countCommercePacks(Long commerceId, Boolean hasAuction);
}
