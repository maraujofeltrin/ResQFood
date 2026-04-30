package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.models.auction.Auction;
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
public class AuctionJdbcDaoTest {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private AuctionJdbcDao auctionDao;

    @Autowired
    private UserJdbcDao userDao;

    @Autowired
    private CommerceJdbcDao commerceDao;
    
    @Autowired
    private ClientJdbcDao clientDao;

    @Autowired
    private PackJdbcDao packDao;

    private JdbcTemplate jdbcTemplate;

    private Long commerceId;
    private Long clientId;
    private Long packId;

    @BeforeEach
    public void setUp() {
        jdbcTemplate = new JdbcTemplate(dataSource);
        JdbcTestUtils.deleteFromTables(jdbcTemplate, "bids", "auctions", "reservation_tokens", "pack_tags", "reservations", "client_pack_favorites", "packs", "images", "commerces", "clients", "tokens", "users");
        
        commerceId = userDao.createUser("commerce@example.com", "pass", "Commerce", "123", User.Role.COMMERCE).getId();
        commerceDao.createCommerce(commerceId, "Comm", Commerce.Category.BAKERY, "Street", 123, "City", "Prov", "1000", "08:00", "20:00");
        
        clientId = userDao.createUser("client@example.com", "pass", "Client", "123", User.Role.CLIENT).getId();
        clientDao.createClient(clientId, "Client", "Last", true);

        Pack pack = packDao.createPack(commerceId, "Pack", "Desc", 1000.0, 500.0, 1, Collections.emptyList(), null);
        packId = pack.getId();
    }

    @Test
    public void testCreateAuction() {
        // 1. Setup
        LocalDateTime endTime = LocalDateTime.now(ZoneOffset.UTC).plusDays(1);

        // 2. Ejercicio
        final double minInc = 500.0;
        Auction auction = auctionDao.createAuction(packId, 500.0, minInc, endTime);

        // 3. Asserts
        assertNotNull(auction);
        assertEquals(packId, auction.getPack().getId());
        assertEquals(500.0, auction.getInitialPrice());
        assertEquals(minInc, auction.getMinBidIncrement());
        assertEquals(Auction.Status.ACTIVE, auction.getStatus());
        assertEquals(1, JdbcTestUtils.countRowsInTable(jdbcTemplate, "auctions"));
    }

    @Test
    public void testFindById() {
        // 1. Setup
        LocalDateTime endTime = LocalDateTime.now(ZoneOffset.UTC).plusDays(1);
        Auction created = auctionDao.createAuction(packId, 500.0, 500.0, endTime);

        // 2. Ejercicio
        Optional<Auction> found = auctionDao.findById(created.getId());

        // 3. Asserts
        assertTrue(found.isPresent());
        assertEquals(created.getId(), found.get().getId());
    }

    @Test
    public void testFindActive() {
        // 1. Setup
        LocalDateTime future = LocalDateTime.now(ZoneOffset.UTC).plusDays(1);
        auctionDao.createAuction(packId, 500.0, 500.0, future);

        // 2. Ejercicio
        List<Auction> activeAuctions = auctionDao.filterAuctions(null, null, null, null, null, 1, Integer.MAX_VALUE);

        // 3. Asserts
        assertEquals(1, activeAuctions.size());
    }

    @Test
    public void testUpdateCurrentBid() {
        // 1. Setup
        LocalDateTime future = LocalDateTime.now(ZoneOffset.UTC).plusDays(1);
        Auction created = auctionDao.createAuction(packId, 500.0, 500.0, future);

        // 2. Ejercicio
        auctionDao.updateCurrentBid(created.getId(), 700.0, clientId);

        // 3. Asserts
        Optional<Auction> found = auctionDao.findById(created.getId());
        assertTrue(found.isPresent());
        assertEquals(700.0, found.get().getCurrentBid());
        assertEquals(clientId, found.get().getCurrentBidderId());
    }

    @Test
    public void testUpdateStatus() {
        // 1. Setup
        LocalDateTime future = LocalDateTime.now(ZoneOffset.UTC).plusDays(1);
        Auction created = auctionDao.createAuction(packId, 500.0, 500.0, future);

        // 2. Ejercicio
        auctionDao.updateStatus(created.getId(), Auction.Status.FINISHED);

        // 3. Asserts
        Optional<Auction> found = auctionDao.findById(created.getId());
        assertTrue(found.isPresent());
        assertEquals(Auction.Status.FINISHED, found.get().getStatus());
    }
}
