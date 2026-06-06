package ar.edu.itba.paw.models.image;

/**
 * Thrown when an image upload or save operation violates business constraints.
 */
public class ProfileImageException extends RuntimeException {

    public enum Reason {
        DATA_EMPTY,
        SIZE_EXCEEDED,
        INVALID_TYPE
    }

    private final Reason reason;

    public ProfileImageException(final Reason reason) {
        super(reason.name());
        this.reason = reason;
    }

    public ProfileImageException(final Reason reason, final String detail) {
        super(detail != null ? reason.name() + ": " + detail : reason.name());
        this.reason = reason;
    }

    public Reason getReason() {
        return reason;
    }
}
