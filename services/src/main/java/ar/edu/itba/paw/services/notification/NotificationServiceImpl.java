package ar.edu.itba.paw.services.notification;

import ar.edu.itba.paw.models.auction.Auction;
import ar.edu.itba.paw.models.auction.Bid;
import ar.edu.itba.paw.models.notification.ClientNotificationPreference;
import ar.edu.itba.paw.models.notification.Notification;
import ar.edu.itba.paw.models.notification.NotificationType;
import ar.edu.itba.paw.models.pack.Pack;
import ar.edu.itba.paw.models.reservation.Reservation;
import ar.edu.itba.paw.models.user.User;
import ar.edu.itba.paw.persistence.AuctionDao;
import ar.edu.itba.paw.persistence.BidDao;
import ar.edu.itba.paw.persistence.ClientNotificationPreferenceDao;
import ar.edu.itba.paw.persistence.CommerceFavoriteDao;
import ar.edu.itba.paw.persistence.NotificationDao;
import ar.edu.itba.paw.persistence.PackFavoriteDao;
import ar.edu.itba.paw.services.auction.AuctionMailService;
import ar.edu.itba.paw.services.pack.FavoriteMailService;
import ar.edu.itba.paw.services.reservation.ReservationMailService;
import ar.edu.itba.paw.services.user.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class NotificationServiceImpl implements NotificationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationServiceImpl.class);

    private static final List<NotificationType> CLIENT_MAIL_TYPES = Arrays.asList(
            NotificationType.RESERVATION_CODE_CLIENT,
            NotificationType.AUCTION_WINNER_CLIENT,
            NotificationType.RESERVATION_REJECTED_CLIENT,
            NotificationType.AUCTION_OUTBID_CLIENT,
            NotificationType.FAVORITE_PACK_RESTOCKED,
            NotificationType.FAVORITE_COMMERCE_NEW_PACK,
            NotificationType.AUCTION_LOST_CLIENT);

    private final NotificationDao notificationDao;
    private final ClientNotificationPreferenceDao clientNotificationPreferenceDao;
    private final ReservationMailService reservationMailService;
    private final AuctionMailService auctionMailService;
    private final FavoriteMailService favoriteMailService;
    private final AuctionDao auctionDao;
    private final UserService userService;
    private final PackFavoriteDao packFavoriteDao;
    private final CommerceFavoriteDao commerceFavoriteDao;
    private final BidDao bidDao;
    private final ZoneId businessZone;

    @Autowired
    public NotificationServiceImpl(final NotificationDao notificationDao,
            final ClientNotificationPreferenceDao clientNotificationPreferenceDao,
            final ReservationMailService reservationMailService,
            final AuctionMailService auctionMailService,
            final FavoriteMailService favoriteMailService,
            final AuctionDao auctionDao,
            final UserService userService,
            final PackFavoriteDao packFavoriteDao,
            final CommerceFavoriteDao commerceFavoriteDao,
            final BidDao bidDao,
            final ZoneId businessZone) {
        this.notificationDao = notificationDao;
        this.clientNotificationPreferenceDao = clientNotificationPreferenceDao;
        this.reservationMailService = reservationMailService;
        this.auctionMailService = auctionMailService;
        this.favoriteMailService = favoriteMailService;
        this.auctionDao = auctionDao;
        this.userService = userService;
        this.packFavoriteDao = packFavoriteDao;
        this.commerceFavoriteDao = commerceFavoriteDao;
        this.bidDao = bidDao;
        this.businessZone = businessZone;
    }

    @Transactional
    @Override
    public void notifyReservationRequested(final Reservation reservation, final String commerceEmail,
            final String acceptToken, final String rejectToken, final String pickupDateStr,
            final Locale commerceLocale) {
        final ReservationSnapshot snapshot = snapshotFromReservation(reservation);
        final Long commerceUserId = snapshot.commerceUserId();
        createWebNotification(commerceUserId, NotificationType.RESERVATION_REQUESTED_COMMERCE, reservation.getId(),
                null, snapshot);
        sendMailSafely(() -> reservationMailService.sendReservationRequestToCommerce(reservation, commerceEmail,
                acceptToken, rejectToken, pickupDateStr, commerceLocale),
                "notifyReservationRequested");
    }

    @Transactional
    @Override
    public void notifyReservationCodeIssued(final Reservation reservation, final String clientEmail,
            final String pickupDateStr, final Locale clientLocale) {
        final ReservationSnapshot snapshot = snapshotFromReservation(reservation);
        createWebNotification(reservation.getCustomerId(), NotificationType.RESERVATION_CODE_CLIENT,
                reservation.getId(), null, snapshot);
        if (shouldSendClientMail(reservation.getCustomerId(), NotificationType.RESERVATION_CODE_CLIENT)) {
            sendMailSafely(() -> reservationMailService.sendReservationCodeToClient(reservation, clientEmail,
                    pickupDateStr, clientLocale), "notifyReservationCodeIssued");
        }
    }

    @Transactional
    @Override
    public void notifyAuctionWinnerForClient(final Reservation reservation, final String clientEmail,
            final String pickupDateStr, final Locale clientLocale) {
        final ReservationSnapshot snapshot = snapshotFromReservation(reservation);
        createWebNotification(reservation.getCustomerId(), NotificationType.AUCTION_WINNER_CLIENT,
                reservation.getId(), null, snapshot);
        if (shouldSendClientMail(reservation.getCustomerId(), NotificationType.AUCTION_WINNER_CLIENT)) {
            sendMailSafely(() -> reservationMailService.sendAuctionWinnerCodeToClient(reservation, clientEmail,
                    pickupDateStr, clientLocale), "notifyAuctionWinnerForClient");
        }
    }

    @Transactional
    @Override
    public void notifyAuctionWinnerForCommerce(final Reservation reservation, final String commerceEmail,
            final String pickupDateStr, final Locale commerceLocale) {
        final ReservationSnapshot snapshot = snapshotFromReservation(reservation);
        createWebNotification(snapshot.commerceUserId(), NotificationType.AUCTION_WINNER_COMMERCE,
                reservation.getId(), null, snapshot);
        sendMailSafely(() -> reservationMailService.sendAuctionWinnerCodeToCommerce(reservation, commerceEmail,
                pickupDateStr, commerceLocale), "notifyAuctionWinnerForCommerce");
    }

    @Transactional
    @Override
    public void notifyReservationRejected(final Reservation reservation, final String clientEmail,
            final Locale clientLocale) {
        final ReservationSnapshot snapshot = snapshotFromReservation(reservation);
        createWebNotification(reservation.getCustomerId(), NotificationType.RESERVATION_REJECTED_CLIENT,
                reservation.getId(), null, snapshot);
        if (shouldSendClientMail(reservation.getCustomerId(), NotificationType.RESERVATION_REJECTED_CLIENT)) {
            sendMailSafely(() -> reservationMailService.sendReservationRejectedToClient(reservation, clientEmail,
                    clientLocale), "notifyReservationRejected");
        }
    }

    @Transactional
    @Override
    public void notifyAuctionOutbid(final long previousBidderId, final long auctionId, final double newAmount) {
        final Auction auction = auctionDao.findById(auctionId)
                .orElseThrow(() -> new IllegalStateException("Auction not found: " + auctionId));
        final Pack pack = auction.getPack();
        final Long packId = pack != null ? pack.getId() : null;
        final String packTitle = pack != null ? pack.getTitle() : null;
        final String commerceName = commerceCommercialName(pack);
        final LocalDateTime now = currentTimestamp();

        notificationDao.create(previousBidderId, NotificationType.AUCTION_OUTBID_CLIENT, null, auctionId, packId,
                packTitle, commerceName, newAmount, null, null, now);

        if (shouldSendClientMail(previousBidderId, NotificationType.AUCTION_OUTBID_CLIENT)) {
            final User clientUser = userService.findById(previousBidderId)
                    .orElse(null);
            if (clientUser != null) {
                final Locale locale = clientUser.getLocale() != null ? clientUser.getLocale() : Locale.forLanguageTag("es");
                sendMailSafely(() -> auctionMailService.sendAuctionOutbidToClient(clientUser.getEmail(),
                        packTitle, commerceName, newAmount, locale), "notifyAuctionOutbid");
            }
        }
    }

    @Transactional
    @Override
    public void notifyPackRestocked(final Pack pack) {
        if (pack == null) return;
        final List<Long> clientIds = packFavoriteDao.findClientIdsByPack(pack.getId());
        final String packTitle = pack.getTitle();
        final String commerceName = commerceCommercialName(pack);
        final LocalDateTime now = currentTimestamp();
        for (final Long clientId : clientIds) {
            notificationDao.create(clientId, NotificationType.FAVORITE_PACK_RESTOCKED, null, null, pack.getId(),
                    packTitle, commerceName, null, null, null, now);
            if (shouldSendClientMail(clientId, NotificationType.FAVORITE_PACK_RESTOCKED)) {
                final User clientUser = userService.findById(clientId).orElse(null);
                if (clientUser != null) {
                    final Locale locale = clientUser.getLocale() != null ? clientUser.getLocale() : Locale.forLanguageTag("es");
                    sendMailSafely(() -> favoriteMailService.sendFavoritePackRestockedToClient(clientUser.getEmail(),
                            packTitle, commerceName, locale), "notifyPackRestocked");
                }
            }
        }
    }

    @Transactional
    @Override
    public void notifyPackPublished(final Pack pack) {
        if (pack == null) return;
        final List<Long> clientIds = commerceFavoriteDao.findClientIdsByCommerce(pack.getCommerceId());
        final String packTitle = pack.getTitle();
        final String commerceName = commerceCommercialName(pack);
        final LocalDateTime now = currentTimestamp();
        for (final Long clientId : clientIds) {
            notificationDao.create(clientId, NotificationType.FAVORITE_COMMERCE_NEW_PACK, null, null, pack.getId(),
                    packTitle, commerceName, null, null, null, now);
            if (shouldSendClientMail(clientId, NotificationType.FAVORITE_COMMERCE_NEW_PACK)) {
                final User clientUser = userService.findById(clientId).orElse(null);
                if (clientUser != null) {
                    final Locale locale = clientUser.getLocale() != null ? clientUser.getLocale() : Locale.forLanguageTag("es");
                    sendMailSafely(() -> favoriteMailService.sendFavoriteCommerceNewPackToClient(clientUser.getEmail(),
                            packTitle, commerceName, locale), "notifyPackPublished");
                }
            }
        }
    }

    @Transactional
    @Override
    public void notifyAuctionFinished(final long auctionId) {
        final Auction auction = auctionDao.findById(auctionId).orElse(null);
        if (auction == null) return;
        final Pack pack = auction.getPack();
        final Long packId = pack != null ? pack.getId() : null;
        final String packTitle = pack != null ? pack.getTitle() : null;
        final String commerceName = commerceCommercialName(pack);
        final Long winnerId = auction.getCurrentBidderId();
        final Double winningAmount = auction.getCurrentBid();
        final List<Bid> bids = bidDao.findByAuctionId(auctionId);
        final Set<Long> bidderIds = bids.stream()
                .map(Bid::getClientId)
                .collect(Collectors.toSet());
        
        final LocalDateTime now = currentTimestamp();
        for (final Long bidderId : bidderIds) {
            if (winnerId != null && winnerId.equals(bidderId)) {
                // Winner already notified by createReservation
                continue;
            }
            notificationDao.create(bidderId, NotificationType.AUCTION_LOST_CLIENT, null, auctionId, packId,
                    packTitle, commerceName, winningAmount, null, null, now);
            if (shouldSendClientMail(bidderId, NotificationType.AUCTION_LOST_CLIENT)) {
                final User clientUser = userService.findById(bidderId).orElse(null);
                if (clientUser != null) {
                    final Locale locale = clientUser.getLocale() != null ? clientUser.getLocale() : Locale.forLanguageTag("es");
                    sendMailSafely(() -> auctionMailService.sendAuctionFinishedLostToClient(clientUser.getEmail(),
                            packTitle, commerceName, locale), "notifyAuctionFinished");
                }
            }
        }
    }

    @Transactional(readOnly = true)
    @Override
    public List<NotificationItemView> findRecentForUser(final long userId, final int limit) {
        return notificationDao.findRecentByRecipient(userId, limit).stream()
                .map(this::toView)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    @Override
    public int countUnread(final long userId) {
        return notificationDao.countUnread(userId);
    }

    @Transactional
    @Override
    public Optional<NotificationItemView> markRead(final long notificationId) {
        return notificationDao.markRead(notificationId, currentTimestamp()).map(this::toView);
    }

    @Transactional
    @Override
    public Optional<NotificationItemView> markUnread(final long notificationId) {
        return notificationDao.markUnread(notificationId).map(this::toView);
    }

    @Transactional
    @Override
    public int markAllRead(final long userId) {
        return notificationDao.markAllRead(userId, currentTimestamp());
    }

    @Transactional
    @Override
    public Optional<NotificationItemView> softDelete(final long notificationId) {
        return notificationDao.softDelete(notificationId, currentTimestamp()).map(this::toView);
    }

    @Transactional(readOnly = true)
    @Override
    public List<ClientMailPreferenceView> getClientMailPreferences(final long clientId) {
        final Map<NotificationType, Boolean> stored = clientNotificationPreferenceDao.findByClient(clientId).stream()
                .collect(Collectors.toMap(ClientNotificationPreference::getType,
                        ClientNotificationPreference::isMailEnabled, (a, b) -> b));
        return CLIENT_MAIL_TYPES.stream()
                .map(type -> new ClientMailPreferenceView(type, stored.getOrDefault(type, true)))
                .collect(Collectors.toList());
    }

    @Transactional
    @Override
    public void updateClientMailPreferences(final long clientId,
            final Map<NotificationType, Boolean> preferencesByType) {
        for (final Map.Entry<NotificationType, Boolean> entry : preferencesByType.entrySet()) {
            if (CLIENT_MAIL_TYPES.contains(entry.getKey())) {
                clientNotificationPreferenceDao.upsert(clientId, entry.getKey(), entry.getValue());
            }
        }
    }

    private void createWebNotification(final Long recipientId, final NotificationType type,
            final Long reservationId, final Long auctionId, final ReservationSnapshot snapshot) {
        final LocalDateTime now = currentTimestamp();
        notificationDao.create(recipientId, type, reservationId, auctionId, snapshot.packId(),
                snapshot.packTitle(), snapshot.commerceName(), snapshot.amount(), snapshot.pickupCode(),
                snapshot.pickupDate(), now);
    }

    private ReservationSnapshot snapshotFromReservation(final Reservation reservation) {
        final Pack pack = reservation.getPack();
        final Long packId = pack != null ? pack.getId() : null;
        final String packTitle = pack != null ? pack.getTitle() : null;
        final Long commerceUserId = pack != null ? pack.getCommerceId() : null;
        return new ReservationSnapshot(packId, packTitle, commerceCommercialName(pack), commerceUserId,
                reservation.getFinalPrice(), reservation.getPickupCode(), reservation.getReservationDate());
    }

    private String commerceCommercialName(final Pack pack) {
        if (pack == null || pack.getCommerce() == null) {
            return null;
        }
        return pack.getCommerce().getCommercialName();
    }

    private boolean shouldSendClientMail(final Long clientId, final NotificationType type) {
        return clientNotificationPreferenceDao.findByClientAndType(clientId, type)
                .map(ClientNotificationPreference::isMailEnabled)
                .orElse(true);
    }

    private LocalDateTime currentTimestamp() {
        return LocalDateTime.now(businessZone);
    }

    private void sendMailSafely(final Runnable mailAction, final String context) {
        try {
            mailAction.run();
        } catch (final RuntimeException ex) {
            LOGGER.warn("Mail dispatch failed context={}", context, ex);
        }
    }

    private NotificationItemView toView(final Notification notification) {
        final Long reservationId = notification.getReservation() != null ? notification.getReservation().getId() : null;
        final Long auctionId = notification.getAuction() != null ? notification.getAuction().getId() : null;
        final Long packId = notification.getPack() != null ? notification.getPack().getId() : null;
        return new NotificationItemView(
                notification.getId(),
                notification.getType(),
                notification.getPackTitle(),
                notification.getCommerceName(),
                notification.getAmount(),
                notification.getPickupCode(),
                notification.getPickupDate(),
                notification.getCreatedAt(),
                notification.getReadAt() != null,
                reservationId,
                auctionId,
                packId);
    }

    private record ReservationSnapshot(Long packId, String packTitle, String commerceName, Long commerceUserId,
            Double amount, String pickupCode, LocalDateTime pickupDate) {
    }
}
