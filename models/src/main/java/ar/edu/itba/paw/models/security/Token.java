package ar.edu.itba.paw.models.security;

import java.time.LocalDateTime;

public class Token {

    private final String token;
    private final Long userId;
    private final boolean used;
    private final TokenType type;
    private final LocalDateTime createdAt;
    private final LocalDateTime expiresAt;

    public Token(final String token, final Long userId, final boolean used, final TokenType type,
            final LocalDateTime createdAt, final LocalDateTime expiresAt) {
        this.token = token;
        this.userId = userId;
        this.used = used;
        this.type = type;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
    }

    public String getToken() {
        return token;
    }

    public Long getUserId() {
        return userId;
    }

    public boolean isUsed() {
        return used;
    }

    public TokenType getType() {
        return type;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }
}
