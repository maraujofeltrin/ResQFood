package ar.edu.itba.paw.models.reservation;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
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

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reservation_id")
    private Reservation reservation;

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

    public ReservationToken(final String token, final Reservation reservation, final Action action, final boolean used,
            final LocalDateTime createdAt, final LocalDateTime expiresAt) {
        this.token = token;
        this.reservation = reservation;
        this.action = action;
        this.used = used;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
    }

    public String getToken() {
        return token;
    }

    public Reservation getReservation() {
        return reservation;
    }

    public Long getReservationId() {
        return reservation.getId();
    }

    public Action getAction() {
        return action;
    }

    public boolean isUsed() {
        return used;
    }

    public void setUsed(final boolean used) {
        this.used = used;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    @Override
    public String toString() {
        return "ReservationToken [token=" + token + ", reservationId=" + getReservationId() + ", action=" + action
                + ", used=" + used + ", createdAt=" + createdAt + ", expiresAt=" + expiresAt + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ReservationToken)) return false;
        ReservationToken that = (ReservationToken) o;
        return token != null && token.equals(that.getToken());
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hashCode(token);
    }
}