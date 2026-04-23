package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.models.auction.Bid;

import java.util.List;
import java.util.Optional;

public interface BidDao {

    Bid createBid(long auctionId, long clientId, double amount);

    List<Bid> findByAuctionId(long auctionId);

    Optional<Bid> findHighestBid(long auctionId);

    List<Bid> findByClientId(long clientId);

    int countByAuctionId(long auctionId);
}
