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
import java.util.List;
import java.util.Optional;

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
        return em.createQuery("FROM Bid b WHERE b.auction.id = :auctionId ORDER BY b.amount DESC, b.timestamp ASC", Bid.class)
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
}
