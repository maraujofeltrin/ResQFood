package ar.edu.itba.paw.services.security;

import ar.edu.itba.paw.models.security.Token;
import ar.edu.itba.paw.models.security.TokenType;
import ar.edu.itba.paw.models.user.User;
import ar.edu.itba.paw.persistence.TokenDao;
import ar.edu.itba.paw.persistence.UserDao;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EmailVerificationTokenServiceImplTest {

    private static final long USER_ID = 42L;
    private static final String EMAIL = "u@example.com";
    private static final Locale LOCALE = Locale.forLanguageTag("en");

    @Mock
    private TokenDao tokenDao;

    @Mock
    private UserDao userDao;

    @Mock
    private EmailVerificationMailService emailVerificationMailService;

    @InjectMocks
    private EmailVerificationTokenServiceImpl service;

    private void stubCreateReturnsTokenString() {
        when(tokenDao.create(anyString(), eq(USER_ID), eq(TokenType.EMAIL_VERIFICATION), any(LocalDateTime.class),
                any(LocalDateTime.class))).thenAnswer(invocation -> {
            final String tokenStr = invocation.getArgument(0);
            final LocalDateTime createdAt = invocation.getArgument(3);
            final LocalDateTime expiresAt = invocation.getArgument(4);
            return new Token(tokenStr, USER_ID, false, TokenType.EMAIL_VERIFICATION, createdAt, expiresAt);
        });
    }

    @Test
    public void sendVerificationMail_createsToken_and_sendsMailWithVerificationUrl() {
        // 1. Setup
        stubCreateReturnsTokenString();
        final String baseUrl = "https://app.example";

        // 2. Ejercicio
        service.sendVerificationMail(USER_ID, EMAIL, baseUrl, LOCALE);

        // 3. Asserts
        verify(tokenDao).create(anyString(), eq(USER_ID), eq(TokenType.EMAIL_VERIFICATION), any(LocalDateTime.class),
                any(LocalDateTime.class));
        final ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
        verify(emailVerificationMailService).sendVerificationMail(eq(EMAIL), urlCaptor.capture(), eq(LOCALE));
        final String url = urlCaptor.getValue();
        assertTrue(url.startsWith(baseUrl + "/verify-email?token="));
        assertFalse(url.endsWith("token="));
    }

    @Test
    public void verifyEmailAndGetUser_validToken_marksVerified_marksTokenUsed_returnsUser() {
        // 1. Setup
        final LocalDateTime now = LocalDateTime.now();
        final Token stored = new Token("tok", USER_ID, false, TokenType.EMAIL_VERIFICATION, now, now.plusHours(24));
        when(tokenDao.findByTokenAndType("tok", TokenType.EMAIL_VERIFICATION)).thenReturn(Optional.of(stored));
        final User verifiedReturned = new User(USER_ID, EMAIL, "pw", "N", null, User.Role.CLIENT, true, LOCALE);
        when(userDao.findById(USER_ID)).thenReturn(Optional.of(verifiedReturned));

        // 2. Ejercicio
        final Optional<User> result = service.verifyEmailAndGetUser("tok");

        // 3. Asserts
        assertTrue(result.isPresent());
        assertTrue(result.get().isVerified());
        verify(userDao).markVerified(USER_ID);
        verify(tokenDao).markAsUsed("tok", TokenType.EMAIL_VERIFICATION);
    }

    @Test
    public void verifyEmailAndGetUser_unknownToken_returnsEmpty() {
        // 1. Setup
        when(tokenDao.findByTokenAndType("missing", TokenType.EMAIL_VERIFICATION)).thenReturn(Optional.empty());

        // 2. Ejercicio
        final Optional<User> result = service.verifyEmailAndGetUser("missing");

        // 3. Asserts
        assertTrue(result.isEmpty());
        verify(userDao, never()).markVerified(anyLong());
        verify(tokenDao, never()).markAsUsed(anyString(), any());
    }

    @Test
    public void verifyEmailAndGetUser_usedToken_returnsEmpty() {
        // 1. Setup
        final LocalDateTime now = LocalDateTime.now();
        final Token used = new Token("tok", USER_ID, true, TokenType.EMAIL_VERIFICATION, now, now.plusHours(1));
        when(tokenDao.findByTokenAndType("tok", TokenType.EMAIL_VERIFICATION)).thenReturn(Optional.of(used));

        // 2. Ejercicio
        final Optional<User> result = service.verifyEmailAndGetUser("tok");

        // 3. Asserts
        assertTrue(result.isEmpty());
        verify(userDao, never()).markVerified(anyLong());
        verify(tokenDao, never()).markAsUsed(anyString(), any());
    }

    @Test
    public void verifyEmailAndGetUser_expiredToken_returnsEmpty() {
        // 1. Setup
        final LocalDateTime now = LocalDateTime.now();
        final Token expired = new Token("tok", USER_ID, false, TokenType.EMAIL_VERIFICATION, now.minusDays(2),
                now.minusHours(1));
        when(tokenDao.findByTokenAndType("tok", TokenType.EMAIL_VERIFICATION)).thenReturn(Optional.of(expired));

        // 2. Ejercicio
        final Optional<User> result = service.verifyEmailAndGetUser("tok");

        // 3. Asserts
        assertTrue(result.isEmpty());
        verify(userDao, never()).markVerified(anyLong());
    }

    @Test
    public void verifyEmail_delegatesToVerifyEmailAndGetUser() {
        // 1. Setup
        final LocalDateTime now = LocalDateTime.now();
        final Token stored = new Token("tok", USER_ID, false, TokenType.EMAIL_VERIFICATION, now, now.plusHours(1));
        when(tokenDao.findByTokenAndType("tok", TokenType.EMAIL_VERIFICATION)).thenReturn(Optional.of(stored));
        when(userDao.findById(USER_ID)).thenReturn(Optional.of(new User(USER_ID, EMAIL, "p", "N")));

        // 2. Ejercicio
        final boolean ok = service.verifyEmail("tok");

        // 3. Asserts
        assertTrue(ok);
    }

    @Test
    public void resendVerificationMail_unverifiedUser_sendsMail() {
        // 1. Setup
        final User unverified = new User(USER_ID, EMAIL, "p", "N", null, User.Role.CLIENT, false, LOCALE);
        when(userDao.findByEmail(EMAIL)).thenReturn(Optional.of(unverified));
        stubCreateReturnsTokenString();

        // 2. Ejercicio
        service.resendVerificationMail(EMAIL, "https://x.example");

        // 3. Asserts
        verify(emailVerificationMailService).sendVerificationMail(eq(EMAIL), contains("/verify-email?token="),
                eq(LOCALE));
    }

    @Test
    public void resendVerificationMail_verifiedUser_doesNotSend() {
        // 1. Setup
        final User verified = new User(USER_ID, EMAIL, "p", "N", null, User.Role.CLIENT, true, LOCALE);
        when(userDao.findByEmail(EMAIL)).thenReturn(Optional.of(verified));

        // 2. Ejercicio
        service.resendVerificationMail(EMAIL, "https://x.example");

        // 3. Asserts
        verifyNoInteractions(tokenDao);
        verifyNoInteractions(emailVerificationMailService);
    }

    @Test
    public void resendVerificationMail_unknownEmail_doesNotSend() {
        // 1. Setup
        when(userDao.findByEmail("nobody@example.com")).thenReturn(Optional.empty());

        // 2. Ejercicio
        service.resendVerificationMail("nobody@example.com", "https://x.example");

        // 3. Asserts
        verifyNoInteractions(tokenDao);
        verifyNoInteractions(emailVerificationMailService);
    }
}
