package ar.edu.itba.paw.webapp.form;

import ar.edu.itba.paw.models.auction.Auction;
import ar.edu.itba.paw.models.reservation.Reservation;

import java.util.Locale;
import java.util.Set;

/**
 * Binds and normalises the reservation list filter parameters from the request.
 * Parsing of enum values and tab normalisation are done eagerly in the setters.
 */
public class ReservationListFilterForm {

    private static final Set<String> ALLOWED_TABS = Set.of("items", "packs", "auctions");

    private String q;
    private Reservation.Status status;
    private Auction.Status auctionStatus;
    private String tab = "items";
    private int page = 1;

    // -- Getters ---------------------------------------------------------------

    /** Returns the trimmed query, or {@code null} if blank. */
    public String getQ() {
        return q;
    }

    public Reservation.Status getStatus() {
        return status;
    }

    public Auction.Status getAuctionStatus() {
        return auctionStatus;
    }

    public String getTab() {
        return tab;
    }

    public int getPage() {
        return page;
    }

    // -- Setters (with normalisation) ------------------------------------------

    public void setQ(final String q) {
        this.q = (q == null || q.isBlank()) ? null : q.trim();
    }

    public void setStatus(final String statusValue) {
        this.status = parseEnum(Reservation.Status.class, statusValue);
    }

    public void setAuctionStatus(final String statusValue) {
        this.auctionStatus = parseEnum(Auction.Status.class, statusValue);
    }

    public void setTab(final String tab) {
        if (tab == null) {
            this.tab = "items";
            return;
        }
        final String normalised = tab.trim().toLowerCase(Locale.ROOT);
        this.tab = ALLOWED_TABS.contains(normalised) ? normalised : "items";
    }

    public void setPage(final int page) {
        this.page = page;
    }

    // -- Helpers ----------------------------------------------------------------

    private static <E extends Enum<E>> E parseEnum(final Class<E> enumType, final String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Enum.valueOf(enumType, value.trim().toUpperCase(Locale.ROOT));
        } catch (final IllegalArgumentException ignored) {
            return null;
        }
    }
}
