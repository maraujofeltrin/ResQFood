package ar.edu.itba.paw.models.auction;

/**
 * Thrown when auction creation fails due to a business rule violation.
 */
public class AuctionCreationException extends RuntimeException {

    public enum Reason {
        PACK_NOT_FOUND,
        PACK_INACTIVE,
        ALREADY_HAS_AUCTION,
        INVALID_INITIAL_PRICE,
        INVALID_MIN_INCREMENT,
        END_TIME_IN_PAST,
        INVALID_END_DATE
    }

    private final Reason reason;

    public AuctionCreationException(final Reason reason) {
        super(reason.name());
        this.reason = reason;
    }

    public AuctionCreationException(final Reason reason, final String detail) {
        super(detail != null ? reason.name() + ": " + detail : reason.name());
        this.reason = reason;
    }

    public Reason getReason() {
        return reason;
    }
}
