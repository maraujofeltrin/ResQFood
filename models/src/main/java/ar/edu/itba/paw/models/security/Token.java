package ar.edu.itba.paw.models.security;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "tokens")
public class Token {

    @Id
    @Column(nullable = false)
    private String token;
    @Column(name = "user_id", nullable = false)
    private Long userId;
    @Column(nullable = false)
    private boolean used;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TokenType type;
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    protected Token() {}

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
