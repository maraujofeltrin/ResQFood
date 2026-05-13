package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.models.pack.Municipality;
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
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.jdbc.JdbcTestUtils;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.sql.DataSource;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Rollback
@Transactional
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestConfig.class)
@Sql("classpath:schema.sql")
public class PackJpaDaoTest {

    private static final LocalDateTime EXPIRED_AUCTION_END = LocalDateTime.of(2020, 1, 1, 0, 0);

    @Autowired
    private DataSource dataSource;

    @Autowired
    private PackJpaDao packDao;

    @Autowired
    private UserJdbcDao userDao;

    @Autowired
    private CommerceJdbcDao commerceDao;

    @Autowired
    private AuctionJdbcDao auctionDao;

    @PersistenceContext
    private EntityManager em;

    private JdbcTemplate jdbcTemplate;

    private Long commerceId;

    @BeforeEach
    public void setUp() {
        jdbcTemplate = new JdbcTemplate(dataSource);
        JdbcTestUtils.deleteFromTables(jdbcTemplate, "commerce_reviews", "bids", "auctions", "reservation_tokens",
                "pack_tags", "reservations", "client_pack_favorites", "packs", "images", "commerces", "clients",
                "tokens", "users");
        commerceId = userDao.createUser("commerce@example.com", "pass", "Commerce", "123", User.Role.COMMERCE).getId();
        commerceDao.createCommerce(commerceId, "Comm", Commerce.Category.BAKERY, "Street", 123, Municipality.AVELLANEDA,
                "Prov", "1000", "08:00", "20:00");
    }

    @Test
    public void testCreatePackPersistsTags() {
        // 1. Setup
        final List<PackTag> tags = Collections.singletonList(PackTag.VEGAN);

        // 2. Ejercicio
        final Pack pack = packDao.createPack(commerceId, "Title", "Desc", 1000.0, 500.0, 10, tags, null);
        em.flush();

        // 3. Asserts
        assertNotNull(pack.getId());
        assertEquals("Title", pack.getTitle());
        assertEquals(1, pack.getTags().size());
        assertEquals(PackTag.VEGAN, pack.getTags().get(0));
        assertEquals(1, JdbcTestUtils.countRowsInTable(jdbcTemplate, "packs"));
        assertEquals(1, JdbcTestUtils.countRowsInTable(jdbcTemplate, "pack_tags"));
    }

    @Test
    public void testFindByIdWhenPackExists() {
        // 1. Setup
        final Pack created = packDao.createPack(commerceId, "Title", "Desc", 1000.0, 500.0, 10,
                Collections.singletonList(PackTag.SWEET), null);
        em.flush();
        em.clear();

        // 2. Ejercicio
        final Optional<Pack> pack = packDao.findById(created.getId());

        // 3. Asserts
        assertTrue(pack.isPresent());
        assertEquals(created.getId(), pack.get().getId());
        assertEquals("Title", pack.get().getTitle());
        assertEquals(1, pack.get().getTags().size());
        assertEquals(1, JdbcTestUtils.countRowsInTable(jdbcTemplate, "packs"));
    }

    @Test
    public void testFindByIdWhenPackDoesNotExist() {
        // 1. Setup
        // No pack row for this id.

        // 2. Ejercicio
        final Optional<Pack> pack = packDao.findById(999L);

        // 3. Asserts
        assertTrue(pack.isEmpty());
    }

    @Test
    public void testFindByCommerceIdExcludesDeletedPacks() {
        // 1. Setup
        final Pack visible = packDao.createPack(commerceId, "Visible", "Desc", 1000.0, 500.0, 10, null, null);
        final Pack deleted = packDao.createPack(commerceId, "Deleted", "Desc", 1000.0, 500.0, 10, null, null);
        em.flush();
        packDao.softDelete(deleted.getId());
        em.flush();
        em.clear();

        // 2. Ejercicio
        final List<Pack> packs = packDao.findByCommerceId(commerceId);

        // 3. Asserts
        assertEquals(1, packs.size());
        assertEquals(visible.getId(), packs.get(0).getId());
    }

    @Test
    public void testFilterPacksByTagQueryAndPositiveStock() {
        // 1. Setup
        final Pack tagged = packDao.createPack(commerceId, "Bread Basket", "Desc", 1000.0, 500.0, 5,
                List.of(PackTag.VEGAN, PackTag.SWEET), null);
        packDao.createPack(commerceId, "Cookies", "Desc", 1000.0, 500.0, 0,
                Collections.singletonList(PackTag.SWEET), null);
        em.flush();

        // 2. Ejercicio
        final List<Pack> byTag = packDao.filterPacks(null, Collections.singletonList(PackTag.VEGAN), null, null,
                PackSortOption.DATE_DESC, 1, 10, false);
        final List<Pack> byQuery = packDao.filterPacks("bread", null, null, null,
                PackSortOption.DATE_DESC, 1, 10, false);
        final List<Pack> positiveStock = packDao.filterPacks(null, null, null, null,
                PackSortOption.DATE_DESC, 1, 10, true);

        // 3. Asserts
        assertEquals(1, byTag.size());
        assertEquals(tagged.getId(), byTag.get(0).getId());
        assertEquals(1, byQuery.size());
        assertEquals(tagged.getId(), byQuery.get(0).getId());
        assertEquals(1, positiveStock.size());
        assertEquals(tagged.getId(), positiveStock.get(0).getId());
        assertEquals(2, JdbcTestUtils.countRowsInTable(jdbcTemplate, "packs"));
        assertEquals(3, JdbcTestUtils.countRowsInTable(jdbcTemplate, "pack_tags"));
    }

