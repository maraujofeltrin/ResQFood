package ar.edu.itba.paw.services.auction;

import ar.edu.itba.paw.models.auction.Auction;
import ar.edu.itba.paw.models.auction.AuctionSortOption;
import ar.edu.itba.paw.models.auction.Bid;
import ar.edu.itba.paw.models.auction.BidFailureReason;
import ar.edu.itba.paw.models.auction.BidPlacementException;
import ar.edu.itba.paw.models.pack.Pack;
import ar.edu.itba.paw.models.pack.PackTag;
import ar.edu.itba.paw.persistence.AuctionDao;
import ar.edu.itba.paw.persistence.BidDao;
import ar.edu.itba.paw.persistence.PackDao;
import ar.edu.itba.paw.services.reservation.ReservationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

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
    public Auction createAuction(final long packId, final double initialPrice, final double minBidIncrement, final LocalDateTime endTime) {
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

        if (minBidIncrement <= 0) {
            throw new IllegalArgumentException("Minimum bid increment must be positive");
        }

        final LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        if (endTime.isBefore(now)) {
            throw new IllegalArgumentException("End time must be in the future");
        }

        return auctionDao.createAuction(packId, initialPrice, minBidIncrement, endTime);
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
                .orElseThrow(() -> new BidPlacementException(BidFailureReason.AUCTION_NOT_FOUND, String.valueOf(auctionId)));

        if (auction.getStatus() != Auction.Status.ACTIVE) {
            throw new BidPlacementException(BidFailureReason.NOT_ACTIVE, String.valueOf(auctionId));
        }

        final LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        if (now.isAfter(auction.getEndTime()) || now.isEqual(auction.getEndTime())) {
            throw new BidPlacementException(BidFailureReason.EXPIRED, String.valueOf(auctionId));
        }

        final Pack pack = auction.getPack();
        if (pack != null && pack.getCommerceId() != null && pack.getCommerceId().equals(clientId)) {
            throw new BidPlacementException(BidFailureReason.OWN_COMMERCE);
        }

        final Long currentLeaderId = auction.getCurrentBidderId();
        if (currentLeaderId != null && currentLeaderId.equals(clientId)) {
            throw new BidPlacementException(BidFailureReason.ALREADY_LEADING);
        }

        final double base = auction.getEffectivePrice();
        final double inc = auction.getMinBidIncrement() != null ? auction.getMinBidIncrement() : 0d;
        final double minimumRequired = base + inc;
        if (amount < minimumRequired) {
            throw new BidPlacementException(BidFailureReason.AMOUNT_BELOW_MINIMUM);
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

    @Override
    public boolean isClientLeading(final long auctionId, final long userId) {
        final Optional<Auction> auctionOpt = auctionDao.findById(auctionId);
        if (auctionOpt.isEmpty()) {
            return false;
        }
        final Auction auction = auctionOpt.get();
        return auction.getCurrentBidderId() != null && auction.getCurrentBidderId().equals(userId);
    }

    @Override
    public List<Auction> findParticipatedAuctionsByClientId(final long clientId) {
        final List<Bid> bids = bidDao.findByClientId(clientId);
        if (bids.isEmpty()) {
            return List.of();
        }
        final Set<Long> seenAuction = new HashSet<>();
        final List<Long> orderedAuctionIds = new ArrayList<>();
        for (final Bid bid : bids) {
            if (seenAuction.add(bid.getAuctionId())) {
                orderedAuctionIds.add(bid.getAuctionId());
            }
        }
        final List<Auction> result = new ArrayList<>();
        for (final Long auctionId : orderedAuctionIds) {
            auctionDao.findById(auctionId).ifPresent(result::add);
        }
        return result;
    }
}
