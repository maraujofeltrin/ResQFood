package ar.edu.itba.paw.services.security;

public interface EmailVerificationMailService {

    void sendVerificationMail(final String toEmail, final String verificationUrl);
}
