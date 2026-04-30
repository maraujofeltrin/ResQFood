package ar.edu.itba.paw.services.pack;

import ar.edu.itba.paw.models.pack.Pack;
import ar.edu.itba.paw.models.pack.PackTag;
import ar.edu.itba.paw.models.pack.PackSortOption;
import ar.edu.itba.paw.persistence.PackDao;
import ar.edu.itba.paw.services.auction.AuctionService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class PackServiceImplTest {

    private static AuctionService noopAuctionService() {
        final AuctionService mock = Mockito.mock(AuctionService.class);
        Mockito.when(mock.findByPackId(org.mockito.ArgumentMatchers.anyLong())).thenReturn(Optional.empty());
        return mock;
    }

    static class InMemoryPackDao implements PackDao {
        private final Map<Long, Pack> store = new HashMap<>();
        private long nextId = 1L;

        @Override
        public Pack createPack(Long commerceId, String title, String description, Double originalPrice, Double finalPrice, Integer stock, List<PackTag> tags, Long imageId) {
            final Pack p = new Pack(nextId++, commerceId, title, description, originalPrice, finalPrice, stock, true, false, tags, imageId);
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
        public List<Pack> findByCommerceId(Long commerceId) {
            return store.values().stream()
                    .filter(p -> p.getCommerceId().equals(commerceId))
                    .collect(java.util.stream.Collectors.toList());
        }

        @Override
        public Pack update(Pack pack) {
            store.put(pack.getId(), pack);
            return pack;
        }

        @Override
        public void softDelete(Long id) {
            final Pack p = store.get(id);
            if (p != null) p.setDeleted(true);
        }

        @Override
        public void setActive(Long id, boolean active) {
            final Pack p = store.get(id);
            if (p != null) p.setActive(active);
        }

        @Override
        public boolean decrementStock(long packId, int quantity) {
            return true;
        }

        @Override
        public boolean incrementStock(long packId, int quantity) {
            return true;
        }

        @Override
        public List<Pack> filterPacks(String query, List<PackTag> tags, String city, List<String> timeRanges, PackSortOption sort, int page, int pageSize) {
            //return all active non-deleted packs
            final List<Pack> r = new ArrayList<>();
            for (Pack p : store.values()) if (Boolean.TRUE.equals(p.getActive()) && !Boolean.TRUE.equals(p.getDeleted())) r.add(p);
            return r;
        }

        @Override
        public int countFilteredPacks(String query, List<PackTag> tags, String city, List<String> timeRanges) {
            int count = 0;
            for (Pack p : store.values()) if (Boolean.TRUE.equals(p.getActive()) && !Boolean.TRUE.equals(p.getDeleted())) count++;
            return count;
        }

        @Override
        public List<Pack> filterCommercePacks(Long commerceId, Boolean hasAuction, int page, int pageSize) {
            return Collections.emptyList();
        }

        @Override
        public int countCommercePacks(Long commerceId, Boolean hasAuction) {
            return 0;
        }
    }

    @Test
    public void createPack_storesAndFinds() {
        // 1. Setup
        final InMemoryPackDao dao = new InMemoryPackDao();
        final PackServiceImpl svc = new PackServiceImpl(dao, noopAuctionService());

        // 2. Ejercicio
        final Pack created = svc.createPack(5L, "T", "D", 10.0, 7.0, 3, Collections.emptyList(), null);

        // 3. Asserts
        assertNotNull(created.getId());
        final var found = svc.findById(created.getId());
        assertTrue(found.isPresent());
        assertEquals("T", found.get().getTitle());
    }

    @Test
    public void deletePack_updatesDeletedFlag() {
        // 1. Setup
        final InMemoryPackDao dao = new InMemoryPackDao();
        final PackServiceImpl svc = new PackServiceImpl(dao, noopAuctionService());

        // 2. Ejercicio
        final Pack p = svc.createPack(1L, "a", "b", 1.0, 1.0, 1, Collections.emptyList(), null);
        assertFalse(svc.findById(p.getId()).get().getDeleted());
        svc.deletePack(p.getId());

        // 3. Asserts
        assertTrue(svc.findById(p.getId()).get().getDeleted());
    }

    @Test
    public void update_changesValues() {
        // 1. Setup
        final InMemoryPackDao dao = new InMemoryPackDao();
        final PackServiceImpl svc = new PackServiceImpl(dao, noopAuctionService());

        final Pack p = svc.createPack(2L, "old", "d", 2.0, 1.0, 2, Collections.emptyList(), null);
        p.setTitle("new");
        p.setStock(5);

        // 2. Ejercicio
        svc.update(p);

        // 3. Asserts
        final Pack stored = svc.findById(p.getId()).get();
        assertEquals("new", stored.getTitle());
        assertEquals(5, stored.getStock());
    }

    @Test
    public void findVisibleForDetail_activePack_returnsPackForAnonymousViewer() {
        // 1. Setup
        final InMemoryPackDao dao = new InMemoryPackDao();
        final PackServiceImpl svc = new PackServiceImpl(dao, noopAuctionService());
        final Pack pack = svc.createPack(10L, "active", "d", 2.0, 1.0, 2, Collections.emptyList(), null);

        // 2. Ejercicio
        final Optional<Pack> result = svc.findVisibleForDetail(pack.getId(), null);

        // 3. Asserts
        assertTrue(result.isPresent());
        assertEquals(pack.getId(), result.get().getId());
    }

    @Test
    public void findVisibleForDetail_inactivePack_returnsPackForOwner() {
        // 1. Setup
        final InMemoryPackDao dao = new InMemoryPackDao();
        final PackServiceImpl svc = new PackServiceImpl(dao, noopAuctionService());
        final Pack pack = svc.createPack(11L, "inactive", "d", 2.0, 1.0, 2, Collections.emptyList(), null);
        pack.setActive(false);

        // 2. Ejercicio
        final Optional<Pack> result = svc.findVisibleForDetail(pack.getId(), 11L);

        // 3. Asserts
        assertTrue(result.isPresent());
        assertEquals(pack.getId(), result.get().getId());
    }

    @Test
    public void findVisibleForDetail_inactivePack_returnsEmptyForNonOwner() {
        // 1. Setup
        final InMemoryPackDao dao = new InMemoryPackDao();
        final PackServiceImpl svc = new PackServiceImpl(dao, noopAuctionService());
        final Pack pack = svc.createPack(12L, "inactive", "d", 2.0, 1.0, 2, Collections.emptyList(), null);
        pack.setActive(false);

        // 2. Ejercicio
        final Optional<Pack> result = svc.findVisibleForDetail(pack.getId(), 99L);

        // 3. Asserts
        assertTrue(result.isEmpty());
    }

    @Test
    public void findVisibleForDetail_missingPack_returnsEmpty() {
        // 1. Setup
        final InMemoryPackDao dao = new InMemoryPackDao();
        final PackServiceImpl svc = new PackServiceImpl(dao, noopAuctionService());

        // 2. Ejercicio
        final Optional<Pack> result = svc.findVisibleForDetail(999L, 99L);

        // 3. Asserts
        assertTrue(result.isEmpty());
    }
}
