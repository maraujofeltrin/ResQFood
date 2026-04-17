package ar.edu.itba.paw.services;

public interface PasswordResetMailService {

    void sendPasswordResetMail(final String toEmail, final String resetUrl);
}