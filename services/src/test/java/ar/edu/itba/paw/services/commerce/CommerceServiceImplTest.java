package ar.edu.itba.paw.services.commerce;

import ar.edu.itba.paw.models.user.Commerce;
import ar.edu.itba.paw.models.pack.Municipality;
import ar.edu.itba.paw.persistence.CommerceDao;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommerceServiceImplTest {

    @Mock
    private CommerceDao commerceDao;

    @InjectMocks
    private CommerceServiceImpl commerceService;

    @Test
    void testUpdateProfileFieldsWhenCommerceExistsKeepsCommercialNameAndUpdatesRest() {
        // 1. Setup
        when(commerceDao.findByUserId(5L)).thenReturn(Optional.of(
                new Commerce(5L, "Panadería Sur", Commerce.Category.BAKERY, "Old", 1, Municipality.AVELLANEDA, "Buenos Aires", "1824",
                        "08:00", "18:00")));
        final AtomicReference<Commerce> captured = new AtomicReference<>();
        doAnswer(invocation -> {
            captured.set(invocation.getArgument(0));
            return invocation.getArgument(0);
        }).when(commerceDao).update(any(Commerce.class));

        // 2. Ejercicio
        commerceService.updateProfileFields(5L, Commerce.Category.RESTAURANT, "Nueva", 99, Municipality.QUILMES, "Buenos Aires", "1878",
                "10:00", "22:00");

        // 3. Asserts
        final Commerce saved = captured.get();
        assertEquals("Panadería Sur", saved.getCommercialName());
        assertEquals(Commerce.Category.RESTAURANT, saved.getCategory());
        assertEquals("Nueva", saved.getStreet());
        assertEquals(Integer.valueOf(99), saved.getStreetNumber());
        assertEquals(Municipality.QUILMES, saved.getCity());
        assertEquals("10:00", saved.getOpeningTime());
        assertEquals("22:00", saved.getClosingTime());
    }

    @Test
    void testUpdateProfileFieldsWhenCommerceMissingThrowsNoSuchElementException() {
        // 1. Setup
        when(commerceDao.findByUserId(1L)).thenReturn(Optional.empty());

        // 2. Ejercicio
        final NoSuchElementException thrown = assertThrows(NoSuchElementException.class,
                () -> commerceService.updateProfileFields(1L, Commerce.Category.OTHER, "S", null, Municipality.AVELLANEDA, "Buenos Aires", null, "09:00",
                        "17:00"));

        // 3. Asserts
        assertTrue(thrown.getMessage().contains("1"));
    }

    @Test
    void testUpdateProfileFieldsWhenCategoryNullThrowsIllegalArgumentException() {
        // 1. Setup

        // 2. Ejercicio
        final IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                () -> commerceService.updateProfileFields(1L, null, "S", 1, Municipality.AVELLANEDA, "Buenos Aires", null, "09:00", "17:00"));

        // 3. Asserts
        assertEquals("Category is required", thrown.getMessage());
    }
}
