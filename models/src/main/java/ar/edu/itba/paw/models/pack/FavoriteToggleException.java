package ar.edu.itba.paw.models.pack;

public class FavoriteToggleException extends RuntimeException {

    public enum Reason {
        PACK_NOT_FOUND,
        PACK_UNAVAILABLE
    }

    private final Reason reason;

    public FavoriteToggleException(final Reason reason) {
        super(reason.name());
        this.reason = reason;
    }

    public FavoriteToggleException(final Reason reason, final String message) {
        super(message);
        this.reason = reason;
    }

    public Reason getReason() {
        return reason;
    }
}
