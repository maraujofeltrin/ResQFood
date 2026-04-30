package ar.edu.itba.paw.services.auction;

import ar.edu.itba.paw.models.auction.Auction;
import ar.edu.itba.paw.models.auction.AuctionSortOption;
import ar.edu.itba.paw.models.auction.Bid;
import ar.edu.itba.paw.models.pack.PackTag;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface AuctionService {

    /**
     * Creates an auction for an existing pack.
     *
     * @param packId            the pack to auction
     * @param initialPrice      the starting price
     * @param minBidIncrement   minimum amount each new bid must exceed the current effective price
     * @param endTime           when the auction closes (UTC)
     * @return the created auction
     * @throws IllegalArgumentException if the pack does not exist or already has an active auction
     */
    Auction createAuction(long packId, double initialPrice, double minBidIncrement, LocalDateTime endTime);

    Optional<Auction> findById(long id);

    Optional<Auction> findByPackId(long packId);

    List<Auction> filterAuctions(String query, List<PackTag> tags, String city, List<String> timeRanges,
                                 AuctionSortOption sort, int page, int pageSize, boolean requirePositiveStock);

    int countFilteredAuctions(String query, List<PackTag> tags, String city, List<String> timeRanges,
                              boolean requirePositiveStock);

    List<Auction> findByCommerceId(long commerceId);

    /**
     * Places a bid on an auction. Validates that:
     * <ul>
     *     <li>The auction is active and has not expired.</li>
     *     <li>The amount is at least the current effective price plus the auction's minimum bid increment.</li>
     *     <li>The client is not the commerce that owns the pack.</li>
     *     <li>The client is not already the highest bidder.</li>
     * </ul>
     *
     * @throws BidPlacementException   for expected domain failures (see {@link ar.edu.itba.paw.models.auction.BidFailureReason})
     * @throws IllegalArgumentException for unexpected data (e.g. missing auction) — prefer {@link BidPlacementException} for known cases
     */
    Bid placeBid(long auctionId, long clientId, double amount);

    /**
     * Closes all auctions whose end time has passed but are still marked as ACTIVE.
     * Determines the winner for each and triggers notification hooks.
     *
     * @return the number of auctions closed
     */
    int closeExpiredAuctions();

    /**
     * Manually cancels an auction (typically by the commerce owner).
     */
    void cancelAuction(long auctionId);

    /**
     * Returns the bid history for an auction, ordered by amount descending.
     */
    List<Bid> getBidHistory(long auctionId);

    /**
     * Checks if the given user is currently leading the given auction.
     */
    boolean isClientLeading(long auctionId, long userId);

    /**
     * Auctions in which the client has placed at least one bid, most recently active first
     * (by the client's latest bid timestamp per auction).
     */
    List<Auction> findParticipatedAuctionsByClientId(long clientId);
}
