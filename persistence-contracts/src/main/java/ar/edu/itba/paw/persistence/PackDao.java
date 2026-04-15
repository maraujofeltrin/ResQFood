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
    void softDelete(Long id);
    Optional<Pack> findImageByPackId(Long id);
    void updateImage(Long packId, byte[] imageData, String imageContentType);

    /**
     * Resta {@code quantity} al stock del pack si hay unidades suficientes.
     *
     * @return {@code true} si se actualizó exactamente una fila
     */
    boolean decrementStock(long packId, int quantity);
}
