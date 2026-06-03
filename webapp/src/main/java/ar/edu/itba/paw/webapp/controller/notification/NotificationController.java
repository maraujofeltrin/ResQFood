package ar.edu.itba.paw.webapp.controller.notification;

import ar.edu.itba.paw.models.user.User;
import ar.edu.itba.paw.services.notification.NotificationItemView;
import ar.edu.itba.paw.services.notification.NotificationService;
import ar.edu.itba.paw.webapp.controller.helpers.AuthenticatedUserResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@Controller
@RequestMapping("/notifications")
public class NotificationController {

    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationController.class);
    private static final int RECENT_LIMIT = 20;
    private static final String JSON_CONTENT_TYPE = "application/json;charset=UTF-8";

    private final AuthenticatedUserResolver authenticatedUserResolver;
    private final NotificationService notificationService;

    @Autowired
    public NotificationController(
            final AuthenticatedUserResolver authenticatedUserResolver,
            final NotificationService notificationService) {
        this.authenticatedUserResolver = authenticatedUserResolver;
        this.notificationService = notificationService;
    }

    @GetMapping
    public void getRecent(final HttpServletResponse response) throws IOException {
        final User user = authenticatedUserResolver.resolveUser();
        final List<NotificationItemView> items = notificationService.findRecentForUser(user.getId(), RECENT_LIMIT);
        writeJson(response, toJsonArray(items));
    }

    @GetMapping("/unread-count")
    public void getUnreadCount(final HttpServletResponse response) throws IOException {
        final User user = authenticatedUserResolver.resolveUser();
        final int count = notificationService.countUnread(user.getId());
        writeJson(response, "{\"count\":" + count + "}");
    }

    @PostMapping("/{id}/read")
    @PreAuthorize("@own.canWriteNotification(#id, authentication.principal.id)")
    public void markRead(@PathVariable("id") final long id, final HttpServletResponse response) throws IOException {
        final var result = notificationService.markRead(id);
        if (result.isPresent()) {
            writeJson(response, toJson(result.get()));
        } else {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    @PostMapping("/{id}/unread")
    @PreAuthorize("@own.canWriteNotification(#id, authentication.principal.id)")
    public void markUnread(@PathVariable("id") final long id, final HttpServletResponse response) throws IOException {
        final var result = notificationService.markUnread(id);
        if (result.isPresent()) {
            writeJson(response, toJson(result.get()));
        } else {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    @PostMapping("/read-all")
    public void markAllRead(final HttpServletResponse response) throws IOException {
        final User user = authenticatedUserResolver.resolveUser();
        final int updated = notificationService.markAllRead(user.getId());
        writeJson(response, "{\"updated\":" + updated + "}");
    }

    @PostMapping("/{id}/delete")
    @PreAuthorize("@own.canWriteNotification(#id, authentication.principal.id)")
    public void softDelete(@PathVariable("id") final long id, final HttpServletResponse response) {
        final var result = notificationService.softDelete(id);
        response.setStatus(result.isPresent() ? HttpServletResponse.SC_NO_CONTENT : HttpServletResponse.SC_NOT_FOUND);
    }

    private static void writeJson(final HttpServletResponse response, final String json) throws IOException {
        response.setContentType(JSON_CONTENT_TYPE);
        response.getWriter().write(json);
    }

    private static String toJsonArray(final List<NotificationItemView> items) {
        final StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < items.size(); i++) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append(toJson(items.get(i)));
        }
        return sb.append(']').toString();
    }

    private static String toJson(final NotificationItemView v) {
        final StringBuilder sb = new StringBuilder("{");
        appendField(sb, "id", v.getId(), true);
        appendField(sb, "type", v.getType().name(), false);
        appendField(sb, "packTitle", v.getPackTitle(), false);
        appendField(sb, "commerceName", v.getCommerceName(), false);
        appendNullableDouble(sb, "amount", v.getAmount());
        appendField(sb, "pickupCode", v.getPickupCode(), false);
        appendField(sb, "pickupDate", v.getPickupDate() != null ? v.getPickupDate().toString() : null, false);
        appendField(sb, "createdAt", v.getCreatedAt() != null ? v.getCreatedAt().toString() : null, false);
        appendField(sb, "read", v.isRead(), false);
        appendNullableLong(sb, "reservationId", v.getReservationId());
        appendNullableLong(sb, "auctionId", v.getAuctionId());
        appendNullableLong(sb, "packId", v.getPackId());
        return sb.append('}').toString();
    }

    private static void appendField(final StringBuilder sb, final String key, final long value, final boolean first) {
        if (!first) sb.append(',');
        sb.append('"').append(key).append("\":").append(value);
    }

    private static void appendField(final StringBuilder sb, final String key, final boolean value, final boolean first) {
        if (!first) sb.append(',');
        sb.append('"').append(key).append("\":").append(value);
    }

    private static void appendField(final StringBuilder sb, final String key, final String value, final boolean first) {
        if (!first) sb.append(',');
        sb.append('"').append(key).append("\":");
        if (value == null) {
            sb.append("null");
        } else {
            sb.append('"');
            escapeJsonString(sb, value);
            sb.append('"');
        }
    }

    private static void appendNullableLong(final StringBuilder sb, final String key, final Long value) {
        sb.append(",\"").append(key).append("\":");
        sb.append(value != null ? value.toString() : "null");
    }

    private static void appendNullableDouble(final StringBuilder sb, final String key, final Double value) {
        sb.append(",\"").append(key).append("\":");
        sb.append(value != null ? value.toString() : "null");
    }

    private static void escapeJsonString(final StringBuilder sb, final String value) {
        for (int i = 0; i < value.length(); i++) {
            final char c = value.charAt(i);
            switch (c) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> {
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
                }
            }
        }
    }
}
