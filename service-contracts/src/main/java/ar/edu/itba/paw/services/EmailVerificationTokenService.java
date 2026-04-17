package ar.edu.itba.paw.services;

public interface EmailVerificationTokenService {

    void sendVerificationMail(final Long userId, final String email, final String baseUrl);

    boolean verifyEmail(final String token);

    void resendVerificationMail(final String email, final String baseUrl);
}