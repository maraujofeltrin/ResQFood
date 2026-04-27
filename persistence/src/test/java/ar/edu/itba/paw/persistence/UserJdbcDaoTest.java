package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.models.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.jdbc.JdbcTestUtils;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestConfig.class)
@Sql("classpath:schema.sql")
public class UserJdbcDaoTest {

    private static final String EMAIL = "test@example.com";
    private static final String PASSWORD = "password123";
    private static final String NAME = "Test User";
    private static final String PHONE = "123456789";
    private static final User.Role ROLE = User.Role.CLIENT;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private UserJdbcDao userDao;

    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    public void setUp() {
        jdbcTemplate = new JdbcTemplate(dataSource);
        JdbcTestUtils.deleteFromTables(jdbcTemplate, "users");
    }

    @Test
    public void testCreateUser() {
        // 1. Setup
        // No setup needed, table is clean

        // 2. Ejercicio
        final User user = userDao.createUser(EMAIL, PASSWORD, NAME, PHONE, ROLE);

        // 3. Asserts
        assertNotNull(user);
        assertNotNull(user.getId());
        assertEquals(EMAIL, user.getEmail());
        assertEquals(1, JdbcTestUtils.countRowsInTable(jdbcTemplate, "users"));
    }

    @Test
    public void testFindByEmail_WhenUserExists() {
        // 1. Setup
        userDao.createUser(EMAIL, PASSWORD, NAME, PHONE, ROLE);

        // 2. Ejercicio
        final Optional<User> user = userDao.findByEmail(EMAIL);

        // 3. Asserts
        assertTrue(user.isPresent());
        assertEquals(EMAIL, user.get().getEmail());
    }

    @Test
    public void testFindByEmail_WhenUserDoesNotExist() {
        // 1. Setup
        // No user inserted

        // 2. Ejercicio
        final Optional<User> user = userDao.findByEmail(EMAIL);

        // 3. Asserts
        assertFalse(user.isPresent());
    }

    @Test
    public void testUpdateProfileImage() {
        // 1. Setup
        final User user = userDao.createUser(EMAIL, PASSWORD, NAME, PHONE, ROLE);
        final GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            final PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO images (data, content_type) VALUES (?, ?)",
                    Statement.RETURN_GENERATED_KEYS);
            ps.setBytes(1, new byte[] { 1, 2, 3 });
            ps.setString(2, "image/png");
            return ps;
        }, keyHolder);
        final long imageId = Objects.requireNonNull(keyHolder.getKey()).longValue();

        // 2. Ejercicio
        userDao.updateProfileImage(user.getId(), imageId);

        // 3. Asserts
        final Optional<User> loaded = userDao.findById(user.getId());
        assertTrue(loaded.isPresent());
        assertEquals(imageId, loaded.get().getProfileImageId().longValue());
    }

    @Test
    public void testUpdateLocale() {
        // 1. Setup
        final User user = userDao.createUser(EMAIL, PASSWORD, NAME, PHONE, ROLE, Locale.forLanguageTag("es"));

        // 2. Ejercicio
        userDao.updateLocale(user.getId(), "en");

        // 3. Asserts
        final Optional<User> loaded = userDao.findById(user.getId());
        assertTrue(loaded.isPresent());
        assertEquals("en", loaded.get().getLocale().getLanguage());
    }
}
