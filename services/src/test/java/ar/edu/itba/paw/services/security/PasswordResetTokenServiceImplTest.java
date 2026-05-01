package ar.edu.itba.paw.services.security;

import ar.edu.itba.paw.models.security.Token;
import ar.edu.itba.paw.models.security.TokenType;
import ar.edu.itba.paw.models.user.User;
import ar.edu.itba.paw.persistence.TokenDao;
import ar.edu.itba.paw.persistence.UserDao;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PasswordResetTokenServiceImplTest {

    private static final long USER_ID = 7L;
    private static final String EMAIL = "reset@example.com";
    private static final Locale LOCALE = Locale.forLanguageTag("en");

    @Mock
    private TokenDao tokenDao;

    @Mock
    private UserDao userDao;

    @Mock
    private PasswordResetMailService passwordResetMailService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private PasswordResetTokenServiceImpl service;

    private void stubCreateReturnsTokenString() {
        when(tokenDao.create(anyString(), eq(USER_ID), eq(TokenType.PASSWORD_RESET), any(LocalDateTime.class),
                any(LocalDateTime.class))).thenAnswer(invocation -> {
            final String tokenStr = invocation.getArgument(0);
            final LocalDateTime createdAt = invocation.getArgument(3);
            final LocalDateTime expiresAt = invocation.getArgument(4);
            return new Token(tokenStr, USER_ID, false, TokenType.PASSWORD_RESET, createdAt, expiresAt);
        });
    }

    @Test
    void testRequestPasswordResetWhenUserExistsSendsMailWithResetUrl() {
        // 1. Setup
        final User user = new User(USER_ID, EMAIL, "old", "Name", null, User.Role.CLIENT, true, LOCALE);
        when(userDao.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        stubCreateReturnsTokenString();
        final String baseUrl = "https://app.example";
        final AtomicReference<String> capturedUrl = new AtomicReference<>();
        doAnswer(invocation -> {
            capturedUrl.set(invocation.getArgument(1));
            return null;
        }).when(passwordResetMailService).sendPasswordResetMail(eq(EMAIL), anyString(), eq(LOCALE));

        // 2. Ejercicio
        service.requestPasswordReset(EMAIL, baseUrl);

        // 3. Asserts
        assertTrue(capturedUrl.get().startsWith(baseUrl + "/password-reset/change?token="));
    }

    @Test
    void testRequestPasswordResetWhenEmailUnknownDoesNotCreateTokenOrSendMail() {
        // 1. Setup
        final AtomicInteger emailLookups = new AtomicInteger();
        when(userDao.findByEmail("missing@example.com")).thenAnswer(invocation -> {
            emailLookups.incrementAndGet();
            return Optional.empty();
        });
        lenient().doThrow(new AssertionError("create no debe invocarse")).when(tokenDao).create(anyString(), anyLong(),
                any(TokenType.class), any(LocalDateTime.class), any(LocalDateTime.class));
        lenient().doThrow(new AssertionError("sendPasswordResetMail no debe invocarse")).when(
                passwordResetMailService).sendPasswordResetMail(anyString(), anyString(), any(Locale.class));

        // 2. Ejercicio
        service.requestPasswordReset("missing@example.com", "https://x.example");

        // 3. Asserts
        assertEquals(1, emailLookups.get());
    }

    @Test
    void testIsPasswordResetTokenValidWhenTokenValidReturnsTrue() {
        // 1. Setup
        final LocalDateTime now = LocalDateTime.now();
        final Token t = new Token("r1", USER_ID, false, TokenType.PASSWORD_RESET, now, now.plusHours(1));
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
        final Token t = new Token("r2", USER_ID, false, TokenType.PASSWORD_RESET, now.minusDays(1), now.minusHours(1));
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
        final Token t = new Token("r3", USER_ID, true, TokenType.PASSWORD_RESET, now, now.plusHours(1));
        when(tokenDao.findByTokenAndType("r3", TokenType.PASSWORD_RESET)).thenReturn(Optional.of(t));

        // 2. Ejercicio
        final boolean valid = service.isPasswordResetTokenValid("r3");

        // 3. Asserts
        assertFalse(valid);
    }

    @Test
    void testResetPasswordWhenTokenValidEncodesPasswordAndMarksTokenUsed() {
        // 1. Setup
        final LocalDateTime now = LocalDateTime.now();
        final Token resetToken = new Token("ok", USER_ID, false, TokenType.PASSWORD_RESET, now, now.plusHours(1));
        when(tokenDao.findByTokenAndType("ok", TokenType.PASSWORD_RESET)).thenReturn(Optional.of(resetToken));
        when(userDao.findById(USER_ID)).thenReturn(Optional.of(new User(USER_ID, EMAIL, "old", "N")));
        when(passwordEncoder.encode("new-secret")).thenReturn("ENC");
        final AtomicReference<Long> updateUserId = new AtomicReference<>();
        final AtomicReference<String> updatePassword = new AtomicReference<>();
        doAnswer(invocation -> {
            updateUserId.set(invocation.getArgument(0));
            updatePassword.set(invocation.getArgument(1));
            return null;
        }).when(userDao).updatePassword(eq(USER_ID), eq("ENC"));
        final AtomicReference<String> markedToken = new AtomicReference<>();
        doAnswer(invocation -> {
            markedToken.set(invocation.getArgument(0));
            return null;
        }).when(tokenDao).markAsUsed(eq("ok"), eq(TokenType.PASSWORD_RESET));

        // 2. Ejercicio
        service.resetPassword("ok", "new-secret");

        // 3. Asserts
        assertEquals(USER_ID, updateUserId.get().longValue());
        assertEquals("ENC", updatePassword.get());
        assertEquals("ok", markedToken.get());
    }

    @Test
    void testResetPasswordWhenTokenUnknownThrowsIllegalStateException() {
        // 1. Setup
        when(tokenDao.findByTokenAndType("bad", TokenType.PASSWORD_RESET)).thenReturn(Optional.empty());
        lenient().doThrow(new AssertionError("updatePassword no debe invocarse")).when(userDao)
                .updatePassword(anyLong(), anyString());

        // 2. Ejercicio
        final IllegalStateException thrown = assertThrows(IllegalStateException.class,
                () -> service.resetPassword("bad", "x"));

        // 3. Asserts
        assertTrue(thrown.getMessage().contains("Password reset failed"));
    }

    @Test
    void testResetPasswordWhenTokenExpiredThrowsIllegalStateException() {
        // 1. Setup
        final LocalDateTime now = LocalDateTime.now();
        final Token expired = new Token("exp", USER_ID, false, TokenType.PASSWORD_RESET, now.minusDays(1),
                now.minusMinutes(1));
        when(tokenDao.findByTokenAndType("exp", TokenType.PASSWORD_RESET)).thenReturn(Optional.of(expired));
        lenient().doThrow(new AssertionError("updatePassword no debe invocarse")).when(userDao)
                .updatePassword(anyLong(), anyString());

        // 2. Ejercicio
        final IllegalStateException thrown = assertThrows(IllegalStateException.class,
                () -> service.resetPassword("exp", "x"));

        // 3. Asserts
        assertTrue(thrown.getMessage().contains("Password reset failed"));
    }

    @Test
    void testGetEmailByTokenWhenTokenValidReturnsEmail() {
        // 1. Setup
        final LocalDateTime now = LocalDateTime.now();
        final Token t = new Token("g1", USER_ID, false, TokenType.PASSWORD_RESET, now, now.plusHours(1));
        when(tokenDao.findByTokenAndType("g1", TokenType.PASSWORD_RESET)).thenReturn(Optional.of(t));
        when(userDao.findById(USER_ID)).thenReturn(Optional.of(new User(USER_ID, EMAIL, "p", "N")));

        // 2. Ejercicio
        final Optional<String> email = service.getEmailByToken("g1");

        // 3. Asserts
        assertEquals(Optional.of(EMAIL), email);
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
