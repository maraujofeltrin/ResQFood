package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.models.pack.Pack;
import ar.edu.itba.paw.models.user.Commerce;
import ar.edu.itba.paw.models.user.CommerceReview;
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
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestConfig.class)
@Sql("classpath:schema.sql")
public class CommerceReviewJdbcDaoTest {

    private static final int RATING = 5;
    private static final String BODY = "Excelente atención y pack muy rico.";

    @Autowired
    private DataSource dataSource;

    @Autowired
    private CommerceReviewJdbcDao commerceReviewDao;

    @Autowired
    private UserJdbcDao userDao;

    @Autowired
    private ClientJdbcDao clientDao;

    @Autowired
    private CommerceJdbcDao commerceDao;

    @Autowired
    private PackJdbcDao packDao;

    private JdbcTemplate jdbcTemplate;
    private Long commerceId;
    private Long clientId;

    @BeforeEach
    public void setUp() {
        jdbcTemplate = new JdbcTemplate(dataSource);
        JdbcTestUtils.deleteFromTables(jdbcTemplate, "commerce_reviews", "bids", "auctions", "reservation_tokens",
                "pack_tags", "reservations", "packs", "images", "commerces", "clients", "tokens", "users");

        commerceId = userDao.createUser("commerce-review@example.com", "pass", "Commerce", "123",
                User.Role.COMMERCE).getId();
        commerceDao.createCommerce(commerceId, "Comm", Commerce.Category.BAKERY, "Street", 123, "City", "Prov",
                "1000", "08:00", "20:00");

        clientId = userDao.createUser("client-review@example.com", "pass", "Client", "123", User.Role.CLIENT)
                .getId();
        clientDao.createClient(clientId, "Client", "Last", true);
        final Pack pack = packDao.createPack(commerceId, "Pack", "Desc", 1000.0, 500.0, 10,
                Collections.emptyList(), null);
        assertNotNull(pack);
    }

    @Test
    public void testCreateReview() {
        // 1. Setup
        // Base commerce and client are ready.

        // 2. Ejercicio
        final CommerceReview review = commerceReviewDao.createReview(commerceId, clientId, RATING, BODY);

        // 3. Asserts
        assertNotNull(review);
        assertNotNull(review.getId());
        assertEquals(commerceId, review.getCommerceUserId());
        assertEquals(clientId, review.getClientUserId());
        assertEquals(RATING, review.getRating());
        assertEquals(BODY, review.getBody());
        assertNotNull(review.getCreatedAt());
        assertNotNull(review.getUpdatedAt());
        assertEquals(1, JdbcTestUtils.countRowsInTable(jdbcTemplate, "commerce_reviews"));
    }

    @Test
    public void testUpdateReview() {
        // 1. Setup
        final CommerceReview created = commerceReviewDao.createReview(commerceId, clientId, RATING, BODY);

        // 2. Ejercicio
        final CommerceReview updated = commerceReviewDao.updateReview(created.getId(), 3, "Cambió la experiencia.");

        // 3. Asserts
        assertEquals(created.getId(), updated.getId());
        assertEquals(3, updated.getRating());
        assertEquals("Cambió la experiencia.", updated.getBody());
        assertEquals(1, JdbcTestUtils.countRowsInTable(jdbcTemplate, "commerce_reviews"));
    }

    @Test
    public void testFindByClientAndCommerce() {
        // 1. Setup
        final CommerceReview created = commerceReviewDao.createReview(commerceId, clientId, RATING, BODY);

        // 2. Ejercicio
        final Optional<CommerceReview> found = commerceReviewDao.findByClientAndCommerce(clientId, commerceId);

        // 3. Asserts
        assertTrue(found.isPresent());
        assertEquals(created.getId(), found.get().getId());
    }

    @Test
    public void testFindByCommerceId() {
        // 1. Setup
        commerceReviewDao.createReview(commerceId, clientId, RATING, BODY);

        // 2. Ejercicio
        final List<CommerceReview> reviews = commerceReviewDao.findByCommerceId(commerceId, 1, 10);

        // 3. Asserts
        assertEquals(1, reviews.size());
        assertEquals(commerceId, reviews.get(0).getCommerceUserId());
    }

    @Test
    public void testCountByCommerceId() {
        // 1. Setup
        commerceReviewDao.createReview(commerceId, clientId, RATING, BODY);

        // 2. Ejercicio
        final int count = commerceReviewDao.countByCommerceId(commerceId);

        // 3. Asserts
        assertEquals(1, count);
    }

    @Test
    public void testAverageRatingByCommerceId_WhenReviewsExist() {
        // 1. Setup
        commerceReviewDao.createReview(commerceId, clientId, 4, BODY);
        final Long client2Id = userDao.createUser("client2-review@example.com", "pass", "Client2", "456",
                ar.edu.itba.paw.models.user.User.Role.CLIENT).getId();
        clientDao.createClient(client2Id, "Client2", "Last2", true);
        commerceReviewDao.createReview(commerceId, client2Id, 2, "Regular.");

        // 2. Ejercicio
        final Double average = commerceReviewDao.averageRatingByCommerceId(commerceId);

        // 3. Asserts
        assertNotNull(average);
        assertEquals(3.0, average, 0.01);
    }

    @Test
    public void testAverageRatingByCommerceId_WhenNoReviews() {
        // 1. Setup — no reviews created

        // 2. Ejercicio
        final Double average = commerceReviewDao.averageRatingByCommerceId(commerceId);

        // 3. Asserts
        assertNull(average);
    }
}
