package ar.edu.itba.paw.persistence;

import javax.persistence.PersistenceContext;
import ar.edu.itba.paw.models.auction.Auction;
import ar.edu.itba.paw.models.auction.Bid;
import ar.edu.itba.paw.models.pack.Pack;
import ar.edu.itba.paw.models.user.Commerce;
import ar.edu.itba.paw.models.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.jdbc.JdbcTestUtils;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@Rollback
@Transactional
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestConfig.class)
public class BidJpaDaoTest {

    private static final LocalDateTime AUCTION_END_TIME = LocalDateTime.of(2030, 6, 15, 18, 0);

    @Autowired
    private DataSource dataSource;

    @Autowired
    private BidDao bidDao;

    @Autowired
    private UserDao userDao;

    @Autowired
    private ClientDao clientDao;

    @Autowired
    private CommerceDao commerceDao;

    @Autowired
    private PackDao packDao;

    @Autowired
    private AuctionDao auctionDao;

    @PersistenceContext
    private javax.persistence.EntityManager em;

    private JdbcTemplate jdbcTemplate;

    private Long commerceId;
    private Long clientId;
    private Long packId;
    private Long auctionId;

    @BeforeEach
    public void setUp() {
        jdbcTemplate = new JdbcTemplate(dataSource);
        JdbcTestUtils.deleteFromTables(jdbcTemplate, "commerce_reviews", "bids", "auctions", "reservation_tokens",
                "pack_tags", "reservations", "client_pack_favorites", "packs", "images", "commerces", "clients",
                "tokens", "users");

        commerceId = userDao.createUser("commerce@example.com", "pass", "Commerce", "123", User.Role.COMMERCE).getId();
        commerceDao.createCommerce(commerceId, "Comm", Commerce.Category.BAKERY, "Street", 123,
                ar.edu.itba.paw.models.pack.Municipality.AVELLANEDA, "Prov", "1000", "08:00", "20:00");

        clientId = userDao.createUser("client@example.com", "pass", "Client", "123", User.Role.CLIENT).getId();
        clientDao.createClient(clientId, "Client", "Last", true);

        Pack pack = packDao.createPack(commerceId, "Pack", "Desc", 1000.0, 500.0, 1, Collections.emptyList(), null);
        packId = pack.getId();

        Auction auction = auctionDao.createAuction(packId, 500.0, 500.0, AUCTION_END_TIME);
        auctionId = auction.getId();
        em.flush();
    }

    @Test
    public void testCreateBidWhenAuctionExists() {
        // 1. Setup
        final double amount = 600.0;

        // 2. Ejercicio
        final Bid bid = bidDao.createBid(auctionId, clientId, amount);
        em.flush();

        // 3. Asserts
        assertNotNull(bid);
        assertEquals(auctionId, bid.getAuctionId());
        assertEquals(clientId, bid.getClientId());
        assertEquals(amount, bid.getAmount());
        assertEquals(1, JdbcTestUtils.countRowsInTable(jdbcTemplate, "bids"));
    }

    @Test
    public void testFindByAuctionIdWhenSeveralBidsExist() {
        // 1. Setup
        bidDao.createBid(auctionId, clientId, 600.0);
        bidDao.createBid(auctionId, clientId, 700.0);
        em.flush();

        // 2. Ejercicio
        final List<Bid> bids = bidDao.findByAuctionId(auctionId, 1, 10);

        // 3. Asserts
        assertEquals(2, bids.size());
        assertEquals(700.0, bids.get(0).getAmount());
        assertEquals(600.0, bids.get(1).getAmount());
        assertEquals(2, JdbcTestUtils.countRowsInTable(jdbcTemplate, "bids"));
    }


    @Test
    public void testFindByAuctionIdReturnsBidsWithClientData() {
        // 1. Setup
        bidDao.createBid(auctionId, clientId, 600.0);
        em.flush();

        // 2. Ejercicio
        final List<Bid> bids = bidDao.findByAuctionId(auctionId, 1, 10);

        // 3. Asserts
        assertEquals(1, bids.size());
        assertEquals("Client Last", bids.get(0).getClient().getFullName());
        assertEquals(1, JdbcTestUtils.countRowsInTable(jdbcTemplate, "bids"));
    }

    @Test
    public void testFindByAuctionIdWhenSecondPageRequestedReturnsNextBid() {
        // 1. Setup
        bidDao.createBid(auctionId, clientId, 800.0);
        bidDao.createBid(auctionId, clientId, 700.0);
        bidDao.createBid(auctionId, clientId, 600.0);
        em.flush();

        // 2. Ejercicio
        final List<Bid> pageTwo = bidDao.findByAuctionId(auctionId, 2, 1);

        // 3. Asserts
        assertEquals(1, pageTwo.size());
        assertEquals(700.0, pageTwo.get(0).getAmount());
    }

    @Test
    public void testFindMaxBidsByClientForAuctions() {
        // 1. Setup
        bidDao.createBid(auctionId, clientId, 50.0);
        bidDao.createBid(auctionId, clientId, 80.0);
        em.flush();

        // 2. Ejercicio
        final Map<Long, Double> result = bidDao.findMaxBidsByClientForAuctions(clientId, Set.of(auctionId));

        // 3. Asserts
        assertEquals(1, result.size());
        assertEquals(80.0, result.get(auctionId), 0.001);
    }

    @Test
    public void testExistsByAuctionIdAndClientUserIdWhenBidExists() {
        // 1. Setup
        bidDao.createBid(auctionId, clientId, 600.0);
        em.flush();

        // 2. Ejercicio
        final boolean exists = bidDao.existsByAuctionIdAndClientUserId(auctionId, clientId);

        // 3. Asserts
        assertTrue(exists);
        assertEquals(1, JdbcTestUtils.countRowsInTable(jdbcTemplate, "bids"));
    }

    @Test
    public void testExistsByAuctionIdAndClientUserIdWhenNoBidReturnsFalse() {
        // 1. Setup
        // No bids created.

        // 2. Ejercicio
        final boolean exists = bidDao.existsByAuctionIdAndClientUserId(auctionId, clientId);

        // 3. Asserts
        assertFalse(exists);
    }

    @Test
    public void testFindBiddersWhenSeveralBiddersExistReturnsDistinctUsers() {
        // 1. Setup
        final Long secondClientId = userDao.createUser("client2@example.com", "pass", "Client2", "123", User.Role.CLIENT).getId();
        clientDao.createClient(secondClientId, "Client2", "Last", true);
        bidDao.createBid(auctionId, clientId, 600.0);
        bidDao.createBid(auctionId, secondClientId, 700.0);
        bidDao.createBid(auctionId, clientId, 800.0);
        em.flush();

        // 2. Ejercicio
        final List<User> bidders = bidDao.findBidders(auctionId);

        // 3. Asserts
        assertEquals(2, bidders.size());
        assertTrue(bidders.stream().anyMatch(u -> clientId.equals(u.getId())));
        assertTrue(bidders.stream().anyMatch(u -> secondClientId.equals(u.getId())));
    }

    @Test
    public void testFindBidderWhenBidExistsReturnsUser() {
        // 1. Setup
        bidDao.createBid(auctionId, clientId, 600.0);
        em.flush();

        // 2. Ejercicio
        final Optional<User> bidder = bidDao.findBidder(auctionId, clientId);

        // 3. Asserts
        assertTrue(bidder.isPresent());
        assertEquals(clientId, bidder.get().getId());
        assertEquals("client@example.com", bidder.get().getEmail());
    }
}
