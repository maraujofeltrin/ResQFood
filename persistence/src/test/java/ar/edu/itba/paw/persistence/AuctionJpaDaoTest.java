package ar.edu.itba.paw.persistence;

import javax.persistence.PersistenceContext;
import ar.edu.itba.paw.models.auction.Auction;
import ar.edu.itba.paw.models.pack.Municipality;
import ar.edu.itba.paw.models.pack.Pack;
import ar.edu.itba.paw.models.pack.PackTag;
import ar.edu.itba.paw.models.user.Commerce;
import ar.edu.itba.paw.models.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.jdbc.JdbcTestUtils;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@Rollback
@Transactional
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestConfig.class)
public class AuctionJpaDaoTest {

    private static final LocalDateTime AUCTION_END_TIME = LocalDateTime.of(2030, 6, 15, 18, 0);

    @Autowired
    private DataSource dataSource;

    @Autowired
    
    private AuctionDao auctionDao;

    @Autowired
    
    private UserDao userDao;

    @Autowired
    
    private CommerceDao commerceDao;

    @Autowired
    
    private ClientDao clientDao;

    @Autowired
    private PackDao packDao;

    @PersistenceContext
    private javax.persistence.EntityManager em;

    private JdbcTemplate jdbcTemplate;

    private Long commerceId;
    private Long clientId;
    private Long packId;

    @BeforeEach
    public void setUp() {
        jdbcTemplate = new JdbcTemplate(dataSource);
        JdbcTestUtils.deleteFromTables(jdbcTemplate, "bids", "auctions", "reservation_tokens", "pack_tags",
                "reservations", "client_pack_favorites", "packs", "images", "commerces", "clients", "tokens", "users");

        commerceId = userDao.createUser("commerce@example.com", "pass", "Commerce", "123", User.Role.COMMERCE).getId();
        commerceDao.createCommerce(commerceId, "Comm", Commerce.Category.BAKERY, "Street", 123,
                ar.edu.itba.paw.models.pack.Municipality.AVELLANEDA, "Prov", "1000", "08:00", "20:00");

        clientId = userDao.createUser("client@example.com", "pass", "Client", "123", User.Role.CLIENT).getId();
        clientDao.createClient(clientId, "Client", "Last", true);

        Pack pack = packDao.createPack(commerceId, "Pack", "Desc", 1000.0, 500.0, 1, Collections.emptyList(), null);
        packId = pack.getId();
    }

    @Test
    public void testCreateAuctionWhenPackExists() {
        // 1. Setup
        final double initialPrice = 500.0;
        final double minInc = 500.0;

        // 2. Ejercicio
        final Auction auction = auctionDao.createAuction(packId, initialPrice, minInc, AUCTION_END_TIME);
        em.flush();

        // 3. Asserts
        assertNotNull(auction);
        assertEquals(packId, auction.getPack().getId());
        assertEquals(initialPrice, auction.getInitialPrice());
        assertEquals(minInc, auction.getMinBidIncrement());
        assertEquals(Auction.Status.ACTIVE, auction.getStatus());
        assertEquals(1, JdbcTestUtils.countRowsInTable(jdbcTemplate, "auctions"));
    }

    @Test
    public void testFindByIdWhenAuctionExists() {
        // 1. Setup
        final Auction created = auctionDao.createAuction(packId, 500.0, 500.0, AUCTION_END_TIME);
        em.flush();

        // 2. Ejercicio
        final Optional<Auction> found = auctionDao.findById(created.getId());

        // 3. Asserts
        assertTrue(found.isPresent());
        assertEquals(created.getId(), found.get().getId());
        assertEquals(1, JdbcTestUtils.countRowsInTable(jdbcTemplate, "auctions"));
    }

    @Test
    public void testFilterAuctionsWhenCityNameMatchesReturnsAuction() {
        // 1. Setup
        auctionDao.createAuction(packId, 500.0, 500.0, AUCTION_END_TIME);
        em.flush();

        // 2. Ejercicio
        final List<Auction> filtered = auctionDao.filterAuctions(null, null, Municipality.AVELLANEDA.getCityName(),
                null, null, 1, 10, false);

        // 3. Asserts
        assertEquals(1, filtered.size());
    }

