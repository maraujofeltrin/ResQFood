package ar.edu.itba.paw.models.reservation;

/**
 * Thrown when a reservation operation fails due to a business rule violation.
 */
public class ReservationCreationException extends RuntimeException {

    public enum Reason {
        INVALID_QUANTITY,
        PICKUP_WINDOW_TOO_LONG,
        NOT_A_CLIENT,
        USER_NOT_FOUND,
        CLIENT_PROFILE_NOT_FOUND,
        INSUFFICIENT_STOCK,
        INVALID_STATUS,
        RESERVATION_NOT_FOUND,
        ALREADY_CANCELED,
        ALREADY_COMPLETED,
        PACK_NOT_FOUND,
        STOCK_RESTORE_FAILED
    }

    private final Reason reason;

    public ReservationCreationException(final Reason reason) {
        super(reason.name());
        this.reason = reason;
    }

    public ReservationCreationException(final Reason reason, final String detail) {
        super(detail != null ? reason.name() + ": " + detail : reason.name());
        this.reason = reason;
    }

    public Reason getReason() {
        return reason;
    }
}
