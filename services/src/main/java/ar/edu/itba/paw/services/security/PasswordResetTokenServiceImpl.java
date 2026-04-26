package ar.edu.itba.paw.services.security;

import ar.edu.itba.paw.models.security.Token;
import ar.edu.itba.paw.models.security.TokenType;
import ar.edu.itba.paw.models.user.User;
import ar.edu.itba.paw.persistence.TokenDao;
import ar.edu.itba.paw.persistence.UserDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Optional;

@Service
public class PasswordResetTokenServiceImpl implements PasswordResetTokenService {

    private final TokenDao tokenDao;
    private final UserDao userDao;
    private final PasswordResetMailService passwordResetMailService;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public PasswordResetTokenServiceImpl(final TokenDao tokenDao, final UserDao userDao,
            final PasswordResetMailService passwordResetMailService, final PasswordEncoder passwordEncoder) {
        this.tokenDao = tokenDao;
        this.userDao = userDao;
        this.passwordResetMailService = passwordResetMailService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void requestPasswordReset(final String email, final String baseUrl) {
        final Optional<User> user = userDao.findByEmail(email);
        if (user.isPresent()) {
            final User requestUser = user.get();
            final Locale locale = requestUser.getLocale() == null
                    ? Locale.forLanguageTag("es")
                    : requestUser.getLocale();
            final Token token = TokenUtils.createToken(tokenDao, requestUser.getId(), TokenType.PASSWORD_RESET, 1L);
            final String resetUrl = baseUrl + "/password-reset/change?token=" + token.getToken();
            passwordResetMailService.sendPasswordResetMail(requestUser.getEmail(), resetUrl, locale);
        }
    }

    @Override
    public boolean isPasswordResetTokenValid(final String token) {
        final Optional<Token> maybeToken = tokenDao.findByTokenAndType(token, TokenType.PASSWORD_RESET);
        return maybeToken.isPresent() && isValid(maybeToken.get());
    }

    @Override
    public void resetPassword(final String token, final String rawPassword) {
        final Token resetToken = tokenDao.findByTokenAndType(token, TokenType.PASSWORD_RESET)
                .orElseThrow(() -> new IllegalStateException("Password reset failed"));
        if (!isValid(resetToken)) {
            throw new IllegalStateException("Password reset failed");
        }
        final User user = userDao.findById(resetToken.getUserId())
                .orElseThrow(() -> new IllegalStateException("Password reset failed"));
        final String encodedPassword = passwordEncoder.encode(rawPassword);
        userDao.updatePassword(user.getId(), encodedPassword);
        tokenDao.markAsUsed(token, TokenType.PASSWORD_RESET);
    }

    @Override
    public Optional<String> getEmailByToken(final String token) {
        return tokenDao.findByTokenAndType(token, TokenType.PASSWORD_RESET)
                .flatMap(t -> userDao.findById(t.getUserId()))
                .map(User::getEmail);
    }

    private static boolean isValid(final Token token) {
        return !token.isUsed() && token.getExpiresAt().isAfter(LocalDateTime.now());
    }
}
