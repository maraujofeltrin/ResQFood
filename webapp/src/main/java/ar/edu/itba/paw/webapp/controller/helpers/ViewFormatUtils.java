package ar.edu.itba.paw.webapp.controller.helpers;

import ar.edu.itba.paw.models.user.Client;
import org.springframework.context.MessageSource;

import java.text.NumberFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Locale;

/**
 * Stateless presentation-formatting utilities shared by view helpers (bid history, reservation history, etc.).
 * Not instantiable.
 */
public final class ViewFormatUtils {

    private static final Locale LOCALE_AR = new Locale("es", "AR");

    private ViewFormatUtils() {
    }

    public static String formatMoney(final Double amount) {
        if (amount == null) {
            return "—";
        }
        return NumberFormat.getCurrencyInstance(LOCALE_AR).format(amount);
    }

    public static String initialsFor(final Client client, final MessageSource messageSource, final Locale locale) {
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

    public static String shortDisplayName(final Client client, final MessageSource messageSource, final Locale locale) {
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

    public static String formatRelativeTime(final LocalDateTime timeUtc, final MessageSource messageSource,
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
