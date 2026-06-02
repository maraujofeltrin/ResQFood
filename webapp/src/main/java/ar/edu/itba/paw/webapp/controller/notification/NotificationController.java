package ar.edu.itba.paw.webapp.controller.notification;

import ar.edu.itba.paw.models.user.User;
import ar.edu.itba.paw.services.notification.NotificationService;
import ar.edu.itba.paw.webapp.controller.helpers.AuthenticatedUserResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.server.ResponseStatusException;

@Controller
public class NotificationController {

    private final NotificationService notificationService;
    private final AuthenticatedUserResolver authResolver;

    @Autowired
    public NotificationController(final NotificationService notificationService,
            final AuthenticatedUserResolver authResolver) {
        this.notificationService = notificationService;
        this.authResolver = authResolver;
    }

    @GetMapping("/notifications/unread-count")
    @ResponseBody
    public int unreadCount(final Authentication authentication) {
        final User user = authResolver.resolveUser(authentication);
        return notificationService.countUnread(user.getId());
    }

    @PreAuthorize("@own.canWriteNotification(#id, authentication.principal.id)")
    @PostMapping("/notifications/{id}/read")
    public ResponseEntity<Void> markRead(@PathVariable("id") final long id) {
        if (notificationService.markRead(id).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("@own.canWriteNotification(#id, authentication.principal.id)")
    @PostMapping("/notifications/{id}/unread")
    public ResponseEntity<Void> markUnread(@PathVariable("id") final long id) {
        if (notificationService.markUnread(id).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("@own.canWriteNotification(#id, authentication.principal.id)")
    @PostMapping("/notifications/{id}/delete")
    public ResponseEntity<Void> softDelete(@PathVariable("id") final long id) {
        if (notificationService.softDelete(id).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/notifications/read-all")
    public ResponseEntity<Void> markAllRead(final Authentication authentication) {
        final User user = authResolver.resolveUser(authentication);
        notificationService.markAllRead(user.getId());
        return ResponseEntity.noContent().build();
    }
}
