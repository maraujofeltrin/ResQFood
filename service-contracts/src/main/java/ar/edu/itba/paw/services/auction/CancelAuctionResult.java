package ar.edu.itba.paw.services.auction;

/**
 * Outcome of {@link AuctionService#cancelAuction}.
 */
public final class CancelAuctionResult {

    public enum Outcome {
        /** Auction was successfully cancelled. */
        SUCCESS,
        /** Auction does not exist. */
        NOT_FOUND,
        /** Requesting user is not the owner of the auction's commerce. */
        FORBIDDEN,
        /** Auction has bids and cannot be cancelled. */
        HAS_BIDS,
        /** Auction is not active (already cancelled or finished). */
        NOT_ACTIVE
    }

    private final Outcome outcome;

    private CancelAuctionResult(final Outcome outcome) {
        this.outcome = outcome;
    }

    public static CancelAuctionResult success() {
        return new CancelAuctionResult(Outcome.SUCCESS);
    }

    public static CancelAuctionResult notFound() {
        return new CancelAuctionResult(Outcome.NOT_FOUND);
    }

    public static CancelAuctionResult forbidden() {
        return new CancelAuctionResult(Outcome.FORBIDDEN);
    }

    public static CancelAuctionResult hasBids() {
        return new CancelAuctionResult(Outcome.HAS_BIDS);
    }

    public static CancelAuctionResult notActive() {
        return new CancelAuctionResult(Outcome.NOT_ACTIVE);
    }

    public Outcome getOutcome() {
        return outcome;
    }
}
