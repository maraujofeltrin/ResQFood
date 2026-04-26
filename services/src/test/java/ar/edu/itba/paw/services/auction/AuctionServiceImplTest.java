package ar.edu.itba.paw.services.auction;

import ar.edu.itba.paw.models.auction.Auction;
import ar.edu.itba.paw.models.auction.Bid;
import ar.edu.itba.paw.models.auction.BidFailureReason;
import ar.edu.itba.paw.models.auction.BidPlacementException;
import ar.edu.itba.paw.models.pack.Pack;
import ar.edu.itba.paw.persistence.AuctionDao;
import ar.edu.itba.paw.persistence.BidDao;
import ar.edu.itba.paw.persistence.PackDao;
import ar.edu.itba.paw.services.reservation.ReservationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuctionServiceImplTest {

    @Mock
    private AuctionDao auctionDao;

    @Mock
    private BidDao bidDao;

    @Mock
    private PackDao packDao;

    @Mock
    private ReservationService reservationService;

    @InjectMocks
    private AuctionServiceImpl auctionService;

    private static final long PACK_ID = 1L;
    private static final long AUCTION_ID = 100L;
    private static final long CLIENT_ID = 2L;
    private static final long COMMERCE_ID = 3L;

    @Test
    public void testCreateAuction_Valid() {
        // 1. Setup
        Pack pack = new Pack(PACK_ID, COMMERCE_ID, "Test Pack", "Desc", 100.0, 50.0, 10, true, null);
        when(packDao.findById(PACK_ID)).thenReturn(Optional.of(pack));
        when(auctionDao.findByPackId(PACK_ID)).thenReturn(Optional.empty());
        
        LocalDateTime endTime = LocalDateTime.now(ZoneOffset.UTC).plusDays(1);
        final double minInc = 50.0;
        Auction createdAuction = new Auction(AUCTION_ID, pack, 100.0, minInc, null, null, endTime, Auction.Status.ACTIVE, LocalDateTime.now());
        when(auctionDao.createAuction(PACK_ID, 100.0, minInc, endTime)).thenReturn(createdAuction);

        // 2. Ejercicio
        Auction auction = auctionService.createAuction(PACK_ID, 100.0, minInc, endTime);

        // 3. Asserts
        assertNotNull(auction);
        assertEquals(AUCTION_ID, auction.getId());
        verify(auctionDao).createAuction(PACK_ID, 100.0, minInc, endTime);
    }

    @Test
    public void testCreateAuction_InactivePack() {
        // 1. Setup
        Pack pack = new Pack(PACK_ID, COMMERCE_ID, "Test Pack", "Desc", 100.0, 50.0, 10, false, null); // Inactive
        when(packDao.findById(PACK_ID)).thenReturn(Optional.of(pack));

        // 2. Ejercicio & 3. Asserts
        LocalDateTime endTime = LocalDateTime.now(ZoneOffset.UTC).plusDays(1);
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            auctionService.createAuction(PACK_ID, 100.0, 10.0, endTime);
        });
        
        assertTrue(exception.getMessage().contains("inactive pack"));
        verify(auctionDao, never()).createAuction(anyLong(), anyDouble(), anyDouble(), any(LocalDateTime.class));
    }

    @Test
    public void testPlaceBid_Valid() {
        // 1. Setup
        Pack pack = new Pack(PACK_ID, COMMERCE_ID, "Test Pack", "Desc", 100.0, 50.0, 10, true, null);
        LocalDateTime endTime = LocalDateTime.now(ZoneOffset.UTC).plusHours(1);
        final double minInc = 500.0;
        Auction auction = new Auction(AUCTION_ID, pack, 1000.0, minInc, null, null, endTime, Auction.Status.ACTIVE, LocalDateTime.now());
        
        when(auctionDao.findById(AUCTION_ID)).thenReturn(Optional.of(auction));
        
        Bid createdBid = new Bid(1L, AUCTION_ID, CLIENT_ID, 1600.0, LocalDateTime.now());
        when(bidDao.createBid(AUCTION_ID, CLIENT_ID, 1600.0)).thenReturn(createdBid);

        // 2. Ejercicio
        Bid bid = auctionService.placeBid(AUCTION_ID, CLIENT_ID, 1600.0);

        // 3. Asserts
        assertNotNull(bid);
        assertEquals(1600.0, bid.getAmount());
        verify(bidDao).createBid(AUCTION_ID, CLIENT_ID, 1600.0);
        verify(auctionDao).updateCurrentBid(AUCTION_ID, 1600.0, CLIENT_ID);
    }

    @Test
    public void testPlaceBid_AmountBelowMinimum() {
        // 1. Setup
        Pack pack = new Pack(PACK_ID, COMMERCE_ID, "Test Pack", "Desc", 100.0, 50.0, 10, true, null);
        LocalDateTime endTime = LocalDateTime.now(ZoneOffset.UTC).plusHours(1);
        final double minInc = 500.0;
        Auction auction = new Auction(AUCTION_ID, pack, 1000.0, minInc, null, null, endTime, Auction.Status.ACTIVE, LocalDateTime.now());
        
        when(auctionDao.findById(AUCTION_ID)).thenReturn(Optional.of(auction));

        // 2. Ejercicio & 3. Asserts
        BidPlacementException exception = assertThrows(BidPlacementException.class, () -> {
            auctionService.placeBid(AUCTION_ID, CLIENT_ID, 1200.0); // Minimum is 1500 (1000 + 500)
        });
        
        assertEquals(BidFailureReason.AMOUNT_BELOW_MINIMUM, exception.getReason());
        verify(bidDao, never()).createBid(anyLong(), anyLong(), anyDouble());
    }

    @Test
    public void testPlaceBid_ExpiredAuction() {
        // 1. Setup
        Pack pack = new Pack(PACK_ID, COMMERCE_ID, "Test Pack", "Desc", 100.0, 50.0, 10, true, null);
        LocalDateTime endTime = LocalDateTime.now(ZoneOffset.UTC).minusHours(1); // Expired
        final double minInc = 1.0;
        Auction auction = new Auction(AUCTION_ID, pack, 1000.0, minInc, null, null, endTime, Auction.Status.ACTIVE, LocalDateTime.now());
        
        when(auctionDao.findById(AUCTION_ID)).thenReturn(Optional.of(auction));

        // 2. Ejercicio & 3. Asserts
        BidPlacementException exception = assertThrows(BidPlacementException.class, () -> {
            auctionService.placeBid(AUCTION_ID, CLIENT_ID, 2000.0);
        });
        
        assertEquals(BidFailureReason.EXPIRED, exception.getReason());
    }

    @Test
    public void testPlaceBid_OwnCommerce() {
        // 1. Setup
        Pack pack = new Pack(PACK_ID, COMMERCE_ID, "Test Pack", "Desc", 100.0, 50.0, 10, true, null);
        LocalDateTime endTime = LocalDateTime.now(ZoneOffset.UTC).plusHours(1);
        final double minInc = 1.0;
        Auction auction = new Auction(AUCTION_ID, pack, 1000.0, minInc, null, null, endTime, Auction.Status.ACTIVE, LocalDateTime.now());
        
        when(auctionDao.findById(AUCTION_ID)).thenReturn(Optional.of(auction));

        // 2. Ejercicio & 3. Asserts
        BidPlacementException exception = assertThrows(BidPlacementException.class, () -> {
            auctionService.placeBid(AUCTION_ID, COMMERCE_ID, 2000.0); // Client is the commerce
        });
        
        assertEquals(BidFailureReason.OWN_COMMERCE, exception.getReason());
    }

    @Test
    public void testFindParticipatedAuctionsByClientId_MostRecentAuctionFirstAndDistinct() {
        // 1. Setup
        final long auctionB = 201L;
        final LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        when(bidDao.findByClientId(CLIENT_ID)).thenReturn(List.of(
                new Bid(3L, auctionB, CLIENT_ID, 300.0, now),
                new Bid(2L, AUCTION_ID, CLIENT_ID, 200.0, now.minusMinutes(1)),
                new Bid(1L, AUCTION_ID, CLIENT_ID, 100.0, now.minusHours(1))
        ));
        final Pack packA = new Pack(PACK_ID, COMMERCE_ID, "P1", "D", 1.0, 1.0, 1, true, null);
        final Pack packB = new Pack(2L, COMMERCE_ID, "P2", "D", 1.0, 1.0, 1, true, null);
        final LocalDateTime end = now.plusDays(1);
        final Auction aA = new Auction(AUCTION_ID, packA, 10.0, 1.0, null, null, end, Auction.Status.ACTIVE, now);
        final Auction aB = new Auction(auctionB, packB, 10.0, 1.0, null, null, end, Auction.Status.ACTIVE, now);
        when(auctionDao.findById(AUCTION_ID)).thenReturn(Optional.of(aA));
        when(auctionDao.findById(auctionB)).thenReturn(Optional.of(aB));

        // 2. Ejercicio
        final List<Auction> result = auctionService.findParticipatedAuctionsByClientId(CLIENT_ID);

        // 3. Asserts
        assertEquals(2, result.size());
        assertEquals(auctionB, result.get(0).getId().longValue());
        assertEquals(AUCTION_ID, result.get(1).getId().longValue());
    }
}
