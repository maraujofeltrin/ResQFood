package ar.edu.itba.paw.services.pack;

import ar.edu.itba.paw.models.auction.Auction;
import ar.edu.itba.paw.models.pack.Municipality;
import ar.edu.itba.paw.models.pack.Pack;
import ar.edu.itba.paw.models.user.Commerce;
import ar.edu.itba.paw.models.pack.FavoriteToggleException;
import ar.edu.itba.paw.persistence.PackFavoriteDao;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PackFavoriteServiceImplTest {

    @Mock
    private PackFavoriteDao packFavoriteDao;

    @Mock
    private PackService packService;

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
    void testToggleFavoriteWhenFavoriteExistsRemovesFavorite() {
        // 1. Setup
        final Set<Long> favorites = new HashSet<>();
        favorites.add(10L);
        when(packFavoriteDao.exists(eq(5L), eq(10L))).thenAnswer(invocation -> favorites.contains(10L));
        doAnswer(invocation -> {
            favorites.remove(10L);
            return null;
        }).when(packFavoriteDao).delete(eq(5L), eq(10L));

        // 2. Ejercicio
        packFavoriteService.toggleFavorite(5L, 10L);

        // 3. Asserts
        assertFalse(packFavoriteService.isFavorite(5L, 10L));
    }

    @Test
    void testToggleFavoriteWhenNotFavoriteAndPackActiveAddsFavorite() {
        // 1. Setup
        final Set<Long> favorites = new HashSet<>();
        when(packFavoriteDao.exists(eq(5L), eq(10L))).thenAnswer(invocation -> favorites.contains(10L));
        doAnswer(invocation -> {
            favorites.add(10L);
            return null;
        }).when(packFavoriteDao).insert(eq(5L), eq(10L));
        final Pack pack = newPack(10L, 1L, "t", "d", 1.0, 1.0, 1, true, false, Collections.emptyList(), null);
        when(packService.findById(10L)).thenReturn(Optional.of(pack));

        // 2. Ejercicio
        packFavoriteService.toggleFavorite(5L, 10L);

        // 3. Asserts
        assertTrue(packFavoriteService.isFavorite(5L, 10L));
    }

    @Test
    void testToggleFavoriteWhenPackIsAuctionThrowsFavoriteToggleException() {
        // 1. Setup
        when(packFavoriteDao.exists(5L, 10L)).thenReturn(false);
        final Pack pack = newPack(10L, 1L, "t", "d", 1.0, 1.0, 1, true, false, Collections.emptyList(), null);
        final Auction auction = new Auction(100L, pack, 10.0, 1.0, null, null,
                LocalDateTime.now(ZoneOffset.UTC).plusDays(1), Auction.Status.ACTIVE, LocalDateTime.now());
        pack.setAuction(auction);
        when(packService.findById(10L)).thenReturn(Optional.of(pack));

        // 2. Ejercicio
        final FavoriteToggleException thrown = assertThrows(FavoriteToggleException.class,
                () -> packFavoriteService.toggleFavorite(5L, 10L));

        // 3. Asserts
        assertEquals(FavoriteToggleException.Reason.PACK_UNAVAILABLE, thrown.getReason());
    }

    @Test
    void testToggleFavoriteWhenPackInactiveThrowsIllegalArgumentException() {
        // 1. Setup
        when(packFavoriteDao.exists(5L, 10L)).thenReturn(false);
        final Pack pack = newPack(10L, 1L, "t", "d", 1.0, 1.0, 1, false, false, Collections.emptyList(), null);
        when(packService.findById(10L)).thenReturn(Optional.of(pack));

        // 2. Ejercicio
        final FavoriteToggleException thrown = assertThrows(FavoriteToggleException.class,
                () -> packFavoriteService.toggleFavorite(5L, 10L));

        // 3. Asserts
        assertEquals(FavoriteToggleException.Reason.PACK_UNAVAILABLE, thrown.getReason());
    }

    @Test
    void testToggleFavoriteWhenPackNotFoundThrowsException() {
        // 1. Setup
        when(packFavoriteDao.exists(5L, 10L)).thenReturn(false);
        when(packService.findById(10L)).thenReturn(Optional.empty());

        // 2. Ejercicio
        final FavoriteToggleException thrown = assertThrows(FavoriteToggleException.class,
                () -> packFavoriteService.toggleFavorite(5L, 10L));

        // 3. Asserts
        assertEquals(FavoriteToggleException.Reason.PACK_NOT_FOUND, thrown.getReason());
    }

    @Test
    void testToggleFavoriteWhenPackDeletedThrowsException() {
        // 1. Setup
        when(packFavoriteDao.exists(5L, 10L)).thenReturn(false);
        final Pack pack = newPack(10L, 1L, "t", "d", 1.0, 1.0, 1, true, true, Collections.emptyList(), null);
        when(packService.findById(10L)).thenReturn(Optional.of(pack));

        // 2. Ejercicio
        final FavoriteToggleException thrown = assertThrows(FavoriteToggleException.class,
                () -> packFavoriteService.toggleFavorite(5L, 10L));

        // 3. Asserts
        assertEquals(FavoriteToggleException.Reason.PACK_UNAVAILABLE, thrown.getReason());
    }

    @Test
    void testCountActiveFavoritePacksWhenDaoReturnsCountReturnsValue() {
        // 1. Setup
        when(packFavoriteDao.countActiveFavoritePacksForClient(5L)).thenReturn(7);

        // 2. Ejercicio
        final int count = packFavoriteService.countActiveFavoritePacks(5L);

        // 3. Asserts
        assertEquals(7, count);
    }
}
