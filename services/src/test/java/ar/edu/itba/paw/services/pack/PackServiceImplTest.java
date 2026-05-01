package ar.edu.itba.paw.services.pack;

import ar.edu.itba.paw.models.pack.Pack;
import ar.edu.itba.paw.persistence.PackDao;
import ar.edu.itba.paw.services.auction.AuctionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PackServiceImplTest {

    @Mock
    private PackDao packDao;

    @Mock
    private AuctionService auctionService;

    @InjectMocks
    private PackServiceImpl packService;

    @Test
    void testCreatePackWhenDaoPersistsReturnsPackWithIdAndTitle() {
        // 1. Setup
        final Pack persisted = new Pack(1L, 5L, "T", "D", 10.0, 7.0, 3, true, false, Collections.emptyList(), null);
        when(packDao.createPack(eq(5L), eq("T"), eq("D"), eq(10.0), eq(7.0), eq(3), eq(Collections.emptyList()),
                isNull())).thenReturn(persisted);

        // 2. Ejercicio
        final Pack created = packService.createPack(5L, "T", "D", 10.0, 7.0, 3, Collections.emptyList(), null);

        // 3. Asserts
        assertNotNull(created.getId());
        assertEquals(1L, created.getId());
        assertEquals("T", created.getTitle());
    }

    @Test
    void testFindByIdWhenPackExistsReturnsPackFromDao() {
        // 1. Setup
        final Pack persisted = new Pack(1L, 5L, "T", "D", 10.0, 7.0, 3, true, false, Collections.emptyList(), null);
        when(packDao.findById(1L)).thenReturn(Optional.of(persisted));

        // 2. Ejercicio
        final Optional<Pack> found = packService.findById(1L);

        // 3. Asserts
        assertTrue(found.isPresent());
        assertEquals("T", found.get().getTitle());
    }

    @Test
    void testDeletePackWhenPackExistsMarksDeletedViaSoftDelete() {
        // 1. Setup
        final Pack pack = new Pack(1L, 1L, "a", "b", 1.0, 1.0, 1, true, false, Collections.emptyList(), null);
        doAnswer(invocation -> {
            pack.setDeleted(true);
            return null;
        }).when(packDao).softDelete(1L);

        // 2. Ejercicio
        packService.deletePack(1L);

        // 3. Asserts
        assertTrue(pack.getDeleted());
    }

    @Test
    void testUpdateWhenDaoReturnsPackReflectsMutatedFields() {
        // 1. Setup
        final Pack pack = new Pack(2L, 2L, "old", "d", 2.0, 1.0, 2, true, false, Collections.emptyList(), null);
        pack.setTitle("new");
        pack.setStock(5);
        when(packDao.update(pack)).thenReturn(pack);

        // 2. Ejercicio
        final Pack result = packService.update(pack);

        // 3. Asserts
        assertEquals("new", result.getTitle());
        assertEquals(5, result.getStock());
    }

    @Test
    void testFindVisibleForDetailWhenPackActiveReturnsPackForAnonymousViewer() {
        // 1. Setup
        final Pack pack = new Pack(7L, 10L, "active", "d", 2.0, 1.0, 2, true, false, Collections.emptyList(), null);
        when(packDao.findById(7L)).thenReturn(Optional.of(pack));

        // 2. Ejercicio
        final Optional<Pack> result = packService.findVisibleForDetail(7L, null);

        // 3. Asserts
        assertTrue(result.isPresent());
        assertEquals(7L, result.get().getId());
    }

    @Test
    void testFindVisibleForDetailWhenPackInactiveReturnsPackForOwnerCommerce() {
        // 1. Setup
        final Pack pack = new Pack(8L, 11L, "inactive", "d", 2.0, 1.0, 2, false, false, Collections.emptyList(), null);
        when(packDao.findById(8L)).thenReturn(Optional.of(pack));

        // 2. Ejercicio
        final Optional<Pack> result = packService.findVisibleForDetail(8L, 11L);

        // 3. Asserts
        assertTrue(result.isPresent());
        assertEquals(8L, result.get().getId());
    }

    @Test
    void testFindVisibleForDetailWhenPackInactiveReturnsEmptyForNonOwner() {
        // 1. Setup
        final Pack pack = new Pack(9L, 12L, "inactive", "d", 2.0, 1.0, 2, false, false, Collections.emptyList(), null);
        when(packDao.findById(9L)).thenReturn(Optional.of(pack));

        // 2. Ejercicio
        final Optional<Pack> result = packService.findVisibleForDetail(9L, 99L);

        // 3. Asserts
        assertTrue(result.isEmpty());
    }

    @Test
    void testFindVisibleForDetailWhenPackMissingReturnsEmpty() {
        // 1. Setup
        when(packDao.findById(999L)).thenReturn(Optional.empty());

        // 2. Ejercicio
        final Optional<Pack> result = packService.findVisibleForDetail(999L, 99L);

        // 3. Asserts
        assertTrue(result.isEmpty());
    }

    @Test
    void testFindVisibleForDetailWhenPackDeletedReturnsEmpty() {
        // 1. Setup
        final Pack pack = new Pack(10L, 1L, "gone", "d", 1.0, 1.0, 1, true, true, Collections.emptyList(), null);
        when(packDao.findById(10L)).thenReturn(Optional.of(pack));

        // 2. Ejercicio
        final Optional<Pack> result = packService.findVisibleForDetail(10L, 1L);

        // 3. Asserts
        assertTrue(result.isEmpty());
    }
}
