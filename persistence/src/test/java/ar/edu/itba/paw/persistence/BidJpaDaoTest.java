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
    public void testFindHighestBidWhenSeveralBidsExist() {
        // 1. Setup
        bidDao.createBid(auctionId, clientId, 600.0);
        bidDao.createBid(auctionId, clientId, 700.0);
        bidDao.createBid(auctionId, clientId, 650.0);
        em.flush();

        // 2. Ejercicio
        final Optional<Bid> highestBid = bidDao.findHighestBid(auctionId);

        // 3. Asserts
        assertTrue(highestBid.isPresent());
        assertEquals(700.0, highestBid.get().getAmount());
        assertEquals(3, JdbcTestUtils.countRowsInTable(jdbcTemplate, "bids"));
    }

    @Test
    public void testFindHighestBidWhenSameAmountUsesEarliestTimestamp() {
        // 1. Setup
        final LocalDateTime earlier = LocalDateTime.of(2030, 1, 1, 10, 0);
        final LocalDateTime later = LocalDateTime.of(2030, 1, 1, 11, 0);
        em.persist(new Bid(null, auctionId, clientId, 700.0, earlier));
        em.persist(new Bid(null, auctionId, clientId, 700.0, later));
        em.flush();
        em.clear();

        // 2. Ejercicio
        final Optional<Bid> highestBid = bidDao.findHighestBid(auctionId);

        // 3. Asserts
        assertTrue(highestBid.isPresent());
        assertEquals(earlier, highestBid.get().getTimestamp());
        assertEquals(2, JdbcTestUtils.countRowsInTable(jdbcTemplate, "bids"));
    }

    @Test
    public void testFindByAuctionIdWhenSeveralBidsExist() {
        // 1. Setup
        bidDao.createBid(auctionId, clientId, 600.0);
        bidDao.createBid(auctionId, clientId, 700.0);
        em.flush();

        // 2. Ejercicio
        final List<Bid> bids = bidDao.findByAuctionId(auctionId);

        // 3. Asserts
        assertEquals(2, bids.size());
        assertEquals(700.0, bids.get(0).getAmount());
        assertEquals(600.0, bids.get(1).getAmount());
        assertEquals(2, JdbcTestUtils.countRowsInTable(jdbcTemplate, "bids"));
    }
}