    @Test
    public void testFilterAuctionsWhenCityNameAndTagsMatchReturnsAuction() {
        // 1. Setup
        final Pack taggedPack = packDao.createPack(commerceId, "Vegan Pack", "Desc", 1000.0, 500.0, 1,
                Collections.singletonList(PackTag.VEGAN), null);
        auctionDao.createAuction(taggedPack.getId(), 500.0, 500.0, AUCTION_END_TIME);
        em.flush();

        // 2. Ejercicio
        final List<Auction> filtered = auctionDao.filterAuctions(null, Collections.singletonList(PackTag.VEGAN),
                Municipality.AVELLANEDA.getCityName(), Collections.singletonList("morning"), null, 1, 10, false);

        // 3. Asserts
        assertEquals(1, filtered.size());
    }

    @Test
    public void testFilterAuctionsWhenOneActiveExists() {
        // 1. Setup
        auctionDao.createAuction(packId, 500.0, 500.0, AUCTION_END_TIME);
        em.flush();

        // 2. Ejercicio
        final List<Auction> activeAuctions = auctionDao.filterAuctions(null, null, null, null, null, 1,
                Integer.MAX_VALUE, false);

        // 3. Asserts
        assertEquals(1, activeAuctions.size());
        assertEquals(1, JdbcTestUtils.countRowsInTable(jdbcTemplate, "auctions"));
    }

    @Test
    public void testUpdateCurrentBidWhenAuctionExists() {
        // 1. Setup
        final Auction created = auctionDao.createAuction(packId, 500.0, 500.0, AUCTION_END_TIME);
        em.flush();

        // 2. Ejercicio
        auctionDao.updateCurrentBid(created.getId(), 700.0, clientId);
        em.flush();
        em.clear();

        // 3. Asserts
        final Optional<Auction> found = auctionDao.findById(created.getId());
        assertTrue(found.isPresent());
        assertEquals(700.0, found.get().getCurrentBid());
        assertEquals(clientId, found.get().getCurrentBidderId());
        assertEquals(1, JdbcTestUtils.countRowsInTable(jdbcTemplate, "auctions"));
    }

    @Test
    public void testUpdateStatusWhenAuctionExists() {
        // 1. Setup
        final Auction created = auctionDao.createAuction(packId, 500.0, 500.0, AUCTION_END_TIME);
        em.flush();

        // 2. Ejercicio
        auctionDao.updateStatus(created.getId(), Auction.Status.FINISHED);
        em.flush();
        em.clear();

        // 3. Asserts
        final Optional<Auction> found = auctionDao.findById(created.getId());
        assertTrue(found.isPresent());
        assertEquals(Auction.Status.FINISHED, found.get().getStatus());
        assertEquals(1, JdbcTestUtils.countRowsInTable(jdbcTemplate, "auctions"));
    }

    @Test
    public void testFilterParticipatedAuctionsWhenClientHasBidsReturnsAuction() {
        // 1. Setup
        final Auction created = auctionDao.createAuction(packId, 500.0, 500.0, AUCTION_END_TIME);
        em.flush();
        jdbcTemplate.update("INSERT INTO bids (id, auction_id, client_id, amount, timestamp) VALUES (?, ?, ?, ?, ?)",
                1L, created.getId(), clientId, 600.0, LocalDateTime.now(ZoneOffset.UTC));

        // 2. Ejercicio
        final List<Auction> participated = auctionDao.filterParticipatedAuctions(clientId, null, null, 1, 10);

        // 3. Asserts
        assertEquals(1, participated.size());
        assertEquals(created.getId(), participated.get(0).getId());
    }

    @Test
    public void testCountParticipatedAuctionsWhenClientHasBidsReturnsCount() {
        // 1. Setup
        final Auction created = auctionDao.createAuction(packId, 500.0, 500.0, AUCTION_END_TIME);
        em.flush();
        jdbcTemplate.update("INSERT INTO bids (id, auction_id, client_id, amount, timestamp) VALUES (?, ?, ?, ?, ?)",
                1L, created.getId(), clientId, 600.0, LocalDateTime.now(ZoneOffset.UTC));

        // 2. Ejercicio
        final int count = auctionDao.countParticipatedAuctions(clientId, null, null);

        // 3. Asserts
        assertEquals(1, count);
    }
}
