package ar.edu.itba.paw.models;

import java.time.LocalDateTime;

public class PasswordResetToken {

    private final String token;
    private final Long userId;
    private final boolean used;
    private final LocalDateTime createdAt;
    private final LocalDateTime expiresAt;

    public PasswordResetToken(final String token, final Long userId, final boolean used,
            final LocalDateTime createdAt, final LocalDateTime expiresAt) {
        this.token = token;
        this.userId = userId;
        this.used = used;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }
}