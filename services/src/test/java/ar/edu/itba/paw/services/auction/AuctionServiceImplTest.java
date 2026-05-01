package ar.edu.itba.paw.services.auction;

import ar.edu.itba.paw.models.auction.Auction;
import ar.edu.itba.paw.models.auction.Bid;
import ar.edu.itba.paw.models.auction.BidFailureReason;
import ar.edu.itba.paw.models.auction.BidPlacementException;
import ar.edu.itba.paw.models.pack.Pack;
import ar.edu.itba.paw.persistence.AuctionDao;
import ar.edu.itba.paw.persistence.BidDao;
import ar.edu.itba.paw.persistence.PackDao;
import ar.edu.itba.paw.services.commerce.CommerceService;
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
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuctionServiceImplTest {

    @Mock
    private AuctionDao auctionDao;

    @Mock
    private BidDao bidDao;

    @Mock
    private PackDao packDao;

    @Mock
    private ReservationService reservationService;

    @Mock
    private CommerceService commerceService;

    @InjectMocks
    private AuctionServiceImpl auctionService;

    private static final long PACK_ID = 1L;
    private static final long AUCTION_ID = 100L;
    private static final long CLIENT_ID = 2L;
    private static final long COMMERCE_ID = 3L;

    @Test
    void testCreateAuctionWhenPackValidReturnsCreatedAuction() {
        // 1. Setup
        final Pack pack = new Pack(PACK_ID, COMMERCE_ID, "Test Pack", "Desc", 100.0, 50.0, 10, true, null);
        when(packDao.findById(PACK_ID)).thenReturn(Optional.of(pack));
        when(auctionDao.findByPackId(PACK_ID)).thenReturn(Optional.empty());
        final LocalDateTime endTime = LocalDateTime.now(ZoneOffset.UTC).plusDays(1);
        final double minInc = 50.0;
        final Auction createdAuction = new Auction(AUCTION_ID, pack, 100.0, minInc, null, null, endTime,
                Auction.Status.ACTIVE, LocalDateTime.now());
        when(auctionDao.createAuction(PACK_ID, 100.0, minInc, endTime)).thenReturn(createdAuction);

        // 2. Ejercicio
        final Auction auction = auctionService.createAuction(PACK_ID, 100.0, minInc, endTime);

        // 3. Asserts
        assertNotNull(auction);
        assertEquals(AUCTION_ID, auction.getId());
        assertEquals(endTime, auction.getEndTime());
    }

    @Test
    void testCreateAuctionWhenPackInactiveThrowsIllegalArgumentException() {
        // 1. Setup
        final Pack pack = new Pack(PACK_ID, COMMERCE_ID, "Test Pack", "Desc", 100.0, 50.0, 10, false, null);
        when(packDao.findById(PACK_ID)).thenReturn(Optional.of(pack));
        lenient().doThrow(new AssertionError("createAuction no debe invocarse")).when(auctionDao)
                .createAuction(anyLong(), anyDouble(), anyDouble(), any(LocalDateTime.class));

        // 2. Ejercicio
        final LocalDateTime endTime = LocalDateTime.now(ZoneOffset.UTC).plusDays(1);
        final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> auctionService.createAuction(PACK_ID, 100.0, 10.0, endTime));

        // 3. Asserts
        assertTrue(exception.getMessage().contains("inactive pack"));
    }

    @Test
    void testPlaceBidWhenAmountValidReturnsBidAndUpdatesAuctionCurrentBid() {
        // 1. Setup
        final Pack pack = new Pack(PACK_ID, COMMERCE_ID, "Test Pack", "Desc", 100.0, 50.0, 10, true, null);
        final LocalDateTime endTime = LocalDateTime.now(ZoneOffset.UTC).plusHours(1);
        final double minInc = 500.0;
        final Auction auction = new Auction(AUCTION_ID, pack, 1000.0, minInc, null, null, endTime,
                Auction.Status.ACTIVE, LocalDateTime.now());
        when(auctionDao.findById(AUCTION_ID)).thenReturn(Optional.of(auction));
        final Bid createdBid = new Bid(1L, AUCTION_ID, CLIENT_ID, 1600.0, LocalDateTime.now());
        when(bidDao.createBid(AUCTION_ID, CLIENT_ID, 1600.0)).thenReturn(createdBid);
        final AtomicReference<Double> capturedAmount = new AtomicReference<>();
        final AtomicReference<Long> capturedBidder = new AtomicReference<>();
        doAnswer(invocation -> {
            capturedAmount.set(invocation.getArgument(1));
            capturedBidder.set(invocation.getArgument(2));
            return null;
        }).when(auctionDao).updateCurrentBid(eq(AUCTION_ID), eq(1600.0), eq(CLIENT_ID));

        // 2. Ejercicio
        final Bid bid = auctionService.placeBid(AUCTION_ID, CLIENT_ID, 1600.0);

        // 3. Asserts
        assertNotNull(bid);
        assertEquals(1600.0, bid.getAmount());
        assertEquals(1600.0, capturedAmount.get());
        assertEquals(CLIENT_ID, capturedBidder.get().longValue());
    }

    @Test
    void testPlaceBidWhenAmountBelowMinimumThrowsBidPlacementException() {
        // 1. Setup
        final Pack pack = new Pack(PACK_ID, COMMERCE_ID, "Test Pack", "Desc", 100.0, 50.0, 10, true, null);
        final LocalDateTime endTime = LocalDateTime.now(ZoneOffset.UTC).plusHours(1);
        final double minInc = 500.0;
        final Auction auction = new Auction(AUCTION_ID, pack, 1000.0, minInc, null, null, endTime,
                Auction.Status.ACTIVE, LocalDateTime.now());
        when(auctionDao.findById(AUCTION_ID)).thenReturn(Optional.of(auction));
        lenient().doThrow(new AssertionError("createBid no debe invocarse")).when(bidDao).createBid(anyLong(),
                anyLong(), anyDouble());
        lenient().doThrow(new AssertionError("updateCurrentBid no debe invocarse")).when(auctionDao)
                .updateCurrentBid(anyLong(), anyDouble(), anyLong());

        // 2. Ejercicio
        final BidPlacementException exception = assertThrows(BidPlacementException.class,
                () -> auctionService.placeBid(AUCTION_ID, CLIENT_ID, 1200.0));

        // 3. Asserts
        assertEquals(BidFailureReason.AMOUNT_BELOW_MINIMUM, exception.getReason());
    }

    @Test
    void testPlaceBidWhenAuctionExpiredThrowsBidPlacementException() {
        // 1. Setup
        final Pack pack = new Pack(PACK_ID, COMMERCE_ID, "Test Pack", "Desc", 100.0, 50.0, 10, true, null);
        final LocalDateTime endTime = LocalDateTime.now(ZoneOffset.UTC).minusHours(1);
        final double minInc = 1.0;
        final Auction auction = new Auction(AUCTION_ID, pack, 1000.0, minInc, null, null, endTime,
                Auction.Status.ACTIVE, LocalDateTime.now());
        when(auctionDao.findById(AUCTION_ID)).thenReturn(Optional.of(auction));
        lenient().doThrow(new AssertionError("createBid no debe invocarse")).when(bidDao).createBid(anyLong(),
                anyLong(), anyDouble());

        // 2. Ejercicio
        final BidPlacementException exception = assertThrows(BidPlacementException.class,
                () -> auctionService.placeBid(AUCTION_ID, CLIENT_ID, 2000.0));

        // 3. Asserts
        assertEquals(BidFailureReason.EXPIRED, exception.getReason());
    }

    @Test
    void testPlaceBidWhenClientIsOwnCommerceThrowsBidPlacementException() {
        // 1. Setup
        final Pack pack = new Pack(PACK_ID, COMMERCE_ID, "Test Pack", "Desc", 100.0, 50.0, 10, true, null);
        final LocalDateTime endTime = LocalDateTime.now(ZoneOffset.UTC).plusHours(1);
        final double minInc = 1.0;
        final Auction auction = new Auction(AUCTION_ID, pack, 1000.0, minInc, null, null, endTime,
                Auction.Status.ACTIVE, LocalDateTime.now());
        when(auctionDao.findById(AUCTION_ID)).thenReturn(Optional.of(auction));
        lenient().doThrow(new AssertionError("createBid no debe invocarse")).when(bidDao).createBid(anyLong(),
                anyLong(), anyDouble());

        // 2. Ejercicio
        final BidPlacementException exception = assertThrows(BidPlacementException.class,
                () -> auctionService.placeBid(AUCTION_ID, COMMERCE_ID, 2000.0));

        // 3. Asserts
        assertEquals(BidFailureReason.OWN_COMMERCE, exception.getReason());
    }

    @Test
    void testFindParticipatedAuctionsByClientIdWhenMultipleBidsReturnsDistinctOrderedByFirstSeen() {
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
