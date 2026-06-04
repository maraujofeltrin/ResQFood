package ar.edu.itba.paw.webapp.controller.advice;

import ar.edu.itba.paw.services.notification.NotificationService;
import ar.edu.itba.paw.webapp.controller.helpers.AuthenticatedUserResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class NavNotificationsModelAdvice {

    private final AuthenticatedUserResolver authResolver;
    private final NotificationService notificationService;

    @Autowired
    public NavNotificationsModelAdvice(
            final AuthenticatedUserResolver authResolver,
            final NotificationService notificationService) {
        this.authResolver = authResolver;
        this.notificationService = notificationService;
    }

    @ModelAttribute("navUnreadNotificationCount")
    public int navUnreadNotificationCount() {
        return authResolver.resolveUserOrEmpty()
                .map(u -> notificationService.countUnread(u.getId()))
                .orElse(0);
    }
}
