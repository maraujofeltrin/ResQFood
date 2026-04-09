package ar.edu.itba.paw.models;

/**
 * Defines allowed sort criteria for pack listings.
 * <p>
 * Each constant carries a hardcoded SQL ORDER BY clause that is
 * concatenated into queries by the DAO layer.  Because the clause is
 * a compile-time constant and {@link #fromString(String)} acts as a
 * strict whitelist (invalid / malicious input falls back to
 * {@code DATE_DESC}), user-supplied values never reach the SQL string,
 * preventing ORDER BY injection.
 */
public enum PackSortOption {
    DATE_DESC("packs.id DESC"),
    TITLE_ASC("packs.title ASC, packs.id DESC"),
    PRICE_ASC("packs.final_price ASC, packs.id DESC"),
    PRICE_DESC("packs.final_price DESC, packs.id DESC"),
    DISCOUNT_DESC("(packs.original_price - packs.final_price) DESC, packs.id DESC");

    private final String orderByClause;

    PackSortOption(String orderByClause) {
        this.orderByClause = orderByClause;
    }

    public String getOrderByClause() {
        return orderByClause;
    }

    /**
     * Safely converts a user-supplied string to a {@code PackSortOption}.
     * Acts as a whitelist: only exact enum names are accepted;
     * any other value (including {@code null}) returns {@link #DATE_DESC}.
     */
    public static PackSortOption fromString(final String sortStr) {
        if (sortStr == null) {
            return DATE_DESC;
        }
        try {
            return PackSortOption.valueOf(sortStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            return DATE_DESC;
        }
    }
}
