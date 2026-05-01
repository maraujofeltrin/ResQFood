package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.models.pack.Pack;
import ar.edu.itba.paw.models.pack.PackSortOption;
import ar.edu.itba.paw.models.pack.PackTag;
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
public class PackJdbcDaoTest {

    private static final LocalDateTime EXPIRED_AUCTION_END = LocalDateTime.of(2020, 1, 1, 0, 0);

    @Autowired
    private DataSource dataSource;

    @Autowired
    private PackJdbcDao packDao;

    @Autowired
    private UserJdbcDao userDao;

    @Autowired
    private CommerceJdbcDao commerceDao;

    @Autowired
    private AuctionJdbcDao auctionDao;

    private JdbcTemplate jdbcTemplate;

    private Long commerceId;

    @BeforeEach
    public void setUp() {
        jdbcTemplate = new JdbcTemplate(dataSource);
        JdbcTestUtils.deleteFromTables(jdbcTemplate, "commerce_reviews", "bids", "auctions", "reservation_tokens",
                "pack_tags", "reservations", "client_pack_favorites", "packs", "images", "commerces", "clients", "tokens",
                "users");

        commerceId = userDao.createUser("commerce@example.com", "pass", "Commerce", "123", User.Role.COMMERCE).getId();
        commerceDao.createCommerce(commerceId, "Comm", Commerce.Category.BAKERY, "Street", 123, "City", "Prov", "1000", "08:00", "20:00");
    }

    @Test
    public void testCreatePackWhenVeganTagProvided() {
        // 1. Setup
        final List<PackTag> tags = Collections.singletonList(PackTag.VEGAN);

        // 2. Ejercicio
        final Pack pack = packDao.createPack(commerceId, "Title", "Desc", 1000.0, 500.0, 10, tags, null);

        // 3. Asserts
        assertNotNull(pack);
        assertEquals("Title", pack.getTitle());
        assertEquals(1, pack.getTags().size());
        assertEquals(PackTag.VEGAN, pack.getTags().get(0));
        assertEquals(1, JdbcTestUtils.countRowsInTable(jdbcTemplate, "packs"));
        assertEquals(1, JdbcTestUtils.countRowsInTable(jdbcTemplate, "pack_tags"));
    }

    @Test
    public void testFindByIdWhenPackExists() {
        // 1. Setup
        final Pack created = packDao.createPack(commerceId, "Title", "Desc", 1000.0, 500.0, 10, null, null);

        // 2. Ejercicio
        final Optional<Pack> pack = packDao.findById(created.getId());

        // 3. Asserts
        assertTrue(pack.isPresent());
        assertEquals(created.getId(), pack.get().getId());
        assertEquals(1, JdbcTestUtils.countRowsInTable(jdbcTemplate, "packs"));
    }

    @Test
    public void testFindAllWhenTwoPacksExist() {
        // 1. Setup
        packDao.createPack(commerceId, "Title1", "Desc1", 1000.0, 500.0, 10, null, null);
        packDao.createPack(commerceId, "Title2", "Desc2", 2000.0, 1000.0, 5, null, null);

        // 2. Ejercicio
        final List<Pack> packs = packDao.findAll();

        // 3. Asserts
        assertEquals(2, packs.size());
        assertEquals(2, JdbcTestUtils.countRowsInTable(jdbcTemplate, "packs"));
    }

    @Test
    public void testFindByCommerceIdWhenTwoCommercesHavePacks() {
        // 1. Setup
        packDao.createPack(commerceId, "Title1", "Desc1", 1000.0, 500.0, 10, null, null);
        final Long otherCommerceId = userDao.createUser("other@example.com", "pass", "Other", "123", User.Role.COMMERCE).getId();
        commerceDao.createCommerce(otherCommerceId, "Other Comm", Commerce.Category.BAKERY, "Street", 123, "City", "Prov", "1000", "08:00", "20:00");
        packDao.createPack(otherCommerceId, "Title2", "Desc2", 2000.0, 1000.0, 5, null, null);

        // 2. Ejercicio
        final List<Pack> packs = packDao.findByCommerceId(commerceId);

        // 3. Asserts
        assertEquals(1, packs.size());
        assertEquals(commerceId, packs.get(0).getCommerceId());
        assertEquals(2, JdbcTestUtils.countRowsInTable(jdbcTemplate, "packs"));
    }

    @Test
    public void testUpdateWhenPackExists() {
        // 1. Setup
        final Pack created = packDao.createPack(commerceId, "Title", "Desc", 1000.0, 500.0, 10, null, null);
        created.setTitle("New Title");
        created.setStock(5);

        // 2. Ejercicio
        packDao.update(created);

        // 3. Asserts
        final Optional<Pack> updated = packDao.findById(created.getId());
        assertTrue(updated.isPresent());
        assertEquals("New Title", updated.get().getTitle());
        assertEquals(5, updated.get().getStock());
        assertEquals(1, JdbcTestUtils.countRowsInTable(jdbcTemplate, "packs"));
    }

    @Test
    public void testSoftDeleteWhenPackExists() {
        // 1. Setup
        final Pack created = packDao.createPack(commerceId, "Title", "Desc", 1000.0, 500.0, 10, null, null);

        // 2. Ejercicio
        packDao.softDelete(created.getId());

        // 3. Asserts
        final Optional<Pack> deleted = packDao.findById(created.getId());
        assertTrue(deleted.isPresent());
        assertTrue(deleted.get().getDeleted());
        assertEquals(1, JdbcTestUtils.countRowsInTable(jdbcTemplate, "packs"));
    }

