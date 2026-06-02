package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.models.notification.Notification;
import ar.edu.itba.paw.models.notification.NotificationType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface NotificationDao {

    Notification create(Long recipientId, NotificationType type, Long reservationId, Long auctionId, Long packId,
            String packTitle, String commerceName, Double amount, String pickupCode, LocalDateTime pickupDate,
            LocalDateTime createdAt);

    Optional<Notification> findById(Long id);

    List<Notification> findRecentByRecipient(Long userId, int limit);

    int countUnread(Long userId);

    Optional<Notification> markRead(Long id, LocalDateTime readAt);

    Optional<Notification> markUnread(Long id);

    int markAllRead(Long userId, LocalDateTime readAt);

    Optional<Notification> softDelete(Long id, LocalDateTime deletedAt);

    boolean belongsToRecipient(Long notificationId, Long userId);
}
