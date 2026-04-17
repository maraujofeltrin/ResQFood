package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.models.PasswordResetToken;

import java.time.LocalDateTime;
import java.util.Optional;

public interface PasswordResetTokenDao {

    PasswordResetToken create(final String token, final Long userId, final LocalDateTime createdAt,
            final LocalDateTime expiresAt);

    Optional<PasswordResetToken> findByToken(final String token);

    void markAsUsed(final String token);
}