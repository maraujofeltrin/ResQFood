package ar.edu.itba.paw.models.auction;

import java.util.Objects;
import java.util.Optional;

/**
 * Thrown by {@code AuctionService#placeBid} for domain failures the UI should translate.
 */
public class BidPlacementException extends RuntimeException {

    private final BidFailureReason reason;
    private final Double bidIncrement;

    public BidPlacementException(final BidFailureReason reason) {
        this(reason, null, null);
    }

    public BidPlacementException(final BidFailureReason reason, final String detail) {
        this(reason, detail, null);
    }

    private BidPlacementException(final BidFailureReason reason, final String detail, final Double bidIncrement) {
        super(detail != null ? reason.name() + ": " + detail : reason.name());
        this.reason = Objects.requireNonNull(reason);
        this.bidIncrement = bidIncrement;
    }

    public static BidPlacementException belowMinimum(final double bidIncrement) {
        return new BidPlacementException(BidFailureReason.AMOUNT_BELOW_MINIMUM, null, bidIncrement);
    }

    public BidFailureReason getReason() {
        return reason;
    }

    /** Only present when reason is {@link BidFailureReason#AMOUNT_BELOW_MINIMUM}. */
    public Optional<Double> getBidIncrement() {
        return Optional.ofNullable(bidIncrement);
    }
}
