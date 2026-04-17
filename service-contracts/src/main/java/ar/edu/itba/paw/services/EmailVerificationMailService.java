package ar.edu.itba.paw.services;

public interface EmailVerificationMailService {

    void sendVerificationMail(final String toEmail, final String verificationUrl);
}
