package ar.edu.itba.paw.services.reservation;

import ar.edu.itba.paw.models.reservation.Reservation;
import ar.edu.itba.paw.models.reservation.ReservationTokenActionError;

import java.util.Objects;
import java.util.Optional;

/**
 * Outcome of consuming a reservation token action that may need reservation context for the view.
 */
public final class ReservationTokenActionResult {

    private final Reservation reservation;
    private final ReservationTokenActionError error;

    private ReservationTokenActionResult(final Reservation reservation, final ReservationTokenActionError error) {
        this.reservation = reservation;
        this.error = error;
    }

    public static ReservationTokenActionResult success(final Reservation reservation) {
        return new ReservationTokenActionResult(Objects.requireNonNull(reservation), null);
    }

    public static ReservationTokenActionResult failure(final ReservationTokenActionError error) {
        return new ReservationTokenActionResult(null, Objects.requireNonNull(error));
    }

    public static ReservationTokenActionResult failure(final ReservationTokenActionError error,
            final Reservation reservation) {
        return new ReservationTokenActionResult(reservation, Objects.requireNonNull(error));
    }

    public boolean isSuccess() {
        return error == null;
    }

    public Optional<Reservation> reservation() {
        return Optional.ofNullable(reservation);
    }

    public Optional<ReservationTokenActionError> error() {
        return Optional.ofNullable(error);
    }
}
