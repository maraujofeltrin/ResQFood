package ar.edu.itba.paw.services;

import ar.edu.itba.paw.models.User;

import java.util.Optional;

public interface EmailVerificationTokenService {

    void sendVerificationMail(final Long userId, final String email, final String baseUrl);

    Optional<User> verifyEmailAndGetUser(final String token);

    boolean verifyEmail(final String token);

    void resendVerificationMail(final String email, final String baseUrl);
}