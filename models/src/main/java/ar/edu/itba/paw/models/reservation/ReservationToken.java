package ar.edu.itba.paw.models.reservation;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "reservation_tokens")
public class ReservationToken {

    public enum Action {
        ACCEPT,
        REJECT
    }

    @Id
    @Column(length = 255)
    private String token;

    @Column(name = "reservation_id", nullable = false)
    private Long reservationId;

    @Enumerated(EnumType.STRING)
    @Column(length = 50, nullable = false)
    private Action action;

    @Column(nullable = false)
    private boolean used;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    protected ReservationToken() {
        // Just for Hibernate
    }

    public ReservationToken(final String token, final Long reservationId, final Action action, final boolean used,
            final LocalDateTime createdAt, final LocalDateTime expiresAt) {
        this.token = token;
        this.reservationId = reservationId;
        this.action = action;
        this.used = used;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
    }

    public String getToken() {
        return token;
    }

    public Long getReservationId() {
        return reservationId;
    }

    public Action getAction() {
        return action;
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
