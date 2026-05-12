package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.models.user.Commerce;
import ar.edu.itba.paw.models.user.CommerceReview;
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

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.sql.DataSource;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@Rollback
@Transactional
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestConfig.class)
public class CommerceReviewJpaDaoTest {

    private static final int RATING = 5;
    private static final String BODY = "Excelente atención y pack muy rico.";

    @Autowired
    private DataSource dataSource;

    @Autowired
    private CommerceReviewJpaDao commerceReviewDao;

    @Autowired
    private UserJpaDao userDao;

    @Autowired
    private ClientJpaDao clientDao;

    @Autowired
    private CommerceJpaDao commerceDao;

    // Pack is still JDBC since it hasn't been migrated yet.
    @Autowired
    private PackJdbcDao packDao;

    @PersistenceContext
    private EntityManager em;

    private JdbcTemplate jdbcTemplate;
    private Long commerceId;
    private Long clientId;

    @BeforeEach
    public void setUp() {
        jdbcTemplate = new JdbcTemplate(dataSource);
        JdbcTestUtils.deleteFromTables(jdbcTemplate, "commerce_reviews", "bids", "auctions", "reservation_tokens",
                "pack_tags", "reservations", "client_pack_favorites", "packs", "images", "commerces", "clients", "tokens",
                "users");

        commerceId = userDao.createUser("commerce-review@example.com", "pass", "Commerce", "123",
                User.Role.COMMERCE).getId();
        em.flush();
        commerceDao.createCommerce(commerceId, "Comm", Commerce.Category.BAKERY, "Street", 123, ar.edu.itba.paw.models.pack.Municipality.AVELLANEDA, "Prov",
                "1000", "08:00", "20:00");
        em.flush();

        clientId = userDao.createUser("client-review@example.com", "pass", "Client", "123", User.Role.CLIENT)
                .getId();
        em.flush();
        clientDao.createClient(clientId, "Client", "Last", true);
        em.flush();
        
        packDao.createPack(commerceId, "Pack", "Desc", 1000.0, 500.0, 10, Collections.emptyList(), null);
    }

    @Test
    public void testCreateReviewWhenCommerceAndClientExist() {
        // 1. Setup
        // Base commerce, client and pack from setUp().

        // 2. Ejercicio
        final CommerceReview review = commerceReviewDao.createReview(commerceId, clientId, RATING, BODY);
        em.flush();

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
    public void testUpdateReviewWhenReviewExists() {
        // 1. Setup
        final CommerceReview created = commerceReviewDao.createReview(commerceId, clientId, RATING, BODY);
        em.flush();

        // 2. Ejercicio
        final CommerceReview updated = commerceReviewDao.updateReview(created.getId(), 3, "Cambió la experiencia.");
        em.flush();

        // 3. Asserts
        assertEquals(created.getId(), updated.getId());
        assertEquals(3, updated.getRating());
        assertEquals("Cambió la experiencia.", updated.getBody());
        assertEquals(1, JdbcTestUtils.countRowsInTable(jdbcTemplate, "commerce_reviews"));
    }

    @Test
    public void testFindByClientAndCommerceWhenReviewExists() {
        // 1. Setup
        final CommerceReview created = commerceReviewDao.createReview(commerceId, clientId, RATING, BODY);
        em.flush();

        // 2. Ejercicio
        final Optional<CommerceReview> found = commerceReviewDao.findByClientAndCommerce(clientId, commerceId);

        // 3. Asserts
        assertTrue(found.isPresent());
        assertEquals(created.getId(), found.get().getId());
        assertEquals(1, JdbcTestUtils.countRowsInTable(jdbcTemplate, "commerce_reviews"));
    }

    @Test
    public void testFindByCommerceIdWhenOneReviewExists() {
        // 1. Setup
        commerceReviewDao.createReview(commerceId, clientId, RATING, BODY);
        em.flush();

        // 2. Ejercicio
        final List<CommerceReview> reviews = commerceReviewDao.findByCommerceId(commerceId, 1, 10);

        // 3. Asserts
        assertEquals(1, reviews.size());
        assertEquals(commerceId, reviews.get(0).getCommerceUserId());
        assertEquals(1, JdbcTestUtils.countRowsInTable(jdbcTemplate, "commerce_reviews"));
    }

    @Test
    public void testCountByCommerceIdWhenOneReviewExists() {
        // 1. Setup
        commerceReviewDao.createReview(commerceId, clientId, RATING, BODY);
        em.flush();

        // 2. Ejercicio
        final int count = commerceReviewDao.countByCommerceId(commerceId);

        // 3. Asserts
        assertEquals(1, count);
        assertEquals(1, JdbcTestUtils.countRowsInTable(jdbcTemplate, "commerce_reviews"));
    }

    @Test
    public void testAverageRatingByCommerceIdWhenReviewsExist() {
        // 1. Setup
        commerceReviewDao.createReview(commerceId, clientId, 4, BODY);
        em.flush();
        final Long client2Id = userDao.createUser("client2-review@example.com", "pass", "Client2", "456",
                User.Role.CLIENT).getId();
        em.flush();
        clientDao.createClient(client2Id, "Client2", "Last2", true);
        em.flush();
        commerceReviewDao.createReview(commerceId, client2Id, 2, "Regular.");
        em.flush();

        // 2. Ejercicio
        final Double average = commerceReviewDao.averageRatingByCommerceId(commerceId);

        // 3. Asserts
        assertNotNull(average);
        assertEquals(3.0, average, 0.01);
        assertEquals(2, JdbcTestUtils.countRowsInTable(jdbcTemplate, "commerce_reviews"));
    }

    @Test
    public void testAverageRatingByCommerceIdWhenNoReviews() {
        // 1. Setup
        // No reviews inserted for this commerce.

        // 2. Ejercicio
        final Double average = commerceReviewDao.averageRatingByCommerceId(commerceId);

        // 3. Asserts
        assertNull(average);
        assertEquals(0, JdbcTestUtils.countRowsInTable(jdbcTemplate, "commerce_reviews"));
    }
}
