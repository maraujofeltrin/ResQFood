package ar.edu.itba.paw.services.commerce;

import ar.edu.itba.paw.models.pack.Pack;
import ar.edu.itba.paw.models.pack.PackTag;
import ar.edu.itba.paw.services.auction.AuctionService;
import ar.edu.itba.paw.services.pack.PackService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doAnswer;
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

    @BeforeEach
    void setUp() {
        commerceOfferService = new CommerceOfferServiceImpl(packService, auctionService, businessZone);
    }

    @Test
    void testCreateDirectPackWhenTagsProvidedReturnsPackFromService() {
        // 1. Setup
        final List<PackTag> tags = Collections.singletonList(PackTag.VEGAN);
        final Pack createdPack = new Pack(PACK_ID, COMMERCE_ID, "Direct Pack", "Desc", 1000.0, 500.0, 5, true, tags);
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
        final Pack createdPack = new Pack(PACK_ID, COMMERCE_ID, "Direct Pack", "Desc", 1000.0, 500.0, 5, true,
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
    void testCreateAuctionOfferWhenValidReturnsPackAndPassesUtcEndToAuction() {
        // 1. Setup
        final List<PackTag> tags = Collections.singletonList(PackTag.VEGETARIAN);
        final Pack createdPack = new Pack(PACK_ID, COMMERCE_ID, "Auction Pack", "Desc", 1500.0, 1000.0, 1, true,
                tags);
        when(packService.createPack(COMMERCE_ID, "Auction Pack", "Desc", 1500.0, 1000.0, 1, tags, null)).thenReturn(
                createdPack);

        final String endDate = "2026-12-31";
        final String endTime = "23:59";
        final LocalDate date = LocalDate.parse(endDate);
        final LocalTime time = LocalTime.parse(endTime);
        final LocalDateTime expectedUtc = ZonedDateTime.of(date, time, businessZone)
                .withZoneSameInstant(ZoneOffset.UTC)
                .toLocalDateTime();

        final double minBidInc = 500.0;
        final AtomicReference<Long> capturedPackId = new AtomicReference<>();
        final AtomicReference<Double> capturedInitialPrice = new AtomicReference<>();
        final AtomicReference<Double> capturedMinBidInc = new AtomicReference<>();
        final AtomicReference<LocalDateTime> capturedEndUtc = new AtomicReference<>();
        doAnswer(invocation -> {
            capturedPackId.set(invocation.getArgument(0));
            capturedInitialPrice.set(invocation.getArgument(1));
            capturedMinBidInc.set(invocation.getArgument(2));
            capturedEndUtc.set(invocation.getArgument(3));
            return null;
        }).when(auctionService).createAuction(anyLong(), anyDouble(), anyDouble(), any(LocalDateTime.class));

        // 2. Ejercicio
        final Pack pack = commerceOfferService.createAuctionOffer(COMMERCE_ID, "Auction Pack", "Desc", 1500.0, 1000.0,
                minBidInc, endDate, endTime, tags, null);

        // 3. Asserts
        assertNotNull(pack);
        assertEquals(PACK_ID, pack.getId());
        assertEquals(PACK_ID, capturedPackId.get().longValue());
        assertEquals(1000.0, capturedInitialPrice.get(), 0.0001);
        assertEquals(minBidInc, capturedMinBidInc.get(), 0.0001);
        assertEquals(expectedUtc, capturedEndUtc.get());
    }
}
