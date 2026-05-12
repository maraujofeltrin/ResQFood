package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.models.image.Image;
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
import java.util.Locale;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@Rollback
@Transactional
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestConfig.class)
public class UserJpaDaoTest {

    private static final String EMAIL = "test@example.com";
    private static final String PASSWORD = "password123";
    private static final String NAME = "Test User";
    private static final String PHONE = "123456789";
    private static final User.Role ROLE = User.Role.CLIENT;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private UserJpaDao userDao;

    @Autowired
    private ImageJpaDao imageDao;

    @PersistenceContext
    private EntityManager em;

    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    public void setUp() {
        jdbcTemplate = new JdbcTemplate(dataSource);
        JdbcTestUtils.deleteFromTables(jdbcTemplate, "commerce_reviews", "bids", "auctions", "reservation_tokens",
                "pack_tags", "reservations", "client_pack_favorites", "packs", "images", "commerces", "clients", "tokens",
                "users");
    }

    @Test
    public void testCreateUserWhenNoUsersExist() {
        // 1. Setup
        // Tables cleared in setUp().

        // 2. Ejercicio
        final User user = userDao.createUser(EMAIL, PASSWORD, NAME, PHONE, ROLE);
        em.flush();

        // 3. Asserts
        assertNotNull(user);
        assertNotNull(user.getId());
        assertEquals(EMAIL, user.getEmail());
        assertEquals(1, JdbcTestUtils.countRowsInTable(jdbcTemplate, "users"));
    }

    @Test
    public void testFindByEmailWhenUserExists() {
        // 1. Setup
        userDao.createUser(EMAIL, PASSWORD, NAME, PHONE, ROLE);
        em.flush();

        // 2. Ejercicio
        final Optional<User> user = userDao.findByEmail(EMAIL);

        // 3. Asserts
        assertTrue(user.isPresent());
        assertEquals(EMAIL, user.get().getEmail());
        assertEquals(1, JdbcTestUtils.countRowsInTable(jdbcTemplate, "users"));
    }

    @Test
    public void testFindByEmailWhenUserDoesNotExist() {
        // 1. Setup
        // No user row for this email.

        // 2. Ejercicio
        final Optional<User> user = userDao.findByEmail(EMAIL);

        // 3. Asserts
        assertTrue(user.isEmpty());
        assertEquals(0, JdbcTestUtils.countRowsInTable(jdbcTemplate, "users"));
    }

    @Test
    public void testUpdateProfileImageWhenUserExists() {
        // 1. Setup
        final User user = userDao.createUser(EMAIL, PASSWORD, NAME, PHONE, ROLE);
        final Image image = imageDao.saveImage(new byte[] {1, 2, 3}, "image/png");
        em.flush();
        final long imageId = image.getId();

        // 2. Ejercicio
        userDao.updateProfileImage(user.getId(), imageId);
        em.flush();
        em.clear();

        // 3. Asserts
        final Optional<User> loaded = userDao.findById(user.getId());
        assertTrue(loaded.isPresent());
        assertEquals(imageId, loaded.get().getProfileImageId().longValue());
        assertEquals(1, JdbcTestUtils.countRowsInTable(jdbcTemplate, "users"));
        assertEquals(1, JdbcTestUtils.countRowsInTable(jdbcTemplate, "images"));
    }

    @Test
    public void testUpdateLocaleWhenUserExists() {
        // 1. Setup
        final User user = userDao.createUser(EMAIL, PASSWORD, NAME, PHONE, ROLE, Locale.forLanguageTag("es"));
        em.flush();

        // 2. Ejercicio
        userDao.updateLocale(user.getId(), "en");
        em.flush();
        em.clear();

        // 3. Asserts
        final Optional<User> loaded = userDao.findById(user.getId());
        assertTrue(loaded.isPresent());
        assertEquals("en", loaded.get().getLocale().getLanguage());
        assertEquals(1, JdbcTestUtils.countRowsInTable(jdbcTemplate, "users"));
    }
}