    @Test
    public void testDecrementStockWhenStockSufficient() {
        // 1. Setup
        final Pack created = packDao.createPack(commerceId, "Title", "Desc", 1000.0, 500.0, 10, null, null);

        // 2. Ejercicio
        final boolean success = packDao.decrementStock(created.getId(), 3);

        // 3. Asserts
        assertTrue(success);
        final Optional<Pack> updated = packDao.findById(created.getId());
        assertTrue(updated.isPresent());
        assertEquals(7, updated.get().getStock());
        assertEquals(1, JdbcTestUtils.countRowsInTable(jdbcTemplate, "packs"));
    }

    @Test
    public void testIncrementStockWhenPackExists() {
        // 1. Setup
        final Pack created = packDao.createPack(commerceId, "Title", "Desc", 1000.0, 500.0, 10, null, null);

        // 2. Ejercicio
        final boolean success = packDao.incrementStock(created.getId(), 5);

        // 3. Asserts
        assertTrue(success);
        final Optional<Pack> updated = packDao.findById(created.getId());
        assertTrue(updated.isPresent());
        assertEquals(15, updated.get().getStock());
        assertEquals(1, JdbcTestUtils.countRowsInTable(jdbcTemplate, "packs"));
    }

    @Test
    public void testFilterPacksWhenPackHasActiveExpiredAuctionExcludesThatPack() {
        // 1. Setup
        final Pack auctionPack = packDao.createPack(commerceId, "AuctionPack", "Desc", 100.0, 50.0, 1, null, null);
        auctionDao.createAuction(auctionPack.getId(), 10.0, 1.0, EXPIRED_AUCTION_END);
        final Pack directPack = packDao.createPack(commerceId, "DirectPack", "Desc2", 100.0, 50.0, 1, null, null);

        // 2. Ejercicio
        final List<Pack> filtered = packDao.filterPacks(
                null, null, null, null, PackSortOption.DATE_DESC, 1, 10, false);

        // 3. Asserts
        assertEquals(1, filtered.size());
        assertEquals(directPack.getId(), filtered.get(0).getId());
        assertEquals(2, JdbcTestUtils.countRowsInTable(jdbcTemplate, "packs"));
        assertEquals(1, JdbcTestUtils.countRowsInTable(jdbcTemplate, "auctions"));
    }

    @Test
    public void testCountFilteredPacksWhenPackHasActiveExpiredAuctionExcludesThatPack() {
        // 1. Setup
        final Pack auctionPack = packDao.createPack(commerceId, "AuctionPack", "Desc", 100.0, 50.0, 1, null, null);
        auctionDao.createAuction(auctionPack.getId(), 10.0, 1.0, EXPIRED_AUCTION_END);
        packDao.createPack(commerceId, "DirectPack", "Desc2", 100.0, 50.0, 1, null, null);

        // 2. Ejercicio
        final int count = packDao.countFilteredPacks(null, null, null, null, false);

        // 3. Asserts
        assertEquals(1, count);
        assertEquals(2, JdbcTestUtils.countRowsInTable(jdbcTemplate, "packs"));
        assertEquals(1, JdbcTestUtils.countRowsInTable(jdbcTemplate, "auctions"));
    }

    @Test
    public void testFilterPacksWhenRequirePositiveStockExcludesZeroStockPack() {
        // 1. Setup
        packDao.createPack(commerceId, "NoStock", "D", 100.0, 50.0, 0, null, null);
        final Pack inStock = packDao.createPack(commerceId, "InStock", "D2", 100.0, 50.0, 3, null, null);

        // 2. Ejercicio
        final List<Pack> withFilter = packDao.filterPacks(
                null, null, null, null, PackSortOption.DATE_DESC, 1, 10, true);

        // 3. Asserts
        assertEquals(1, withFilter.size());
        assertEquals(inStock.getId(), withFilter.get(0).getId());
        assertEquals(2, JdbcTestUtils.countRowsInTable(jdbcTemplate, "packs"));
    }

    @Test
    public void testCountFilteredPacksWhenRequirePositiveStockExcludesZeroStockPack() {
        // 1. Setup
        packDao.createPack(commerceId, "NoStock", "D", 100.0, 50.0, 0, null, null);
        packDao.createPack(commerceId, "InStock", "D2", 100.0, 50.0, 3, null, null);

        // 2. Ejercicio
        final int countWith = packDao.countFilteredPacks(null, null, null, null, true);

        // 3. Asserts
        assertEquals(1, countWith);
        assertEquals(2, JdbcTestUtils.countRowsInTable(jdbcTemplate, "packs"));
    }

    @Test
    public void testFilterPacksWhenRequirePositiveStockFalseIncludesZeroStockPack() {
        // 1. Setup
        packDao.createPack(commerceId, "NoStock", "D", 100.0, 50.0, 0, null, null);
        packDao.createPack(commerceId, "InStock", "D2", 100.0, 50.0, 3, null, null);

        // 2. Ejercicio
        final List<Pack> withoutFilter = packDao.filterPacks(
                null, null, null, null, PackSortOption.DATE_DESC, 1, 10, false);

        // 3. Asserts
        assertEquals(2, withoutFilter.size());
        assertEquals(2, JdbcTestUtils.countRowsInTable(jdbcTemplate, "packs"));
    }
}
