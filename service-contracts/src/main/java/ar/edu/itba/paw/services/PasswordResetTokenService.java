package ar.edu.itba.paw.services;

public interface PasswordResetTokenService {

    void requestPasswordReset(final String email, final String baseUrl);

    boolean isPasswordResetTokenValid(final String token);

    void resetPassword(final String token, final String rawPassword);
}