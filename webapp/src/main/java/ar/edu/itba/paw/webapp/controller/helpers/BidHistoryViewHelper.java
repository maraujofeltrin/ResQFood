package ar.edu.itba.paw.webapp.controller.helpers;

import ar.edu.itba.paw.models.auction.Bid;
import ar.edu.itba.paw.models.user.Client;
import org.springframework.context.MessageSource;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Builds pack-detail bid history rows (presentation strings for the JSP tag), colocated with the row data holder.
 */
public final class BidHistoryViewHelper {

    /**
     * One row in the bid history card: display-only data derived from {@link Bid} + {@link Client}.
     */
    public static final class BidHistoryRow {

        private final String initials;
        private final String displayName;
        private final String relativeTimeLabel;
        private final String amountDisplay;
        private final boolean leading;
        private final int index;

        private BidHistoryRow(final String initials, final String displayName, final String relativeTimeLabel,
                final String amountDisplay, final boolean leading, final int index) {
            this.initials = initials;
            this.displayName = displayName;
            this.relativeTimeLabel = relativeTimeLabel;
            this.amountDisplay = amountDisplay;
            this.leading = leading;
            this.index = index;
        }

        public String getInitials() {
            return initials;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getRelativeTimeLabel() {
            return relativeTimeLabel;
        }

        public String getAmountDisplay() {
            return amountDisplay;
        }

        public boolean isLeading() {
            return leading;
        }

        public int getIndex() {
            return index;
        }
    }

    private BidHistoryViewHelper() {
    }

    /**
     * Builds rows from bids whose {@link Client} is already hydrated by the persistence layer
     * ({@code JOIN FETCH b.client} in {@code BidDao.findByAuctionId}).
     *
     * @param bids          the bids (ordered by amount descending)
     * @param messageSource for i18n labels
     * @param locale        the current locale
     */
    public static List<BidHistoryRow> buildRows(final List<Bid> bids,
            final MessageSource messageSource, final Locale locale) {
        return buildRows(bids, messageSource, locale, 0);
    }

    /**
     * @param globalOffset 0-based index of the first item in this page within the full result set.
     *                     Used to determine which bid is globally leading (offset == 0 means first overall).
     */
    public static List<BidHistoryRow> buildRows(final List<Bid> bids,
            final MessageSource messageSource, final Locale locale, final int globalOffset) {
        final List<BidHistoryRow> rows = new ArrayList<>();
        for (int i = 0; i < bids.size(); i++) {
            final Bid bid = bids.get(i);
            final Client client = bid.getClient();
            final String displayName = ViewFormatUtils.shortDisplayName(client, messageSource, locale);
            final String initials = ViewFormatUtils.initialsFor(client, messageSource, locale);
            final String amountDisplay = ViewFormatUtils.formatMoney(bid.getAmount());
            final String relative = ViewFormatUtils.formatRelativeTime(bid.getTimestamp(), messageSource, locale);
            final int globalIndex = globalOffset + i;
            rows.add(new BidHistoryRow(initials, displayName, relative, amountDisplay, globalIndex == 0, globalIndex));
        }
        return rows;
    }
}
