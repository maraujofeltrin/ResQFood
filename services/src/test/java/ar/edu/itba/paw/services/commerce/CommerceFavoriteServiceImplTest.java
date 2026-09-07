package ar.edu.itba.paw.services.commerce;

import ar.edu.itba.paw.models.user.Commerce;
import ar.edu.itba.paw.models.user.CommerceFavoriteToggleException;
import ar.edu.itba.paw.models.user.User;
import ar.edu.itba.paw.persistence.CommerceDao;
import ar.edu.itba.paw.persistence.CommerceFavoriteDao;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
class CommerceFavoriteServiceImplTest {

    @Mock
    private CommerceFavoriteDao commerceFavoriteDao;

    @Mock
    private CommerceDao commerceDao;

    @InjectMocks
    private CommerceFavoriteServiceImpl commerceFavoriteService;

    @Test
    void testListFavoriteCommercesWhenLimitExceedsMaxUsesClampedPageSize() {
        // 1. Setup
        when(commerceFavoriteDao.findFavoriteCommercesForClient(eq(1L), eq(1), eq(48))).thenReturn(Collections.emptyList());

        // 2. Ejercicio
        final List<Commerce> result = commerceFavoriteService.listFavoriteCommerces(1L, 1, 999);

        // 3. Asserts
        assertEquals(Collections.emptyList(), result);
    }

    @Test
    void testToggleFavoriteWhenFavoriteExistsRemovesFavorite() {
        // 1. Setup
        final Set<Long> favorites = new HashSet<>();
        favorites.add(10L);
        when(commerceFavoriteDao.exists(eq(5L), eq(10L))).thenAnswer(invocation -> favorites.contains(10L));
        doAnswer(invocation -> {
            favorites.remove(10L);
            return null;
        }).when(commerceFavoriteDao).delete(eq(5L), eq(10L));

        // 2. Ejercicio
        commerceFavoriteService.toggleFavorite(5L, 10L);

        // 3. Asserts
        assertFalse(commerceFavoriteService.isFavorite(5L, 10L));
    }

    @Test
    void testToggleFavoriteWhenNotFavoriteAndCommerceExistsAddsFavorite() {
        // 1. Setup
        final Set<Long> favorites = new HashSet<>();
        when(commerceFavoriteDao.exists(eq(5L), eq(10L))).thenAnswer(invocation -> favorites.contains(10L));
        doAnswer(invocation -> {
            favorites.add(10L);
            return null;
        }).when(commerceFavoriteDao).insert(eq(5L), eq(10L));
        final Commerce commerce = new Commerce(10L, "c", Commerce.Category.BAKERY, "st", 1, null, "p", "1000", "09", "18");
        when(commerceDao.findByUserId(10L)).thenReturn(Optional.of(commerce));

        // 2. Ejercicio
        commerceFavoriteService.toggleFavorite(5L, 10L);

        // 3. Asserts
        assertTrue(commerceFavoriteService.isFavorite(5L, 10L));
    }

    @Test
    void testToggleFavoriteWhenCommerceDoesNotExistThrowsCommerceFavoriteToggleException() {
        // 1. Setup
        when(commerceFavoriteDao.exists(5L, 10L)).thenReturn(false);
        when(commerceDao.findByUserId(10L)).thenReturn(Optional.empty());

        // 2. Ejercicio
        final CommerceFavoriteToggleException thrown = assertThrows(CommerceFavoriteToggleException.class,
                () -> commerceFavoriteService.toggleFavorite(5L, 10L));

        // 3. Asserts
        assertEquals(CommerceFavoriteToggleException.Reason.COMMERCE_NOT_FOUND, thrown.getReason());
        assertEquals("Commerce not found: 10", thrown.getMessage());
    }

    @Test
    void testCountFavoriteCommercesForClientWhenDaoReturnsCountReturnsValue() {
        // 1. Setup
        when(commerceFavoriteDao.countFavoriteCommercesForClient(1L)).thenReturn(5);

        // 2. Ejercicio
        final int count = commerceFavoriteService.countFavoriteCommerces(1L);

        // 3. Asserts
        assertEquals(5, count);
    }

    @Test
    void testFindFavoritingClientsDelegatesToDao() {
        // 1. Setup
        final User client = new User(5L, "c@test.com", "p", "C", null, User.Role.CLIENT, true);
        when(commerceFavoriteDao.findFavoritingClientsByCommerce(10L)).thenReturn(List.of(client));

        // 2. Ejercicio
        final List<User> result = commerceFavoriteService.findFavoritingClients(10L);

        // 3. Asserts
        assertEquals(1, result.size());
        assertEquals(5L, result.get(0).getId());
    }
}
