package ar.edu.itba.paw.webapp.controller.notification;

import ar.edu.itba.paw.services.notification.NotificationItemView;
import ar.edu.itba.paw.services.notification.NotificationService;
import ar.edu.itba.paw.webapp.auth.AuthUser;
import ar.edu.itba.paw.webapp.controller.helpers.NotificationViewHelper;
import ar.edu.itba.paw.webapp.controller.helpers.NotificationViewHelper.NotificationDisplayRow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.Locale;

@Controller
@RequestMapping("/notifications")
public class NotificationController {

    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationController.class);
    private static final int PAGE_SIZE = 10;
    private static final String REDIRECT_NOTIFICATIONS = "redirect:/notifications";

    private final NotificationService notificationService;
    private final MessageSource messageSource;

    @Autowired
    public NotificationController(
            final NotificationService notificationService,
            final MessageSource messageSource) {
        this.notificationService = notificationService;
        this.messageSource = messageSource;
    }

    @GetMapping
    public ModelAndView list(@AuthenticationPrincipal final AuthUser principal,
            @RequestParam(defaultValue = "1") final int page) {
        final long userId = principal.getId();
        final int total = notificationService.countForUser(userId);
        final int totalPages = Math.max(1, (int) Math.ceil((double) total / PAGE_SIZE));
        final int safePage = Math.max(1, Math.min(page, totalPages));
        final List<NotificationItemView> items =
                notificationService.findPageForUser(userId, safePage, PAGE_SIZE);
        final Locale locale = LocaleContextHolder.getLocale();
        final List<NotificationDisplayRow> rows =
                NotificationViewHelper.buildRows(items, messageSource, locale);
        final int unreadCount = notificationService.countUnread(userId);

        LOGGER.debug("Loading notifications page for userId={} page={} items={} unread={}",
                Long.valueOf(userId), safePage, rows.size(), unreadCount);

        final ModelAndView mav = new ModelAndView("notifications/notificationsView");
        mav.addObject("notificationRows", rows);
        mav.addObject("unreadCount", unreadCount);
        mav.addObject("currentPage", Integer.valueOf(safePage));
        mav.addObject("totalPages", Integer.valueOf(totalPages));
        return mav;
    }

    @PostMapping("/{id}/read")
    @PreAuthorize("@own.canWriteNotification(#id, authentication.principal.id)")
    public String markRead(@PathVariable("id") final long id) {
        notificationService.markRead(id);
        return REDIRECT_NOTIFICATIONS;
    }

    @PostMapping("/{id}/unread")
    @PreAuthorize("@own.canWriteNotification(#id, authentication.principal.id)")
    public String markUnread(@PathVariable("id") final long id) {
        notificationService.markUnread(id);
        return REDIRECT_NOTIFICATIONS;
    }

    @PostMapping("/read-all")
    public String markAllRead(@AuthenticationPrincipal final AuthUser principal) {
        notificationService.markAllRead(principal.getId());
        return REDIRECT_NOTIFICATIONS;
    }

    @PostMapping("/{id}/delete")
    @PreAuthorize("@own.canWriteNotification(#id, authentication.principal.id)")
    public String softDelete(@PathVariable("id") final long id) {
        notificationService.softDelete(id);
        return REDIRECT_NOTIFICATIONS;
    }
}
