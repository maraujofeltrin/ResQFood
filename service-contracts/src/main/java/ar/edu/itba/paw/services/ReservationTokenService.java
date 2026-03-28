package ar.edu.itba.paw.services;

import ar.edu.itba.paw.models.ReservationToken;

import java.util.Optional;

public interface ReservationTokenService {

    enum TokenValidationResult {
        SUCCESS,
        ALREADY_USED,
        EXPIRED,
        NOT_FOUND
    }

    TokenValidationResult validateOnly(String token, ReservationToken.Action action);

    TokenValidationResult validateAndConsume(String token, ReservationToken.Action action);

    Optional<Long> findReservationIdByToken(String token);
}
