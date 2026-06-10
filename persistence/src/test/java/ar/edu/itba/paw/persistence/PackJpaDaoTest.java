package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.models.auction.Auction;
import ar.edu.itba.paw.models.pack.Municipality;
import ar.edu.itba.paw.models.pack.Pack;
import ar.edu.itba.paw.models.pack.PackSortOption;
import ar.edu.itba.paw.models.pack.PackTag;
import ar.edu.itba.paw.models.image.Image;
import ar.edu.itba.paw.models.user.Commerce;
import ar.edu.itba.paw.models.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.jdbc.JdbcTestUtils;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.sql.DataSource;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Rollback
@Transactional
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestConfig.class)
public class PackJpaDaoTest {

    private static final LocalDateTime EXPIRED_AUCTION_END = LocalDateTime.of(2020, 1, 1, 0, 0);

    @Autowired
    private DataSource dataSource;

    @Autowired
    private PackDao packDao;

    @Autowired
    private UserDao userDao;

    @Autowired
    private CommerceDao commerceDao;

    @Autowired
    private AuctionDao auctionDao;

    @Autowired
    private ImageJpaDao imageDao;

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
    public void testCreatePackWhenTagsProvidedPersistsTags() {
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
        assertTrue(Hibernate.isInitialized(pack.get().getCommerce()));
        assertNotNull(pack.get().getCommerce().getCommercialName());
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
    public void testFilterPacksWhenVeganTagProvidedReturnsMatchingPack() {
        // 1. Setup
        final Pack tagged = packDao.createPack(commerceId, "Bread Basket", "Desc", 1000.0, 500.0, 5,
                List.of(PackTag.VEGAN, PackTag.SWEET), null);
        packDao.createPack(commerceId, "Cookies", "Desc", 1000.0, 500.0, 0,
                Collections.singletonList(PackTag.SWEET), null);
        em.flush();

        // 2. Ejercicio
        final List<Pack> filtered = packDao.filterPacks(null, Collections.singletonList(PackTag.VEGAN), null, null,
                PackSortOption.DATE_DESC, 1, 10, false, null);

        // 3. Asserts
        assertEquals(1, filtered.size());
        assertEquals(tagged.getId(), filtered.get(0).getId());
        assertEquals(2, JdbcTestUtils.countRowsInTable(jdbcTemplate, "packs"));
    }

    @Test
    public void testFilterPacksWhenQueryMatchesTitleReturnsPack() {
        // 1. Setup
        final Pack tagged = packDao.createPack(commerceId, "Bread Basket", "Desc", 1000.0, 500.0, 5,
                List.of(PackTag.VEGAN, PackTag.SWEET), null);
        packDao.createPack(commerceId, "Cookies", "Desc", 1000.0, 500.0, 0,
                Collections.singletonList(PackTag.SWEET), null);
        em.flush();

        // 2. Ejercicio
        final List<Pack> filtered = packDao.filterPacks("bread", null, null, null,
                PackSortOption.DATE_DESC, 1, 10, false, null);

        // 3. Asserts
        assertEquals(1, filtered.size());
        assertEquals(tagged.getId(), filtered.get(0).getId());
        assertEquals(2, JdbcTestUtils.countRowsInTable(jdbcTemplate, "packs"));
    }

    @Test
    public void testFilterPacksWhenPositiveStockRequiredReturnsPackWithStock() {
        // 1. Setup
        final Pack tagged = packDao.createPack(commerceId, "Bread Basket", "Desc", 1000.0, 500.0, 5,
                List.of(PackTag.VEGAN, PackTag.SWEET), null);
        packDao.createPack(commerceId, "Cookies", "Desc", 1000.0, 500.0, 0,
                Collections.singletonList(PackTag.SWEET), null);
        em.flush();

        // 2. Ejercicio
        final List<Pack> filtered = packDao.filterPacks(null, null, null, null,
                PackSortOption.DATE_DESC, 1, 10, true, null);

        // 3. Asserts
        assertEquals(1, filtered.size());
        assertEquals(tagged.getId(), filtered.get(0).getId());
        assertEquals(2, JdbcTestUtils.countRowsInTable(jdbcTemplate, "packs"));
    }

    @Test
    public void testCountFilteredPacksWhenVeganTagProvidedReturnsOne() {
        // 1. Setup
        packDao.createPack(commerceId, "Bread Basket", "Desc", 1000.0, 500.0, 5,
                List.of(PackTag.VEGAN, PackTag.SWEET), null);
        packDao.createPack(commerceId, "Cookies", "Desc", 1000.0, 500.0, 0,
                Collections.singletonList(PackTag.SWEET), null);
        em.flush();

        // 2. Ejercicio
        final int count = packDao.countFilteredPacks(null, Collections.singletonList(PackTag.VEGAN), null, null, false, null);

        // 3. Asserts
        assertEquals(1, count);
        assertEquals(2, JdbcTestUtils.countRowsInTable(jdbcTemplate, "packs"));
        assertEquals(3, JdbcTestUtils.countRowsInTable(jdbcTemplate, "pack_tags"));
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
        final List<Pack> filtered = packDao.filterPacks(null, null, null, null, PackSortOption.DATE_DESC, 1, 10, false, null);

        // 3. Asserts
        assertEquals(1, filtered.size());
        assertEquals(directPack.getId(), filtered.get(0).getId());
        assertEquals(2, JdbcTestUtils.countRowsInTable(jdbcTemplate, "packs"));
        assertEquals(1, JdbcTestUtils.countRowsInTable(jdbcTemplate, "auctions"));
    }

    @Test
    public void testSoftDeleteWhenPackExistsMarksItAsDeleted() {
        // 1. Setup
        final Pack created = packDao.createPack(commerceId, "Title", "Desc", 1000.0, 500.0, 10, null, null);
        em.flush();

        // 2. Ejercicio
        packDao.softDelete(created.getId());
        em.flush();
        em.clear();

        // 3. Asserts
        final Optional<Pack> softDeleted = packDao.findById(created.getId());
        assertTrue(softDeleted.isPresent());
        assertTrue(softDeleted.get().getDeleted());
        assertEquals(1, JdbcTestUtils.countRowsInTable(jdbcTemplate, "packs"));
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
        assertEquals(1, JdbcTestUtils.countRowsInTable(jdbcTemplate, "packs"));
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
        assertEquals(1, JdbcTestUtils.countRowsInTable(jdbcTemplate, "packs"));
    }

    @Test
    public void testFilterPacksByMorningTimeRangeWhenOpeningTimeIsEightAm() {
        // 1. Setup
        final Pack pack = packDao.createPack(commerceId, "Morning Pack", "Desc", 1000.0, 500.0, 5, null, null);
        em.flush();

        // 2. Ejercicio
        final List<Pack> filtered = packDao.filterPacks(null, null, null, Collections.singletonList("morning"),
                PackSortOption.DATE_DESC, 1, 10, false, null);

        // 3. Asserts
        assertEquals(1, filtered.size());
        assertEquals(pack.getId(), filtered.get(0).getId());
        assertEquals(1, JdbcTestUtils.countRowsInTable(jdbcTemplate, "packs"));
    }

    @Test
    public void testCountFilteredPacksByMorningTimeRangeWhenOpeningTimeIsEightAm() {
        // 1. Setup
        packDao.createPack(commerceId, "Morning Pack", "Desc", 1000.0, 500.0, 5, null, null);
        em.flush();

        // 2. Ejercicio
        final int count = packDao.countFilteredPacks(null, null, null, Collections.singletonList("morning"), false, null);

        // 3. Asserts
        assertEquals(1, count);
        assertEquals(1, JdbcTestUtils.countRowsInTable(jdbcTemplate, "packs"));
    }

    @Test
    public void testFilterPacksRequiresAllTagsWhenMultipleTagsProvided() {
        // 1. Setup
        final Pack bothTags = packDao.createPack(commerceId, "Both Tags", "Desc", 1000.0, 500.0, 5,
                Arrays.asList(PackTag.VEGAN, PackTag.SWEET), null);
        packDao.createPack(commerceId, "Sweet Only", "Desc", 1000.0, 500.0, 5,
                Collections.singletonList(PackTag.SWEET), null);
        em.flush();

        // 2. Ejercicio
        final List<Pack> filtered = packDao.filterPacks(null, Arrays.asList(PackTag.VEGAN, PackTag.SWEET), null, null,
                PackSortOption.DATE_DESC, 1, 10, false, null);

        // 3. Asserts
        assertEquals(1, filtered.size());
        assertEquals(bothTags.getId(), filtered.get(0).getId());
        assertEquals(2, JdbcTestUtils.countRowsInTable(jdbcTemplate, "packs"));
    }

    @Test
    public void testCountFilteredPacksRequiresAllTagsWhenMultipleTagsProvided() {
        // 1. Setup
        packDao.createPack(commerceId, "Both Tags", "Desc", 1000.0, 500.0, 5,
                Arrays.asList(PackTag.VEGAN, PackTag.SWEET), null);
        packDao.createPack(commerceId, "Sweet Only", "Desc", 1000.0, 500.0, 5,
                Collections.singletonList(PackTag.SWEET), null);
        em.flush();

        // 2. Ejercicio
        final int count = packDao.countFilteredPacks(null, Arrays.asList(PackTag.VEGAN, PackTag.SWEET), null, null,
                false, null);

        // 3. Asserts
        assertEquals(1, count);
        assertEquals(2, JdbcTestUtils.countRowsInTable(jdbcTemplate, "packs"));
    }

    @Test
    public void testFilterPacksWhenCommerceUserIdProvidedReturnsOnlyThatCommercePacks() {
        // 1. Setup
        final Long otherCommerceId = userDao.createUser("other@example.com", "pass", "Other", "456", User.Role.COMMERCE)
                .getId();
        commerceDao.createCommerce(otherCommerceId, "Other Comm", Commerce.Category.RESTAURANT, "Other St", 1,
                Municipality.QUILMES, "Prov", "1000", "09:00", "21:00");
        final Pack ownPack = packDao.createPack(commerceId, "Own Pack", "Desc", 100.0, 50.0, 3, null, null);
        packDao.createPack(otherCommerceId, "Other Pack", "Desc", 100.0, 50.0, 3, null, null);
        em.flush();

        // 2. Ejercicio
        final List<Pack> filtered = packDao.filterPacks(null, null, null, null, PackSortOption.DATE_DESC, 1, 10, false,
                commerceId);

        // 3. Asserts
        assertEquals(1, filtered.size());
        assertEquals(ownPack.getId(), filtered.get(0).getId());
        assertEquals(2, JdbcTestUtils.countRowsInTable(jdbcTemplate, "packs"));
    }

    @Test
    public void testFilterPacksEscapesLikeWildcardsInQuery() {
        // 1. Setup
        final Pack pack = packDao.createPack(commerceId, "100% off", "Desc", 1000.0, 500.0, 5, null, null);
        em.flush();

        // 2. Ejercicio
        final List<Pack> filtered = packDao.filterPacks("100%", null, null, null,
                PackSortOption.DATE_DESC, 1, 10, false, null);

        // 3. Asserts
        assertEquals(1, filtered.size());
        assertEquals(pack.getId(), filtered.get(0).getId());
        assertEquals(1, JdbcTestUtils.countRowsInTable(jdbcTemplate, "packs"));
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
        assertEquals(1, JdbcTestUtils.countRowsInTable(jdbcTemplate, "packs"));
    }

    @Test
    public void testFilterPacksEagerlyLoadsCommerce() {
        // 1. Setup
        packDao.createPack(commerceId, "Title", "Desc", 1000.0, 500.0, 10, null, null);
        em.flush();

        // 2. Ejercicio
        final List<Pack> packs = packDao.filterPacks(null, null, null, null, PackSortOption.DATE_DESC, 1, 10, false, null);

        // 3. Asserts
        assertFalse(packs.isEmpty());
        assertTrue(Hibernate.isInitialized(packs.get(0).getCommerce()));
        assertNotNull(packs.get(0).getCommerce().getCommercialName());
        assertEquals(1, JdbcTestUtils.countRowsInTable(jdbcTemplate, "packs"));
    }

    @Test
    public void testFilterPacksOrdersByPriceAscWhenMultiplePacksExist() {
        // 1. Setup
        final Pack cheapest = packDao.createPack(commerceId, "Cheap", "Desc", 1000.0, 100.0, 5, null, null);
        final Pack mid = packDao.createPack(commerceId, "Mid", "Desc", 1000.0, 300.0, 5, null, null);
        final Pack expensive = packDao.createPack(commerceId, "Expensive", "Desc", 1000.0, 500.0, 5, null, null);
        em.flush();

        // 2. Ejercicio
        final List<Pack> filtered = packDao.filterPacks(null, null, null, null, PackSortOption.PRICE_ASC, 1, 10,
                false, null);

        // 3. Asserts
        assertEquals(3, filtered.size());
        assertEquals(cheapest.getId(), filtered.get(0).getId());
        assertEquals(mid.getId(), filtered.get(1).getId());
        assertEquals(expensive.getId(), filtered.get(2).getId());
        assertEquals(3, JdbcTestUtils.countRowsInTable(jdbcTemplate, "packs"));
    }

    @Test
    public void testFilterPacksPaginatesSecondPagePreservingSortOrder() {
        // 1. Setup
        final Pack first = packDao.createPack(commerceId, "First", "Desc", 1000.0, 500.0, 5, null, null);
        em.flush();
        final Pack second = packDao.createPack(commerceId, "Second", "Desc", 1000.0, 500.0, 5, null, null);
        em.flush();
        final Pack third = packDao.createPack(commerceId, "Third", "Desc", 1000.0, 500.0, 5, null, null);
        em.flush();

        // 2. Ejercicio
        final List<Pack> pageTwo = packDao.filterPacks(null, null, null, null, PackSortOption.DATE_DESC, 2, 1,
                false, null);

        // 3. Asserts
        assertEquals(1, pageTwo.size());
        assertEquals(second.getId(), pageTwo.get(0).getId());
        assertEquals(3, JdbcTestUtils.countRowsInTable(jdbcTemplate, "packs"));
    }

    @Test
    public void testFilterCommercePacksReturnsOnlyNonDeletedPacksForCommerce() {
        // 1. Setup
        final Pack visible = packDao.createPack(commerceId, "Visible", "Desc", 1000.0, 500.0, 10, null, null);
        final Pack deleted = packDao.createPack(commerceId, "Deleted", "Desc", 1000.0, 500.0, 10, null, null);
        final Long otherCommerceId = userDao.createUser("other@example.com", "pass", "Other", "456", User.Role.COMMERCE)
                .getId();
        commerceDao.createCommerce(otherCommerceId, "Other Comm", Commerce.Category.RESTAURANT, "Other St", 1,
                Municipality.QUILMES, "Prov", "1000", "09:00", "21:00");
        packDao.createPack(otherCommerceId, "Other Pack", "Desc", 1000.0, 500.0, 10, null, null);
        em.flush();
        packDao.softDelete(deleted.getId());
        em.flush();

        // 2. Ejercicio
        final List<Pack> filtered = packDao.filterCommercePacks(commerceId, null, 1, 10);

        // 3. Asserts
        assertEquals(1, filtered.size());
        assertEquals(visible.getId(), filtered.get(0).getId());
        assertEquals(3, JdbcTestUtils.countRowsInTable(jdbcTemplate, "packs"));
    }

    @Test
    public void testFilterCommercePacksWhenHasAuctionFalseExcludesAuctionPacks() {
        // 1. Setup
        final Pack directPack = packDao.createPack(commerceId, "Direct", "Desc", 1000.0, 500.0, 10, null, null);
        final Pack auctionPack = packDao.createPack(commerceId, "Auction", "Desc", 1000.0, 500.0, 10, null, null);
        em.flush();
        auctionDao.createAuction(auctionPack.getId(), 10.0, 1.0, EXPIRED_AUCTION_END);
        em.flush();

        // 2. Ejercicio
        final List<Pack> filtered = packDao.filterCommercePacks(commerceId, false, 1, 10);

        // 3. Asserts
        assertEquals(1, filtered.size());
        assertEquals(directPack.getId(), filtered.get(0).getId());
        assertEquals(2, JdbcTestUtils.countRowsInTable(jdbcTemplate, "packs"));
        assertEquals(1, JdbcTestUtils.countRowsInTable(jdbcTemplate, "auctions"));
    }

    @Test
    public void testFilterCommercePacksDoesNotEagerlyLoadImage() {
        // 1. Setup
        final Image image = imageDao.saveImage(new byte[] {1, 2, 3}, "image/png");
        em.flush();
        packDao.createPack(commerceId, "With Image", "Desc", 1000.0, 500.0, 10, null, image.getId());
        em.flush();
        em.clear();

        // 2. Ejercicio
        final List<Pack> filtered = packDao.filterCommercePacks(commerceId, null, 1, 10);

        // 3. Asserts
        assertEquals(1, filtered.size());
        // Image is lazy: the proxy is NOT initialized, but imageId is still accessible via FK
        assertFalse(Hibernate.isInitialized(filtered.get(0).getImage()));
        assertNotNull(filtered.get(0).getImageId());
        assertEquals(image.getId(), filtered.get(0).getImageId());
        assertEquals(1, JdbcTestUtils.countRowsInTable(jdbcTemplate, "packs"));
        assertEquals(1, JdbcTestUtils.countRowsInTable(jdbcTemplate, "images"));
    }

    @Test
    public void testFilterCommercePacksPaginatesByIdDesc() {
        // 1. Setup
        packDao.createPack(commerceId, "First", "Desc", 1000.0, 500.0, 5, null, null);
        em.flush();
        final Pack second = packDao.createPack(commerceId, "Second", "Desc", 1000.0, 500.0, 5, null, null);
        em.flush();
        packDao.createPack(commerceId, "Third", "Desc", 1000.0, 500.0, 5, null, null);
        em.flush();

        // 2. Ejercicio
        final List<Pack> pageTwo = packDao.filterCommercePacks(commerceId, null, 2, 1);

        // 3. Asserts
        assertEquals(1, pageTwo.size());
        assertEquals(second.getId(), pageTwo.get(0).getId());
        assertEquals(3, JdbcTestUtils.countRowsInTable(jdbcTemplate, "packs"));
    }

    @Test
    public void testPackAuctionInverseIsHydratedWhenAuctionExists() {
        // 1. Setup
        final Pack pack = packDao.createPack(commerceId, "Auction Pack", "Desc", 100.0, 50.0, 1, null, null);
        em.flush();
        auctionDao.createAuction(pack.getId(), 10.0, 1.0, EXPIRED_AUCTION_END);
        em.flush();
        em.clear();

        // 2. Ejercicio
        final Optional<Pack> reloaded = packDao.findById(pack.getId());

        // 3. Asserts
        assertTrue(reloaded.isPresent());
        assertNotNull(reloaded.get().getAuction());
        assertEquals(pack.getId(), reloaded.get().getAuction().getPack().getId());
        assertEquals(Auction.Status.ACTIVE, reloaded.get().getAuction().getStatus());
        assertEquals(1, JdbcTestUtils.countRowsInTable(jdbcTemplate, "auctions"));
    }

    @Test
    public void testPackAuctionInverseIsNullWhenNoAuction() {
        // 1. Setup
        final Pack pack = packDao.createPack(commerceId, "Direct Pack", "Desc", 100.0, 50.0, 1, null, null);
        em.flush();
        em.clear();

        // 2. Ejercicio
        final Optional<Pack> reloaded = packDao.findById(pack.getId());

        // 3. Asserts
        assertTrue(reloaded.isPresent());
        assertNull(reloaded.get().getAuction());
        assertEquals(1, JdbcTestUtils.countRowsInTable(jdbcTemplate, "packs"));
    }

    @Test
    public void testFindPublicOffersByCommerceIncludesDirectPacksAndAuctionPacks() {
        // 1. Setup
        final Pack direct = packDao.createPack(commerceId, "Direct", "Desc", 100.0, 50.0, 3, null, null);
        final Pack auctionPack = packDao.createPack(commerceId, "Auction", "Desc", 100.0, 50.0, 1, null, null);
        em.flush();
        auctionDao.createAuction(auctionPack.getId(), 10.0, 1.0, EXPIRED_AUCTION_END);
        em.flush();
        em.clear();

        // 2. Ejercicio
        final List<Pack> offers = packDao.findPublicOffersByCommerce(commerceId, 1, 10);

        // 3. Asserts
        assertEquals(2, offers.size());
        assertEquals(auctionPack.getId(), offers.get(0).getId());
        assertEquals(direct.getId(), offers.get(1).getId());
        assertNotNull(offers.get(0).getAuction());
        assertNull(offers.get(1).getAuction());
        assertEquals(2, JdbcTestUtils.countRowsInTable(jdbcTemplate, "packs"));
    }

    @Test
    public void testFindPublicOffersByCommerceExcludesDirectPackWithZeroStock() {
        // 1. Setup
        final Pack withStock = packDao.createPack(commerceId, "Has Stock", "Desc", 100.0, 50.0, 1, null, null);
        packDao.createPack(commerceId, "No Stock", "Desc", 100.0, 50.0, 0, null, null);
        em.flush();

        // 2. Ejercicio
        final List<Pack> offers = packDao.findPublicOffersByCommerce(commerceId, 1, 10);

        // 3. Asserts
        assertEquals(1, offers.size());
        assertEquals(withStock.getId(), offers.get(0).getId());
        assertEquals(2, JdbcTestUtils.countRowsInTable(jdbcTemplate, "packs"));
    }

    @Test
    public void testFindPublicOffersByCommerceExcludesInactiveAndDeletedPacks() {
        // 1. Setup
        final Pack visible = packDao.createPack(commerceId, "Visible", "Desc", 100.0, 50.0, 1, null, null);
        final Pack inactive = packDao.createPack(commerceId, "Inactive", "Desc", 100.0, 50.0, 1, null, null);
        final Pack deleted = packDao.createPack(commerceId, "Deleted", "Desc", 100.0, 50.0, 1, null, null);
        em.flush();
        inactive.setActive(false);
        packDao.update(inactive);
        packDao.softDelete(deleted.getId());
        em.flush();

        // 2. Ejercicio
        final List<Pack> offers = packDao.findPublicOffersByCommerce(commerceId, 1, 10);

        // 3. Asserts
        assertEquals(1, offers.size());
        assertEquals(visible.getId(), offers.get(0).getId());
        assertEquals(3, JdbcTestUtils.countRowsInTable(jdbcTemplate, "packs"));
    }

    @Test
    public void testCountPublicOffersByCommerceMatchesFindResults() {
        // 1. Setup
        packDao.createPack(commerceId, "A", "Desc", 100.0, 50.0, 1, null, null);
        packDao.createPack(commerceId, "B", "Desc", 100.0, 50.0, 1, null, null);
        final Pack auctionPack = packDao.createPack(commerceId, "Auction", "Desc", 100.0, 50.0, 1, null, null);
        em.flush();
        auctionDao.createAuction(auctionPack.getId(), 10.0, 1.0, EXPIRED_AUCTION_END);
        em.flush();

        // 2. Ejercicio
        final int count = packDao.countPublicOffersByCommerce(commerceId);

        // 3. Asserts
        assertEquals(3, count);
        assertEquals(3, JdbcTestUtils.countRowsInTable(jdbcTemplate, "packs"));
    }

    @Test
    public void testFindPublicOffersByCommercePaginatesByIdDesc() {
        // 1. Setup
        packDao.createPack(commerceId, "First", "Desc", 100.0, 50.0, 1, null, null);
        em.flush();
        final Pack second = packDao.createPack(commerceId, "Second", "Desc", 100.0, 50.0, 1, null, null);
        em.flush();
        packDao.createPack(commerceId, "Third", "Desc", 100.0, 50.0, 1, null, null);
        em.flush();

        // 2. Ejercicio
        final List<Pack> pageTwo = packDao.findPublicOffersByCommerce(commerceId, 2, 1);

        // 3. Asserts
        assertEquals(1, pageTwo.size());
        assertEquals(second.getId(), pageTwo.get(0).getId());
        assertEquals(3, JdbcTestUtils.countRowsInTable(jdbcTemplate, "packs"));
    }

    @Test
    public void testFindByIdWithAuctionHydratesAuctionWhenPresent() {
        // 1. Setup
        final Pack pack = packDao.createPack(commerceId, "Auction Pack", "Desc", 100.0, 50.0, 1, null, null);
        em.flush();
        auctionDao.createAuction(pack.getId(), 10.0, 1.0, EXPIRED_AUCTION_END);
        em.flush();
        em.clear();

        // 2. Ejercicio
        final Optional<Pack> reloaded = packDao.findById(pack.getId());

        // 3. Asserts
        assertTrue(reloaded.isPresent());
        assertTrue(Hibernate.isInitialized(reloaded.get().getCommerce()));
        assertNotNull(reloaded.get().getAuction());
        assertTrue(Hibernate.isInitialized(reloaded.get().getAuction()));
        assertEquals(pack.getId(), reloaded.get().getAuction().getPack().getId());
        assertEquals(1, JdbcTestUtils.countRowsInTable(jdbcTemplate, "packs"));
        assertEquals(1, JdbcTestUtils.countRowsInTable(jdbcTemplate, "auctions"));
    }

    @Test
    public void testFindByIdWithAuctionReturnsNullAuctionWhenNone() {
        // 1. Setup
        final Pack pack = packDao.createPack(commerceId, "Direct Pack", "Desc", 100.0, 50.0, 1, null, null);
        em.flush();
        em.clear();

        // 2. Ejercicio
        final Optional<Pack> reloaded = packDao.findById(pack.getId());

        // 3. Asserts
        assertTrue(reloaded.isPresent());
        assertNull(reloaded.get().getAuction());
        assertEquals(1, JdbcTestUtils.countRowsInTable(jdbcTemplate, "packs"));
    }

    @Test
    public void testFilterCommercePacksHydratesAuctionWhenPresent() {
        // 1. Setup
        final Pack direct = packDao.createPack(commerceId, "Direct", "Desc", 100.0, 50.0, 3, null, null);
        final Pack auctionPack = packDao.createPack(commerceId, "Auction", "Desc", 100.0, 50.0, 1, null, null);
        em.flush();
        auctionDao.createAuction(auctionPack.getId(), 10.0, 1.0, EXPIRED_AUCTION_END);
        em.flush();
        em.clear();

        // 2. Ejercicio
        final List<Pack> packs = packDao.filterCommercePacks(commerceId, null, 1, 10);

        // 3. Asserts
        assertEquals(2, packs.size());
        for (final Pack p : packs) {
            if (p.getId().equals(auctionPack.getId())) {
                assertNotNull(p.getAuction());
                assertTrue(Hibernate.isInitialized(p.getAuction()));
            } else if (p.getId().equals(direct.getId())) {
                assertNull(p.getAuction());
            }
        }
        assertEquals(2, JdbcTestUtils.countRowsInTable(jdbcTemplate, "packs"));
        assertEquals(1, JdbcTestUtils.countRowsInTable(jdbcTemplate, "auctions"));
    }

    @Test
    public void testCountCommercePacksWhenNoAuctionFilterReturnsAllPacks() {
        // 1. Setup
        packDao.createPack(commerceId, "Direct One", "Desc", 1000.0, 500.0, 10, null, null);
        packDao.createPack(commerceId, "Direct Two", "Desc", 1000.0, 500.0, 10, null, null);
        final Pack auctionPack = packDao.createPack(commerceId, "Auction", "Desc", 1000.0, 500.0, 10, null, null);
        em.flush();
        auctionDao.createAuction(auctionPack.getId(), 10.0, 1.0, EXPIRED_AUCTION_END);
        em.flush();

        // 2. Ejercicio
        final int allCount = packDao.countCommercePacks(commerceId, null);

        // 3. Asserts
        assertEquals(3, allCount);
        assertEquals(3, JdbcTestUtils.countRowsInTable(jdbcTemplate, "packs"));
    }

    @Test
    public void testCountCommercePacksWhenDirectFilterReturnsDirectPacksOnly() {
        // 1. Setup
        packDao.createPack(commerceId, "Direct One", "Desc", 1000.0, 500.0, 10, null, null);
        packDao.createPack(commerceId, "Direct Two", "Desc", 1000.0, 500.0, 10, null, null);
        final Pack auctionPack = packDao.createPack(commerceId, "Auction", "Desc", 1000.0, 500.0, 10, null, null);
        em.flush();
        auctionDao.createAuction(auctionPack.getId(), 10.0, 1.0, EXPIRED_AUCTION_END);
        em.flush();

        // 2. Ejercicio
        final int directCount = packDao.countCommercePacks(commerceId, false);

        // 3. Asserts
        assertEquals(2, directCount);
        assertEquals(3, JdbcTestUtils.countRowsInTable(jdbcTemplate, "packs"));
    }

    @Test
    public void testCountCommercePacksWhenAuctionFilterReturnsAuctionPacksOnly() {
        // 1. Setup
        packDao.createPack(commerceId, "Direct One", "Desc", 1000.0, 500.0, 10, null, null);
        packDao.createPack(commerceId, "Direct Two", "Desc", 1000.0, 500.0, 10, null, null);
        final Pack auctionPack = packDao.createPack(commerceId, "Auction", "Desc", 1000.0, 500.0, 10, null, null);
        em.flush();
        auctionDao.createAuction(auctionPack.getId(), 10.0, 1.0, EXPIRED_AUCTION_END);
        em.flush();

        // 2. Ejercicio
        final int auctionCount = packDao.countCommercePacks(commerceId, true);

        // 3. Asserts
        assertEquals(1, auctionCount);
        assertEquals(3, JdbcTestUtils.countRowsInTable(jdbcTemplate, "packs"));
    }
}
