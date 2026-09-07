package ar.edu.itba.paw.services.security;

import ar.edu.itba.paw.models.security.Token;
import ar.edu.itba.paw.models.security.TokenType;
import ar.edu.itba.paw.persistence.TokenDao;

import java.time.LocalDateTime;
import java.util.UUID;

final class TokenUtils {

    private TokenUtils() {
    }

    static Token createToken(final TokenDao tokenDao, final Long userId, final TokenType type,
            final long expirationHours) {
        final String token = UUID.randomUUID().toString();
        final LocalDateTime createdAt = LocalDateTime.now();
        final LocalDateTime expiresAt = createdAt.plusHours(expirationHours);
        return tokenDao.create(token, userId, type, createdAt, expiresAt);
    }
}