package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.models.Auction;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface AuctionDao {

    Auction createAuction(long packId, double initialPrice, LocalDateTime endTime);

    Optional<Auction> findById(long id);

    Optional<Auction> findByPackId(long packId);

    List<Auction> findActive();

    List<Auction> findByCommerceId(long commerceId);

    List<Auction> findByStatus(Auction.Status status);

    void updateStatus(long auctionId, Auction.Status status);

    void updateCurrentBid(long auctionId, double amount, long bidderId);

    /**
     * Returns auctions that are still marked as ACTIVE but whose {@code end_time} has passed.
     */
    List<Auction> findExpiredActive();
}
