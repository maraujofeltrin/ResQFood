package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.models.auction.Auction;
import ar.edu.itba.paw.models.auction.Bid;
import ar.edu.itba.paw.models.user.Client;
import ar.edu.itba.paw.models.user.User;
import ar.edu.itba.paw.persistence.util.Pagination;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    public List<Bid> findByAuctionId(final long auctionId, final int page, final int pageSize) {
        final Query idQuery = em.createNativeQuery(
                "SELECT b.id FROM bids b WHERE b.auction_id = :auctionId ORDER BY b.amount DESC, b.timestamp ASC");
        idQuery.setParameter("auctionId", auctionId);
        idQuery.setFirstResult(Pagination.offset(page, pageSize));
        idQuery.setMaxResults(pageSize);

        final List<Long> ids = parseLongIds(idQuery.getResultList());
        if (ids.isEmpty()) {
            return Collections.emptyList();
        }

        return em.createQuery(
                        "FROM Bid b JOIN FETCH b.client WHERE b.id IN :ids ORDER BY "
                                + buildBidIdPositionOrderByClause(ids),
                        Bid.class)
                .setParameter("ids", ids)
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

    @Override
    public List<User> findBidders(final long auctionId) {
        return em.createQuery(
                        "SELECT DISTINCT b.client.user FROM Bid b WHERE b.auction.id = :auctionId",
                        User.class)
                .setParameter("auctionId", auctionId)
                .getResultList();
    }

    @Override
    public Optional<User> findBidder(final long auctionId, final long bidderUserId) {
        return em.createQuery(
                        "SELECT b.client.user FROM Bid b WHERE b.auction.id = :auctionId AND b.client.userId = :bidderId",
                        User.class)
                .setParameter("auctionId", auctionId)
                .setParameter("bidderId", bidderUserId)
                .setMaxResults(1)
                .getResultList()
                .stream()
                .findFirst();
    }

    private List<Long> parseLongIds(final List<?> rawIds) {
        final List<Long> ids = new ArrayList<>(rawIds.size());
        for (final Object rawId : rawIds) {
            ids.add(rawId instanceof Number ? ((Number) rawId).longValue() : Long.parseLong(rawId.toString()));
        }
        return ids;
    }

    private String buildBidIdPositionOrderByClause(final List<Long> ids) {
        final StringBuilder orderBy = new StringBuilder("CASE b.id ");
        for (int index = 0; index < ids.size(); index++) {
            orderBy.append("WHEN ").append(ids.get(index).longValue()).append(" THEN ").append(index).append(' ');
        }
        orderBy.append("ELSE ").append(ids.size()).append(" END");
        return orderBy.toString();
    }
}
