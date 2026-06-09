package ar.edu.itba.paw.services.security;

import ar.edu.itba.paw.models.security.Token;
import ar.edu.itba.paw.models.security.TokenType;
import ar.edu.itba.paw.models.user.User;
import ar.edu.itba.paw.persistence.TokenDao;
import ar.edu.itba.paw.services.user.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VerificationTokenServiceImplTest {

    private static final long USER_ID = 42L;
    private static final String EMAIL = "u@example.com";

    @Mock
    private TokenDao tokenDao;

    @Mock
    private UserService userService;

    @Mock
    private EmailVerificationMailService emailVerificationMailService;

    private VerificationTokenServiceImpl service;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        service = new VerificationTokenServiceImpl(tokenDao, userService, emailVerificationMailService, "https://app.example");
    }

    private static User userRef(final long id) {
        return new User(id, EMAIL, "pw", "N");
    }

    @Test
    void testVerifyEmailAndGetUserWhenTokenValidMarksUserAndTokenReturnsUser() {
        // 1. Setup
        final LocalDateTime now = LocalDateTime.now();
        final User user = userRef(USER_ID);
        final Token stored = new Token("tok", user, false, TokenType.EMAIL_VERIFICATION, now, now.plusHours(24));
        when(tokenDao.findByTokenAndType("tok", TokenType.EMAIL_VERIFICATION)).thenReturn(Optional.of(stored));

        // 2. Ejercicio
        final Optional<User> result = service.verifyEmailAndGetUser("tok");

        // 3. Asserts
        assertTrue(result.isPresent());
        assertTrue(result.get().isVerified());
        assertTrue(stored.isUsed());
    }

    @Test
    void testVerifyEmailAndGetUserWhenTokenUnknownReturnsEmpty() {
        // 1. Setup
        when(tokenDao.findByTokenAndType("missing", TokenType.EMAIL_VERIFICATION)).thenReturn(Optional.empty());

        // 2. Ejercicio
        final Optional<User> result = service.verifyEmailAndGetUser("missing");

        // 3. Asserts
        assertTrue(result.isEmpty());
    }

    @Test
    void testVerifyEmailAndGetUserWhenTokenUsedReturnsEmpty() {
        // 1. Setup
        final LocalDateTime now = LocalDateTime.now();
        final Token used = new Token("tok", userRef(USER_ID), true, TokenType.EMAIL_VERIFICATION, now, now.plusHours(1));
        when(tokenDao.findByTokenAndType("tok", TokenType.EMAIL_VERIFICATION)).thenReturn(Optional.of(used));

        // 2. Ejercicio
        final Optional<User> result = service.verifyEmailAndGetUser("tok");

        // 3. Asserts
        assertTrue(result.isEmpty());
    }

    @Test
    void testVerifyEmailAndGetUserWhenTokenExpiredReturnsEmpty() {
        // 1. Setup
        final LocalDateTime now = LocalDateTime.now();
        final Token expired = new Token("tok", userRef(USER_ID), false, TokenType.EMAIL_VERIFICATION, now.minusDays(2),
                now.minusHours(1));
        when(tokenDao.findByTokenAndType("tok", TokenType.EMAIL_VERIFICATION)).thenReturn(Optional.of(expired));

        // 2. Ejercicio
        final Optional<User> result = service.verifyEmailAndGetUser("tok");

        // 3. Asserts
        assertTrue(result.isEmpty());
    }

    @Test
    void testVerifyEmailWhenTokenValidReturnsTrue() {
        // 1. Setup
        final LocalDateTime now = LocalDateTime.now();
        final User user = userRef(USER_ID);
        final Token stored = new Token("tok", user, false, TokenType.EMAIL_VERIFICATION, now, now.plusHours(1));
        when(tokenDao.findByTokenAndType("tok", TokenType.EMAIL_VERIFICATION)).thenReturn(Optional.of(stored));

        // 2. Ejercicio
        final boolean ok = service.verifyEmail("tok");

        // 3. Asserts
        assertTrue(ok);
        assertTrue(user.isVerified());
        assertTrue(stored.isUsed());
    }

    @Test
    void testVerifyEmailWhenTokenUnknownReturnsFalse() {
        // 1. Setup
        when(tokenDao.findByTokenAndType("missing", TokenType.EMAIL_VERIFICATION)).thenReturn(Optional.empty());

        // 2. Ejercicio
        final boolean ok = service.verifyEmail("missing");

        // 3. Asserts
        assertFalse(ok);
    }

    @Test
    void testVerifyEmailWhenTokenExpiredReturnsFalse() {
        // 1. Setup
        final LocalDateTime now = LocalDateTime.now();
        final Token expired = new Token("tok", userRef(USER_ID), false, TokenType.EMAIL_VERIFICATION, now.minusDays(2),
                now.minusHours(1));
        when(tokenDao.findByTokenAndType("tok", TokenType.EMAIL_VERIFICATION)).thenReturn(Optional.of(expired));

        // 2. Ejercicio
        final boolean ok = service.verifyEmail("tok");

        // 3. Asserts
        assertFalse(ok);
    }
}
