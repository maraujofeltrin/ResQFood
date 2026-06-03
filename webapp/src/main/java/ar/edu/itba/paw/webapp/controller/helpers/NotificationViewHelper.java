package ar.edu.itba.paw.webapp.controller.helpers;

import ar.edu.itba.paw.models.notification.NotificationType;
import ar.edu.itba.paw.services.notification.NotificationItemView;
import org.springframework.context.MessageSource;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * Builds notification presentation rows from pre-fetched data, resolving
 * i18n titles, body text, and relative timestamps server-side.
 * Not instantiable — mirrors {@link CommerceReviewViewHelper}.
 */
public final class NotificationViewHelper {

    private NotificationViewHelper() {}

    public static List<NotificationDisplayRow> buildRows(final List<NotificationItemView> items,
            final MessageSource messageSource, final Locale locale) {
        return items.stream()
                .map(item -> toRow(item, messageSource, locale))
                .collect(Collectors.toList());
    }

    private static NotificationDisplayRow toRow(final NotificationItemView item,
            final MessageSource messageSource, final Locale locale) {
        final String title = messageSource.getMessage(
                "notification.type." + item.getType().name() + ".title", null, item.getType().name(), locale);
        final String body = resolveBody(item, messageSource, locale);
        final String time = relativeTime(item.getCreatedAt(), messageSource, locale);
        return new NotificationDisplayRow(item.getId(), item.getType(), title, body, time, item.isRead());
    }

    private static String resolveBody(final NotificationItemView item,
            final MessageSource messageSource, final Locale locale) {
        final String code = "notification.type." + item.getType().name() + ".body";
        final Object[] args = bodyArgs(item);
        return messageSource.getMessage(code, args, "", locale);
    }

    private static Object[] bodyArgs(final NotificationItemView item) {
        return switch (item.getType()) {
            case RESERVATION_REQUESTED_COMMERCE -> new Object[]{safe(item.getPackTitle())};
            case RESERVATION_CODE_CLIENT -> new Object[]{safe(item.getCommerceName()), safe(item.getPickupCode())};
            case AUCTION_WINNER_CLIENT -> new Object[]{safe(item.getPackTitle()), safe(item.getCommerceName()), formatAmount(item.getAmount())};
            case AUCTION_WINNER_COMMERCE -> new Object[]{safe(item.getPackTitle())};
            case RESERVATION_REJECTED_CLIENT -> new Object[]{safe(item.getCommerceName()), safe(item.getPackTitle())};
            case AUCTION_OUTBID_CLIENT -> new Object[]{formatAmount(item.getAmount()), safe(item.getPackTitle()), safe(item.getCommerceName())};
        };
    }

    private static String relativeTime(final LocalDateTime createdAt,
            final MessageSource messageSource, final Locale locale) {
        if (createdAt == null) {
            return "";
        }
        final long minutes = Duration.between(createdAt, LocalDateTime.now(ZoneOffset.UTC)).toMinutes();
        if (minutes < 1) {
            return messageSource.getMessage("notification.time.justNow", null, locale);
        }
        if (minutes < 60) {
            return messageSource.getMessage("notification.time.minutesAgo", new Object[]{minutes}, locale);
        }
        final long hours = minutes / 60;
        if (hours < 24) {
            return messageSource.getMessage("notification.time.hoursAgo", new Object[]{hours}, locale);
        }
        final long days = hours / 24;
        return messageSource.getMessage("notification.time.daysAgo", new Object[]{days}, locale);
    }

    private static String safe(final String value) {
        return value != null ? value : "";
    }

    private static String formatAmount(final Double amount) {
        return amount != null ? "$" + String.format("%.2f", amount) : "";
    }

    public static final class NotificationDisplayRow {
        private final long id;
        private final NotificationType type;
        private final String title;
        private final String body;
        private final String relativeTime;
        private final boolean read;

        private NotificationDisplayRow(final long id, final NotificationType type,
                final String title, final String body, final String relativeTime, final boolean read) {
            this.id = id;
            this.type = type;
            this.title = title;
            this.body = body;
            this.relativeTime = relativeTime;
            this.read = read;
        }

        public long getId() { return id; }
        public NotificationType getType() { return type; }
        public String getTitle() { return title; }
        public String getBody() { return body; }
        public String getRelativeTime() { return relativeTime; }
        public boolean isRead() { return read; }
    }
}
