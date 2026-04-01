package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.models.ReservationToken;

import java.time.LocalDateTime;
import java.util.Optional;

public interface ReservationTokenDao {

    ReservationToken create(String token, Long reservationId, ReservationToken.Action action, LocalDateTime createdAt,
            LocalDateTime expiresAt);

    Optional<ReservationToken> findByToken(String token);

    void markAsUsed(String token);
}
