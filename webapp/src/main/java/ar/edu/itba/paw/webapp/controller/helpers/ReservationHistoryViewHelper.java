package ar.edu.itba.paw.webapp.controller.helpers;

import ar.edu.itba.paw.models.reservation.Reservation;
import ar.edu.itba.paw.models.user.Client;
import org.springframework.context.MessageSource;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Builds pack-detail reservation history rows (presentation strings for the JSP tag), colocated with the row data holder.
 */
public final class ReservationHistoryViewHelper {

    public static final class ReservationHistoryRow {

        private final String initials;
        private final String displayName;
        private final String relativeTimeLabel;
        private final String amountDisplay;
        private final String status;
        private final int index;

        private ReservationHistoryRow(final String initials, final String displayName, final String relativeTimeLabel,
                final String amountDisplay, final String status, final int index) {
            this.initials = initials;
            this.displayName = displayName;
            this.relativeTimeLabel = relativeTimeLabel;
            this.amountDisplay = amountDisplay;
            this.status = status;
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

        public String getStatus() {
            return status;
        }

        public int getIndex() {
            return index;
        }
    }

    private ReservationHistoryViewHelper() {
    }

    /**
     * Builds rows from pre-fetched client data to avoid N+1 queries.
     *
     * @param reservations     the reservations to display
     * @param clientsByUserId  pre-fetched map of userId → Client
     * @param messageSource    for i18n labels
     * @param locale           the current locale
     */
    public static List<ReservationHistoryRow> buildRows(final List<Reservation> reservations,
            final Map<Long, Client> clientsByUserId,
            final MessageSource messageSource, final Locale locale) {
        final List<ReservationHistoryRow> rows = new ArrayList<>();
        for (int i = 0; i < reservations.size(); i++) {
            final Reservation reservation = reservations.get(i);
            final Client client = clientsByUserId.get(reservation.getCustomer().getUserId());
            final String displayName = ViewFormatUtils.shortDisplayName(client, messageSource, locale);
            final String initials = ViewFormatUtils.initialsFor(client, messageSource, locale);
            final String amountDisplay = ViewFormatUtils.formatMoney(reservation.getFinalPrice());
            final String relative = ViewFormatUtils.formatRelativeTime(reservation.getReservationDate(), messageSource, locale);
            final String statusStr = reservation.getStatus() != null ? reservation.getStatus().name() : "";
            rows.add(new ReservationHistoryRow(initials, displayName, relative, amountDisplay, statusStr, i));
        }
        return rows;
    }
}
