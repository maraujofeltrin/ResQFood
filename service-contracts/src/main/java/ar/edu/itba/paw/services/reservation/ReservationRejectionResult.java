package ar.edu.itba.paw.services.reservation;

import ar.edu.itba.paw.models.reservation.Reservation;
import ar.edu.itba.paw.models.reservation.ReservationRejectionError;

import java.util.Objects;
import java.util.Optional;

/**
 * Outcome of rejecting a reservation without exception-based control flow.
 */
public final class ReservationRejectionResult {

    private final Reservation reservation;
    private final ReservationRejectionError error;

    private ReservationRejectionResult(final Reservation reservation, final ReservationRejectionError error) {
        this.reservation = reservation;
        this.error = error;
    }

    public static ReservationRejectionResult success(final Reservation reservation) {
        return new ReservationRejectionResult(Objects.requireNonNull(reservation), null);
    }

    public static ReservationRejectionResult failure(final ReservationRejectionError error) {
        return new ReservationRejectionResult(null, Objects.requireNonNull(error));
    }

    public boolean isSuccess() {
        return reservation != null;
    }

    public Optional<Reservation> reservation() {
        return Optional.ofNullable(reservation);
    }

    public Optional<ReservationRejectionError> error() {
        return Optional.ofNullable(error);
    }
}
