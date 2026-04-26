package ar.edu.itba.paw.services.commerce;

import ar.edu.itba.paw.models.pack.Pack;
import ar.edu.itba.paw.models.pack.PackTag;

import java.util.List;

/**
 * Creates or updates commerce offers (direct pack or auction), including business-zone time handling.
 */
public interface CommerceOfferService {

    Pack createDirectPack(long commerceId, String title, String description, double originalPrice, double finalPrice,
            int stock, List<PackTag> tags, byte[] imageData, String imageContentType);

    /**
     * Creates a pack (stock 1) and an auction for it. {@code endDate}/{@code endTime} are wall-clock in the app display zone.
     */
    Pack createAuctionOffer(long commerceId, String title, String description, double originalPrice,
            double initialPrice, double minBidIncrement, String endDate, String endTime, List<PackTag> tags, byte[] imageData,
            String imageContentType);
}
