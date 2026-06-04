package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.models.auction.Auction;
import ar.edu.itba.paw.models.auction.Bid;
import ar.edu.itba.paw.models.user.Client;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Primary
@Repository("bidJpaDao")
public class BidJpaDao implements BidDao {

    @PersistenceContext
    private EntityManager em;

    @Override
    public Bid createBid(final long auctionId, final long clientId, final double amount) {
        final Auction auction = em.getReference(Auction.class, auctionId);
        final Client client = em.getReference(Client.class, clientId);
        final Bid bid = new Bid(null, auction, client, amount, LocalDateTime.now(ZoneOffset.UTC));
        em.persist(bid);
        return bid;
    }

    @Override
    public List<Bid> findByAuctionId(final long auctionId) {
        return em.createQuery(
                "FROM Bid b JOIN FETCH b.client WHERE b.auction.id = :auctionId ORDER BY b.amount DESC, b.timestamp ASC",
                Bid.class)
                .setParameter("auctionId", auctionId)
                .getResultList();
    }

    @Override
    public Optional<Bid> findHighestBid(final long auctionId) {
        return em.createQuery("FROM Bid b WHERE b.auction.id = :auctionId ORDER BY b.amount DESC, b.timestamp ASC", Bid.class)
            .setParameter("auctionId", auctionId)
            .setMaxResults(1)
            .getResultList()
            .stream()
            .findFirst();
    }

    @Override
    public List<Bid> findByClientId(final long clientId) {
        return em.createQuery("FROM Bid b WHERE b.client.userId = :clientId ORDER BY b.timestamp DESC", Bid.class)
                .setParameter("clientId", clientId)
                .getResultList();
    }

    @Override
    public int countByAuctionId(final long auctionId) {
        final Number count = em.createQuery("SELECT COUNT(b) FROM Bid b WHERE b.auction.id = :auctionId", Number.class)
                .setParameter("auctionId", auctionId)
                .getSingleResult();
        return count != null ? count.intValue() : 0;
    }

    @Override
    public Map<Long, Double> findMaxBidsByClientForAuctions(final long clientUserId, final Collection<Long> auctionIds) {
        if (auctionIds == null || auctionIds.isEmpty()) {
            return Collections.emptyMap();
        }
        final List<Object[]> rows = em.createQuery(
                        "SELECT b.auction.id, MAX(b.amount) FROM Bid b "
                                + "WHERE b.client.userId = :clientId AND b.auction.id IN :auctionIds "
                                + "GROUP BY b.auction.id",
                        Object[].class)
                .setParameter("clientId", clientUserId)
                .setParameter("auctionIds", new ArrayList<>(auctionIds))
                .getResultList();
        final Map<Long, Double> result = new HashMap<>();
        for (final Object[] row : rows) {
            result.put((Long) row[0], (Double) row[1]);
        }
        return result;
    }

    @Override
    public Set<Long> findAuctionIdsWithBids(final Collection<Long> auctionIds) {
        if (auctionIds == null || auctionIds.isEmpty()) {
            return Collections.emptySet();
        }
        final List<Long> ids = em.createQuery(
                        "SELECT DISTINCT b.auction.id FROM Bid b WHERE b.auction.id IN :ids",
                        Long.class)
                .setParameter("ids", new ArrayList<>(auctionIds))
                .getResultList();
        return new HashSet<>(ids);
    }

    @Override
    public boolean existsByAuctionIdAndClientUserId(final long auctionId, final long clientUserId) {
        final Number count = em.createQuery(
                        "SELECT COUNT(b.id) FROM Bid b "
                                + "WHERE b.auction.id = :auctionId AND b.client.userId = :clientId",
                        Number.class)
                .setParameter("auctionId", auctionId)
                .setParameter("clientId", clientUserId)
                .getSingleResult();
        return count != null && count.intValue() > 0;
    }
}
