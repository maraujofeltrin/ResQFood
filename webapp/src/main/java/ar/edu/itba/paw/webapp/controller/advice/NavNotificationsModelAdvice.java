package ar.edu.itba.paw.webapp.controller.advice;

import ar.edu.itba.paw.services.notification.NotificationService;
import ar.edu.itba.paw.webapp.controller.helpers.AuthenticatedUserResolver;
import ar.edu.itba.paw.webapp.controller.helpers.NotificationSidebarItem;
import ar.edu.itba.paw.webapp.controller.helpers.NotificationViewHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

@ControllerAdvice
public class NavNotificationsModelAdvice {

    private static final int RECENT_NOTIFICATION_LIMIT = 20;

    private final AuthenticatedUserResolver authResolver;
    private final NotificationService notificationService;
    private final NotificationViewHelper notificationViewHelper;

    @Autowired
    public NavNotificationsModelAdvice(final AuthenticatedUserResolver authResolver,
            final NotificationService notificationService,
            final NotificationViewHelper notificationViewHelper) {
        this.authResolver = authResolver;
        this.notificationService = notificationService;
        this.notificationViewHelper = notificationViewHelper;
    }

    @ModelAttribute("navUnreadNotificationCount")
    public int navUnreadNotificationCount() {
        return authResolver.resolveUserOrEmpty()
                .map(user -> notificationService.countUnread(user.getId()))
                .orElse(0);
    }

    @ModelAttribute("recentNotifications")
    public List<NotificationSidebarItem> recentNotifications(final Locale locale) {
        return authResolver.resolveUserOrEmpty()
                .map(user -> notificationViewHelper.toSidebarItems(
                        notificationService.findRecentForUser(user.getId(), RECENT_NOTIFICATION_LIMIT),
                        locale))
                .orElse(Collections.emptyList());
    }
}
