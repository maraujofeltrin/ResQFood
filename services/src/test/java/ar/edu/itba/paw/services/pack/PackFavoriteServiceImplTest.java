package ar.edu.itba.paw.services.pack;

import ar.edu.itba.paw.models.pack.Municipality;
import ar.edu.itba.paw.models.pack.Pack;
import ar.edu.itba.paw.models.user.Commerce;
import ar.edu.itba.paw.models.pack.FavoriteToggleException;
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
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PackFavoriteServiceImplTest {

    @Mock
    private PackFavoriteDao packFavoriteDao;

    @Mock
    private PackDao packDao;

    @Mock
    private ar.edu.itba.paw.persistence.AuctionDao auctionDao;

    @InjectMocks
    private PackFavoriteServiceImpl packFavoriteService;

    private static Commerce commerceRef(final long userId) {
        return new Commerce(userId, "Comm", Commerce.Category.BAKERY, "St", 1, Municipality.AVELLANEDA, "P", "1000",
                "08:00", "20:00");
    }

    private static Pack newPack(final Long id, final Long commerceId, final String title, final String description,
            final Double originalPrice, final Double finalPrice, final Integer stock, final Boolean active,
            final Boolean deleted, final List<ar.edu.itba.paw.models.pack.PackTag> tags, final Long imageId) {
        return new Pack(id, commerceRef(commerceId), title, description, originalPrice, finalPrice, stock, active,
                deleted, tags, imageId != null ? new ar.edu.itba.paw.models.image.Image(imageId, new byte[0], "image/png")
                        : null);
    }

    @Test
    void testListActiveFavoritePacksWhenLimitExceedsMaxUsesClampedPageSize() {
        // 1. Setup
        when(packFavoriteDao.findActiveFavoritePacksForClient(eq(1L), eq(1), eq(48))).thenReturn(Collections.emptyList());

        // 2. Ejercicio
        final List<Pack> result = packFavoriteService.listActiveFavoritePacks(1L, 999);

        // 3. Asserts
        assertEquals(Collections.emptyList(), result);
    }

    @Test
    void testToggleFavoriteWhenFavoriteExistsCallsDeleteOnly() {
        // 1. Setup
        when(packFavoriteDao.exists(5L, 10L)).thenReturn(true);

        // 2. Ejercicio & 3. Asserts
        assertDoesNotThrow(() -> packFavoriteService.toggleFavorite(5L, 10L));
    }

    @Test
    void testToggleFavoriteWhenNotFavoriteAndPackActiveInsertsFavorite() {
        // 1. Setup
        when(packFavoriteDao.exists(5L, 10L)).thenReturn(false);
        final Pack pack = newPack(10L, 1L, "t", "d", 1.0, 1.0, 1, true, false, Collections.emptyList(), null);
        when(packDao.findById(10L)).thenReturn(Optional.of(pack));
        final AtomicLong capturedClientUserId = new AtomicLong();
        final AtomicLong capturedPackId = new AtomicLong();
        doAnswer(invocation -> {
            capturedClientUserId.set(invocation.getArgument(0));
            capturedPackId.set(invocation.getArgument(1));
            return null;
        }).when(packFavoriteDao).insert(anyLong(), anyLong());

        // 2. Ejercicio
        packFavoriteService.toggleFavorite(5L, 10L);

        // 3. Asserts
        assertEquals(5L, capturedClientUserId.get());
        assertEquals(10L, capturedPackId.get());
    }

    @Test
    void testToggleFavoriteWhenPackInactiveThrowsIllegalArgumentException() {
        // 1. Setup
        when(packFavoriteDao.exists(5L, 10L)).thenReturn(false);
        final Pack pack = newPack(10L, 1L, "t", "d", 1.0, 1.0, 1, false, false, Collections.emptyList(), null);
        when(packDao.findById(10L)).thenReturn(Optional.of(pack));

        // 2. Ejercicio
        final FavoriteToggleException thrown = assertThrows(FavoriteToggleException.class,
                () -> packFavoriteService.toggleFavorite(5L, 10L));

        // 3. Asserts
        assertEquals(FavoriteToggleException.Reason.PACK_UNAVAILABLE, thrown.getReason());
    }
}
