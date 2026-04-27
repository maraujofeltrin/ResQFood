package ar.edu.itba.paw.services.commerce;

import ar.edu.itba.paw.models.user.Commerce;
import ar.edu.itba.paw.persistence.CommerceDao;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommerceServiceImplTest {

    @Mock
    private CommerceDao commerceDao;

    @InjectMocks
    private CommerceServiceImpl commerceService;

    @Test
    void updateProfileFields_keepsCommercialName_and_updatesRest() {
        // 1. Setup
        when(commerceDao.findByUserId(5L)).thenReturn(Optional.of(
                new Commerce(5L, "Panadería Sur", Commerce.Category.BAKERY, "Old", 1, "Lanús", "BA", "1824",
                        "08:00", "18:00")));

        // 2. Ejercicio
        commerceService.updateProfileFields(5L, Commerce.Category.RESTAURANT, "Nueva", 99, "Quilmes", "BA", "1878",
                "10:00", "22:00");

        // 3. Asserts
        final ArgumentCaptor<Commerce> captor = ArgumentCaptor.forClass(Commerce.class);
        verify(commerceDao).update(captor.capture());
        final Commerce saved = captor.getValue();
        assertEquals("Panadería Sur", saved.getCommercialName());
        assertEquals(Commerce.Category.RESTAURANT, saved.getCategory());
        assertEquals("Nueva", saved.getStreet());
        assertEquals(Integer.valueOf(99), saved.getStreetNumber());
        assertEquals("Quilmes", saved.getCity());
        assertEquals("10:00", saved.getOpeningTime());
        assertEquals("22:00", saved.getClosingTime());
    }

    @Test
    void updateProfileFields_missingCommerce_throws() {
        // 1. Setup
        when(commerceDao.findByUserId(1L)).thenReturn(Optional.empty());

        // 2. Ejercicio / 3. Asserts
        assertThrows(NoSuchElementException.class,
                () -> commerceService.updateProfileFields(1L, Commerce.Category.OTHER, "S", null, "C", null, null, "09:00",
                        "17:00"));
    }

    @Test
    void updateProfileFields_nullCategory_throws() {
        // 1. Setup — sin stub: no debe consultar DAO si falla validación temprana

        // 2. Ejercicio / 3. Asserts
        assertThrows(IllegalArgumentException.class,
                () -> commerceService.updateProfileFields(1L, null, "S", 1, "C", null, null, "09:00", "17:00"));
    }
}
