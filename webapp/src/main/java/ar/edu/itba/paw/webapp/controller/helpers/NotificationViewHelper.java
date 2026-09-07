package ar.edu.itba.paw.webapp.controller.helpers;

import ar.edu.itba.paw.models.notification.Notification;
import ar.edu.itba.paw.models.notification.NotificationType;
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

    /**
     * Builds rows from notifications whose {@code reservation.customer} is already hydrated
     * by the persistence layer ({@code JOIN FETCH r.customer} in {@code NotificationDao}).
     */
    public static List<NotificationDisplayRow> buildRows(final List<Notification> notifications,
            final MessageSource messageSource, final Locale locale) {
        return notifications.stream()
                .map(notification -> toRow(notification, messageSource, locale))
                .collect(Collectors.toList());
    }

    private static NotificationDisplayRow toRow(final Notification notification,
            final MessageSource messageSource, final Locale locale) {
        final String title = messageSource.getMessage(
                "notification.type." + notification.getType().name() + ".title",
                null, notification.getType().name(), locale);
        final String body = resolveBody(notification, messageSource, locale);
        final String time = relativeTime(notification.getCreatedAt(), messageSource, locale);
        final String targetUrl = resolveTargetUrl(notification);
        return new NotificationDisplayRow(notification.getId(), notification.getType(), title, body, time,
                isRead(notification), targetUrl);
    }

    private static String resolveTargetUrl(final Notification notification) {
        if (notification == null || notification.getType() == null) {
            return null;
        }
        return switch (notification.getType()) {
            case RESERVATION_REQUESTED_COMMERCE,
                 RESERVATION_CODE_CLIENT,
                 AUCTION_WINNER_CLIENT,
                 AUCTION_WINNER_COMMERCE,
                 RESERVATION_REJECTED_CLIENT -> "/reservations";
            case AUCTION_OUTBID_CLIENT,
                 FAVORITE_PACK_RESTOCKED,
                 AUCTION_LOST_CLIENT -> {
                if (notification.getPack() != null) {
                    yield "/packs/" + notification.getPack().getId();
                }
                yield null;
            }
            case FAVORITE_COMMERCE_NEW_PACK -> {
                if (notification.getPack() != null && notification.getPack().getCommerce() != null) {
                    yield "/commerces/" + notification.getPack().getCommerce().getUserId();
                }
                yield null;
            }
        };
    }

    private static String resolveBody(final Notification notification,
            final MessageSource messageSource, final Locale locale) {
        final String code = "notification.type." + notification.getType().name() + ".body";
        final Object[] args = bodyArgs(notification);
        return messageSource.getMessage(code, args, "", locale);
    }

    private static Object[] bodyArgs(final Notification notification) {
        return switch (notification.getType()) {
            case RESERVATION_REQUESTED_COMMERCE -> new Object[]{safe(customerName(notification)), safe(notification.getPackTitle())};
            case RESERVATION_CODE_CLIENT -> new Object[]{safe(notification.getCommerceName()), safe(notification.getPickupCode())};
            case AUCTION_WINNER_CLIENT -> new Object[]{safe(notification.getPackTitle()), safe(notification.getCommerceName()), formatAmount(notification.getAmount())};
            case AUCTION_WINNER_COMMERCE -> new Object[]{safe(notification.getPackTitle())};
            case RESERVATION_REJECTED_CLIENT -> new Object[]{safe(notification.getCommerceName()), safe(notification.getPackTitle())};
            case AUCTION_OUTBID_CLIENT -> new Object[]{formatAmount(notification.getAmount()), safe(notification.getPackTitle()), safe(notification.getCommerceName())};
            case FAVORITE_PACK_RESTOCKED -> new Object[]{safe(notification.getCommerceName()), safe(notification.getPackTitle())};
            case FAVORITE_COMMERCE_NEW_PACK -> new Object[]{safe(notification.getCommerceName()), safe(notification.getPackTitle())};
            case AUCTION_LOST_CLIENT -> new Object[]{safe(notification.getPackTitle()), safe(notification.getCommerceName())};
        };
    }

    private static boolean isRead(final Notification notification) {
        return notification.getReadAt() != null;
    }

    private static String customerName(final Notification notification) {
        if (notification.getReservation() == null || notification.getReservation().getCustomer() == null) {
            return null;
        }
        return notification.getReservation().getCustomer().getFullName();
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
        private final String targetUrl;

        private NotificationDisplayRow(final long id, final NotificationType type,
                final String title, final String body, final String relativeTime, final boolean read,
                final String targetUrl) {
            this.id = id;
            this.type = type;
            this.title = title;
            this.body = body;
            this.relativeTime = relativeTime;
            this.read = read;
            this.targetUrl = targetUrl;
        }

        public long getId() { return id; }
        public NotificationType getType() { return type; }
        public String getTitle() { return title; }
        public String getBody() { return body; }
        public String getRelativeTime() { return relativeTime; }
        public boolean isRead() { return read; }
        public String getTargetUrl() { return targetUrl; }
    }
}
