package ar.edu.itba.paw.models.security;

/**
 * Thrown when a password reset operation fails due to a business rule violation.
 */
public class PasswordResetException extends RuntimeException {

    public enum Reason {
        TOKEN_NOT_FOUND,
        TOKEN_EXPIRED,
        USER_NOT_FOUND
    }

    private final Reason reason;

    public PasswordResetException(final Reason reason) {
        super(reason.name());
        this.reason = reason;
    }

    public PasswordResetException(final Reason reason, final String detail) {
        super(detail != null ? reason.name() + ": " + detail : reason.name());
        this.reason = reason;
    }

    public Reason getReason() {
        return reason;
    }
}
