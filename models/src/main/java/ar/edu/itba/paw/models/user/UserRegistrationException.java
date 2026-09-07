package ar.edu.itba.paw.models.user;

/**
 * Thrown when user registration fails due to missing or invalid profile data.
 */
public class UserRegistrationException extends RuntimeException {

    public enum Reason {
        MISSING_CLIENT_PROFILE,
        MISSING_COMMERCE_PROFILE,
        INVALID_PROVINCE
    }

    private final Reason reason;

    public UserRegistrationException(final Reason reason) {
        super(reason.name());
        this.reason = reason;
    }

    public UserRegistrationException(final Reason reason, final String detail) {
        super(detail != null ? reason.name() + ": " + detail : reason.name());
        this.reason = reason;
    }

    public Reason getReason() {
        return reason;
    }
}
