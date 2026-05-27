package ar.edu.itba.paw.services.commerce;

import ar.edu.itba.paw.models.auction.Auction;
import ar.edu.itba.paw.models.auction.AuctionSortOption;
import ar.edu.itba.paw.models.pack.Pack;
import ar.edu.itba.paw.models.pack.PackSortOption;
import ar.edu.itba.paw.models.user.Commerce;
import ar.edu.itba.paw.models.pack.Municipality;
import ar.edu.itba.paw.persistence.CommerceDao;
import ar.edu.itba.paw.services.auction.AuctionService;
import ar.edu.itba.paw.services.pack.PackService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommerceServiceImplTest {

    @Mock
    private CommerceDao commerceDao;

    @Mock
    private PackService packService;

    @Mock
    private AuctionService auctionService;

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

    @Test
    void testFilterCommercesUsesDefaultPageAndSizeWhenInvalid() {
        // 1. Setup
        when(commerceDao.filterCommerces("query", "city", Commerce.Category.BAKERY, 1, 12)).thenReturn(java.util.Collections.emptyList());

        // 2. Ejercicio
        commerceService.filterCommerces("query", "city", Commerce.Category.BAKERY, -5, 0);

        // 3. Asserts
        org.mockito.Mockito.verify(commerceDao).filterCommerces("query", "city", Commerce.Category.BAKERY, 1, 12);
    }

    @Test
    void testGetPublicOffersWhenCommerceExistsDelegatesToPackAndAuctionServices() {
        // 1. Setup
        final long commerceUserId = 7L;
        final Pack directPack = new Pack(10L, commerceUserId, "Pack", "d", 10.0, 8.0, 2, true, false, null, null);
        final Pack auctionPack = new Pack(20L, commerceUserId, "AuctionPack", "d", 10.0, 8.0, 1, true, false, null, null);
        final Auction auction = new Auction(2L, auctionPack, 10.0, 1.0, null, null,
                LocalDateTime.now().plusDays(1), Auction.Status.ACTIVE, LocalDateTime.now());
        when(packService.countFilteredPacks(isNull(), isNull(), isNull(), isNull(), eq(true), eq(commerceUserId)))
                .thenReturn(1);
        when(auctionService.countFilteredAuctions(isNull(), isNull(), isNull(), isNull(), eq(true),
                eq(commerceUserId))).thenReturn(1);
        when(packService.filterPacks(isNull(), isNull(), isNull(), isNull(), eq(PackSortOption.DATE_DESC), eq(1), eq(1),
                eq(true), eq(commerceUserId))).thenReturn(List.of(directPack));
        when(auctionService.filterAuctions(isNull(), isNull(), isNull(), isNull(), any(), eq(1), eq(1),
                eq(true), eq(commerceUserId))).thenReturn(List.of(auction));

        // 2. Ejercicio
        final CommercePublicOffers offers = commerceService.getPublicOffers(commerceUserId, 1, 12);

        // 3. Asserts
        assertEquals(2, offers.getTotalOffers());
        assertEquals(2, offers.getItems().size());
        verify(packService).filterPacks(isNull(), isNull(), isNull(), isNull(), eq(PackSortOption.DATE_DESC), eq(1),
                eq(1), eq(true), eq(commerceUserId));
        verify(auctionService).filterAuctions(isNull(), isNull(), isNull(), isNull(), any(), eq(1), eq(1),
                eq(true), eq(commerceUserId));
    }

    @Test
    void testGetPublicOffersMergesAuctionsAndDirectPacksSortedByPackIdDesc() {
        // 1. Setup
        final long commerceUserId = 7L;
        final Pack olderDirect = new Pack(10L, commerceUserId, "Direct", "d", 10.0, 8.0, 2, true, false, null, null);
        final Pack auctionPack = new Pack(20L, commerceUserId, "AuctionPack", "d", 10.0, 8.0, 1, true, false, null, null);
        final Auction auction = new Auction(2L, auctionPack, 10.0, 1.0, null, null,
                LocalDateTime.now().plusDays(1), Auction.Status.ACTIVE, LocalDateTime.now());

        when(packService.countFilteredPacks(isNull(), isNull(), isNull(), isNull(), eq(true), eq(commerceUserId))).thenReturn(1);
        when(auctionService.countFilteredAuctions(isNull(), isNull(), isNull(), isNull(), eq(true), eq(commerceUserId))).thenReturn(1);
        when(packService.filterPacks(isNull(), isNull(), isNull(), isNull(), eq(PackSortOption.DATE_DESC), eq(1), eq(1),
                eq(true), eq(commerceUserId))).thenReturn(List.of(olderDirect));
        when(auctionService.filterAuctions(isNull(), isNull(), isNull(), isNull(), any(), eq(1), eq(1),
                eq(true), eq(commerceUserId))).thenReturn(List.of(auction));

        // 2. Ejercicio
        final CommercePublicOffers offers = commerceService.getPublicOffers(commerceUserId, 1, 12);

        // 3. Asserts
        assertEquals(2, offers.getTotalOffers());
        assertEquals(2, offers.getItems().size());
        assertNotNull(offers.getItems().get(0).getAuction());
        assertEquals(Long.valueOf(20L), offers.getItems().get(0).getPack().getId());
        assertNull(offers.getItems().get(1).getAuction());
        assertEquals(Long.valueOf(10L), offers.getItems().get(1).getPack().getId());
    }

    @Test
    void testGetPublicOffersPaginatesUnifiedList() {
        // 1. Setup
        final long commerceUserId = 7L;
        when(packService.countFilteredPacks(isNull(), isNull(), isNull(), isNull(), eq(true), eq(commerceUserId))).thenReturn(3);
        when(auctionService.countFilteredAuctions(isNull(), isNull(), isNull(), isNull(), eq(true), eq(commerceUserId))).thenReturn(0);
        when(packService.filterPacks(isNull(), isNull(), isNull(), isNull(), eq(PackSortOption.DATE_DESC), eq(1), eq(3),
                eq(true), eq(commerceUserId))).thenReturn(List.of(
                new Pack(30L, commerceUserId, "C", "d", 10.0, 8.0, 1, true, false, null, null),
                new Pack(20L, commerceUserId, "B", "d", 10.0, 8.0, 1, true, false, null, null),
                new Pack(10L, commerceUserId, "A", "d", 10.0, 8.0, 1, true, false, null, null)));

        // 2. Ejercicio
        final CommercePublicOffers pageOne = commerceService.getPublicOffers(commerceUserId, 1, 2);

        // 3. Asserts
        assertEquals(3, pageOne.getTotalOffers());
        assertEquals(2, pageOne.getItems().size());
        assertEquals(Long.valueOf(30L), pageOne.getItems().get(0).getPack().getId());
        assertEquals(Long.valueOf(20L), pageOne.getItems().get(1).getPack().getId());
    }

    @Test
    void testGetPublicOffersWhenPageOutOfRangeClampsBeforeSlice() {
        // 1. Setup
        final long commerceUserId = 7L;
        when(packService.countFilteredPacks(isNull(), isNull(), isNull(), isNull(), eq(true), eq(commerceUserId)))
                .thenReturn(1);
        when(packService.filterPacks(isNull(), isNull(), isNull(), isNull(), eq(PackSortOption.DATE_DESC), eq(1), eq(1),
                eq(true), eq(commerceUserId))).thenReturn(List.of(
                new Pack(10L, commerceUserId, "A", "d", 10.0, 8.0, 1, true, false, null, null)));
        when(auctionService.countFilteredAuctions(isNull(), isNull(), isNull(), isNull(), eq(true),
                eq(commerceUserId))).thenReturn(0);

        // 2. Ejercicio
        final CommercePublicOffers offers = commerceService.getPublicOffers(commerceUserId, 99, 12);

        // 3. Asserts
        assertEquals(1, offers.getTotalOffers());
        assertEquals(1, offers.getItems().size());
        verify(packService).filterPacks(isNull(), isNull(), isNull(), isNull(), eq(PackSortOption.DATE_DESC), eq(1),
                eq(1), eq(true), eq(commerceUserId));
    }

    @Test
    void testCountFilteredCommercesDelegatesToDao() {
        // 1. Setup
        when(commerceDao.countFilteredCommerces("query", "city", null)).thenReturn(10);

        // 2. Ejercicio
        int count = commerceService.countFilteredCommerces("query", "city", null);

        // 3. Asserts
        assertEquals(10, count);
        org.mockito.Mockito.verify(commerceDao).countFilteredCommerces("query", "city", null);
    }
}
