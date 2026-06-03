package ar.edu.itba.paw.models.pack;

public class PackDirectEditException extends RuntimeException {

    public enum Reason {
        NOT_FOUND,
        FORBIDDEN_AUCTION
    }

    public enum ForbiddenAction {
        EDIT,
        DELETE
    }

    private final Reason reason;
    private final ForbiddenAction forbiddenAction;

    public PackDirectEditException(final Reason reason, final String message) {
        this(reason, null, message);
    }

    public PackDirectEditException(final Reason reason, final ForbiddenAction forbiddenAction,
            final String message) {
        super(message);
        this.reason = reason;
        this.forbiddenAction = forbiddenAction;
    }

    public Reason getReason() {
        return reason;
    }

    public ForbiddenAction getForbiddenAction() {
        return forbiddenAction;
    }
}
