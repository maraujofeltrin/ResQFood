package ar.edu.itba.paw.services.reservation;

import ar.edu.itba.paw.models.reservation.PickupByCodeError;
import ar.edu.itba.paw.models.reservation.Reservation;

import java.util.Objects;
import java.util.Optional;

/**
 * Outcome of confirming pickup by code (no exception-based control flow for expected failures).
 */
public final class PickupByCodeResult {

    private final Reservation reservation;
    private final PickupByCodeError error;

    private PickupByCodeResult(final Reservation reservation, final PickupByCodeError error) {
        this.reservation = reservation;
        this.error = error;
    }

    public static PickupByCodeResult success(final Reservation reservation) {
        return new PickupByCodeResult(Objects.requireNonNull(reservation), null);
    }

    public static PickupByCodeResult failure(final PickupByCodeError error) {
        return new PickupByCodeResult(null, Objects.requireNonNull(error));
    }

    public boolean isSuccess() {
        return reservation != null;
    }

    public Optional<Reservation> reservation() {
        return Optional.ofNullable(reservation);
    }

    public Optional<PickupByCodeError> error() {
        return Optional.ofNullable(error);
    }
}
