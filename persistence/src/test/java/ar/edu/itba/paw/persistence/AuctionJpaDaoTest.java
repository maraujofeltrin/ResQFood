package ar.edu.itba.paw.persistence;

import javax.persistence.PersistenceContext;
import ar.edu.itba.paw.models.auction.Auction;
import ar.edu.itba.paw.models.auction.AuctionSortOption;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

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

    @Autowired
    private BidDao bidDao;

    @PersistenceContext
    private javax.persistence.EntityManager em;

    private JdbcTemplate jdbcTemplate;

    private Long commerceId;
    private Long clientId;
    private Long packId;

    @BeforeEach
    public void setUp() {
        jdbcTemplate = new JdbcTemplate(dataSource);
        JdbcTestUtils.deleteFromTables(jdbcTemplate, "commerce_reviews", "bids", "auctions", "reservation_tokens",
                "pack_tags", "reservations", "client_pack_favorites", "packs", "images", "commerces", "clients",
                "tokens", "users");

        commerceId = userDao.createUser("commerce@example.com", "pass", "Commerce", "123", User.Role.COMMERCE).getId();
        commerceDao.createCommerce(commerceId, "Comm", Commerce.Category.BAKERY, "Street", 123,
                Municipality.AVELLANEDA, "Prov", "1000", "08:00", "20:00");

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
                null, null, 1, 10, false, null);

        // 3. Asserts
        assertEquals(1, filtered.size());
        assertEquals(1, JdbcTestUtils.countRowsInTable(jdbcTemplate, "auctions"));
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
                Municipality.AVELLANEDA.getCityName(), Collections.singletonList("morning"), null, 1, 10, false, null);

        // 3. Asserts
        assertEquals(1, filtered.size());
        assertEquals(1, JdbcTestUtils.countRowsInTable(jdbcTemplate, "auctions"));
    }

    @Test
    public void testFilterAuctionsWhenOneActiveExists() {
        // 1. Setup
        auctionDao.createAuction(packId, 500.0, 500.0, AUCTION_END_TIME);
        em.flush();

        // 2. Ejercicio
        final List<Auction> activeAuctions = auctionDao.filterAuctions(null, null, null, null, null, 1,
                Integer.MAX_VALUE, false, null);

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
        bidDao.createBid(created.getId(), clientId, 600.0);
        em.flush();

        // 2. Ejercicio
        final List<Auction> participated = auctionDao.filterParticipatedAuctions(clientId, null, null, 1, 10);

        // 3. Asserts
        assertEquals(1, participated.size());
        assertEquals(created.getId(), participated.get(0).getId());
        assertEquals(1, JdbcTestUtils.countRowsInTable(jdbcTemplate, "bids"));
    }

    @Test
    public void testFilterAuctionsRequiresAllTagsWhenMultipleTagsProvided() {
        // 1. Setup
        final Pack bothTagsPack = packDao.createPack(commerceId, "Both Tags Pack", "Desc", 1000.0, 500.0, 1,
                Arrays.asList(PackTag.VEGAN, PackTag.SWEET), null);
        final Pack sweetOnlyPack = packDao.createPack(commerceId, "Sweet Only Pack", "Desc", 1000.0, 500.0, 1,
                Collections.singletonList(PackTag.SWEET), null);
        final Auction bothTagsAuction = auctionDao.createAuction(bothTagsPack.getId(), 500.0, 50.0, AUCTION_END_TIME);
        auctionDao.createAuction(sweetOnlyPack.getId(), 500.0, 50.0, AUCTION_END_TIME);
        em.flush();

        // 2. Ejercicio
        final List<Auction> filtered = auctionDao.filterAuctions(null, Arrays.asList(PackTag.VEGAN, PackTag.SWEET),
                null, null, null, 1, 10, false, null);

        // 3. Asserts
        assertEquals(1, filtered.size());
        assertEquals(bothTagsAuction.getId(), filtered.get(0).getId());
        assertEquals(2, JdbcTestUtils.countRowsInTable(jdbcTemplate, "auctions"));
    }

    @Test
    public void testCountFilteredAuctionsRequiresAllTagsWhenMultipleTagsProvided() {
        // 1. Setup
        final Pack bothTagsPack = packDao.createPack(commerceId, "Both Tags Pack", "Desc", 1000.0, 500.0, 1,
                Arrays.asList(PackTag.VEGAN, PackTag.SWEET), null);
        final Pack sweetOnlyPack = packDao.createPack(commerceId, "Sweet Only Pack", "Desc", 1000.0, 500.0, 1,
                Collections.singletonList(PackTag.SWEET), null);
        auctionDao.createAuction(bothTagsPack.getId(), 500.0, 50.0, AUCTION_END_TIME);
        auctionDao.createAuction(sweetOnlyPack.getId(), 500.0, 50.0, AUCTION_END_TIME);
        em.flush();

        // 2. Ejercicio
        final int count = auctionDao.countFilteredAuctions(null, Arrays.asList(PackTag.VEGAN, PackTag.SWEET),
                null, null, false, null);

        // 3. Asserts
        assertEquals(1, count);
        assertEquals(2, JdbcTestUtils.countRowsInTable(jdbcTemplate, "auctions"));
    }

    @Test
    public void testFilterAuctionsPageTwoRespectsPageSize() {
        // 1. Setup
        final LocalDateTime end1 = LocalDateTime.of(2030, 6, 10, 18, 0);
        final LocalDateTime end2 = LocalDateTime.of(2030, 6, 11, 18, 0);
        final LocalDateTime end3 = LocalDateTime.of(2030, 6, 12, 18, 0);
        final Pack pack2 = packDao.createPack(commerceId, "Pack 2", "Desc", 1000.0, 500.0, 1, null, null);
        final Pack pack3 = packDao.createPack(commerceId, "Pack 3", "Desc", 1000.0, 500.0, 1, null, null);
        auctionDao.createAuction(packId, 500.0, 50.0, end1);
        final Auction second = auctionDao.createAuction(pack2.getId(), 500.0, 50.0, end2);
        auctionDao.createAuction(pack3.getId(), 500.0, 50.0, end3);
        em.flush();

        // 2. Ejercicio
        final List<Auction> pageTwo = auctionDao.filterAuctions(null, null, null, null,
                AuctionSortOption.TIME_REMAINING_ASC, 2, 1, false, null);

        // 3. Asserts
        assertEquals(1, pageTwo.size());
        assertEquals(second.getId(), pageTwo.get(0).getId());
        assertEquals(3, JdbcTestUtils.countRowsInTable(jdbcTemplate, "auctions"));
    }

    @Test
    public void testCountFilteredAuctionsWhenThreeActiveExist() {
        // 1. Setup
        final LocalDateTime end1 = LocalDateTime.of(2030, 6, 10, 18, 0);
        final LocalDateTime end2 = LocalDateTime.of(2030, 6, 11, 18, 0);
        final LocalDateTime end3 = LocalDateTime.of(2030, 6, 12, 18, 0);
        final Pack pack2 = packDao.createPack(commerceId, "Pack 2", "Desc", 1000.0, 500.0, 1, null, null);
        final Pack pack3 = packDao.createPack(commerceId, "Pack 3", "Desc", 1000.0, 500.0, 1, null, null);
        auctionDao.createAuction(packId, 500.0, 50.0, end1);
        auctionDao.createAuction(pack2.getId(), 500.0, 50.0, end2);
        auctionDao.createAuction(pack3.getId(), 500.0, 50.0, end3);
        em.flush();

        // 2. Ejercicio
        final int count = auctionDao.countFilteredAuctions(null, null, null, null, false, null);

        // 3. Asserts
        assertEquals(3, count);
        assertEquals(3, JdbcTestUtils.countRowsInTable(jdbcTemplate, "auctions"));
    }

    @Test
    public void testFindPackIdsWithAuction() {
        // 1. Setup
        final User commerceUser = new User(null, "commerce2@example.com", "pass", "Commerce2", "123",
                User.Role.COMMERCE, true);
        em.persist(commerceUser);
        final Commerce commerce = new Commerce(commerceUser, "Comm 2", Commerce.Category.BAKERY, "Street", 123,
                Municipality.AVELLANEDA, "Prov", "1000", "08:00", "20:00");
        em.persist(commerce);
        final Pack packWithAuctionOne = new Pack(null, commerce, "Pack A", "Desc", 1000.0, 500.0, 1, true,
                Collections.emptyList());
        final Pack packWithAuctionTwo = new Pack(null, commerce, "Pack B", "Desc", 1000.0, 500.0, 1, true,
                Collections.emptyList());
        final Pack packWithoutAuction = new Pack(null, commerce, "Pack C", "Desc", 1000.0, 500.0, 1, true,
                Collections.emptyList());
        em.persist(packWithAuctionOne);
        em.persist(packWithAuctionTwo);
        em.persist(packWithoutAuction);
        final Auction auctionOne = new Auction(null, packWithAuctionOne, 500.0, 50.0, null, null, AUCTION_END_TIME,
                Auction.Status.ACTIVE, LocalDateTime.now(ZoneOffset.UTC));
        final Auction auctionTwo = new Auction(null, packWithAuctionTwo, 500.0, 50.0, null, null, AUCTION_END_TIME,
                Auction.Status.ACTIVE, LocalDateTime.now(ZoneOffset.UTC));
        em.persist(auctionOne);
        em.persist(auctionTwo);
        em.flush();

        // 2. Ejercicio
        final Set<Long> result = auctionDao.findPackIdsWithAuction(Set.of(packWithAuctionOne.getId(),
                packWithAuctionTwo.getId(), packWithoutAuction.getId()));

        // 3. Asserts
        assertTrue(result.contains(packWithAuctionOne.getId()));
        assertTrue(result.contains(packWithAuctionTwo.getId()));
        assertFalse(result.contains(packWithoutAuction.getId()));
    }

    @Test
    public void testFindSummariesByPackIds() {
        // 1. Setup
        final Auction auction = auctionDao.createAuction(packId, 500.0, 500.0, AUCTION_END_TIME);
        final Pack packWithoutAuction = packDao.createPack(commerceId, "Regular Pack", "Desc", 1000.0, 500.0, 1,
                Collections.emptyList(), null);
        em.flush();

        // 2. Ejercicio
        final List<Object[]> rows = auctionDao.findSummariesByPackIds(
                List.of(packId, packWithoutAuction.getId()));

        // 3. Asserts
        assertEquals(1, rows.size());
        assertEquals(packId, rows.get(0)[0]);
        assertEquals(auction.getId(), rows.get(0)[1]);
        assertEquals(Auction.Status.ACTIVE, rows.get(0)[2]);
    }

    @Test
    public void testCountParticipatedAuctionsWhenClientHasBidsReturnsCount() {
        // 1. Setup
        final Auction created = auctionDao.createAuction(packId, 500.0, 500.0, AUCTION_END_TIME);
        em.flush();
        bidDao.createBid(created.getId(), clientId, 600.0);
        em.flush();

        // 2. Ejercicio
        final int count = auctionDao.countParticipatedAuctions(clientId, null, null);

        // 3. Asserts
        assertEquals(1, count);
        assertEquals(1, JdbcTestUtils.countRowsInTable(jdbcTemplate, "bids"));
    }
}
