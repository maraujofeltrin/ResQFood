package ar.edu.itba.paw.services.security;

import ar.edu.itba.paw.models.security.Token;
import ar.edu.itba.paw.models.security.TokenType;
import ar.edu.itba.paw.models.user.User;
import ar.edu.itba.paw.persistence.TokenDao;
import ar.edu.itba.paw.persistence.UserDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;

@Service
public class PasswordResetTokenServiceImpl implements PasswordResetTokenService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PasswordResetTokenServiceImpl.class);

    private final TokenDao tokenDao;
    private final UserDao userDao;
    private final PasswordResetMailService passwordResetMailService;
    private final PasswordEncoder passwordEncoder;
    private final String baseUrl;

    @Autowired
    public PasswordResetTokenServiceImpl(final TokenDao tokenDao, final UserDao userDao,
            final PasswordResetMailService passwordResetMailService, final PasswordEncoder passwordEncoder,
            @Value("${app.base-url}") final String baseUrl) {
        this.tokenDao = tokenDao;
        this.userDao = userDao;
        this.passwordResetMailService = passwordResetMailService;
        this.passwordEncoder = passwordEncoder;
        this.baseUrl = baseUrl;
    }

    @Transactional
    @Override
    public void requestPasswordReset(final String email) {
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

    @Transactional(readOnly = true)
    @Override
    public boolean isPasswordResetTokenValid(final String token) {
        final Optional<Token> maybeToken = tokenDao.findByTokenAndType(token, TokenType.PASSWORD_RESET);
        return maybeToken.isPresent() && isValid(maybeToken.get());
    }

    @Transactional
    @Override
    public void resetPassword(final String token, final String rawPassword) {
        final Token resetToken = tokenDao.findByTokenAndType(token, TokenType.PASSWORD_RESET)
                .orElseThrow(() -> {
                    LOGGER.warn("Password reset failed: token not found");
                    return new IllegalStateException("Password reset failed");
                });
        if (!isValid(resetToken)) {
            LOGGER.warn("Password reset failed: invalid or expired token userId={}", resetToken.getUser().getId());
            throw new IllegalStateException("Password reset failed");
        }
        final User user = userDao.findById(resetToken.getUser().getId())
                .orElseThrow(() -> {
                    LOGGER.warn("Password reset failed: user not found userId={}", resetToken.getUser().getId());
                    return new IllegalStateException("Password reset failed");
                });
        final String encodedPassword = passwordEncoder.encode(rawPassword);
        userDao.updatePassword(user.getId(), encodedPassword);
        tokenDao.markAsUsed(token, TokenType.PASSWORD_RESET);
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<String> getEmailByToken(final String token) {
        return tokenDao.findByTokenAndType(token, TokenType.PASSWORD_RESET)
                .flatMap(t -> userDao.findById(t.getUser().getId()))
                .map(User::getEmail);
    }

    private static boolean isValid(final Token token) {
        return !token.isUsed() && token.getExpiresAt().isAfter(LocalDateTime.now());
    }
}
