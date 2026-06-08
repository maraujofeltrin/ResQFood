package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.models.auction.Auction;
import ar.edu.itba.paw.models.auction.AuctionSortOption;
import ar.edu.itba.paw.models.pack.PackTag;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface AuctionDao {

    Auction createAuction(long packId, double initialPrice, double minBidIncrement, LocalDateTime endTime);

    Optional<Auction> findById(long id);

    Optional<Auction> findByPackId(long packId);

    /**
     * @param requirePositiveStock when {@code true}, only auctions whose pack has {@code stock > 0} are included
     * @param commerceUserId       when non-null, only auctions whose pack belongs to this commerce user id
     */
    List<Auction> filterAuctions(String query, List<PackTag> tags, String city, List<String> timeRanges,
                                 AuctionSortOption sort, int page, int pageSize, boolean requirePositiveStock,
                                 Long commerceUserId);

    int countFilteredAuctions(String query, List<PackTag> tags, String city, List<String> timeRanges,
                              boolean requirePositiveStock, Long commerceUserId);

    List<Auction> findByCommerceId(long commerceId);

    List<Auction> findByStatus(Auction.Status status);

    void updateStatus(long auctionId, Auction.Status status);

    void updateCurrentBid(long auctionId, double amount, long bidderId);

    /**
     * Returns auctions that are still marked as ACTIVE but whose {@code end_time} has passed.
     */
    List<Auction> findExpiredActive();

    /**
     * Paginated auctions in which the given client has placed at least one bid,
     * optionally filtered by status and/or search query (title/description/commerce name).
     */
    List<Auction> filterParticipatedAuctions(long clientId, Auction.Status status, String query,
                                              int page, int pageSize);

    int countParticipatedAuctions(long clientId, Auction.Status status, String query);

}
