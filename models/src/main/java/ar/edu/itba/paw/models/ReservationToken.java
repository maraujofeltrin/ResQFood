package ar.edu.itba.paw.models;

import java.time.LocalDateTime;

public class ReservationToken {

    public enum Action {
        ACCEPT,
        REJECT
    }

    private final String token;
    private final Long reservationId;
    private final Action action;
    private final boolean used;
    private final LocalDateTime createdAt;
    private final LocalDateTime expiresAt;

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
