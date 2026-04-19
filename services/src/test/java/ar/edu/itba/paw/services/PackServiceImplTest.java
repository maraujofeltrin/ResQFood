package ar.edu.itba.paw.services;

import ar.edu.itba.paw.models.Pack;
import ar.edu.itba.paw.models.PackTag;
import ar.edu.itba.paw.models.PackSortOption;
import ar.edu.itba.paw.persistence.PackDao;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class PackServiceImplTest {

    static class InMemoryPackDao implements PackDao {
        private final Map<Long, Pack> store = new HashMap<>();
        private long nextId = 1L;

        @Override
        public Pack createPack(Long commerceId, String title, String description, Double originalPrice, Double finalPrice, Integer stock, List<PackTag> tags, byte[] imageData, String imageContentType) {
            final Pack p = new Pack(nextId++, commerceId, title, description, originalPrice, finalPrice, stock, true, tags, imageData, imageContentType);
            store.put(p.getId(), p);
            return p;
        }

        @Override
        public Optional<Pack> findById(Long id) {
            return Optional.ofNullable(store.get(id));
        }

        @Override
        public List<Pack> findAll() {
            return new ArrayList<>(store.values());
        }

        @Override
        public List<Pack> findActive() {
            final List<Pack> r = new ArrayList<>();
            for (Pack p : store.values()) if (Boolean.TRUE.equals(p.getActive())) r.add(p);
            return r;
        }

        @Override
        public List<Pack> searchPacks(String query) {
            final List<Pack> r = new ArrayList<>();
            for (Pack p : store.values()) if (p.getTitle().contains(query) || p.getDescription().contains(query)) r.add(p);
            return r;
        }

        @Override
        public Pack update(Pack pack) {
            store.put(pack.getId(), pack);
            return pack;
        }

        @Override
        public void setActive(Long id, boolean active) {
            final Pack p = store.get(id);
            if (p != null) p.setActive(active);
        }

        @Override
        public Optional<Pack> findImageByPackId(Long id) {
            final Pack p = store.get(id);
            return Optional.ofNullable(p != null && p.getImageData() != null ? p : null);
        }

        @Override
        public boolean decrementStock(long packId, int quantity) {
            return true;
        }

        @Override
        public List<Pack> findActiveByTags(List<PackTag> tags) {
            return Collections.emptyList();
        }

        @Override
        public List<Pack> searchPacksWithTags(String query, List<PackTag> tags) {
            return Collections.emptyList();
        }

        @Override
        public List<Pack> findActive(PackSortOption sort) { return findActive(); }

        @Override
        public List<Pack> searchPacks(String query, PackSortOption sort) { return searchPacks(query); }

        @Override
        public List<Pack> findActiveByTags(List<PackTag> tags, PackSortOption sort) { return findActiveByTags(tags); }

        @Override
        public List<Pack> searchPacksWithTags(String query, List<PackTag> tags, PackSortOption sort) { return searchPacksWithTags(query, tags); }

        @Override
        public List<Pack> filterPacks(String query, List<PackTag> tags, String city, List<String> timeRanges, PackSortOption sort) { return findActive(); }
    }

    @Test
    public void createPack_storesAndFinds() {
        final InMemoryPackDao dao = new InMemoryPackDao();
        final PackServiceImpl svc = new PackServiceImpl(dao);

        final Pack created = svc.createPack(5L, "T", "D", 10.0, 7.0, 3, Collections.emptyList(), null, null);
        assertNotNull(created.getId());
        final var found = svc.findById(created.getId());
        assertTrue(found.isPresent());
        assertEquals("T", found.get().getTitle());
    }

    @Test
    public void setActive_updatesActiveFlag() {
        final InMemoryPackDao dao = new InMemoryPackDao();
        final PackServiceImpl svc = new PackServiceImpl(dao);

        final Pack p = svc.createPack(1L, "a", "b", 1.0, 1.0, 1, Collections.emptyList(), null, null);
        assertTrue(svc.findById(p.getId()).get().getActive());
        svc.setActive(p.getId(), false);
        assertFalse(svc.findById(p.getId()).get().getActive());
    }

    @Test
    public void update_changesValues() {
        final InMemoryPackDao dao = new InMemoryPackDao();
        final PackServiceImpl svc = new PackServiceImpl(dao);

        final Pack p = svc.createPack(2L, "old", "d", 2.0, 1.0, 2, Collections.emptyList(), null, null);
        p.setTitle("new");
        p.setStock(5);
        svc.update(p);
        final Pack stored = svc.findById(p.getId()).get();
        assertEquals("new", stored.getTitle());
        assertEquals(5, stored.getStock());
    }
}
