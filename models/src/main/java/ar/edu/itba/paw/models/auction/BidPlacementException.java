package ar.edu.itba.paw.models.auction;

import java.util.Objects;

/**
 * Thrown by {@code AuctionService#placeBid} for domain failures the UI should translate.
 */
public class BidPlacementException extends RuntimeException {

    private final BidFailureReason reason;

    public BidPlacementException(final BidFailureReason reason) {
        this(reason, null);
    }

    public BidPlacementException(final BidFailureReason reason, final String detail) {
        super(detail != null ? reason.name() + ": " + detail : reason.name());
        this.reason = Objects.requireNonNull(reason);
    }

    public BidFailureReason getReason() {
        return reason;
    }
}
