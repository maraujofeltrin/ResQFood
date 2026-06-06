package ar.edu.itba.paw.models.user;

/**
 * Thrown when updating a commerce profile fails due to missing or invalid fields.
 */
public class CommerceProfileException extends RuntimeException {

    public enum Reason {
        MISSING_CATEGORY,
        MISSING_TIMES,
        MISSING_ADDRESS,
        INVALID_PROVINCE
    }

    private final Reason reason;

    public CommerceProfileException(final Reason reason) {
        super(reason.name());
        this.reason = reason;
    }

    public CommerceProfileException(final Reason reason, final String detail) {
        super(detail != null ? reason.name() + ": " + detail : reason.name());
        this.reason = reason;
    }

    public Reason getReason() {
        return reason;
    }
}
