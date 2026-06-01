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
import ar.edu.itba.paw.persistence.CommerceDao;
import ar.edu.itba.paw.persistence.PackDao;
import ar.edu.itba.paw.services.reservation.ReservationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Service
public class AuctionServiceImpl implements AuctionService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuctionServiceImpl.class);

    private final AuctionDao auctionDao;
    private final BidDao bidDao;
    private final PackDao packDao;
    private final ReservationService reservationService;
    private final CommerceDao commerceDao;

    @Autowired
    public AuctionServiceImpl(final AuctionDao auctionDao, final BidDao bidDao, final PackDao packDao,
            final ReservationService reservationService, final CommerceDao commerceDao) {
        this.auctionDao = auctionDao;
        this.bidDao = bidDao;
        this.packDao = packDao;
        this.reservationService = reservationService;
        this.commerceDao = commerceDao;
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

        final Auction createdAuction = auctionDao.createAuction(packId, initialPrice, minBidIncrement, endTime);
        LOGGER.info("Auction created: auctionId={}, packId={}", createdAuction.getId(), packId);
        return createdAuction;
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
    public List<Auction> filterAuctions(final String query, final List<PackTag> tags, final String city,
            final List<String> timeRanges, final AuctionSortOption sort, final int page, final int pageSize,
            final boolean requirePositiveStock, final Long commerceUserId) {
        return auctionDao.filterAuctions(query, tags, city, timeRanges, sort, page, pageSize, requirePositiveStock,
                commerceUserId);
    }

    @Override
    public int countFilteredAuctions(final String query, final List<PackTag> tags, final String city,
            final List<String> timeRanges, final boolean requirePositiveStock, final Long commerceUserId) {
        return auctionDao.countFilteredAuctions(query, tags, city, timeRanges, requirePositiveStock, commerceUserId);
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

        LOGGER.info("Bid placed: auctionId={}, clientId={}, amount={}", auctionId, clientId, amount);
        return bid;
    }

    @Transactional
    @Override
    public int closeExpiredAuctions() {
        final List<Auction> expired = auctionDao.findExpiredActive();
        int closed = 0;

        for (final Auction auction : expired) {
            auction.setStatus(Auction.Status.FINISHED);
            auction.getPack().setActive(false);
            closed++;

            if (auction.getCurrentBidderId() != null && auction.getCurrentBid() != null) {
                try {
                    reservationService.createReservation(
                            auction.getPack().getId(),
                            auction.getCurrentBidderId(),
                            1,
                            auction.getCurrentBid(),
                            null,
                            ""
                    );
                } catch (final RuntimeException e) {
                    final Long packId = auction.getPack() != null ? auction.getPack().getId() : null;
                    LOGGER.error(
                            "closeExpiredAuctions: reservation creation failed auctionId={} packId={} bidderUserId={}",
                            auction.getId(), packId, auction.getCurrentBidderId(), e);
                    throw e;
                }
            }

        }

        return closed;
    }

    @Transactional
    @Override
    public CancelAuctionResult cancelAuction(final long auctionId) {
        final Auction auction = auctionDao.findById(auctionId).orElse(null);
        if (auction == null) {
            LOGGER.warn("Failed to cancel auction: auctionId={}, result={}", auctionId, CancelAuctionResult.notFound().getOutcome().name());
            return CancelAuctionResult.notFound();
        }

        // Check if auction is active
        if (auction.getStatus() != Auction.Status.ACTIVE) {
            LOGGER.warn("Failed to cancel auction: auctionId={}, result={}", auctionId, CancelAuctionResult.notActive().getOutcome().name());
            return CancelAuctionResult.notActive();
        }

        final Pack pack = auction.getPack();

        final int bidCount = bidDao.countByAuctionId(auctionId);
        if (bidCount > 0) {
            LOGGER.warn("Failed to cancel auction: auctionId={}, result={}", auctionId, CancelAuctionResult.hasBids().getOutcome().name());
            return CancelAuctionResult.hasBids();
        }

        auctionDao.updateStatus(auctionId, Auction.Status.CANCELLED);
        packDao.setActive(pack.getId(), false);

        LOGGER.info("Auction cancelled: auctionId={}", auctionId);
        return CancelAuctionResult.success();
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
    public List<Auction> filterParticipatedAuctions(final long clientId, final Auction.Status status,
                                                     final String query, final int page, final int pageSize) {
        return auctionDao.filterParticipatedAuctions(clientId, status, query, page, pageSize);
    }

    @Override
    public int countParticipatedAuctions(final long clientId, final Auction.Status status, final String query) {
        return auctionDao.countParticipatedAuctions(clientId, status, query);
    }

    @Override
    public Set<Long> findPackIdsWithAuction(final Collection<Long> packIds) {
        return auctionDao.findPackIdsWithAuction(packIds);
    }

    @Override
    public Map<Long, Double> getMaxBidsByClientForAuctions(final long clientUserId, final Collection<Long> auctionIds) {
        return bidDao.findMaxBidsByClientForAuctions(clientUserId, auctionIds);
    }

    @Override
    public Set<Long> findAuctionIdsWhereClientLeads(final long clientUserId, final Collection<Long> auctionIds) {
        return bidDao.findAuctionIdsWhereClientLeads(clientUserId, auctionIds);
    }

    @Override
    public List<AuctionPackSummary> findSummariesByPackIds(final Collection<Long> packIds) {
        if (packIds == null || packIds.isEmpty()) {
            return Collections.emptyList();
        }
        final List<Object[]> rows = auctionDao.findSummariesByPackIds(packIds);
        if (rows.isEmpty()) {
            return Collections.emptyList();
        }
        final List<Long> auctionIds = new ArrayList<>(rows.size());
        for (final Object[] row : rows) {
            auctionIds.add((Long) row[1]);
        }
        final Set<Long> auctionIdsWithBids = bidDao.findAuctionIdsWithBids(auctionIds);
        final List<AuctionPackSummary> summaries = new ArrayList<>(rows.size());
        for (final Object[] row : rows) {
            final long packId = (Long) row[0];
            final long auctionId = (Long) row[1];
            final Auction.Status status = (Auction.Status) row[2];
            summaries.add(new AuctionPackSummary(packId, auctionId, status, auctionIdsWithBids.contains(auctionId)));
        }
        return summaries;
    }

    @Override
    public boolean hasClientBidOnAuction(final long auctionId, final long clientUserId) {
        return bidDao.existsByAuctionIdAndClientUserId(auctionId, clientUserId);
    }
}
