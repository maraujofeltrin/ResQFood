package ar.edu.itba.paw.services.commerce;

import ar.edu.itba.paw.models.pack.Pack;
import ar.edu.itba.paw.models.pack.PackTag;
import ar.edu.itba.paw.services.auction.AuctionService;
import ar.edu.itba.paw.services.pack.PackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;

@Service
public class CommerceOfferServiceImpl implements CommerceOfferService {

    private final PackService packService;
    private final AuctionService auctionService;
    private final ZoneId businessZone;

    @Autowired
    public CommerceOfferServiceImpl(final PackService packService, final AuctionService auctionService,
            final ZoneId businessZone) {
        this.packService = packService;
        this.auctionService = auctionService;
        this.businessZone = businessZone;
    }

    @Transactional
    @Override
    public Pack createDirectPack(final long commerceId, final String title, final String description,
            final double originalPrice, final double finalPrice, final int stock, final List<PackTag> tags,
            final Long imageId) {
        return packService.createPack(commerceId, title, description, originalPrice, finalPrice, stock,
                tags != null ? tags : Collections.emptyList(), imageId);
    }

    @Transactional
    @Override
    public Pack createAuctionOffer(final long commerceId, final String title, final String description,
            final double originalPrice, final double initialPrice, final double minBidIncrement, final String endDate, final String endTime,
            final List<PackTag> tags, final Long imageId) {
        final Pack pack = packService.createPack(
                commerceId, title, description, originalPrice, initialPrice, 1,
                tags != null ? tags : Collections.emptyList(), imageId);
        final LocalDateTime endUtc = parseAuctionEndAsUtc(endDate, endTime);
        auctionService.createAuction(pack.getId(), initialPrice, minBidIncrement, endUtc);
        return pack;
    }

    private LocalDateTime parseAuctionEndAsUtc(final String endDate, final String endTime) {
        final LocalDate date = LocalDate.parse(endDate.trim());
        final LocalTime time = LocalTime.parse(endTime.trim());
        return ZonedDateTime.of(date, time, businessZone)
                .withZoneSameInstant(ZoneOffset.UTC)
                .toLocalDateTime();
    }
}
