package ar.edu.itba.paw.services.auction;

import ar.edu.itba.paw.models.auction.Auction;

public record AuctionPackSummary(
        long packId,
        long auctionId,
        Auction.Status status,
        boolean hasBids
) {
}
