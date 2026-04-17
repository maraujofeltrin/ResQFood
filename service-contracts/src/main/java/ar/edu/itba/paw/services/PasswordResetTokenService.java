package ar.edu.itba.paw.services;

import ar.edu.itba.paw.models.PasswordResetToken;

import java.util.Optional;

public interface PasswordResetTokenService {

    PasswordResetToken createForUser(final Long userId);

    Optional<PasswordResetToken> findByToken(final String token);

    boolean isValid(final PasswordResetToken token);

    void markAsUsed(final String token);
}