    @Test
    public void testCountFilteredPacksMatchesFilterSize() {
        // 1. Setup
        packDao.createPack(commerceId, "Bread Basket", "Desc", 1000.0, 500.0, 5,
                List.of(PackTag.VEGAN, PackTag.SWEET), null);
        packDao.createPack(commerceId, "Cookies", "Desc", 1000.0, 500.0, 0,
                Collections.singletonList(PackTag.SWEET), null);
        em.flush();

        // 2. Ejercicio
        final List<Pack> filtered = packDao.filterPacks(null, Collections.singletonList(PackTag.VEGAN), null, null,
                PackSortOption.DATE_DESC, 1, 10, false);
        final int count = packDao.countFilteredPacks(null, Collections.singletonList(PackTag.VEGAN), null, null, false);

        // 3. Asserts
        assertEquals(filtered.size(), count);
    }

    @Test
    public void testFilterPacksExcludesActiveAuctionPack() {
        // 1. Setup
        final Pack auctionPack = packDao.createPack(commerceId, "AuctionPack", "Desc", 100.0, 50.0, 1, null, null);
        em.flush();
        auctionDao.createAuction(auctionPack.getId(), 10.0, 1.0, EXPIRED_AUCTION_END);
        final Pack directPack = packDao.createPack(commerceId, "DirectPack", "Desc2", 100.0, 50.0, 1, null, null);
        em.flush();

        // 2. Ejercicio
        final List<Pack> filtered = packDao.filterPacks(null, null, null, null, PackSortOption.DATE_DESC, 1, 10, false);

        // 3. Asserts
        assertEquals(1, filtered.size());
        assertEquals(directPack.getId(), filtered.get(0).getId());
        assertEquals(2, JdbcTestUtils.countRowsInTable(jdbcTemplate, "packs"));
        assertEquals(1, JdbcTestUtils.countRowsInTable(jdbcTemplate, "auctions"));
    }

    @Test
    public void testSoftDeleteHidesPackFromFindByCommerceId() {
        // 1. Setup
        final Pack created = packDao.createPack(commerceId, "Title", "Desc", 1000.0, 500.0, 10, null, null);
        em.flush();

        // 2. Ejercicio
        packDao.softDelete(created.getId());
        em.flush();
        em.clear();

        // 3. Asserts
        final List<Pack> packs = packDao.findByCommerceId(commerceId);
        assertTrue(packs.isEmpty());
        assertTrue(packDao.findById(created.getId()).isPresent());
        assertTrue(packDao.findById(created.getId()).map(Pack::getDeleted).orElse(false));
    }

    @Test
    public void testSetActiveChangesField() {
        // 1. Setup
        final Pack created = packDao.createPack(commerceId, "Title", "Desc", 1000.0, 500.0, 10, null, null);
        em.flush();

        // 2. Ejercicio
        packDao.setActive(created.getId(), false);
        em.flush();
        em.clear();

        // 3. Asserts
        assertFalse(packDao.findById(created.getId()).map(Pack::getActive).orElse(true));
    }

    @Test
    public void testDecrementStockWhenStockSufficient() {
        // 1. Setup
        final Pack created = packDao.createPack(commerceId, "Title", "Desc", 1000.0, 500.0, 10, null, null);
        em.flush();

        // 2. Ejercicio
        final boolean success = packDao.decrementStock(created.getId(), 3);
        em.flush();
        em.clear();

        // 3. Asserts
        assertTrue(success);
        assertEquals(7, packDao.findById(created.getId()).map(Pack::getStock).orElse(-1));
    }

    @Test
    public void testDecrementStockWhenStockInsufficient() {
        // 1. Setup
        final Pack created = packDao.createPack(commerceId, "Title", "Desc", 1000.0, 500.0, 2, null, null);
        em.flush();

        // 2. Ejercicio
        final boolean success = packDao.decrementStock(created.getId(), 3);
        em.flush();

        // 3. Asserts
        assertFalse(success);
        assertEquals(2, packDao.findById(created.getId()).map(Pack::getStock).orElse(-1));
    }

    @Test
    public void testIncrementStockWhenPackExists() {
        // 1. Setup
        final Pack created = packDao.createPack(commerceId, "Title", "Desc", 1000.0, 500.0, 10, null, null);
        em.flush();

        // 2. Ejercicio
        final boolean success = packDao.incrementStock(created.getId(), 5);
        em.flush();
        em.clear();

        // 3. Asserts
        assertTrue(success);
        assertEquals(15, packDao.findById(created.getId()).map(Pack::getStock).orElse(-1));
    }
}