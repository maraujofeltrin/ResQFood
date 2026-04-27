package ar.edu.itba.paw.services.commerce;

import ar.edu.itba.paw.models.pack.Pack;
import ar.edu.itba.paw.models.pack.PackTag;
import ar.edu.itba.paw.services.auction.AuctionService;
import ar.edu.itba.paw.services.pack.PackService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CommerceOfferServiceImplTest {

    @Mock
    private PackService packService;

    @Mock
    private AuctionService auctionService;

    private ZoneId businessZone = ZoneId.of("America/Argentina/Buenos_Aires");

    private CommerceOfferServiceImpl commerceOfferService;

    private static final long COMMERCE_ID = 1L;
    private static final long PACK_ID = 10L;

    @BeforeEach
    public void setUp() {
        commerceOfferService = new CommerceOfferServiceImpl(packService, auctionService, businessZone);
    }

    @Test
    public void testCreateDirectPack() {
        // 1. Setup
        List<PackTag> tags = Collections.singletonList(PackTag.VEGAN);
        Pack createdPack = new Pack(PACK_ID, COMMERCE_ID, "Direct Pack", "Desc", 1000.0, 500.0, 5, true, tags);
        
        when(packService.createPack(COMMERCE_ID, "Direct Pack", "Desc", 1000.0, 500.0, 5, tags, null))
                .thenReturn(createdPack);

        // 2. Ejercicio
        Pack pack = commerceOfferService.createDirectPack(COMMERCE_ID, "Direct Pack", "Desc", 1000.0, 500.0, 5, tags, null);

        // 3. Asserts
        assertNotNull(pack);
        assertEquals(PACK_ID, pack.getId());
        verify(packService).createPack(COMMERCE_ID, "Direct Pack", "Desc", 1000.0, 500.0, 5, tags, null);
        verifyNoInteractions(auctionService);
    }

    @Test
    public void testCreateDirectPack_NullTags() {
        // 1. Setup
        Pack createdPack = new Pack(PACK_ID, COMMERCE_ID, "Direct Pack", "Desc", 1000.0, 500.0, 5, true, Collections.emptyList());
        
        when(packService.createPack(COMMERCE_ID, "Direct Pack", "Desc", 1000.0, 500.0, 5, Collections.emptyList(), null))
                .thenReturn(createdPack);

        // 2. Ejercicio
        Pack pack = commerceOfferService.createDirectPack(COMMERCE_ID, "Direct Pack", "Desc", 1000.0, 500.0, 5, null, null);

        // 3. Asserts
        assertNotNull(pack);
        assertEquals(PACK_ID, pack.getId());
        verify(packService).createPack(COMMERCE_ID, "Direct Pack", "Desc", 1000.0, 500.0, 5, Collections.emptyList(), null);
    }

    @Test
    public void testCreateAuctionOffer() {
        // 1. Setup
        List<PackTag> tags = Collections.singletonList(PackTag.VEGETARIAN);
        Pack createdPack = new Pack(PACK_ID, COMMERCE_ID, "Auction Pack", "Desc", 1500.0, 1000.0, 1, true, tags);
        
        // Stock must be 1 and final price is initialPrice (1000.0)
        when(packService.createPack(COMMERCE_ID, "Auction Pack", "Desc", 1500.0, 1000.0, 1, tags, null))
                .thenReturn(createdPack);

        String endDate = "2026-12-31";
        String endTime = "23:59";
        
        // Expected UTC parsing
        LocalDate date = LocalDate.parse(endDate);
        LocalTime time = LocalTime.parse(endTime);
        LocalDateTime expectedUtc = ZonedDateTime.of(date, time, businessZone)
                .withZoneSameInstant(ZoneOffset.UTC)
                .toLocalDateTime();

        // 2. Ejercicio
        final double minBidInc = 500.0;
        Pack pack = commerceOfferService.createAuctionOffer(COMMERCE_ID, "Auction Pack", "Desc", 1500.0, 1000.0, minBidInc, endDate, endTime, tags, null);

        // 3. Asserts
        assertNotNull(pack);
        assertEquals(PACK_ID, pack.getId());
        verify(packService).createPack(COMMERCE_ID, "Auction Pack", "Desc", 1500.0, 1000.0, 1, tags, null);
        verify(auctionService).createAuction(PACK_ID, 1000.0, minBidInc, expectedUtc);
    }
}
