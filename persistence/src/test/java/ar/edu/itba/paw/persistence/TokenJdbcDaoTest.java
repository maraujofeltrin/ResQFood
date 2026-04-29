package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.models.security.Token;
import ar.edu.itba.paw.models.security.TokenType;
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
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestConfig.class)
@Sql("classpath:schema.sql")
public class TokenJdbcDaoTest {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private TokenJdbcDao tokenDao;

    @Autowired
    private UserJdbcDao userDao;

    private JdbcTemplate jdbcTemplate;

    private Long userId;

    @BeforeEach
    public void setUp() {
        jdbcTemplate = new JdbcTemplate(dataSource);
        JdbcTestUtils.deleteFromTables(jdbcTemplate, "bids", "auctions", "reservation_tokens", "pack_tags", "reservations", "client_pack_favorites", "packs", "commerces", "clients", "tokens", "users");
        
        userId = userDao.createUser("user@example.com", "pass", "User", "123", User.Role.CLIENT).getId();
    }

    @Test
    public void testCreate() {
        // 1. Setup
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expires = now.plusDays(1);

        // 2. Ejercicio
        Token token = tokenDao.create("token123", userId, TokenType.EMAIL_VERIFICATION, now, expires);

        // 3. Asserts
        assertNotNull(token);
        assertEquals("token123", token.getToken());
        assertEquals(userId, token.getUserId());
        assertEquals(TokenType.EMAIL_VERIFICATION, token.getType());
        assertFalse(token.isUsed());
        assertEquals(1, JdbcTestUtils.countRowsInTable(jdbcTemplate, "tokens"));
    }

    @Test
    public void testFindByTokenAndType() {
        // 1. Setup
        LocalDateTime now = LocalDateTime.now();
        tokenDao.create("token123", userId, TokenType.EMAIL_VERIFICATION, now, now.plusDays(1));

        // 2. Ejercicio
        Optional<Token> found = tokenDao.findByTokenAndType("token123", TokenType.EMAIL_VERIFICATION);

        // 3. Asserts
        assertTrue(found.isPresent());
        assertEquals(userId, found.get().getUserId());
    }

    @Test
    public void testMarkAsUsed() {
        // 1. Setup
        LocalDateTime now = LocalDateTime.now();
        tokenDao.create("token123", userId, TokenType.EMAIL_VERIFICATION, now, now.plusDays(1));

        // 2. Ejercicio
        tokenDao.markAsUsed("token123", TokenType.EMAIL_VERIFICATION);

        // 3. Asserts
        Optional<Token> found = tokenDao.findByTokenAndType("token123", TokenType.EMAIL_VERIFICATION);
        assertTrue(found.isPresent());
        assertTrue(found.get().isUsed());
    }

    @Test
    public void testFindByTokenAndType_notFound_returnsEmpty() {
        // 1. Setup
        // (no row inserted for this token)

        // 2. Ejercicio
        final Optional<Token> found = tokenDao.findByTokenAndType("does-not-exist", TokenType.EMAIL_VERIFICATION);

        // 3. Asserts
        assertTrue(found.isEmpty());
    }

    @Test
    public void testFindByTokenAndType_wrongType_returnsEmpty() {
        // 1. Setup
        final LocalDateTime now = LocalDateTime.now();
        tokenDao.create("same-string", userId, TokenType.EMAIL_VERIFICATION, now, now.plusDays(1));

        // 2. Ejercicio
        final Optional<Token> found = tokenDao.findByTokenAndType("same-string", TokenType.PASSWORD_RESET);

        // 3. Asserts
        assertTrue(found.isEmpty());
    }

    @Test
    public void testMarkAsUsed_unknownToken_throws() {
        // 1. Setup
        // (no matching row)

        // 2. Ejercicio & 3. Asserts
        assertThrows(IllegalArgumentException.class,
                () -> tokenDao.markAsUsed("unknown-token", TokenType.PASSWORD_RESET));
    }
}
