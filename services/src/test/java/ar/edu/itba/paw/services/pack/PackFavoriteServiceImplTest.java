package ar.edu.itba.paw.services.pack;

import ar.edu.itba.paw.models.pack.Pack;
import ar.edu.itba.paw.persistence.PackDao;
import ar.edu.itba.paw.persistence.PackFavoriteDao;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PackFavoriteServiceImplTest {

    @Mock
    private PackFavoriteDao packFavoriteDao;

    @Mock
    private PackDao packDao;

    @InjectMocks
    private PackFavoriteServiceImpl packFavoriteService;

    @Test
    public void testListActiveFavoritePacksFirstPageClampsMax() {
        // 1. Setup
        when(packFavoriteDao.findActiveFavoritePacksForClient(eq(1L), eq(1), eq(48))).thenReturn(Collections.emptyList());

        // 2. Ejercicio
        final List<Pack> result = packFavoriteService.listActiveFavoritePacks(1L, 999);

        // 3. Asserts
        assertNotNull(result);
        verify(packFavoriteDao).findActiveFavoritePacksForClient(1L, 1, 48);
    }

    @Test
    public void testToggleFavoriteRemovesWhenExists() {
        // 1. Setup
        when(packFavoriteDao.exists(5L, 10L)).thenReturn(true);

        // 2. Ejercicio
        packFavoriteService.toggleFavorite(5L, 10L);

        // 3. Asserts
        verify(packFavoriteDao).delete(5L, 10L);
        verify(packFavoriteDao, never()).insert(anyLong(), anyLong());
        verify(packDao, never()).findById(anyLong());
    }

    @Test
    public void testToggleFavoriteAddsWhenActivePack() {
        // 1. Setup
        when(packFavoriteDao.exists(5L, 10L)).thenReturn(false);
        final Pack pack = new Pack(10L, 1L, "t", "d", 1.0, 1.0, 1, true, false, Collections.emptyList(), null);
        when(packDao.findById(10L)).thenReturn(Optional.of(pack));

        // 2. Ejercicio
        packFavoriteService.toggleFavorite(5L, 10L);

        // 3. Asserts
        verify(packFavoriteDao).insert(5L, 10L);
    }

    @Test
    public void testToggleFavoriteThrowsWhenPackInactive() {
        // 1. Setup
        when(packFavoriteDao.exists(5L, 10L)).thenReturn(false);
        final Pack pack = new Pack(10L, 1L, "t", "d", 1.0, 1.0, 1, false, false, Collections.emptyList(), null);
        when(packDao.findById(10L)).thenReturn(Optional.of(pack));

        // 2. Ejercicio / 3. Asserts
        assertThrows(IllegalArgumentException.class, () -> packFavoriteService.toggleFavorite(5L, 10L));
        verify(packFavoriteDao, never()).insert(anyLong(), anyLong());
    }
}
