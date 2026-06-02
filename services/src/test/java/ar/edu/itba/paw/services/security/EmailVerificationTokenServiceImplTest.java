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

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmailVerificationTokenServiceImplTest {

    private static final long USER_ID = 42L;
    private static final String EMAIL = "u@example.com";
    private static final Locale LOCALE = Locale.forLanguageTag("en");

    @Mock
    private TokenDao tokenDao;

    @Mock
    private UserDao userDao;

    @Mock
    private EmailVerificationMailService emailVerificationMailService;

    private EmailVerificationTokenServiceImpl service;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        service = new EmailVerificationTokenServiceImpl(tokenDao, userDao, emailVerificationMailService, "https://app.example");
    }

    private static User userRef(final long id) {
        return new User(id, EMAIL, "pw", "N");
    }

    private void stubCreateReturnsTokenString() {
        when(tokenDao.create(anyString(), eq(USER_ID), eq(TokenType.EMAIL_VERIFICATION), any(LocalDateTime.class),
                any(LocalDateTime.class))).thenAnswer(invocation -> {
            final String tokenStr = invocation.getArgument(0);
            final LocalDateTime createdAt = invocation.getArgument(3);
            final LocalDateTime expiresAt = invocation.getArgument(4);
            return new Token(tokenStr, userRef(USER_ID), false, TokenType.EMAIL_VERIFICATION, createdAt, expiresAt);
        });
    }

    @Test
    void testSendVerificationMailWhenValidSendsMailWithVerificationUrl() {
        // 1. Setup
        stubCreateReturnsTokenString();
        final String baseUrl = "https://app.example";
        final AtomicReference<String> capturedUrl = new AtomicReference<>();
        doAnswer(invocation -> {
            capturedUrl.set(invocation.getArgument(1));
            return null;
        }).when(emailVerificationMailService).sendVerificationMail(eq(EMAIL), anyString(), eq(LOCALE));

        // 2. Ejercicio
        service.sendVerificationMail(USER_ID, EMAIL, LOCALE);

        // 3. Asserts
        final String url = capturedUrl.get();
        assertTrue(url.startsWith(baseUrl + "/verify-email?token="));
        assertFalse(url.endsWith("token="));
    }

    @Test
    void testVerifyEmailAndGetUserWhenTokenValidMarksUserAndTokenReturnsUser() {
        // 1. Setup
        final LocalDateTime now = LocalDateTime.now();
        final Token stored = new Token("tok", userRef(USER_ID), false, TokenType.EMAIL_VERIFICATION, now, now.plusHours(24));
        when(tokenDao.findByTokenAndType("tok", TokenType.EMAIL_VERIFICATION)).thenReturn(Optional.of(stored));
        final User verifiedReturned = new User(USER_ID, EMAIL, "pw", "N", null, User.Role.CLIENT, true, LOCALE);
        when(userDao.findById(USER_ID)).thenReturn(Optional.of(verifiedReturned));
        final AtomicBoolean markVerifiedCalled = new AtomicBoolean(false);
        doAnswer(invocation -> {
            markVerifiedCalled.set(true);
            return null;
        }).when(userDao).markVerified(USER_ID);
        final AtomicReference<String> markUsedToken = new AtomicReference<>();
        doAnswer(invocation -> {
            markUsedToken.set(invocation.getArgument(0));
            return null;
        }).when(tokenDao).markAsUsed(eq("tok"), eq(TokenType.EMAIL_VERIFICATION));

        // 2. Ejercicio
        final Optional<User> result = service.verifyEmailAndGetUser("tok");

        // 3. Asserts
        assertTrue(result.isPresent());
        assertTrue(result.get().isVerified());
        assertTrue(markVerifiedCalled.get());
        assertEquals("tok", markUsedToken.get());
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
        final Token stored = new Token("tok", userRef(USER_ID), false, TokenType.EMAIL_VERIFICATION, now, now.plusHours(1));
        when(tokenDao.findByTokenAndType("tok", TokenType.EMAIL_VERIFICATION)).thenReturn(Optional.of(stored));
        when(userDao.findById(USER_ID)).thenReturn(Optional.of(new User(USER_ID, EMAIL, "p", "N")));
        doAnswer(invocation -> null).when(userDao).markVerified(USER_ID);
        doAnswer(invocation -> null).when(tokenDao).markAsUsed(eq("tok"), eq(TokenType.EMAIL_VERIFICATION));

        // 2. Ejercicio
        final boolean ok = service.verifyEmail("tok");

        // 3. Asserts
        assertTrue(ok);
    }

    @Test
    void testResendVerificationMailWhenUserUnverifiedSendsMailWithVerificationLink() {
        // 1. Setup
        final User unverified = new User(USER_ID, EMAIL, "p", "N", null, User.Role.CLIENT, false, LOCALE);
        when(userDao.findByEmail(EMAIL)).thenReturn(Optional.of(unverified));
        stubCreateReturnsTokenString();
        final AtomicReference<String> capturedUrl = new AtomicReference<>();
        doAnswer(invocation -> {
            capturedUrl.set(invocation.getArgument(1));
            return null;
        }).when(emailVerificationMailService).sendVerificationMail(eq(EMAIL), anyString(), eq(LOCALE));

        // 2. Ejercicio
        service.resendVerificationMail(EMAIL);

        // 3. Asserts
        assertTrue(capturedUrl.get().contains("/verify-email?token="));
    }

    @Test
    void testResendVerificationMailWhenUserAlreadyVerifiedDoesNotCreateTokenOrSendMail() {
        // 1. Setup
        final User verified = new User(USER_ID, EMAIL, "p", "N", null, User.Role.CLIENT, true, LOCALE);
        final AtomicInteger emailLookups = new AtomicInteger();
        when(userDao.findByEmail(EMAIL)).thenAnswer(invocation -> {
            emailLookups.incrementAndGet();
            return Optional.of(verified);
        });

        // 2. Ejercicio
        service.resendVerificationMail(EMAIL);

        // 3. Asserts
        assertEquals(1, emailLookups.get());
    }

    @Test
    void testResendVerificationMailWhenEmailUnknownDoesNotCreateTokenOrSendMail() {
        // 1. Setup
        final AtomicInteger emailLookups = new AtomicInteger();
        when(userDao.findByEmail("nobody@example.com")).thenAnswer(invocation -> {
            emailLookups.incrementAndGet();
            return Optional.empty();
        });

        // 2. Ejercicio
        service.resendVerificationMail("nobody@example.com");

        // 3. Asserts
        assertEquals(1, emailLookups.get());
    }
}
