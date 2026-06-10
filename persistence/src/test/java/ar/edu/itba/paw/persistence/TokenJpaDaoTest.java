package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.models.security.Token;
import ar.edu.itba.paw.models.security.TokenType;
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
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@Rollback
@Transactional
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestConfig.class)
public class TokenJpaDaoTest {

    private static final LocalDateTime TOKEN_CREATED = LocalDateTime.of(2030, 3, 10, 9, 0);
    private static final LocalDateTime TOKEN_EXPIRES = LocalDateTime.of(2030, 3, 11, 9, 0);

    @Autowired
    private DataSource dataSource;

    @Autowired
    private TokenJpaDao tokenDao;

    @Autowired
    private UserJpaDao userDao;

    @PersistenceContext
    private EntityManager em;

    private JdbcTemplate jdbcTemplate;

    private Long userId;

    @BeforeEach
    public void setUp() {
        jdbcTemplate = new JdbcTemplate(dataSource);
        JdbcTestUtils.deleteFromTables(jdbcTemplate, "commerce_reviews", "bids", "auctions", "reservation_tokens",
                "pack_tags", "reservations", "client_pack_favorites", "packs", "images", "commerces", "clients", "tokens",
                "users");

        userId = userDao.createUser("user@example.com", "pass", "User", "123", User.Role.CLIENT).getId();
        em.flush();
    }

    @Test
    public void testCreateWhenUserExists() {
        // 1. Setup
        // userId from setUp().

        // 2. Ejercicio
        final Token token = tokenDao.create("token123", userId, TokenType.EMAIL_VERIFICATION, TOKEN_CREATED, TOKEN_EXPIRES);
        em.flush();

        // 3. Asserts
        assertNotNull(token);
        assertEquals("token123", token.getToken());
        assertEquals(userId, token.getUserId());
        assertEquals(userId, token.getUser().getId());
        assertEquals(TokenType.EMAIL_VERIFICATION, token.getType());
        assertFalse(token.isUsed());
        assertEquals(1, JdbcTestUtils.countRowsInTable(jdbcTemplate, "tokens"));
    }

    @Test
    public void testFindByTokenAndTypeWhenTokenExists() {
        // 1. Setup
        tokenDao.create("token123", userId, TokenType.EMAIL_VERIFICATION, TOKEN_CREATED, TOKEN_EXPIRES);
        em.flush();

        // 2. Ejercicio
        final Optional<Token> found = tokenDao.findByTokenAndType("token123", TokenType.EMAIL_VERIFICATION);

        // 3. Asserts
        assertTrue(found.isPresent());
        assertEquals(userId, found.get().getUserId());
        assertEquals(userId, found.get().getUser().getId());
        assertEquals(1, JdbcTestUtils.countRowsInTable(jdbcTemplate, "tokens"));
    }

    @Test
    public void testFindByTokenAndTypeAfterClearReturnsTokenWithAccessibleUser() {
        // 1. Setup
        tokenDao.create("token123", userId, TokenType.EMAIL_VERIFICATION, TOKEN_CREATED, TOKEN_EXPIRES);
        em.flush();
        em.clear();

        // 2. Ejercicio
        final Optional<Token> found = tokenDao.findByTokenAndType("token123", TokenType.EMAIL_VERIFICATION);

        // 3. Asserts
        assertTrue(found.isPresent());
        assertEquals("user@example.com", found.get().getUser().getEmail());
        assertEquals(1, JdbcTestUtils.countRowsInTable(jdbcTemplate, "tokens"));
    }

    @Test
    public void testMarkAsUsedWhenTokenExists() {
        // 1. Setup
        tokenDao.create("token123", userId, TokenType.EMAIL_VERIFICATION, TOKEN_CREATED, TOKEN_EXPIRES);
        em.flush();

        // 2. Ejercicio
        tokenDao.markAsUsed("token123", TokenType.EMAIL_VERIFICATION);
        em.flush();
        em.clear();

        // 3. Asserts
        final Optional<Token> found = tokenDao.findByTokenAndType("token123", TokenType.EMAIL_VERIFICATION);
        assertTrue(found.isPresent());
        assertTrue(found.get().isUsed());
        assertEquals(1, JdbcTestUtils.countRowsInTable(jdbcTemplate, "tokens"));
    }

    @Test
    public void testFindByTokenAndTypeWhenTokenDoesNotExist() {
        // 1. Setup
        // No token row for this string.

        // 2. Ejercicio
        final Optional<Token> found = tokenDao.findByTokenAndType("does-not-exist", TokenType.EMAIL_VERIFICATION);

        // 3. Asserts
        assertTrue(found.isEmpty());
        assertEquals(0, JdbcTestUtils.countRowsInTable(jdbcTemplate, "tokens"));
    }

    @Test
    public void testFindByTokenAndTypeWhenTypeDoesNotMatch() {
        // 1. Setup
        tokenDao.create("same-string", userId, TokenType.EMAIL_VERIFICATION, TOKEN_CREATED, TOKEN_EXPIRES);
        em.flush();

        // 2. Ejercicio
        final Optional<Token> found = tokenDao.findByTokenAndType("same-string", TokenType.PASSWORD_RESET);

        // 3. Asserts
        assertTrue(found.isEmpty());
        assertEquals(1, JdbcTestUtils.countRowsInTable(jdbcTemplate, "tokens"));
    }

    @Test
    public void testMarkAsUsedWhenTokenDoesNotExistThrows() {
        // 1. Setup
        // No matching token row.

        // 2. Ejercicio
        final IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                () -> tokenDao.markAsUsed("unknown-token", TokenType.PASSWORD_RESET));

        // 3. Asserts
        assertNotNull(thrown);
        assertEquals(0, JdbcTestUtils.countRowsInTable(jdbcTemplate, "tokens"));
    }
}
