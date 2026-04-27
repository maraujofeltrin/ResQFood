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
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PasswordResetTokenServiceImplTest {

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
    public void requestPasswordReset_existingUser_sendsMailWithResetUrl() {
        // 1. Setup
        final User user = new User(USER_ID, EMAIL, "old", "Name", null, User.Role.CLIENT, true, LOCALE);
        when(userDao.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        stubCreateReturnsTokenString();
        final String baseUrl = "https://app.example";

        // 2. Ejercicio
        service.requestPasswordReset(EMAIL, baseUrl);

        // 3. Asserts
        verify(tokenDao).create(anyString(), eq(USER_ID), eq(TokenType.PASSWORD_RESET), any(LocalDateTime.class),
                any(LocalDateTime.class));
        final ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
        verify(passwordResetMailService).sendPasswordResetMail(eq(EMAIL), urlCaptor.capture(), eq(LOCALE));
        assertTrue(urlCaptor.getValue().startsWith(baseUrl + "/password-reset/change?token="));
    }

    @Test
    public void requestPasswordReset_unknownEmail_doesNotTouchTokenOrMail() {
        // 1. Setup
        when(userDao.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        // 2. Ejercicio
        service.requestPasswordReset("missing@example.com", "https://x.example");

        // 3. Asserts
        verifyNoInteractions(tokenDao);
        verifyNoInteractions(passwordResetMailService);
    }

    @Test
    public void isPasswordResetTokenValid_valid_returnsTrue() {
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
    public void isPasswordResetTokenValid_expired_returnsFalse() {
        // 1. Setup
        final LocalDateTime now = LocalDateTime.now();
        final Token t = new Token("r2", USER_ID, false, TokenType.PASSWORD_RESET, now.minusDays(1), now.minusHours(1));
        when(tokenDao.findByTokenAndType("r2", TokenType.PASSWORD_RESET)).thenReturn(Optional.of(t));

        // 2. Ejercicio
        assertFalse(service.isPasswordResetTokenValid("r2"));
    }

    @Test
    public void isPasswordResetTokenValid_used_returnsFalse() {
        // 1. Setup
        final LocalDateTime now = LocalDateTime.now();
        final Token t = new Token("r3", USER_ID, true, TokenType.PASSWORD_RESET, now, now.plusHours(1));
        when(tokenDao.findByTokenAndType("r3", TokenType.PASSWORD_RESET)).thenReturn(Optional.of(t));

        // 2. Ejercicio
        assertFalse(service.isPasswordResetTokenValid("r3"));
    }

    @Test
    public void resetPassword_validToken_encodesUpdatesPassword_and_marksTokenUsed() {
        // 1. Setup
        final LocalDateTime now = LocalDateTime.now();
        final Token resetToken = new Token("ok", USER_ID, false, TokenType.PASSWORD_RESET, now, now.plusHours(1));
        when(tokenDao.findByTokenAndType("ok", TokenType.PASSWORD_RESET)).thenReturn(Optional.of(resetToken));
        when(userDao.findById(USER_ID)).thenReturn(Optional.of(new User(USER_ID, EMAIL, "old", "N")));
        when(passwordEncoder.encode("new-secret")).thenReturn("ENC");

        // 2. Ejercicio
        service.resetPassword("ok", "new-secret");

        // 3. Asserts
        verify(userDao).updatePassword(USER_ID, "ENC");
        verify(tokenDao).markAsUsed("ok", TokenType.PASSWORD_RESET);
    }

    @Test
    public void resetPassword_unknownToken_throws() {
        // 1. Setup
        when(tokenDao.findByTokenAndType("bad", TokenType.PASSWORD_RESET)).thenReturn(Optional.empty());

        // 2. Ejercicio & 3. Asserts
        assertThrows(IllegalStateException.class, () -> service.resetPassword("bad", "x"));
        verify(userDao, never()).updatePassword(anyLong(), anyString());
    }

    @Test
    public void resetPassword_expiredToken_throws() {
        // 1. Setup
        final LocalDateTime now = LocalDateTime.now();
        final Token expired = new Token("exp", USER_ID, false, TokenType.PASSWORD_RESET, now.minusDays(1),
                now.minusMinutes(1));
        when(tokenDao.findByTokenAndType("exp", TokenType.PASSWORD_RESET)).thenReturn(Optional.of(expired));

        // 2. Ejercicio & 3. Asserts
        assertThrows(IllegalStateException.class, () -> service.resetPassword("exp", "x"));
        verify(userDao, never()).updatePassword(anyLong(), anyString());
    }

    @Test
    public void getEmailByToken_resolvesEmailFromUser() {
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
    public void getEmailByToken_unknownToken_returnsEmpty() {
        // 1. Setup
        when(tokenDao.findByTokenAndType("nope", TokenType.PASSWORD_RESET)).thenReturn(Optional.empty());

        // 2. Ejercicio
        assertTrue(service.getEmailByToken("nope").isEmpty());
    }
}
