package ar.edu.itba.paw.webapp.controller.helpers;

import ar.edu.itba.paw.models.auction.Bid;
import ar.edu.itba.paw.models.user.Client;
import org.springframework.context.MessageSource;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

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
     * Builds rows from pre-fetched client data to avoid N+1 queries.
     *
     * @param bids           the bids (ordered by amount descending)
     * @param clientsByUserId pre-fetched map of userId → Client
     * @param messageSource  for i18n labels
     * @param locale         the current locale
     */
    public static List<BidHistoryRow> buildRows(final List<Bid> bids, final Map<Long, Client> clientsByUserId,
            final MessageSource messageSource, final Locale locale) {
        final List<BidHistoryRow> rows = new ArrayList<>();
        for (int i = 0; i < bids.size(); i++) {
            final Bid bid = bids.get(i);
            final Client client = clientsByUserId.get(bid.getClientId());
            final String displayName = ViewFormatUtils.shortDisplayName(client, messageSource, locale);
            final String initials = ViewFormatUtils.initialsFor(client, messageSource, locale);
            final String amountDisplay = ViewFormatUtils.formatMoney(bid.getAmount());
            final String relative = ViewFormatUtils.formatRelativeTime(bid.getTimestamp(), messageSource, locale);
            rows.add(new BidHistoryRow(initials, displayName, relative, amountDisplay, i == 0, i));
        }
        return rows;
    }
}
