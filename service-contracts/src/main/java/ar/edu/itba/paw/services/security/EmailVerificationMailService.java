package ar.edu.itba.paw.services.security;

import java.util.Locale;

public interface EmailVerificationMailService {

    void sendVerificationMail(final String toEmail, final String verificationUrl, final Locale locale);
}
