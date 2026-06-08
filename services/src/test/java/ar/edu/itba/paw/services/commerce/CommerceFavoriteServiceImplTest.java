package ar.edu.itba.paw.services.commerce;

import ar.edu.itba.paw.models.user.Commerce;
import ar.edu.itba.paw.persistence.CommerceDao;
import ar.edu.itba.paw.persistence.CommerceFavoriteDao;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.eq;
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
    void testToggleFavoriteWhenFavoriteExistsCallsDeleteOnly() {
        // 1. Setup
        when(commerceFavoriteDao.exists(5L, 10L)).thenReturn(true);

        // 2. Ejercicio
        final boolean completed = assertDoesNotThrow(() -> {
            commerceFavoriteService.toggleFavorite(5L, 10L);
            return true;
        });

        // 3. Asserts
        assertEquals(true, completed);
    }

    @Test
    void testToggleFavoriteWhenNotFavoriteAndCommerceExistsInsertsFavorite() {
        // 1. Setup
        when(commerceFavoriteDao.exists(5L, 10L)).thenReturn(false);
        final Commerce commerce = new Commerce(10L, "c", Commerce.Category.BAKERY, "st", 1, null, "p", "1000", "09", "18");
        when(commerceDao.findByUserId(10L)).thenReturn(Optional.of(commerce));

        // 2. Ejercicio
        final boolean completed = assertDoesNotThrow(() -> {
            commerceFavoriteService.toggleFavorite(5L, 10L);
            return true;
        });

        // 3. Asserts
        assertEquals(true, completed);
    }

    @Test
    void testToggleFavoriteWhenCommerceDoesNotExistThrowsIllegalArgumentException() {
        // 1. Setup
        when(commerceFavoriteDao.exists(5L, 10L)).thenReturn(false);
        when(commerceDao.findByUserId(10L)).thenReturn(Optional.empty());

        // 2. Ejercicio
        final IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                () -> commerceFavoriteService.toggleFavorite(5L, 10L));

        // 3. Asserts
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
}
