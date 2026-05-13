package ar.edu.itba.paw.models.auction;

/**
 * Defines allowed sort criteria for active auction listings.
 *
 * The ORDER BY clause is fixed per enum constant and selected through
 * {@link #fromString(String)} as a strict whitelist.
 */
public enum AuctionSortOption {
    TIME_REMAINING_DESC("a.end_time DESC, a.id DESC"),
    TIME_REMAINING_ASC("a.end_time ASC, a.id ASC"),
    PRICE_ASC("COALESCE(a.current_bid, a.initial_price) ASC, a.id ASC"),
    PRICE_DESC("COALESCE(a.current_bid, a.initial_price) DESC, a.id DESC");

    private final String orderByClause;

    AuctionSortOption(final String orderByClause) {
        this.orderByClause = orderByClause;
    }

    public String getOrderByClause() {
        return orderByClause;
    }

    public static AuctionSortOption fromString(final String sortStr) {
        if (sortStr == null) {
            return TIME_REMAINING_DESC;
        }
        try {
            return AuctionSortOption.valueOf(sortStr.toUpperCase());
        } catch (final IllegalArgumentException e) {
            return TIME_REMAINING_DESC;
        }
    }
}
