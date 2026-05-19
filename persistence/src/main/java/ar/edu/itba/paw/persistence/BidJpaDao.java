package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.models.auction.Bid;
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
        final Bid bid = new Bid(null, auctionId, clientId, amount, LocalDateTime.now(ZoneOffset.UTC));
        em.persist(bid);
        return bid;
    }

    @Override
    public List<Bid> findByAuctionId(final long auctionId) {
        return em.createQuery("FROM Bid b WHERE b.auctionId = :auctionId ORDER BY b.amount DESC, b.timestamp ASC", Bid.class)
                .setParameter("auctionId", auctionId)
                .getResultList();
    }

    @Override
    public Optional<Bid> findHighestBid(final long auctionId) {
        return em.createQuery("FROM Bid b WHERE b.auctionId = :auctionId ORDER BY b.amount DESC, b.timestamp ASC", Bid.class)
            .setParameter("auctionId", auctionId)
            .setMaxResults(1)
            .getResultList()
            .stream()
            .findFirst();
    }

    @Override
    public List<Bid> findByClientId(final long clientId) {
        return em.createQuery("FROM Bid b WHERE b.clientId = :clientId ORDER BY b.timestamp DESC", Bid.class)
                .setParameter("clientId", clientId)
                .getResultList();
    }

    @Override
    public int countByAuctionId(final long auctionId) {
        final Number count = em.createQuery("SELECT COUNT(b) FROM Bid b WHERE b.auctionId = :auctionId", Number.class)
                .setParameter("auctionId", auctionId)
                .getSingleResult();
        return count != null ? count.intValue() : 0;
    }
}
