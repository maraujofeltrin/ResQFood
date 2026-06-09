package ar.edu.itba.paw.services.security;

import ar.edu.itba.paw.models.security.Token;
import ar.edu.itba.paw.models.security.TokenType;
import ar.edu.itba.paw.models.user.User;
import ar.edu.itba.paw.persistence.TokenDao;
import ar.edu.itba.paw.services.user.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;

@Service
public class VerificationTokenServiceImpl implements VerificationTokenService {

    private static final Logger LOGGER = LoggerFactory.getLogger(VerificationTokenServiceImpl.class);

    private final TokenDao tokenDao;
    private final UserService userService;
    private final EmailVerificationMailService emailVerificationMailService;
    private final String baseUrl;

    @Autowired
    public VerificationTokenServiceImpl(final TokenDao tokenDao, @Lazy final UserService userService,
            final EmailVerificationMailService emailVerificationMailService,
            @Value("${app.base-url}") final String baseUrl) {
        this.tokenDao = tokenDao;
        this.userService = userService;
        this.emailVerificationMailService = emailVerificationMailService;
        this.baseUrl = baseUrl;
    }

    @Transactional
    @Override
    public void sendVerificationMail(final Long userId, final String email, final Locale locale) {
        final Token token = TokenUtils.createToken(tokenDao, userId, TokenType.EMAIL_VERIFICATION, 24L);
        final String verificationUrl = baseUrl + "/verify-email?token=" + token.getToken();
        emailVerificationMailService.sendVerificationMail(email, verificationUrl, locale);
    }

    @Transactional
    @Override
    public Optional<User> verifyEmailAndGetUser(final String token) {
        final Optional<Token> maybeToken = tokenDao.findByTokenAndType(token, TokenType.EMAIL_VERIFICATION);
        if (maybeToken.isEmpty() || !isValid(maybeToken.get())) {
            LOGGER.debug("Email verification rejected: missing, used, or expired token");
            return Optional.empty();
        }

        final Token verificationToken = maybeToken.get();
        final User user = verificationToken.getUser();
        user.setVerified(true);
        verificationToken.setUsed(true);

        return Optional.of(user);
    }

    @Transactional
    @Override
    public boolean verifyEmail(final String token) {
        return verifyEmailAndGetUser(token).isPresent();
    }

    @Transactional
    @Override
    public void resendVerificationMail(final String email) {
        final Optional<User> maybeUser = userService.findByEmail(email);
        if (maybeUser.isPresent() && !maybeUser.get().isVerified()) {
            final User user = maybeUser.get();
            sendVerificationMail(user.getId(), user.getEmail(), user.getLocale());
        }
    }

    private static boolean isValid(final Token token) {
        return !token.isUsed() && token.getExpiresAt().isAfter(LocalDateTime.now());
    }
}
