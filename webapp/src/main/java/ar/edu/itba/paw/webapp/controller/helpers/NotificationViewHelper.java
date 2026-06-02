package ar.edu.itba.paw.webapp.controller.helpers;

import ar.edu.itba.paw.models.notification.NotificationType;
import ar.edu.itba.paw.services.notification.NotificationItemView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Component
public class NotificationViewHelper {

    private static final String TITLE_KEY_PREFIX = "notification.type.";
    private static final String TITLE_KEY_SUFFIX = ".title";
    private static final String BODY_KEY_SUFFIX = ".body";
    private static final String FALLBACK_TITLE = "notification.type.unknown.title";
    private static final String FALLBACK_BODY = "notification.type.unknown.body";

    private final MessageSource messageSource;

    @Autowired
    public NotificationViewHelper(final MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    public List<NotificationSidebarItem> toSidebarItems(final List<NotificationItemView> notifications,
            final Locale locale) {
        if (notifications == null || notifications.isEmpty()) {
            return Collections.emptyList();
        }
        return notifications.stream()
                .map(item -> toSidebarItem(item, locale))
                .collect(Collectors.toList());
    }

    private NotificationSidebarItem toSidebarItem(final NotificationItemView item, final Locale locale) {
        final String title = resolveTitle(item.getType(), locale);
        final String body = resolveBody(item, locale);
        final String time = ViewFormatUtils.formatRelativeTime(item.getCreatedAt(), messageSource, locale);
        return new NotificationSidebarItem(item.getId(), item.isRead(), title, body, time);
    }

    private String resolveTitle(final NotificationType type, final Locale locale) {
        if (type == null) {
            return messageSource.getMessage(FALLBACK_TITLE, null, locale);
        }
        return messageSource.getMessage(TITLE_KEY_PREFIX + type.name() + TITLE_KEY_SUFFIX, null, locale);
    }

    private String resolveBody(final NotificationItemView item, final Locale locale) {
        final NotificationType type = item.getType();
        if (type == null) {
            return messageSource.getMessage(FALLBACK_BODY, null, locale);
        }
        final Object[] args = bodyArgsFor(type, item);
        return messageSource.getMessage(TITLE_KEY_PREFIX + type.name() + BODY_KEY_SUFFIX, args, locale);
    }

    private Object[] bodyArgsFor(final NotificationType type, final NotificationItemView item) {
        final String packTitle = orDash(item.getPackTitle());
        final String commerceName = orDash(item.getCommerceName());
        final String pickupCode = orDash(item.getPickupCode());
        final String amount = ViewFormatUtils.formatMoney(item.getAmount());

        return switch (type) {
            case RESERVATION_REQUESTED_COMMERCE -> new Object[] { packTitle, amount };
            case RESERVATION_CODE_CLIENT, AUCTION_WINNER_CLIENT ->
                    new Object[] { packTitle, commerceName, pickupCode };
            case AUCTION_WINNER_COMMERCE -> new Object[] { packTitle, pickupCode };
            case RESERVATION_REJECTED_CLIENT -> new Object[] { packTitle, commerceName };
            case AUCTION_OUTBID_CLIENT -> new Object[] { packTitle, amount };
        };
    }

    private static String orDash(final String value) {
        if (value == null || value.isBlank()) {
            return "—";
        }
        return value;
    }
}
