package ar.edu.itba.paw.services.notification;

import ar.edu.itba.paw.models.notification.NotificationType;
import ar.edu.itba.paw.models.reservation.Reservation;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public interface NotificationService {

    void notifyReservationRequested(Reservation reservation, String commerceEmail,
            String acceptToken, String rejectToken, String pickupDateStr, Locale commerceLocale);

    void notifyReservationCodeIssued(Reservation reservation, String clientEmail,
            String pickupDateStr, Locale clientLocale);

    void notifyAuctionWinnerForClient(Reservation reservation, String clientEmail,
            String pickupDateStr, Locale clientLocale);

    void notifyAuctionWinnerForCommerce(Reservation reservation, String commerceEmail,
            String pickupDateStr, Locale commerceLocale);

    void notifyReservationRejected(Reservation reservation, String clientEmail, Locale clientLocale);

    void notifyAuctionOutbid(long previousBidderId, long auctionId, double newAmount);

    List<NotificationItemView> findRecentForUser(long userId, int limit);

    int countUnread(long userId);

    Optional<NotificationItemView> markRead(long notificationId);

    Optional<NotificationItemView> markUnread(long notificationId);

    int markAllRead(long userId);

    Optional<NotificationItemView> softDelete(long notificationId);

    List<ClientMailPreferenceView> getClientMailPreferences(long clientId);

    void updateClientMailPreferences(long clientId, Map<NotificationType, Boolean> preferencesByType);
}
