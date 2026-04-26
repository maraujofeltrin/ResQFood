package ar.edu.itba.paw.services.security;

import java.util.Locale;

public interface PasswordResetMailService {

    void sendPasswordResetMail(final String toEmail, final String resetUrl, final Locale locale);
}