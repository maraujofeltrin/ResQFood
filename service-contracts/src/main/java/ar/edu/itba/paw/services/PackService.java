package ar.edu.itba.paw.services;

import ar.edu.itba.paw.models.Pack;

import java.util.List;
import java.util.Optional;

public interface PackService {
    Pack createPack(Long commerceId, String title, String description, Double originalPrice, Double finalPrice, Integer stock);
    Optional<Pack> findById(final Long id);
    List<Pack> findAll();
    List<Pack> findActive();
    Pack update(Pack pack);
    void setActive(final Long id, final boolean active);
}
