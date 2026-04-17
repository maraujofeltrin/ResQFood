package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.models.Token;
import ar.edu.itba.paw.models.TokenType;

import java.time.LocalDateTime;
import java.util.Optional;

public interface TokenDao {

    Token create(final String token, final Long userId, final TokenType type, final LocalDateTime createdAt,
            final LocalDateTime expiresAt);

    Optional<Token> findByTokenAndType(final String token, final TokenType type);

    void markAsUsed(final String token, final TokenType type);
}