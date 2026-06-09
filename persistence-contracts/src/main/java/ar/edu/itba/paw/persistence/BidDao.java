package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.models.auction.Bid;
import ar.edu.itba.paw.models.user.User;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public interface BidDao {

    Bid createBid(long auctionId, long clientId, double amount);

    List<Bid> findByAuctionId(long auctionId, int page, int pageSize);

    Optional<Bid> findHighestBid(long auctionId);

    int countByAuctionId(long auctionId);

    Map<Long, Double> findMaxBidsByClientForAuctions(long clientUserId, Collection<Long> auctionIds);

    Set<Long> findAuctionIdsWithBids(Collection<Long> auctionIds);

    boolean existsByAuctionIdAndClientUserId(long auctionId, long clientUserId);

    List<User> findBidders(long auctionId);

    Optional<User> findBidder(long auctionId, long bidderUserId);
}
