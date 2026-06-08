package ar.edu.itba.paw.services.commerce;

import ar.edu.itba.paw.models.pack.Pack;
import ar.edu.itba.paw.models.user.Commerce;
import ar.edu.itba.paw.models.user.CommerceProfileException;
import ar.edu.itba.paw.models.pack.Municipality;
import ar.edu.itba.paw.persistence.CommerceDao;
import ar.edu.itba.paw.services.pack.PackService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
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

    @Mock
    private PackService packService;

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
    void testUpdateProfileFieldsWhenCategoryNullThrowsCommerceProfileException() {
        // 1. Setup

        // 2. Ejercicio
        final CommerceProfileException thrown = assertThrows(CommerceProfileException.class,
                () -> commerceService.updateProfileFields(1L, null, "S", 1, Municipality.AVELLANEDA, "Buenos Aires", null, "09:00", "17:00"));

        // 3. Asserts
        assertEquals(CommerceProfileException.Reason.MISSING_CATEGORY, thrown.getReason());
    }

    @Test
    void testFilterCommercesUsesDefaultPageAndSizeWhenInvalid() {
        // 1. Setup
        final List<Commerce> expected = List.of(commerceForUserId(1L));
        when(commerceDao.filterCommerces("query", "city", Commerce.Category.BAKERY, 1, 12)).thenReturn(expected);

        // 2. Ejercicio
        final List<Commerce> result = commerceService.filterCommerces("query", "city", Commerce.Category.BAKERY, -5, 0);

        // 3. Asserts
        assertEquals(expected, result);
    }

    private static Commerce commerceForUserId(final long commerceUserId) {
        return new Commerce(commerceUserId, "Comm", Commerce.Category.BAKERY, "St", 1,
                Municipality.AVELLANEDA, "P", "1000", "08:00", "20:00");
    }

    private static Pack packForCommerce(final Long id, final Commerce commerce, final String title) {
        return new Pack(id, commerce, title, "d", 10.0, 8.0, 1, true, false, Collections.emptyList(), null);
    }

    @Test
    void testGetPublicOffersDelegatesToPackService() {
        // 1. Setup
        final long commerceUserId = 7L;
        final Pack pack = packForCommerce(10L, commerceForUserId(commerceUserId), "P");
        when(packService.getPublicOffersByCommerce(commerceUserId, 1, 12)).thenReturn(List.of(pack));

        // 2. Ejercicio
        final List<Pack> offers = commerceService.getPublicOffers(commerceUserId, 1, 12);

        // 3. Asserts
        assertEquals(1, offers.size());
        assertEquals(Long.valueOf(10L), offers.get(0).getId());
    }

    @Test
    void testCountPublicOffersDelegatesToPackService() {
        // 1. Setup
        when(packService.countPublicOffersByCommerce(7L)).thenReturn(5);

        // 2. Ejercicio
        final int count = commerceService.countPublicOffers(7L);

        // 3. Asserts
        assertEquals(5, count);
    }

    @Test
    void testCountFilteredCommercesReturnsDaoCount() {
        // 1. Setup
        when(commerceDao.countFilteredCommerces("query", "city", null)).thenReturn(10);

        // 2. Ejercicio
        int count = commerceService.countFilteredCommerces("query", "city", null);

        // 3. Asserts
        assertEquals(10, count);
    }
}
