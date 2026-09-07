package ar.edu.itba.paw.models.user;

public class CommerceReviewException extends RuntimeException {

    public enum Reason {
        NOT_ELIGIBLE,
        INVALID_BODY,
        INVALID_RATING
    }

    private final Reason reason;

    public CommerceReviewException(final Reason reason) {
        super(reason.name());
        this.reason = reason;
    }

    public CommerceReviewException(final Reason reason, final String message) {
        super(message);
        this.reason = reason;
    }

    public Reason getReason() {
        return reason;
    }
}
