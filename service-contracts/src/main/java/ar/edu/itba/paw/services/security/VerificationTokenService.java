package ar.edu.itba.paw.services.security;

import ar.edu.itba.paw.models.user.User;

import java.util.Optional;
import java.util.Locale;

public interface VerificationTokenService {

    void sendVerificationMail(final Long userId, final String email, final Locale locale);

    Optional<User> verifyEmailAndGetUser(final String token);

    boolean verifyEmail(final String token);

    void resendVerificationMail(final String email);
}
