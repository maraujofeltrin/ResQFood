package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.models.Pack;
import java.util.Optional;
import java.util.List;

public interface PackDao {
    public Pack createPack(Long commerceId, String title, String description, Double originalPrice, Double finalPrice, Integer stock);
    public Optional<Pack> findById(final Long id);
    public List<Pack> findAll();
    public List<Pack> findActive();
    public List<Pack> searchPacks(String query);
    public Pack update(Pack pack);
    public void setActive(final Long id, final boolean active);
}
