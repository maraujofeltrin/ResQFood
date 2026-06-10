package ar.edu.itba.paw.models.user;

public class CommerceFavoriteToggleException extends RuntimeException {

    public enum Reason {
        COMMERCE_NOT_FOUND
    }

    private final Reason reason;

    public CommerceFavoriteToggleException(final Reason reason) {
        super(reason.name());
        this.reason = reason;
    }

    public CommerceFavoriteToggleException(final Reason reason, final String message) {
        super(message);
        this.reason = reason;
    }

    public Reason getReason() {
        return reason;
    }
}
