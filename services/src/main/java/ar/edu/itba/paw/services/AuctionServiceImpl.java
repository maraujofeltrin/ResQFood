package ar.edu.itba.paw.services;

import ar.edu.itba.paw.models.Auction;
import ar.edu.itba.paw.models.AuctionSortOption;
import ar.edu.itba.paw.models.Bid;
import ar.edu.itba.paw.models.Pack;
import ar.edu.itba.paw.models.PackTag;
import ar.edu.itba.paw.persistence.AuctionDao;
import ar.edu.itba.paw.persistence.BidDao;
import ar.edu.itba.paw.persistence.PackDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

@Service
public class AuctionServiceImpl implements AuctionService {

    private final AuctionDao auctionDao;
    private final BidDao bidDao;
    private final PackDao packDao;
    private final ReservationService reservationService;

    @Autowired
    public AuctionServiceImpl(final AuctionDao auctionDao, final BidDao bidDao, final PackDao packDao, final ReservationService reservationService) {
        this.auctionDao = auctionDao;
        this.bidDao = bidDao;
        this.packDao = packDao;
        this.reservationService = reservationService;
    }

    @Transactional
    @Override
    public Auction createAuction(final long packId, final double initialPrice, final LocalDateTime endTime) {
        final Pack pack = packDao.findById(packId)
                .orElseThrow(() -> new IllegalArgumentException("Pack not found: " + packId));

        if (!pack.getActive()) {
            throw new IllegalArgumentException("Cannot create auction for inactive pack: " + packId);
        }

        if (auctionDao.findByPackId(packId).isPresent()) {
            throw new IllegalArgumentException("Pack already has an auction: " + packId);
        }

        if (initialPrice <= 0) {
            throw new IllegalArgumentException("Initial price must be positive");
        }

        final LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        if (endTime.isBefore(now)) {
            throw new IllegalArgumentException("End time must be in the future");
        }

        return auctionDao.createAuction(packId, initialPrice, endTime);
    }

    @Override
    public Optional<Auction> findById(final long id) {
        return auctionDao.findById(id);
    }

    @Override
    public Optional<Auction> findByPackId(final long packId) {
        return auctionDao.findByPackId(packId);
    }

    @Override
    public List<Auction> findActive() {
        return auctionDao.findActive();
    }

    @Override
    public List<Auction> findActive(final AuctionSortOption sort) {
        return auctionDao.findActive(sort);
    }

    @Override
    public List<Auction> searchActive(final String query, final AuctionSortOption sort) {
        return auctionDao.searchActive(query, sort);
    }

    @Override
    public List<Auction> findActiveByTags(final List<PackTag> tags, final AuctionSortOption sort) {
        return auctionDao.findActiveByTags(tags, sort);
    }

    @Override
    public List<Auction> searchActiveWithTags(final String query, final List<PackTag> tags,
            final AuctionSortOption sort) {
        return auctionDao.searchActiveWithTags(query, tags, sort);
    }

    @Override
    public List<Auction> findByCommerceId(final long commerceId) {
        return auctionDao.findByCommerceId(commerceId);
    }

    @Transactional
    @Override
    public Bid placeBid(final long auctionId, final long clientId, final double amount) {
        final Auction auction = auctionDao.findById(auctionId)
                .orElseThrow(() -> new IllegalArgumentException("Auction not found: " + auctionId));

        // Validate auction is still active
        if (auction.getStatus() != Auction.Status.ACTIVE) {
            throw new IllegalStateException("Auction is not active: " + auctionId);
        }

        final LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        if (now.isAfter(auction.getEndTime()) || now.isEqual(auction.getEndTime())) {
            throw new IllegalStateException("Auction has expired: " + auctionId);
        }

        // Prevent the commerce from bidding on its own pack
        final Pack pack = auction.getPack();
        if (pack != null && pack.getCommerceId() != null && pack.getCommerceId().equals(clientId)) {
            throw new IllegalArgumentException("Cannot bid on your own auction");
        }

        // Validate bid amount
        final double minimumBid = auction.getEffectivePrice();
        if (amount <= minimumBid) {
            throw new IllegalArgumentException(
                    "Bid amount (" + amount + ") must be greater than current price (" + minimumBid + ")");
        }

        // Capture previous bidder for notification hook
        final Long previousBidderId = auction.getCurrentBidderId();

        // Persist the bid and update the auction's denormalized fields
        final Bid bid = bidDao.createBid(auctionId, clientId, amount);
        auctionDao.updateCurrentBid(auctionId, amount, clientId);

        // TODO: Notification hook — notify outbid user
        // if (previousBidderId != null && !previousBidderId.equals(clientId)) {
        //     notificationService.notifyOutbid(previousBidderId, auctionId, amount);
        // }

        return bid;
    }

    @Transactional
    @Override
    public int closeExpiredAuctions() {
        final List<Auction> expired = auctionDao.findExpiredActive();
        int closed = 0;

        for (final Auction auction : expired) {
            auctionDao.updateStatus(auction.getId(), Auction.Status.FINISHED);
            packDao.setActive(auction.getPack().getId(), false);
            closed++;

            if (auction.getCurrentBidderId() != null && auction.getCurrentBid() != null) {
                reservationService.createReservation(
                        auction.getPack().getId(),
                        auction.getCurrentBidderId(),
                        1,
                        auction.getCurrentBid(),
                        null,
                        ""
                );
            }

            // TODO: Notification hook — notify winner
            // if (auction.getCurrentBidderId() != null) {
            //     notificationService.notifyAuctionWon(auction.getCurrentBidderId(), auction.getId());
            // }

            // TODO: Notification hook — notify commerce that auction ended
            // final Long commerceId = auction.getPack().getCommerceId();
            // notificationService.notifyAuctionEnded(commerceId, auction.getId(), auction.getCurrentBid());
        }

        return closed;
    }

    @Transactional
    @Override
    public void cancelAuction(final long auctionId) {
        final Auction auction = auctionDao.findById(auctionId)
                .orElseThrow(() -> new IllegalArgumentException("Auction not found: " + auctionId));

        if (auction.getStatus() != Auction.Status.ACTIVE) {
            throw new IllegalStateException("Only active auctions can be cancelled");
        }

        auctionDao.updateStatus(auctionId, Auction.Status.CANCELLED);

        // TODO: Notification hook — notify all bidders that auction was cancelled
        // final List<Bid> bids = bidDao.findByAuctionId(auctionId);
        // for (final Bid bid : bids) {
        //     notificationService.notifyAuctionCancelled(bid.getClientId(), auctionId);
        // }
    }

    @Override
    public List<Bid> getBidHistory(final long auctionId) {
        return bidDao.findByAuctionId(auctionId);
    }
}
