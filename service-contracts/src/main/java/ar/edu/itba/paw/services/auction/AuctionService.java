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
                                 AuctionSortOption sort, int page, int pageSize, boolean requirePositiveStock,
                                 Long commerceUserId);

    int countFilteredAuctions(String query, List<PackTag> tags, String city, List<String> timeRanges,
                              boolean requirePositiveStock, Long commerceUserId);


    /**
     * Places a bid on an auction by auction id.
     *
     * @throws BidPlacementException for expected domain failures
     */
    Bid placeBid(long auctionId, long clientId, double amount);

    /**
     * Resolves the active auction for the given pack and places a bid.
     * Equivalent to looking up the auction and calling {@link #placeBid}, but keeps
     * that coupling in the service layer instead of the controller.
     *
     * @throws BidPlacementException if no auction exists for the pack or any bid validation fails
     */
    Bid placeBidForPack(long packId, long clientId, double amount);

    /**
     * Closes all auctions whose end time has passed but are still marked as ACTIVE.
     * Determines the winner for each and triggers notification hooks.
     *
     * @return the number of auctions closed
     */
    int closeExpiredAuctions();

    /**
     * Manually cancels an auction (typically by the commerce owner).
     * @param auctionId the auction to cancel
     * @return the result of the cancellation attempt
     */
    CancelAuctionResult cancelAuction(long auctionId);

    /**
     * Returns the bid history for an auction, ordered by amount descending.
     */
    List<Bid> getBidHistory(long auctionId, int page, int pageSize);

    /**
     * Returns the total number of bids for an auction.
     */
    int countBidsByAuction(long auctionId);

    /**
     * Checks if the given user is currently leading the given auction.
     */
    boolean isClientLeading(long auctionId, long userId);

    /**
     * Paginated version of participated auctions with optional status and text filters.
     * Text search matches pack title, description, or commerce name.
     */
    List<Auction> filterParticipatedAuctions(long clientId, Auction.Status status, String query,
                                              int page, int pageSize);

    int countParticipatedAuctions(long clientId, Auction.Status status, String query);

    boolean hasClientBidOnAuction(long auctionId, long clientUserId);
}
