package ar.edu.itba.paw.webapp.form;

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

    private static final Set<String> ALLOWED_TYPES = Set.of("packs", "auctions", "favorites");
    private static final Set<String> ALLOWED_TIME_RANGES = Set.of("morning", "afternoon", "evening");

    private String q;
    private List<String> tags;
    private String sort;
    private List<String> types;
    private String auctionSort;
    private String location;
    private List<String> timeRange;
    private int page = 1;

    // -- Getters ---------------------------------------------------------------

    public String getQ() {
        return q;
    }

    public List<String> getTags() {
        return tags;
    }

    public String getSort() {
        return sort;
    }

    public List<String> getTypes() {
        return types;
    }

    public String getAuctionSort() {
        return auctionSort;
    }

    public String getLocation() {
        return location;
    }

    public List<String> getTimeRange() {
        return timeRange;
    }

    public int getPage() {
        return page;
    }

    // -- Setters (with normalisation) ------------------------------------------

    public void setQ(final String q) {
        this.q = q;
    }

    public void setTags(final List<String> tags) {
        this.tags = tags;
    }

    public void setSort(final String sort) {
        this.sort = sort;
    }

    public void setTypes(final List<String> rawTypes) {
        this.types = normalizeList(rawTypes, ALLOWED_TYPES);
    }

    public void setAuctionSort(final String auctionSort) {
        this.auctionSort = auctionSort;
    }

    public void setLocation(final String location) {
        this.location = location;
    }

    public void setTimeRange(final List<String> rawTimeRange) {
        this.timeRange = normalizeList(rawTimeRange, ALLOWED_TIME_RANGES);
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
