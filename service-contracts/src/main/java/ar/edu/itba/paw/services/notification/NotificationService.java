package ar.edu.itba.paw.services.notification;

import ar.edu.itba.paw.models.auction.Auction;
import ar.edu.itba.paw.models.notification.Notification;
import ar.edu.itba.paw.models.notification.NotificationType;
import ar.edu.itba.paw.models.reservation.Reservation;
import ar.edu.itba.paw.models.pack.Pack;
import ar.edu.itba.paw.models.user.User;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

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

    void notifyAuctionOutbid(User previousBidder, Auction auction, double newAmount);

    void notifyPackRestocked(Pack pack, List<User> favoritingClients);

    void notifyPackPublished(Pack pack, List<User> favoritingClients);

    void notifyAuctionFinished(Auction auction, List<User> bidders);

    List<Notification> findRecentForUser(long userId, int limit);

    List<Notification> findPageForUser(long userId, int page, int pageSize);

    int countForUser(long userId);

    int countUnread(long userId);

    void markRead(long notificationId);

    void markUnread(long notificationId);

    int markAllRead(long userId);

    void softDelete(long notificationId);

    Map<NotificationType, Boolean> getClientMailPreferences(long clientId);

    void updateClientMailPreferences(long clientId, Map<NotificationType, Boolean> preferencesByType);

    Set<NotificationType> getClientConfigurableMailTypes();
}
