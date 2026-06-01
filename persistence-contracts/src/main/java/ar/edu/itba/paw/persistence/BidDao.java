package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.models.auction.Bid;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public interface BidDao {

    Bid createBid(long auctionId, long clientId, double amount);

    List<Bid> findByAuctionId(long auctionId);

    Optional<Bid> findHighestBid(long auctionId);

    List<Bid> findByClientId(long clientId);

    int countByAuctionId(long auctionId);

    Map<Long, Double> findMaxBidsByClientForAuctions(long clientUserId, Collection<Long> auctionIds);

    Set<Long> findAuctionIdsWhereClientLeads(long clientUserId, Collection<Long> auctionIds);

    Set<Long> findAuctionIdsWithBids(Collection<Long> auctionIds);
}
