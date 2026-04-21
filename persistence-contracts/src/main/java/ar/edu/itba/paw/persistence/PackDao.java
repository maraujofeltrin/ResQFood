package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.models.Pack;
import java.util.Optional;
import java.util.List;

import ar.edu.itba.paw.models.PackSortOption;
import ar.edu.itba.paw.models.PackTag;

public interface PackDao {
    Pack createPack(Long commerceId, String title, String description, Double originalPrice, Double finalPrice, Integer stock, List<PackTag> tags, byte[] imageData, String imageContentType);
    Optional<Pack> findById(Long id);
    List<Pack> findAll();
    List<Pack> findByCommerceId(Long commerceId);
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
    void softDelete(Long id);
    Optional<Pack> findImageByPackId(Long id);
    void updateImage(Long packId, byte[] imageData, String imageContentType);

    /**
     * Resta {@code quantity} al stock del pack si hay unidades suficientes.
     *
     * @return {@code true} si se actualizó exactamente una fila
     */
    boolean decrementStock(long packId, int quantity);

    /**
     * Unified filter: searches active packs applying all optional criteria at once.
     *
     * @param query     free-text search on title/commerce name (nullable = skip)
     * @param tags      required tags – all must match (nullable/empty = skip)
     * @param city      exact city to match against {@code commerces.city} (nullable = skip)
     * @param timeRanges list of time-of-day labels ("morning","afternoon","evening") to match against
     *                   {@code commerces.opening_time} (nullable/empty = skip)
     * @param sort      ordering criterion
     */
    List<Pack> filterPacks(String query, List<PackTag> tags, String city,
                           List<String> timeRanges, PackSortOption sort);
}
