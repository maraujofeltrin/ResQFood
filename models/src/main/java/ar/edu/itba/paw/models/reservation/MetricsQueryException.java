package ar.edu.itba.paw.models.reservation;

/**
 * Thrown when a metrics query has invalid parameters.
 */
public class MetricsQueryException extends RuntimeException {

    public enum Reason {
        INVALID_DATE_RANGE
    }

    private final Reason reason;

    public MetricsQueryException(final Reason reason) {
        super(reason.name());
        this.reason = reason;
    }

    public MetricsQueryException(final Reason reason, final String detail) {
        super(detail != null ? reason.name() + ": " + detail : reason.name());
        this.reason = reason;
    }

    public Reason getReason() {
        return reason;
    }
}
