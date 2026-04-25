package ar.edu.itba.paw.persistence;

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
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.jdbc.JdbcTestUtils;

import javax.sql.DataSource;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestConfig.class)
@Sql("classpath:schema.sql")
public class BidJdbcDaoTest {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private BidJdbcDao bidDao;

    @Autowired
    private UserJdbcDao userDao;

    @Autowired
    private ClientJdbcDao clientDao;

    @Autowired
    private CommerceJdbcDao commerceDao;

    @Autowired
    private PackJdbcDao packDao;

    @Autowired
    private AuctionJdbcDao auctionDao;

    private JdbcTemplate jdbcTemplate;

    private Long commerceId;
    private Long clientId;
    private Long packId;
    private Long auctionId;

    @BeforeEach
    public void setUp() {
        jdbcTemplate = new JdbcTemplate(dataSource);
        JdbcTestUtils.deleteFromTables(jdbcTemplate, "bids", "auctions", "reservation_tokens", "pack_tags", "reservations", "packs", "commerces", "clients", "tokens", "users");
        
        commerceId = userDao.createUser("commerce@example.com", "pass", "Commerce", "123", User.Role.COMMERCE).getId();
        commerceDao.createCommerce(commerceId, "Comm", Commerce.Category.BAKERY, "Street", 123, "City", "Prov", "1000", "08:00", "20:00");

        clientId = userDao.createUser("client@example.com", "pass", "Client", "123", User.Role.CLIENT).getId();
        clientDao.createClient(clientId, "Client", "Last", true);

        Pack pack = packDao.createPack(commerceId, "Pack", "Desc", 1000.0, 500.0, 1, Collections.emptyList(), null, null);
        packId = pack.getId();

        LocalDateTime endTime = LocalDateTime.now(ZoneOffset.UTC).plusDays(1);
        Auction auction = auctionDao.createAuction(packId, 500.0, endTime);
        auctionId = auction.getId();
    }

    @Test
    public void testCreateBid() {
        // 1. Setup
        double amount = 600.0;

        // 2. Ejercicio
        Bid bid = bidDao.createBid(auctionId, clientId, amount);

        // 3. Asserts
        assertNotNull(bid);
        assertEquals(auctionId, bid.getAuctionId());
        assertEquals(clientId, bid.getClientId());
        assertEquals(amount, bid.getAmount());
        assertEquals(1, JdbcTestUtils.countRowsInTable(jdbcTemplate, "bids"));
    }

    @Test
    public void testFindHighestBid() {
        // 1. Setup
        bidDao.createBid(auctionId, clientId, 600.0);
        bidDao.createBid(auctionId, clientId, 700.0);
        bidDao.createBid(auctionId, clientId, 650.0);

        // 2. Ejercicio
        Optional<Bid> highestBid = bidDao.findHighestBid(auctionId);

        // 3. Asserts
        assertTrue(highestBid.isPresent());
        assertEquals(700.0, highestBid.get().getAmount());
    }

    @Test
    public void testFindByAuctionId() {
        // 1. Setup
        bidDao.createBid(auctionId, clientId, 600.0);
        bidDao.createBid(auctionId, clientId, 700.0);

        // 2. Ejercicio
        List<Bid> bids = bidDao.findByAuctionId(auctionId);

        // 3. Asserts
        assertEquals(2, bids.size());
        assertEquals(700.0, bids.get(0).getAmount()); // Ordered descending by amount
        assertEquals(600.0, bids.get(1).getAmount());
    }
}
