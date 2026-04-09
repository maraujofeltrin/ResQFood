package ar.edu.itba.paw.models;

public enum PackSortOption {
    DATE_DESC("packs.id DESC"),
    TITLE_ASC("packs.title ASC, packs.id DESC"),
    PRICE_ASC("packs.final_price ASC, packs.id DESC"),
    PRICE_DESC("packs.final_price DESC, packs.id DESC");

    private final String orderByClause;

    PackSortOption(String orderByClause) {
        this.orderByClause = orderByClause;
    }

    public String getOrderByClause() {
        return orderByClause;
    }

    public static PackSortOption fromString(final String sortStr) {
        if (sortStr == null) {
            return DATE_DESC; // Default
        }
        try {
            return PackSortOption.valueOf(sortStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            return DATE_DESC;
        }
    }
}
