package ar.edu.itba.paw.services.security;

public interface PasswordResetMailService {

    void sendPasswordResetMail(final String toEmail, final String resetUrl);
}