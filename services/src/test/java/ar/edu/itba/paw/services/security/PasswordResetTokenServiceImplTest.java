package ar.edu.itba.paw.services.security;

import ar.edu.itba.paw.models.security.PasswordResetException;
import ar.edu.itba.paw.models.security.Token;
import ar.edu.itba.paw.models.security.TokenType;
import ar.edu.itba.paw.models.user.User;
import ar.edu.itba.paw.services.user.UserService;
import ar.edu.itba.paw.persistence.TokenDao;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PasswordResetTokenServiceImplTest {

    private static final long USER_ID = 7L;
    private static final String EMAIL = "reset@example.com";

    @Mock
    private TokenDao tokenDao;

    @Mock
    private UserService userService;

    @Mock
    private PasswordResetMailService passwordResetMailService;

    @Mock
    private PasswordEncoder passwordEncoder;

    private PasswordResetTokenServiceImpl service;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        service = new PasswordResetTokenServiceImpl(tokenDao, userService, passwordResetMailService, passwordEncoder, "https://app.example");
    }

    private static User userRef(final long id) {
        return new User(id, EMAIL, "pw", "N");
    }

    @Test
    void testIsPasswordResetTokenValidWhenTokenValidReturnsTrue() {
        // 1. Setup
        final LocalDateTime now = LocalDateTime.now();
        final Token t = new Token("r1", userRef(USER_ID), false, TokenType.PASSWORD_RESET, now, now.plusHours(1));
        when(tokenDao.findByTokenAndType("r1", TokenType.PASSWORD_RESET)).thenReturn(Optional.of(t));

        // 2. Ejercicio
        final boolean valid = service.isPasswordResetTokenValid("r1");

        // 3. Asserts
        assertTrue(valid);
    }

    @Test
    void testIsPasswordResetTokenValidWhenExpiredReturnsFalse() {
        // 1. Setup
        final LocalDateTime now = LocalDateTime.now();
        final Token t = new Token("r2", userRef(USER_ID), false, TokenType.PASSWORD_RESET, now.minusDays(1), now.minusHours(1));
        when(tokenDao.findByTokenAndType("r2", TokenType.PASSWORD_RESET)).thenReturn(Optional.of(t));

        // 2. Ejercicio
        final boolean valid = service.isPasswordResetTokenValid("r2");

        // 3. Asserts
        assertFalse(valid);
    }

    @Test
    void testIsPasswordResetTokenValidWhenUsedReturnsFalse() {
        // 1. Setup
        final LocalDateTime now = LocalDateTime.now();
        final Token t = new Token("r3", userRef(USER_ID), true, TokenType.PASSWORD_RESET, now, now.plusHours(1));
        when(tokenDao.findByTokenAndType("r3", TokenType.PASSWORD_RESET)).thenReturn(Optional.of(t));

        // 2. Ejercicio
        final boolean valid = service.isPasswordResetTokenValid("r3");

        // 3. Asserts
        assertFalse(valid);
    }

    @Test
    void testResetPasswordWhenTokenUnknownThrowsPasswordResetException() {
        // 1. Setup
        when(tokenDao.findByTokenAndType("bad", TokenType.PASSWORD_RESET)).thenReturn(Optional.empty());

        // 2. Ejercicio
        final PasswordResetException thrown = assertThrows(PasswordResetException.class,
                () -> service.resetPassword("bad", "x"));

        // 3. Asserts
        assertEquals(PasswordResetException.Reason.TOKEN_NOT_FOUND, thrown.getReason());
    }

    @Test
    void testResetPasswordWhenTokenExpiredThrowsPasswordResetException() {
        // 1. Setup
        final LocalDateTime now = LocalDateTime.now();
        final Token expired = new Token("exp", userRef(USER_ID), false, TokenType.PASSWORD_RESET, now.minusDays(1),
                now.minusMinutes(1));
        when(tokenDao.findByTokenAndType("exp", TokenType.PASSWORD_RESET)).thenReturn(Optional.of(expired));

        // 2. Ejercicio
        final PasswordResetException thrown = assertThrows(PasswordResetException.class,
                () -> service.resetPassword("exp", "x"));

        // 3. Asserts
        assertEquals(PasswordResetException.Reason.TOKEN_EXPIRED, thrown.getReason());
    }

    @Test
    void testGetEmailByTokenWhenTokenValidReturnsEmail() {
        // 1. Setup
        final LocalDateTime now = LocalDateTime.now();
        final Token t = new Token("g1", userRef(USER_ID), false, TokenType.PASSWORD_RESET, now, now.plusHours(1));
        when(tokenDao.findByTokenAndType("g1", TokenType.PASSWORD_RESET)).thenReturn(Optional.of(t));

        // 2. Ejercicio
        final Optional<String> email = service.getEmailByToken("g1");

        // 3. Asserts
        assertEquals(Optional.of(EMAIL), email);
    }

    @Test
    void testResetPasswordWhenTokenValidEncodesPasswordAndMarksTokenUsed() {
        // 1. Setup
        final LocalDateTime now = LocalDateTime.now();
        final User user = userRef(USER_ID);
        final Token t = new Token("ok", user, false, TokenType.PASSWORD_RESET, now, now.plusHours(1));
        when(tokenDao.findByTokenAndType("ok", TokenType.PASSWORD_RESET)).thenReturn(Optional.of(t));
        when(passwordEncoder.encode("newpass")).thenReturn("encoded");

        // 2. Ejercicio
        service.resetPassword("ok", "newpass");

        // 3. Asserts
        assertEquals("encoded", user.getPassword());
        assertTrue(t.isUsed());
    }

    @Test
    void testGetEmailByTokenWhenTokenUnknownReturnsEmpty() {
        // 1. Setup
        when(tokenDao.findByTokenAndType("nope", TokenType.PASSWORD_RESET)).thenReturn(Optional.empty());

        // 2. Ejercicio
        final Optional<String> email = service.getEmailByToken("nope");

        // 3. Asserts
        assertTrue(email.isEmpty());
    }
}
