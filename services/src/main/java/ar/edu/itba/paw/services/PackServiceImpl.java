package ar.edu.itba.paw.services;

import ar.edu.itba.paw.models.Pack;
import ar.edu.itba.paw.persistence.PackDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PackServiceImpl implements PackService {

    private final PackDao packDao;

    @Autowired
    public PackServiceImpl(final PackDao packDao) {
        this.packDao = packDao;
    }

    @Override
    public Optional<Pack> findById(final Long id) {
        return packDao.findById(id);
    }

    @Override
    public List<Pack> findAll() {
        return packDao.findAll();
    }

    @Override
    public List<Pack> findActive() {
        return packDao.findActive();
    }

    @Override
    public Pack createPack(Long commerceId, String title, String description, Double originalPrice, Double finalPrice, Integer stock) {
        return packDao.createPack(commerceId, title, description, originalPrice, finalPrice, stock);
    }

    @Override
    public Pack update(Pack pack) {
        return packDao.update(pack);
    }

    @Override
    public void setActive(final Long id, final boolean active) {
        packDao.setActive(id, active);
    }
}
