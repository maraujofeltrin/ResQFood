package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.models.pack.Municipality;
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

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.sql.DataSource;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@Rollback
@Transactional
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestConfig.class)
public class CommerceJpaDaoTest {

    private static final String EMAIL = "test@example.com";
    private static final String PASSWORD = "password123";
    private static final String NAME = "Test";
    private static final String PHONE = "123456789";

    private static final String COMMERCIAL_NAME = "Test Commerce";
    private static final Commerce.Category CATEGORY = Commerce.Category.BAKERY;
    private static final String STREET = "Av. Siempre Viva";
    private static final Integer STREET_NUMBER = 742;
    private static final Municipality CITY = Municipality.AVELLANEDA;
    private static final String PROVINCE = "Buenos Aires";
    private static final String POSTAL_CODE = "1000";
    private static final String OPENING_TIME = "08:00";
    private static final String CLOSING_TIME = "20:00";

    @Autowired
    private DataSource dataSource;

    @Autowired
    private CommerceJpaDao commerceDao;

    @Autowired
    private UserJpaDao userDao;

    @PersistenceContext
    private EntityManager em;

    private JdbcTemplate jdbcTemplate;

    private Long userId;

    @BeforeEach
    public void setUp() {
        jdbcTemplate = new JdbcTemplate(dataSource);
        JdbcTestUtils.deleteFromTables(jdbcTemplate, "bids", "auctions", "reservation_tokens", "pack_tags", "reservations", "client_pack_favorites", "packs", "images", "commerces", "clients", "tokens", "users");

        userId = userDao.createUser(EMAIL, PASSWORD, NAME, PHONE, User.Role.COMMERCE).getId();
        em.flush();
    }

    @Test
    public void testCreateCommerceWhenUserExists() {
        // 1. Setup
        // Base commerce user prepared in setUp().

        // 2. Ejercicio
        final Commerce commerce = commerceDao.createCommerce(userId, COMMERCIAL_NAME, CATEGORY, STREET, STREET_NUMBER, CITY, PROVINCE, POSTAL_CODE, OPENING_TIME, CLOSING_TIME);
        em.flush();

        // 3. Asserts
        assertNotNull(commerce);
        assertEquals(userId, commerce.getUserId());
        assertEquals(COMMERCIAL_NAME, commerce.getCommercialName());
        assertEquals(CATEGORY, commerce.getCategory());
        assertEquals(STREET, commerce.getStreet());
        assertEquals(STREET_NUMBER, commerce.getStreetNumber());
        assertEquals(CITY, commerce.getCity());
        assertEquals(PROVINCE, commerce.getProvince());
        assertEquals(POSTAL_CODE, commerce.getPostalCode());
        assertEquals(OPENING_TIME, commerce.getOpeningTime());
        assertEquals(CLOSING_TIME, commerce.getClosingTime());
        assertEquals(1, JdbcTestUtils.countRowsInTable(jdbcTemplate, "commerces"));
    }

    @Test
    public void testFindByUserIdWhenCommerceExists() {
        // 1. Setup
        commerceDao.createCommerce(userId, COMMERCIAL_NAME, CATEGORY, STREET, STREET_NUMBER, CITY, PROVINCE, POSTAL_CODE, OPENING_TIME, CLOSING_TIME);
        em.flush();

        // 2. Ejercicio
        final Optional<Commerce> commerce = commerceDao.findByUserId(userId);

        // 3. Asserts
        assertTrue(commerce.isPresent());
        assertEquals(userId, commerce.get().getUserId());
        assertEquals(COMMERCIAL_NAME, commerce.get().getCommercialName());
        assertEquals(1, JdbcTestUtils.countRowsInTable(jdbcTemplate, "commerces"));
    }

    @Test
    public void testFindByUserIdWhenCommerceDoesNotExist() {
        // 1. Setup
        // No commerce row for the user created in setUp().

        // 2. Ejercicio
        final Optional<Commerce> commerce = commerceDao.findByUserId(userId);

        // 3. Asserts
        assertTrue(commerce.isEmpty());
        assertEquals(0, JdbcTestUtils.countRowsInTable(jdbcTemplate, "commerces"));
    }

    @Test
    public void testUpdateWhenCommerceExists() {
        // 1. Setup
        commerceDao.createCommerce(userId, COMMERCIAL_NAME, CATEGORY, STREET, STREET_NUMBER, CITY, PROVINCE, POSTAL_CODE, OPENING_TIME, CLOSING_TIME);
        em.flush();
        em.clear();

        final String newName = "Updated Commerce";
        final Commerce.Category newCategory = Commerce.Category.RESTAURANT;
        final Commerce commerce = new Commerce(userId, newName, newCategory, STREET, STREET_NUMBER, CITY, PROVINCE, POSTAL_CODE, OPENING_TIME, CLOSING_TIME);

        // 2. Ejercicio
        final Commerce updatedCommerce = commerceDao.update(commerce);
        em.flush();

        // 3. Asserts
        assertNotNull(updatedCommerce);
        assertEquals(newName, updatedCommerce.getCommercialName());
        assertEquals(newCategory, updatedCommerce.getCategory());
        assertEquals(1, JdbcTestUtils.countRowsInTable(jdbcTemplate, "commerces"));
    }

    @Test
    public void testFilterCommerces() {
        // 1. Setup
        commerceDao.createCommerce(userId, COMMERCIAL_NAME, CATEGORY, STREET, STREET_NUMBER, CITY, PROVINCE, POSTAL_CODE, OPENING_TIME, CLOSING_TIME);
        em.flush();

        // 2. Ejercicio
        final java.util.List<Commerce> commerces = commerceDao.filterCommerces("Test", null, 1, 10);

        // 3. Asserts
        assertFalse(commerces.isEmpty());
        assertEquals(1, commerces.size());
        assertEquals(COMMERCIAL_NAME, commerces.get(0).getCommercialName());
    }

    @Test
    public void testCountFilteredCommerces() {
        // 1. Setup
        commerceDao.createCommerce(userId, COMMERCIAL_NAME, CATEGORY, STREET, STREET_NUMBER, CITY, PROVINCE, POSTAL_CODE, OPENING_TIME, CLOSING_TIME);
        em.flush();

        // 2. Ejercicio
        final int count = commerceDao.countFilteredCommerces(null, CITY.getCityName());

        // 3. Asserts
        assertEquals(1, count);
    }
}
