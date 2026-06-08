package ar.edu.itba.paw.services.reservation;

import ar.edu.itba.paw.models.reservation.ReservationToken;
import ar.edu.itba.paw.models.reservation.ReservationTokenActionError;

import java.time.LocalDateTime;
import java.util.Optional;

public interface ReservationTokenService {

    ReservationToken create(String token, Long reservationId, ReservationToken.Action action, LocalDateTime createdAt,
            LocalDateTime expiresAt);

    enum TokenValidationResult {
        SUCCESS,
        ALREADY_USED,
        EXPIRED,
        NOT_FOUND
    }

    TokenValidationResult validateOnly(String token, ReservationToken.Action action);

    Optional<Long> findReservationIdByToken(String token);

    /**
     * Consumes an ACCEPT token after validating pickup code,
     * then confirms pickup.
     */
    ReservationServiceResult<ReservationTokenActionError> acceptReservationTokenWithPickupCode(
            String token, String pickupCode);

    /**
     * Consumes a REJECT token,
     * then rejects the reservation (stock restore + status + email).
     */
    ReservationServiceResult<ReservationTokenActionError> rejectReservationToken(String token);
}
