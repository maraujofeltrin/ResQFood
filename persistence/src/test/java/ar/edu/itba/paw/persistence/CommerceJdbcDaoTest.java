package ar.edu.itba.paw.persistence;

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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestConfig.class)
@Sql("classpath:schema.sql")
public class CommerceJdbcDaoTest {

    private static final String EMAIL = "test@example.com";
    private static final String PASSWORD = "password123";
    private static final String NAME = "Test";
    private static final String PHONE = "123456789";
    
    private static final String COMMERCIAL_NAME = "Test Commerce";
    private static final Commerce.Category CATEGORY = Commerce.Category.BAKERY;
    private static final String STREET = "Av. Siempre Viva";
    private static final Integer STREET_NUMBER = 742;
    private static final String CITY = "Springfield";
    private static final String PROVINCE = "Buenos Aires";
    private static final String POSTAL_CODE = "1000";
    private static final String OPENING_TIME = "08:00";
    private static final String CLOSING_TIME = "20:00";

    @Autowired
    private DataSource dataSource;

    @Autowired
    private CommerceJdbcDao commerceDao;

    @Autowired
    private UserJdbcDao userDao;

    private JdbcTemplate jdbcTemplate;

    private Long userId;

    @BeforeEach
    public void setUp() {
        jdbcTemplate = new JdbcTemplate(dataSource);
        JdbcTestUtils.deleteFromTables(jdbcTemplate, "commerces", "users");
        
        // Create an underlying user for foreign key constraints
        userId = userDao.createUser(EMAIL, PASSWORD, NAME, PHONE, User.Role.COMMERCE).getId();
    }

    @Test
    public void testCreateCommerce() {
        // 1. Setup
        // Clean table with user ready

        // 2. Ejercicio
        final Commerce commerce = commerceDao.createCommerce(userId, COMMERCIAL_NAME, CATEGORY, STREET, STREET_NUMBER, CITY, PROVINCE, POSTAL_CODE, OPENING_TIME, CLOSING_TIME);

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
    public void testFindByUserId_WhenCommerceExists() {
        // 1. Setup
        commerceDao.createCommerce(userId, COMMERCIAL_NAME, CATEGORY, STREET, STREET_NUMBER, CITY, PROVINCE, POSTAL_CODE, OPENING_TIME, CLOSING_TIME);

        // 2. Ejercicio
        final Optional<Commerce> commerce = commerceDao.findByUserId(userId);

        // 3. Asserts
        assertTrue(commerce.isPresent());
        assertEquals(userId, commerce.get().getUserId());
        assertEquals(COMMERCIAL_NAME, commerce.get().getCommercialName());
    }

    @Test
    public void testFindByUserId_WhenCommerceDoesNotExist() {
        // 1. Setup
        // Commerce not created for existing user

        // 2. Ejercicio
        final Optional<Commerce> commerce = commerceDao.findByUserId(userId);

        // 3. Asserts
        assertFalse(commerce.isPresent());
    }

    @Test
    public void testUpdate() {
        // 1. Setup
        commerceDao.createCommerce(userId, COMMERCIAL_NAME, CATEGORY, STREET, STREET_NUMBER, CITY, PROVINCE, POSTAL_CODE, OPENING_TIME, CLOSING_TIME);
        
        String newName = "Updated Commerce";
        Commerce.Category newCategory = Commerce.Category.RESTAURANT; // Valid enum constant
        
        Commerce commerce = new Commerce(userId, newName, newCategory, STREET, STREET_NUMBER, CITY, PROVINCE, POSTAL_CODE, OPENING_TIME, CLOSING_TIME);

        // 2. Ejercicio
        Commerce updatedCommerce = commerceDao.update(commerce);

        // 3. Asserts
        assertNotNull(updatedCommerce);
        assertEquals(newName, updatedCommerce.getCommercialName());
        assertEquals(newCategory, updatedCommerce.getCategory());
        
        Optional<Commerce> dbCommerce = commerceDao.findByUserId(userId);
        assertTrue(dbCommerce.isPresent());
        assertEquals(newName, dbCommerce.get().getCommercialName());
        assertEquals(newCategory, dbCommerce.get().getCategory());
    }
}
