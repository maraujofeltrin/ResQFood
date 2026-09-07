package ar.edu.itba.paw.services.reservation;

import ar.edu.itba.paw.models.reservation.Reservation;

import java.util.Objects;
import java.util.Optional;

/**
 * Generic outcome for reservation operations that may fail with a typed error.
 *
 * @param <E> the error enum type (e.g. {@code ReservationRejectionError},
 *            {@code ReservationTokenActionError}, {@code PickupByCodeError})
 */
public final class ReservationServiceResult<E> {

    private final Reservation reservation;
    private final E error;

    private ReservationServiceResult(final Reservation reservation, final E error) {
        this.reservation = reservation;
        this.error = error;
    }

    public static <E> ReservationServiceResult<E> success(final Reservation reservation) {
        return new ReservationServiceResult<>(Objects.requireNonNull(reservation), null);
    }

    public static <E> ReservationServiceResult<E> failure(final E error) {
        return new ReservationServiceResult<>(null, Objects.requireNonNull(error));
    }

    public static <E> ReservationServiceResult<E> failure(final E error, final Reservation reservation) {
        return new ReservationServiceResult<>(reservation, Objects.requireNonNull(error));
    }

    public boolean isSuccess() {
        return error == null;
    }

    public Optional<Reservation> reservation() {
        return Optional.ofNullable(reservation);
    }

    public Optional<E> error() {
        return Optional.ofNullable(error);
    }
}
