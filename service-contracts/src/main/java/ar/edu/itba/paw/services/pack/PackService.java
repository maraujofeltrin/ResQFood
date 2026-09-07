package ar.edu.itba.paw.services.pack;

import ar.edu.itba.paw.models.pack.Pack;
import ar.edu.itba.paw.models.pack.PackDirectEditException;
import ar.edu.itba.paw.models.pack.PackSortOption;
import ar.edu.itba.paw.models.pack.PackTag;

import java.util.List;
import java.util.Optional;

public interface PackService {
    Pack createPack(Long commerceId, String title, String description, Double originalPrice, Double finalPrice, Integer stock, List<PackTag> tags, Long imageId);
    Optional<Pack> findById(Long id);
    Pack update(Pack pack);
    void deletePack(long packId) throws PackDirectEditException;

    /**
     * Returns a pack visible on its public detail page.
     * Inactive packs are visible only to their owning commerce.
     */
    Optional<Pack> findVisibleForDetail(Long packId, Long viewerUserId);


    /**
     * Updates an existing pack. If imageData is provided, it updates the image too.
     *
     * @throws PackDirectEditException if the pack does not exist, is deleted, or is tied to an auction
     */
    Pack updatePack(long packId, String title, String description, Double originalPrice,
                    Double finalPrice, Integer stock, List<PackTag> tags,
                    Long imageId) throws PackDirectEditException;

    /**
     * Direct-sale pack owned by the commerce, not soft-deleted, and not under an auction.
     *
     * @throws PackDirectEditException if the pack does not exist, is deleted, or is tied to an auction
     */
    Pack resolvePackForDirectEdit(long packId) throws PackDirectEditException;

    List<Pack> filterPacks(String query, List<PackTag> tags, String city,
                           List<String> timeRanges, PackSortOption sort,
                           int page, int pageSize, boolean requirePositiveStock, Long commerceUserId);

    int countFilteredPacks(String query, List<PackTag> tags, String city, List<String> timeRanges,
                           boolean requirePositiveStock, Long commerceUserId);

    List<Pack> filterCommercePacks(Long commerceId, Boolean hasAuction, int page, int pageSize);

    int countCommercePacks(Long commerceId, Boolean hasAuction);

    List<Pack> getPublicOffersByCommerce(Long commerceUserId, int page, int pageSize);

    int countPublicOffersByCommerce(Long commerceUserId);

    boolean decrementStock(long packId, int quantity);

    boolean incrementStock(long packId, int quantity);
}
