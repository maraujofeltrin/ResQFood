package ar.edu.itba.paw.services;

import ar.edu.itba.paw.models.Auction;
import ar.edu.itba.paw.models.AuctionSortOption;
import ar.edu.itba.paw.models.Bid;
import ar.edu.itba.paw.models.PackTag;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface AuctionService {

    /**
     * Creates an auction for an existing pack.
     *
     * @param packId       the pack to auction
     * @param initialPrice the starting price
     * @param endTime      when the auction closes (UTC)
     * @return the created auction
     * @throws IllegalArgumentException if the pack does not exist or already has an active auction
     */
    Auction createAuction(long packId, double initialPrice, LocalDateTime endTime);

    Optional<Auction> findById(long id);

    Optional<Auction> findByPackId(long packId);

    List<Auction> findActive();

    List<Auction> findActive(AuctionSortOption sort);

    List<Auction> searchActive(String query, AuctionSortOption sort);

    List<Auction> findActiveByTags(List<PackTag> tags, AuctionSortOption sort);

    List<Auction> searchActiveWithTags(String query, List<PackTag> tags, AuctionSortOption sort);

    List<Auction> findByCommerceId(long commerceId);

    /**
     * Places a bid on an auction. Validates that:
     * <ul>
     *     <li>The auction is active and has not expired.</li>
     *     <li>The amount is greater than the current bid (or initial price if no bids exist).</li>
     *     <li>The client is not the commerce that owns the pack.</li>
     * </ul>
     *
     * @throws IllegalArgumentException if the bid amount is too low or the client is the pack owner
     * @throws IllegalStateException    if the auction is not active or has expired
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
}
