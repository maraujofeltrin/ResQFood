package ar.edu.itba.paw.webapp.controller.utils;

import ar.edu.itba.paw.models.reservation.Reservation;
import ar.edu.itba.paw.models.user.Client;
import ar.edu.itba.paw.services.user.ClientService;
import org.springframework.context.MessageSource;

import java.text.NumberFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Builds pack-detail reservation history rows (presentation strings for the JSP tag), colocated with the row data holder.
 */
public final class ReservationHistoryViewHelper {

    private static final Locale LOCALE_AR = new Locale("es", "AR");

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

    public static List<ReservationHistoryRow> buildRows(final List<Reservation> reservations, final ClientService clientService,
            final MessageSource messageSource, final Locale locale) {
        final List<ReservationHistoryRow> rows = new ArrayList<>();
        for (int i = 0; i < reservations.size(); i++) {
            final Reservation reservation = reservations.get(i);
            final Optional<Client> clientOpt = clientService.findByUserId(reservation.getCustomerId());
            final String displayName = shortDisplayName(clientOpt.orElse(null), messageSource, locale);
            final String initials = initialsFor(clientOpt.orElse(null), messageSource, locale);
            final String amountDisplay = formatMoney(reservation.getFinalPrice());
            final String relative = formatRelativeTime(reservation.getReservationDate(), messageSource, locale);
            final String statusStr = reservation.getStatus() != null ? reservation.getStatus().name() : "";
            rows.add(new ReservationHistoryRow(initials, displayName, relative, amountDisplay, statusStr, i));
        }
        return rows;
    }

    private static String formatMoney(final Double amount) {
        if (amount == null) {
            return "—";
        }
        return NumberFormat.getCurrencyInstance(LOCALE_AR).format(amount);
    }

    private static String initialsFor(final Client client, final MessageSource messageSource, final Locale locale) {
        if (client == null) {
            return messageSource.getMessage("pack.detail.bidHistory.initialsUnknown", null, locale);
        }
        final String first = client.getName() == null ? "" : client.getName().trim();
        final String last = client.getLastName() == null ? "" : client.getLastName().trim();
        if (first.isEmpty() && last.isEmpty()) {
            return "?";
        }
        final String a = !first.isEmpty() ? first.substring(0, 1).toUpperCase(Locale.ROOT) : "";
        final String b = !last.isEmpty() ? last.substring(0, 1).toUpperCase(Locale.ROOT) : "";
        if (!a.isEmpty() && !b.isEmpty()) {
            return a + b;
        }
        return !a.isEmpty() ? a : b;
    }

    private static String shortDisplayName(final Client client, final MessageSource messageSource, final Locale locale) {
        if (client == null) {
            return messageSource.getMessage("pack.detail.bidHistory.anonymous", null, locale);
        }
        final String first = client.getName() == null ? "" : client.getName().trim();
        final String last = client.getLastName() == null ? "" : client.getLastName().trim();
        if (first.isEmpty() && last.isEmpty()) {
            return messageSource.getMessage("pack.detail.bidHistory.anonymous", null, locale);
        }
        if (last.isEmpty()) {
            return first;
        }
        if (first.isEmpty()) {
            return last.length() > 1 ? last.substring(0, 1) + "." : last;
        }
        return first + " " + last.charAt(0) + ".";
    }

    private static String formatRelativeTime(final LocalDateTime timeUtc, final MessageSource messageSource,
            final Locale locale) {
        if (timeUtc == null) {
            return messageSource.getMessage("pack.detail.bidHistory.relativeUnknown", null, locale);
        }
        final LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        Duration d = Duration.between(timeUtc, now);
        if (d.isNegative()) {
            d = Duration.ZERO;
        }
        final long minutes = d.toMinutes();
        if (minutes < 1) {
            return messageSource.getMessage("pack.detail.bidHistory.relativeNow", null, locale);
        }
        if (minutes < 60) {
            if (minutes == 1) {
                return messageSource.getMessage("pack.detail.bidHistory.relativeOneMinute", null, locale);
            }
            return messageSource.getMessage("pack.detail.bidHistory.relativeMinutes", new Object[] { minutes }, locale);
        }
        final long hours = d.toHours();
        if (hours < 24) {
            if (hours == 1) {
                return messageSource.getMessage("pack.detail.bidHistory.relativeOneHour", null, locale);
            }
            return messageSource.getMessage("pack.detail.bidHistory.relativeHours", new Object[] { hours }, locale);
        }
        final long days = d.toDays();
        if (days == 1) {
            return messageSource.getMessage("pack.detail.bidHistory.relativeOneDay", null, locale);
        }
        return messageSource.getMessage("pack.detail.bidHistory.relativeDays", new Object[] { days }, locale);
    }
}
