package ar.edu.itba.paw.services.auction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class AuctionScheduler {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuctionScheduler.class);

    private final AuctionService auctionService;

    @Autowired
    public AuctionScheduler(final AuctionService auctionService) {
        this.auctionService = auctionService;
    }

    @Scheduled(fixedDelay = 60_000)
    public void closeExpiredAuctions() {
        final int closed = auctionService.closeExpiredAuctions();
        if (closed > 0) {
            LOGGER.info("Closed {} expired auction(s)", closed);
        }
    }
}
