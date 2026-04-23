package ar.edu.itba.paw.models.auction;

/**
 * Stable reason for a failed bid; used to map to i18n in the web layer.
 */
public enum BidFailureReason {
    AUCTION_NOT_FOUND,
    NOT_ACTIVE,
    EXPIRED,
    OWN_COMMERCE,
    ALREADY_LEADING,
    AMOUNT_BELOW_MINIMUM
}
