package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.models.pack.Pack;
import java.util.Optional;
import java.util.List;

import ar.edu.itba.paw.models.pack.PackSortOption;
import ar.edu.itba.paw.models.pack.PackTag;

public interface PackDao {
    Pack createPack(Long commerceId, String title, String description, Double originalPrice, Double finalPrice, Integer stock, List<PackTag> tags, Long imageId);
    Optional<Pack> findById(Long id);
    List<Pack> findAll();
    List<Pack> findByCommerceId(Long commerceId);
    Pack update(Pack pack);
    void setActive(Long id, boolean active);
    void softDelete(Long id);


    /**
     * Resta {@code quantity} al stock del pack si hay unidades suficientes.
     *
     * @return {@code true} si se actualizó exactamente una fila
     */
    boolean decrementStock(long packId, int quantity);

    /**
     * Suma {@code quantity} al stock del pack.
     *
     * @return {@code true} si se actualizó exactamente una fila
     */
    boolean incrementStock(long packId, int quantity);

    /**
     * Unified filter: searches active packs applying all optional criteria at once.
     *
     * @param query     free-text search on title/commerce name (nullable = skip)
     * @param tags      required tags – all must match (nullable/empty = skip)
     * @param city      exact city to match against {@code commerces.city} (nullable = skip)
     * @param timeRanges list of time-of-day labels ("morning","afternoon","evening") to match against
     *                   {@code commerces.opening_time} (nullable/empty = skip)
     * @param sort      ordering criterion
     * @param requirePositiveStock when {@code true}, only packs with {@code stock > 0} are included
     * @param commerceUserId       when non-null, only packs owned by this commerce user id
     */
    List<Pack> filterPacks(String query, List<PackTag> tags, String city,
                           List<String> timeRanges, PackSortOption sort,
                           int page, int pageSize, boolean requirePositiveStock, Long commerceUserId);

    /**
     * Returns the total number of active packs matching the filters (ignoring sort/pagination).
     *
     * @param requirePositiveStock when {@code true}, only packs with {@code stock > 0} are counted
     * @param commerceUserId       when non-null, only packs owned by this commerce user id
     */
    int countFilteredPacks(String query, List<PackTag> tags, String city, List<String> timeRanges,
                           boolean requirePositiveStock, Long commerceUserId);

    /**
     * Filters packs belonging to a specific commerce, optionally filtering by whether they have an associated auction.
     * @param commerceId the commerce ID
     * @param hasAuction if true, returns only packs with an auction; if false, packs without; if null, all packs
     */
    List<Pack> filterCommercePacks(Long commerceId, Boolean hasAuction, int page, int pageSize);

    /**
     * Counts packs belonging to a specific commerce, optionally filtering by whether they have an associated auction.
     */
    int countCommercePacks(Long commerceId, Boolean hasAuction);
}
