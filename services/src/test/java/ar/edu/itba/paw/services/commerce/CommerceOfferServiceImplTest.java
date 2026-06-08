package ar.edu.itba.paw.services.commerce;

import ar.edu.itba.paw.models.auction.Auction;
import ar.edu.itba.paw.models.auction.AuctionCreationException;
import ar.edu.itba.paw.models.pack.Municipality;
import ar.edu.itba.paw.models.pack.Pack;
import ar.edu.itba.paw.models.pack.PackTag;
import ar.edu.itba.paw.models.user.Commerce;
import ar.edu.itba.paw.services.auction.AuctionService;
import ar.edu.itba.paw.services.pack.PackService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommerceOfferServiceImplTest {

    @Mock
    private PackService packService;

    @Mock
    private AuctionService auctionService;

    private final ZoneId businessZone = ZoneId.of("America/Argentina/Buenos_Aires");

    private CommerceOfferServiceImpl commerceOfferService;

    private static final long COMMERCE_ID = 1L;
    private static final long PACK_ID = 10L;

    private static Commerce commerceRef(final long userId) {
        return new Commerce(userId, "Comm", Commerce.Category.BAKERY, "St", 1, Municipality.AVELLANEDA, "P", "1000",
                "08:00", "20:00");
    }

    private static Pack newPack(final long id, final long commerceId, final String title, final String desc,
            final double originalPrice, final double finalPrice, final int stock, final boolean active,
            final List<PackTag> tags) {
        return new Pack(id, commerceRef(commerceId), title, desc, originalPrice, finalPrice, stock, active, tags);
    }

    @BeforeEach
    void setUp() {
        commerceOfferService = new CommerceOfferServiceImpl(packService, auctionService, businessZone);
    }

    @Test
    void testCreateDirectPackWhenTagsProvidedReturnsPackFromService() {
        // 1. Setup
        final List<PackTag> tags = Collections.singletonList(PackTag.VEGAN);
        final Pack createdPack = newPack(PACK_ID, COMMERCE_ID, "Direct Pack", "Desc", 1000.0, 500.0, 5, true, tags);
        when(packService.createPack(COMMERCE_ID, "Direct Pack", "Desc", 1000.0, 500.0, 5, tags, null)).thenReturn(
                createdPack);

        // 2. Ejercicio
        final Pack pack = commerceOfferService.createDirectPack(COMMERCE_ID, "Direct Pack", "Desc", 1000.0, 500.0, 5,
                tags, null);

        // 3. Asserts
        assertNotNull(pack);
        assertEquals(PACK_ID, pack.getId());
    }

    @Test
    void testCreateDirectPackWhenTagsNullUsesEmptyListAndReturnsPack() {
        // 1. Setup
        final Pack createdPack = newPack(PACK_ID, COMMERCE_ID, "Direct Pack", "Desc", 1000.0, 500.0, 5, true,
                Collections.emptyList());
        when(packService.createPack(COMMERCE_ID, "Direct Pack", "Desc", 1000.0, 500.0, 5, Collections.emptyList(),
                null)).thenReturn(createdPack);

        // 2. Ejercicio
        final Pack pack = commerceOfferService.createDirectPack(COMMERCE_ID, "Direct Pack", "Desc", 1000.0, 500.0, 5,
                null, null);

        // 3. Asserts
        assertNotNull(pack);
        assertEquals(PACK_ID, pack.getId());
    }

    @Test
    void testCreateAuctionOfferWhenValidReturnsCreatedPack() {
        // 1. Setup
        final List<PackTag> tags = Collections.singletonList(PackTag.VEGETARIAN);
        final Pack createdPack = newPack(PACK_ID, COMMERCE_ID, "Auction Pack", "Desc", 1500.0, 1000.0, 1, true,
                tags);
        when(packService.createPack(COMMERCE_ID, "Auction Pack", "Desc", 1500.0, 1000.0, 1, tags, null)).thenReturn(
                createdPack);

        final String endDate = "2026-12-31";
        final String endTime = "23:59";
        final double minBidInc = 500.0;
        when(auctionService.createAuction(anyLong(), anyDouble(), anyDouble(), any(LocalDateTime.class)))
                .thenReturn(new Auction(1L, createdPack, 1000.0, minBidInc, null, null,
                        LocalDateTime.now(), Auction.Status.ACTIVE, LocalDateTime.now()));

        // 2. Ejercicio
        final Pack pack = commerceOfferService.createAuctionOffer(COMMERCE_ID, "Auction Pack", "Desc", 1500.0, 1000.0,
                minBidInc, endDate, endTime, tags, null);

        // 3. Asserts
        assertNotNull(pack);
        assertEquals(PACK_ID, pack.getId());
    }

    @Test
    void testCreateAuctionOfferWhenEndDateInvalidThrowsAuctionCreationException() {
        // 1. Setup
        final Pack createdPack = newPack(PACK_ID, COMMERCE_ID, "Auction Pack", "Desc", 1500.0, 1000.0, 1, true,
                Collections.emptyList());
        when(packService.createPack(COMMERCE_ID, "Auction Pack", "Desc", 1500.0, 1000.0, 1,
                Collections.emptyList(), null)).thenReturn(createdPack);

        // 2. Ejercicio
        final AuctionCreationException exception = assertThrows(AuctionCreationException.class,
                () -> commerceOfferService.createAuctionOffer(COMMERCE_ID, "Auction Pack", "Desc", 1500.0, 1000.0,
                        500.0, "not-a-date", "23:59", Collections.emptyList(), null));

        // 3. Asserts
        assertEquals(AuctionCreationException.Reason.INVALID_END_DATE, exception.getReason());
    }
}
