package ar.edu.itba.paw.services.auction;

import ar.edu.itba.paw.models.auction.Auction;
import ar.edu.itba.paw.models.auction.Bid;
import ar.edu.itba.paw.models.auction.BidFailureReason;
import ar.edu.itba.paw.models.auction.AuctionCreationException;
import ar.edu.itba.paw.models.auction.BidPlacementException;
import ar.edu.itba.paw.models.pack.Municipality;
import ar.edu.itba.paw.models.pack.Pack;
import ar.edu.itba.paw.models.user.Commerce;
import ar.edu.itba.paw.models.user.Client;
import ar.edu.itba.paw.persistence.AuctionDao;
import ar.edu.itba.paw.persistence.BidDao;
import ar.edu.itba.paw.persistence.CommerceDao;
import ar.edu.itba.paw.persistence.PackDao;
import ar.edu.itba.paw.services.notification.NotificationService;
import ar.edu.itba.paw.services.reservation.ReservationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
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
    private CommerceDao commerceDao;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private AuctionServiceImpl auctionService;

    private static final long PACK_ID = 1L;
    private static final long AUCTION_ID = 100L;
    private static final long CLIENT_ID = 2L;
    private static final long COMMERCE_ID = 3L;

    private static Auction auctionRef(final long id) {
        return new Auction(id, null, 1000.0, 500.0, null, null, null, Auction.Status.ACTIVE, null);
    }

    private static Client clientRef(final long id) {
        return new Client(id, "N", "L", true);
    }

    private static Commerce commerceRef(final long userId) {
        return new Commerce(userId, "Comm", Commerce.Category.BAKERY, "St", 1, Municipality.AVELLANEDA, "P", "1000",
                "08:00", "20:00");
    }

    private static Pack newPack(final long id, final long commerceId, final String title, final String desc,
            final double originalPrice, final double finalPrice, final int stock, final boolean active,
            final List<ar.edu.itba.paw.models.pack.PackTag> tags) {
        return new Pack(id, commerceRef(commerceId), title, desc, originalPrice, finalPrice, stock, active, tags);
    }

    @Test
    void testCreateAuctionWhenPackValidReturnsCreatedAuction() {
        // 1. Setup
        final Pack pack = newPack(PACK_ID, COMMERCE_ID, "Test Pack", "Desc", 100.0, 50.0, 10, true, null);
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
    void testCreateAuctionWhenPackInactiveThrowsAuctionCreationException() {
        // 1. Setup
        final Pack pack = newPack(PACK_ID, COMMERCE_ID, "Test Pack", "Desc", 100.0, 50.0, 10, false, null);
        when(packDao.findById(PACK_ID)).thenReturn(Optional.of(pack));

        // 2. Ejercicio
        final LocalDateTime endTime = LocalDateTime.now(ZoneOffset.UTC).plusDays(1);
        final AuctionCreationException exception = assertThrows(AuctionCreationException.class,
                () -> auctionService.createAuction(PACK_ID, 100.0, 10.0, endTime));

        // 3. Asserts
        assertEquals(AuctionCreationException.Reason.PACK_INACTIVE, exception.getReason());
    }

    @Test
    void testPlaceBidWhenAmountValidReturnsBidAndUpdatesAuctionCurrentBid() {
        // 1. Setup
        final Pack pack = newPack(PACK_ID, COMMERCE_ID, "Test Pack", "Desc", 100.0, 50.0, 10, true, null);
        final LocalDateTime endTime = LocalDateTime.now(ZoneOffset.UTC).plusHours(1);
        final double minInc = 500.0;
        final Auction auction = new Auction(AUCTION_ID, pack, 1000.0, minInc, null, null, endTime,
                Auction.Status.ACTIVE, LocalDateTime.now());
        when(auctionDao.findById(AUCTION_ID)).thenReturn(Optional.of(auction));
        final Bid createdBid = new Bid(1L, auctionRef(AUCTION_ID), clientRef(CLIENT_ID), 1600.0, LocalDateTime.now());
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
    void testPlaceBidWhenPreviousBidderExistsReturnsBid() {
        // 1. Setup
        final long previousBidderId = 50L;
        final long newBidderId = 60L;
        final double amount = 75.0;
        final Pack pack = newPack(PACK_ID, COMMERCE_ID, "Test Pack", "Desc", 100.0, 50.0, 10, true, null);
        final LocalDateTime endTime = LocalDateTime.now(ZoneOffset.UTC).plusHours(1);
        final Auction auction = new Auction(AUCTION_ID, pack, 10.0, 5.0, 50.0, previousBidderId, endTime,
                Auction.Status.ACTIVE, LocalDateTime.now());
        when(auctionDao.findById(AUCTION_ID)).thenReturn(Optional.of(auction));
        final Bid createdBid = new Bid(1L, auctionRef(AUCTION_ID), clientRef(newBidderId), amount, LocalDateTime.now());
        when(bidDao.createBid(AUCTION_ID, newBidderId, amount)).thenReturn(createdBid);

        // 2. Ejercicio
        final Bid bid = auctionService.placeBid(AUCTION_ID, newBidderId, amount);

        // 3. Asserts
        assertNotNull(bid);
        assertEquals(amount, bid.getAmount());
        assertEquals(newBidderId, bid.getClient().getUserId().longValue());
    }

    @Test
    void testPlaceBidWhenAmountBelowMinimumThrowsBidPlacementException() {
        // 1. Setup
        final Pack pack = newPack(PACK_ID, COMMERCE_ID, "Test Pack", "Desc", 100.0, 50.0, 10, true, null);
        final LocalDateTime endTime = LocalDateTime.now(ZoneOffset.UTC).plusHours(1);
        final double minInc = 500.0;
        final Auction auction = new Auction(AUCTION_ID, pack, 1000.0, minInc, null, null, endTime,
                Auction.Status.ACTIVE, LocalDateTime.now());
        when(auctionDao.findById(AUCTION_ID)).thenReturn(Optional.of(auction));

        // 2. Ejercicio
        final BidPlacementException exception = assertThrows(BidPlacementException.class,
                () -> auctionService.placeBid(AUCTION_ID, CLIENT_ID, 1200.0));

        // 3. Asserts
        assertEquals(BidFailureReason.AMOUNT_BELOW_MINIMUM, exception.getReason());
    }

    @Test
    void testPlaceBidWhenAuctionExpiredThrowsBidPlacementException() {
        // 1. Setup
        final Pack pack = newPack(PACK_ID, COMMERCE_ID, "Test Pack", "Desc", 100.0, 50.0, 10, true, null);
        final LocalDateTime endTime = LocalDateTime.now(ZoneOffset.UTC).minusHours(1);
        final double minInc = 1.0;
        final Auction auction = new Auction(AUCTION_ID, pack, 1000.0, minInc, null, null, endTime,
                Auction.Status.ACTIVE, LocalDateTime.now());
        when(auctionDao.findById(AUCTION_ID)).thenReturn(Optional.of(auction));

        // 2. Ejercicio
        final BidPlacementException exception = assertThrows(BidPlacementException.class,
                () -> auctionService.placeBid(AUCTION_ID, CLIENT_ID, 2000.0));

        // 3. Asserts
        assertEquals(BidFailureReason.EXPIRED, exception.getReason());
    }

    @Test
    void testPlaceBidWhenClientIsOwnCommerceThrowsBidPlacementException() {
        // 1. Setup
        final Pack pack = newPack(PACK_ID, COMMERCE_ID, "Test Pack", "Desc", 100.0, 50.0, 10, true, null);
        final LocalDateTime endTime = LocalDateTime.now(ZoneOffset.UTC).plusHours(1);
        final double minInc = 1.0;
        final Auction auction = new Auction(AUCTION_ID, pack, 1000.0, minInc, null, null, endTime,
                Auction.Status.ACTIVE, LocalDateTime.now());
        when(auctionDao.findById(AUCTION_ID)).thenReturn(Optional.of(auction));

        // 2. Ejercicio
        final BidPlacementException exception = assertThrows(BidPlacementException.class,
                () -> auctionService.placeBid(AUCTION_ID, COMMERCE_ID, 2000.0));

        // 3. Asserts
        assertEquals(BidFailureReason.OWN_COMMERCE, exception.getReason());
    }

    @Test
    void testFilterParticipatedAuctionsWhenInvokedDelegatesToDao() {
        // 1. Setup
        final int page = 1;
        final int pageSize = 10;
        final String query = "search";
        final Auction auction = new Auction(AUCTION_ID, null, 10.0, 1.0, null, null, null, Auction.Status.ACTIVE, null);
        final List<Auction> expected = List.of(auction);
        when(auctionDao.filterParticipatedAuctions(CLIENT_ID, Auction.Status.ACTIVE, query, page, pageSize)).thenReturn(expected);
        when(bidDao.findMaxBidsByClientForAuctions(CLIENT_ID, List.of(AUCTION_ID))).thenReturn(Map.of());

        // 2. Ejercicio
        final List<Auction> result = auctionService.filterParticipatedAuctions(CLIENT_ID, Auction.Status.ACTIVE, query, page, pageSize);

        // 3. Asserts
        assertEquals(expected, result);
    }

    @Test
    void testFilterParticipatedAuctionsHydratesMyMaxBid() {
        // 1. Setup
        final int page = 1;
        final int pageSize = 10;
        final Auction auction = new Auction(AUCTION_ID, null, 10.0, 1.0, null, null, null, Auction.Status.ACTIVE, null);
        when(auctionDao.filterParticipatedAuctions(CLIENT_ID, null, null, page, pageSize))
                .thenReturn(List.of(auction));
        when(bidDao.findMaxBidsByClientForAuctions(CLIENT_ID, List.of(AUCTION_ID)))
                .thenReturn(Map.of(AUCTION_ID, 50.0));

        // 2. Ejercicio
        final List<Auction> result = auctionService.filterParticipatedAuctions(CLIENT_ID, null, null, page, pageSize);

        // 3. Asserts
        assertEquals(1, result.size());
        assertEquals(50.0, result.get(0).getMyMaxBid());
    }

    @Test
    void testFilterParticipatedAuctionsWhenEmptyResultSkipsBidQuery() {
        // 1. Setup
        final int page = 1;
        final int pageSize = 10;
        when(auctionDao.filterParticipatedAuctions(CLIENT_ID, null, null, page, pageSize))
                .thenReturn(Collections.emptyList());

        // 2. Ejercicio
        final List<Auction> result = auctionService.filterParticipatedAuctions(CLIENT_ID, null, null, page, pageSize);

        // 3. Asserts
        assertTrue(result.isEmpty());
    }

    @Test
    void testFilterParticipatedAuctionsSetsDefaultMyMaxBidWhenNoBidFound() {
        // 1. Setup
        final int page = 1;
        final int pageSize = 10;
        final Auction auction = new Auction(AUCTION_ID, null, 10.0, 1.0, null, null, null, Auction.Status.ACTIVE, null);
        when(auctionDao.filterParticipatedAuctions(CLIENT_ID, null, null, page, pageSize))
                .thenReturn(List.of(auction));
        when(bidDao.findMaxBidsByClientForAuctions(CLIENT_ID, List.of(AUCTION_ID)))
                .thenReturn(Collections.emptyMap());

        // 2. Ejercicio
        final List<Auction> result = auctionService.filterParticipatedAuctions(CLIENT_ID, null, null, page, pageSize);

        // 3. Asserts
        assertEquals(1, result.size());
        assertEquals(0d, result.get(0).getMyMaxBid());
    }

    @Test
    void testCountParticipatedAuctionsWhenInvokedDelegatesToDao() {
        // 1. Setup
        final String query = "search";
        final int expected = 5;
        when(auctionDao.countParticipatedAuctions(CLIENT_ID, Auction.Status.ACTIVE, query)).thenReturn(expected);

        // 2. Ejercicio
        final int result = auctionService.countParticipatedAuctions(CLIENT_ID, Auction.Status.ACTIVE, query);

        // 3. Asserts
        assertEquals(expected, result);
    }

    @Test
    void testCancelAuctionWhenAuctionNotFoundReturnsNotFound() {
        // 1. Setup
        when(auctionDao.findById(AUCTION_ID)).thenReturn(Optional.empty());

        // 2. Ejercicio
        final CancelAuctionResult result = auctionService.cancelAuction(AUCTION_ID);

        // 3. Asserts
        assertEquals(CancelAuctionResult.Outcome.NOT_FOUND, result.getOutcome());
    }

    @Test
    void testCancelAuctionWhenAuctionNotActiveReturnsNotActive() {
        // 1. Setup
        final Pack pack = newPack(PACK_ID, COMMERCE_ID, "Test Pack", "Desc", 100.0, 50.0, 10, true, null);
        final Auction auction = new Auction(AUCTION_ID, pack, 1000.0, 10.0, null, null, null,
                Auction.Status.FINISHED, LocalDateTime.now());
        when(auctionDao.findById(AUCTION_ID)).thenReturn(Optional.of(auction));

        // 2. Ejercicio
        final CancelAuctionResult result = auctionService.cancelAuction(AUCTION_ID);

        // 3. Asserts
        assertEquals(CancelAuctionResult.Outcome.NOT_ACTIVE, result.getOutcome());
    }

    @Test
    void testCancelAuctionWhenAuctionHasBidsReturnsHasBids() {
        // 1. Setup
        final Pack pack = newPack(PACK_ID, COMMERCE_ID, "Test Pack", "Desc", 100.0, 50.0, 10, true, null);
        final Auction auction = new Auction(AUCTION_ID, pack, 1000.0, 10.0, null, null, null,
                Auction.Status.ACTIVE, LocalDateTime.now());
        when(auctionDao.findById(AUCTION_ID)).thenReturn(Optional.of(auction));
        when(bidDao.countByAuctionId(AUCTION_ID)).thenReturn(1);

        // 2. Ejercicio
        final CancelAuctionResult result = auctionService.cancelAuction(AUCTION_ID);

        // 3. Asserts
        assertEquals(CancelAuctionResult.Outcome.HAS_BIDS, result.getOutcome());
    }

    @Test
    void testCancelAuctionWhenValidCancelsAuctionAndSetsPackInactive() {
        // 1. Setup
        final Pack pack = newPack(PACK_ID, COMMERCE_ID, "Test Pack", "Desc", 100.0, 50.0, 10, true, null);
        final Auction auction = new Auction(AUCTION_ID, pack, 1000.0, 10.0, null, null, null,
                Auction.Status.ACTIVE, LocalDateTime.now());
        when(auctionDao.findById(AUCTION_ID)).thenReturn(Optional.of(auction));
        when(bidDao.countByAuctionId(AUCTION_ID)).thenReturn(0);

        // 2. Ejercicio
        final CancelAuctionResult result = auctionService.cancelAuction(AUCTION_ID);

        // 3. Asserts
        assertEquals(CancelAuctionResult.Outcome.SUCCESS, result.getOutcome());
    }

    @Test
    void testCloseExpiredAuctionsUpdatesEntityStatus() {
        // 1. Setup
        final Pack pack = newPack(PACK_ID, COMMERCE_ID, "Test Pack", "Desc", 100.0, 50.0, 10, true, null);
        final Auction expiredAuction = new Auction(AUCTION_ID, pack, 1000.0, 10.0, null, null,
                LocalDateTime.now(ZoneOffset.UTC).minusHours(1), Auction.Status.ACTIVE, LocalDateTime.now());
        when(auctionDao.findExpiredActive()).thenReturn(List.of(expiredAuction));

        // 2. Ejercicio
        final int closed = auctionService.closeExpiredAuctions();

        // 3. Asserts
        assertEquals(1, closed);
        assertEquals(Auction.Status.FINISHED, expiredAuction.getStatus());
        assertFalse(expiredAuction.getPack().getActive());
    }

    @Test
    void testFindSummariesByPackIdsMergesHasBids() {
        // 1. Setup
        when(auctionDao.findSummariesByPackIds(List.of(PACK_ID)))
                .thenReturn(List.<Object[]>of(new Object[] { PACK_ID, AUCTION_ID, Auction.Status.ACTIVE }));
        when(bidDao.findAuctionIdsWithBids(List.of(AUCTION_ID))).thenReturn(Set.of(AUCTION_ID));

        // 2. Ejercicio
        final List<AuctionPackSummary> summaries = auctionService.findSummariesByPackIds(List.of(PACK_ID));

        // 3. Asserts
        assertEquals(1, summaries.size());
        assertEquals(PACK_ID, summaries.get(0).packId());
        assertEquals(AUCTION_ID, summaries.get(0).auctionId());
        assertEquals(Auction.Status.ACTIVE, summaries.get(0).status());
        assertTrue(summaries.get(0).hasBids());
    }

    @Test
    void testFindSummariesByPackIdsWhenNoBidsReturnsHasBidsFalse() {
        // 1. Setup
        when(auctionDao.findSummariesByPackIds(List.of(PACK_ID)))
                .thenReturn(List.<Object[]>of(new Object[] { PACK_ID, AUCTION_ID, Auction.Status.ACTIVE }));
        when(bidDao.findAuctionIdsWithBids(List.of(AUCTION_ID))).thenReturn(Collections.emptySet());

        // 2. Ejercicio
        final List<AuctionPackSummary> summaries = auctionService.findSummariesByPackIds(List.of(PACK_ID));

        // 3. Asserts
        assertEquals(1, summaries.size());
        assertFalse(summaries.get(0).hasBids());
    }

    @Test
    void testFindSummariesByPackIdsWhenPackIdsEmptyReturnsEmptyList() {
        // 1. Setup — no mocks needed

        // 2. Ejercicio
        final List<AuctionPackSummary> summaries = auctionService.findSummariesByPackIds(Collections.emptyList());

        // 3. Asserts
        assertTrue(summaries.isEmpty());
    }

    @Test
    void testCloseExpiredAuctionsTriggersNotification() {
        // 1. Setup
        final Pack pack = newPack(PACK_ID, COMMERCE_ID, "Test Pack", "Desc", 100.0, 50.0, 10, true, null);
        final Auction expiredAuction = new Auction(AUCTION_ID, pack, 1000.0, 10.0, null, null,
                LocalDateTime.now(ZoneOffset.UTC).minusHours(1), Auction.Status.ACTIVE, LocalDateTime.now());
        when(auctionDao.findExpiredActive()).thenReturn(List.of(expiredAuction));
        final AtomicLong notifiedAuctionId = new AtomicLong();
        doAnswer(inv -> {
            notifiedAuctionId.set(inv.getArgument(0));
            return null;
        }).when(notificationService).notifyAuctionFinished(anyLong());

        // 2. Ejercicio
        final int closed = auctionService.closeExpiredAuctions();

        // 3. Asserts
        assertEquals(1, closed);
        assertEquals(AUCTION_ID, notifiedAuctionId.get());
    }
}
