package ar.edu.itba.paw.webapp.form;

import ar.edu.itba.paw.models.auction.AuctionSortOption;
import ar.edu.itba.paw.models.pack.Municipality;
import ar.edu.itba.paw.models.pack.PackSortOption;
import ar.edu.itba.paw.models.pack.PackTag;
import ar.edu.itba.paw.models.user.Commerce;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Binds and normalises the pack catalog filter parameters from the request.
 * Normalisation (whitelist validation) is performed eagerly in the setters
 * so that controllers and model builders receive clean data.
 */
public class CatalogFilterForm {

    private static final Set<String> ALLOWED_TYPES = Set.of("packs", "auctions", "favorites", "commerces");
    private static final Set<String> ALLOWED_TIME_RANGES = Set.of("morning", "afternoon", "evening");

    private String q;
    private List<PackTag> tags;
    private PackSortOption sort;
    private List<String> types;
    private AuctionSortOption auctionSort;
    private Municipality location;
    private List<String> timeRange;
    private Commerce.Category commerceCategory;
    private int page = 1;

    // -- Getters ---------------------------------------------------------------

    public String getQ() {
        return q;
    }

    public List<PackTag> getTags() {
        return tags;
    }

    public PackSortOption getSort() {
        return sort;
    }

    public List<String> getTypes() {
        return types;
    }

    public AuctionSortOption getAuctionSort() {
        return auctionSort;
    }

    public Municipality getLocation() {
        return location;
    }

    public List<String> getTimeRange() {
        return timeRange;
    }

    public Commerce.Category getCommerceCategory() {
        return commerceCategory;
    }

    public int getPage() {
        return page;
    }

    // -- Setters (with normalisation) ------------------------------------------

    public void setQ(final String q) {
        this.q = q;
    }

    public void setTags(final List<PackTag> tags) {
        this.tags = tags;
    }

    public void setSort(final PackSortOption sort) {
        this.sort = sort;
    }

    public void setTypes(final List<String> rawTypes) {
        this.types = normalizeList(rawTypes, ALLOWED_TYPES);
    }

    public void setAuctionSort(final AuctionSortOption auctionSort) {
        this.auctionSort = auctionSort;
    }

    public void setLocation(final Municipality location) {
        this.location = location;
    }

    public void setTimeRange(final List<String> rawTimeRange) {
        this.timeRange = normalizeList(rawTimeRange, ALLOWED_TIME_RANGES);
    }

    public void setCommerceCategory(final String rawCommerceCategory) {
        if (rawCommerceCategory == null || rawCommerceCategory.isBlank()) {
            this.commerceCategory = null;
            return;
        }
        try {
            this.commerceCategory = Commerce.Category.valueOf(rawCommerceCategory.trim().toUpperCase(Locale.ROOT));
        } catch (final IllegalArgumentException ignored) {
            this.commerceCategory = null;
        }
    }

    public void setPage(final int page) {
        this.page = page;
    }

    // -- Normalisation ---------------------------------------------------------

    private static List<String> normalizeList(final List<String> raw, final Set<String> allowed) {
        if (raw == null || raw.isEmpty()) {
            return Collections.emptyList();
        }
        final Set<String> values = new LinkedHashSet<>();
        for (final String item : raw) {
            if (item == null) {
                continue;
            }
            final String normalised = item.trim().toLowerCase(Locale.ROOT);
            if (allowed.contains(normalised)) {
                values.add(normalised);
            }
        }
        return new ArrayList<>(values);
    }
}
