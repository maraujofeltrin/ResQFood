package ar.edu.itba.paw.models.security;

import ar.edu.itba.paw.models.user.User;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "tokens")
public class Token {

    @Id
    @Column(nullable = false)
    private String token;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private User user;

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

    public Token(final String token, final User user, final boolean used, final TokenType type,
            final LocalDateTime createdAt, final LocalDateTime expiresAt) {
        this.token = token;
        this.user = user;
        this.used = used;
        this.type = type;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
    }

    public String getToken() {
        return token;
    }

    public User getUser() {
        return user;
    }

    public Long getUserId() {
        return user.getId();
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

    @Override
    public String toString() {
        return "Token [token=" + token + ", userId=" + getUserId() + ", used=" + used + ", type=" + type
                + ", createdAt=" + createdAt + ", expiresAt=" + expiresAt + "]";
    }
}
