package ar.edu.itba.paw.services.notification;

import ar.edu.itba.paw.models.auction.Auction;
import ar.edu.itba.paw.models.notification.ClientNotificationPreference;
import ar.edu.itba.paw.models.notification.Notification;
import ar.edu.itba.paw.models.notification.NotificationType;
import ar.edu.itba.paw.models.pack.Municipality;
import ar.edu.itba.paw.models.pack.Pack;
import ar.edu.itba.paw.models.user.Commerce;
import ar.edu.itba.paw.models.user.User;
import ar.edu.itba.paw.persistence.ClientNotificationPreferenceDao;
import ar.edu.itba.paw.persistence.NotificationDao;
import ar.edu.itba.paw.services.auction.AuctionMailService;
import ar.edu.itba.paw.services.pack.FavoriteMailService;
import ar.edu.itba.paw.services.reservation.ReservationMailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {

    private static final ZoneId BUSINESS_ZONE = ZoneId.of("America/Argentina/Buenos_Aires");

    @Mock
    private NotificationDao notificationDao;
    @Mock
    private ClientNotificationPreferenceDao clientNotificationPreferenceDao;
    @Mock
    private ReservationMailService reservationMailService;
    @Mock
    private AuctionMailService auctionMailService;
    @Mock
    private FavoriteMailService favoriteMailService;

    private NotificationServiceImpl notificationService;

    @BeforeEach
    void setUp() {
        notificationService = new NotificationServiceImpl(
                notificationDao,
                clientNotificationPreferenceDao,
                reservationMailService,
                auctionMailService,
                favoriteMailService,
                BUSINESS_ZONE);
    }

    @Test
    void testCountUnreadWhenDaoReturnsCountReturnsValue() {
        // 1. Setup
        when(notificationDao.countUnread(5L)).thenReturn(3);

        // 2. Ejercicio
        final int count = notificationService.countUnread(5L);

        // 3. Asserts
        assertEquals(3, count);
    }

    @Test
    void testMarkAllReadWhenNotificationsExistReturnsUpdatedCount() {
        // 1. Setup
        when(notificationDao.markAllRead(eq(5L), any())).thenReturn(4);

        // 2. Ejercicio
        final int updated = notificationService.markAllRead(5L);

        // 3. Asserts
        assertEquals(4, updated);
    }

    @Test
    void testFindRecentForUserWhenNotificationsExistReturnsEntities() {
        // 1. Setup
        final Notification notification = new Notification(10L,
                new User(5L, "u@test.com", "p", "U", null, User.Role.CLIENT, false),
                NotificationType.FAVORITE_PACK_RESTOCKED, null, null, null, "Surplus Box", "Panadería", 120.0,
                "CODE1", LocalDateTime.now(ZoneOffset.UTC), LocalDateTime.now(ZoneOffset.UTC), null, null);
        when(notificationDao.findRecentByRecipient(5L, 10)).thenReturn(List.of(notification));

        // 2. Ejercicio
        final List<Notification> result = notificationService.findRecentForUser(5L, 10);

        // 3. Asserts
        assertEquals(1, result.size());
        assertEquals(10L, result.get(0).getId());
        assertEquals(NotificationType.FAVORITE_PACK_RESTOCKED, result.get(0).getType());
        assertEquals("Surplus Box", result.get(0).getPackTitle());
        assertNull(result.get(0).getReadAt());
    }

    @Test
    void testGetClientMailPreferencesWhenNoStoredDefaultsMailEnabled() {
        // 1. Setup
        when(clientNotificationPreferenceDao.findByClient(3L)).thenReturn(Collections.emptyList());

        // 2. Ejercicio
        final Map<NotificationType, Boolean> preferences = notificationService.getClientMailPreferences(3L);

        // 3. Asserts
        assertEquals(7, preferences.size());
        assertTrue(preferences.values().stream().allMatch(Boolean::booleanValue));
    }

    @Test
    void testGetClientMailPreferencesWhenStoredDisabledReturnsDisabled() {
        // 1. Setup
        final ClientNotificationPreference disabled = new ClientNotificationPreference(1L, null,
                NotificationType.RESERVATION_CODE_CLIENT, false);
        when(clientNotificationPreferenceDao.findByClient(3L)).thenReturn(List.of(disabled));

        // 2. Ejercicio
        final Map<NotificationType, Boolean> preferences = notificationService.getClientMailPreferences(3L);

        // 3. Asserts
        assertFalse(preferences.get(NotificationType.RESERVATION_CODE_CLIENT));
    }

    @Test
    void testNotifyAuctionOutbidWhenPreviousBidderProvidedCreatesNotification() {
        // 1. Setup
        final Commerce commerce = new Commerce(10L, "Panadería", Commerce.Category.BAKERY, "St", 1,
                Municipality.AVELLANEDA, "P", "1000", "08:00", "20:00");
        final Pack pack = new Pack(3L, commerce, "Surplus", "Desc", 100.0, 50.0, 1, true, false,
                Collections.emptyList(), null);
        final Auction auction = new Auction(99L, pack, 50.0, 5.0, 75.0, 7L,
                LocalDateTime.now(ZoneOffset.UTC).plusDays(1), Auction.Status.ACTIVE, LocalDateTime.now());
        final User previousBidder = new User(7L, "outbid@test.com", "p", "Outbid", null, User.Role.CLIENT, true);
        final AtomicReference<Notification> createdNotification = new AtomicReference<>();
        when(notificationDao.create(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenAnswer(inv -> notificationFromCreateInvocation(inv, createdNotification));

        // 2. Ejercicio
        notificationService.notifyAuctionOutbid(previousBidder, auction, 150.0);

        // 3. Asserts
        assertNotNull(createdNotification.get());
        assertEquals(7L, createdNotification.get().getRecipientId());
        assertEquals(NotificationType.AUCTION_OUTBID_CLIENT, createdNotification.get().getType());
        assertEquals("Surplus", createdNotification.get().getPackTitle());
        assertEquals("Panadería", createdNotification.get().getCommerceName());
        assertEquals(150.0, createdNotification.get().getAmount());
    }

    @Test
    void testNotifyPackRestockedWhenClientsProvidedCreatesNotificationsForEach() {
        // 1. Setup
        final Commerce commerce = new Commerce(10L, "Panadería", Commerce.Category.BAKERY, "St", 1,
                Municipality.AVELLANEDA, "P", "1000", "08:00", "20:00");
        final Pack pack = new Pack(3L, commerce, "Surplus", "Desc", 100.0, 50.0, 5, true, false,
                Collections.emptyList(), null);
        final User client = new User(5L, "client@test.com", "p", "Client", null, User.Role.CLIENT, true);
        final List<Notification> createdNotifications = new ArrayList<>();
        when(notificationDao.create(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenAnswer(inv -> notificationFromCreateInvocation(inv, createdNotifications));

        // 2. Ejercicio
        notificationService.notifyPackRestocked(pack, List.of(client));

        // 3. Asserts
        assertEquals(1, createdNotifications.size());
        assertEquals(5L, createdNotifications.get(0).getRecipientId());
        assertEquals(NotificationType.FAVORITE_PACK_RESTOCKED, createdNotifications.get(0).getType());
        assertEquals("Surplus", createdNotifications.get(0).getPackTitle());
        assertEquals("Panadería", createdNotifications.get(0).getCommerceName());
    }

    @Test
    void testNotifyPackPublishedWhenClientsProvidedCreatesNotificationsForEach() {
        // 1. Setup
        final Commerce commerce = new Commerce(10L, "Panadería", Commerce.Category.BAKERY, "St", 1,
                Municipality.AVELLANEDA, "P", "1000", "08:00", "20:00");
        final Pack pack = new Pack(3L, commerce, "New Pack", "Desc", 100.0, 50.0, 5, true, false,
                Collections.emptyList(), null);
        final User client = new User(5L, "client@test.com", "p", "Client", null, User.Role.CLIENT, true);
        final List<Notification> createdNotifications = new ArrayList<>();
        when(notificationDao.create(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenAnswer(inv -> notificationFromCreateInvocation(inv, createdNotifications));

        // 2. Ejercicio
        notificationService.notifyPackPublished(pack, List.of(client));

        // 3. Asserts
        assertEquals(1, createdNotifications.size());
        assertEquals(5L, createdNotifications.get(0).getRecipientId());
        assertEquals(NotificationType.FAVORITE_COMMERCE_NEW_PACK, createdNotifications.get(0).getType());
        assertEquals("New Pack", createdNotifications.get(0).getPackTitle());
        assertEquals("Panadería", createdNotifications.get(0).getCommerceName());
    }

    @Test
    void testNotifyAuctionFinishedWhenBiddersProvidedSkipsWinnerAndNotifiesLosers() {
        // 1. Setup
        final Commerce commerce = new Commerce(10L, "Panadería", Commerce.Category.BAKERY, "St", 1,
                Municipality.AVELLANEDA, "P", "1000", "08:00", "20:00");
        final Pack pack = new Pack(3L, commerce, "Auction Pack", "Desc", 100.0, 50.0, 1, true, false,
                Collections.emptyList(), null);
        final Auction auction = new Auction(99L, pack, 50.0, 5.0, 120.0, 7L,
                LocalDateTime.now(ZoneOffset.UTC).minusHours(1), Auction.Status.FINISHED, LocalDateTime.now());
        final User winner = new User(7L, "winner@test.com", "p", "Winner", null, User.Role.CLIENT, true);
        final User loser = new User(8L, "loser@test.com", "p", "Loser", null, User.Role.CLIENT, true);
        final List<Notification> createdNotifications = new ArrayList<>();
        when(notificationDao.create(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenAnswer(inv -> notificationFromCreateInvocation(inv, createdNotifications));

        // 2. Ejercicio
        notificationService.notifyAuctionFinished(auction, List.of(winner, loser));

        // 3. Asserts
        assertEquals(1, createdNotifications.size());
        assertEquals(8L, createdNotifications.get(0).getRecipientId());
        assertEquals(NotificationType.AUCTION_LOST_CLIENT, createdNotifications.get(0).getType());
        assertEquals("Auction Pack", createdNotifications.get(0).getPackTitle());
        assertEquals("Panadería", createdNotifications.get(0).getCommerceName());
        assertEquals(120.0, createdNotifications.get(0).getAmount());
    }

    private static Notification notificationFromCreateInvocation(final InvocationOnMock invocation,
            final AtomicReference<Notification> captured) {
        final Notification notification = buildNotificationFromCreateInvocation(invocation);
        captured.set(notification);
        return notification;
    }

    private static Notification notificationFromCreateInvocation(final InvocationOnMock invocation,
            final List<Notification> captured) {
        final Notification notification = buildNotificationFromCreateInvocation(invocation);
        captured.add(notification);
        return notification;
    }

    private static Notification buildNotificationFromCreateInvocation(final InvocationOnMock invocation) {
        final Long recipientId = invocation.getArgument(0);
        final NotificationType type = invocation.getArgument(1);
        final String packTitle = invocation.getArgument(5);
        final String commerceName = invocation.getArgument(6);
        final Double amount = invocation.getArgument(7);
        final LocalDateTime createdAt = invocation.getArgument(10);
        return new Notification(1L,
                new User(recipientId, "stub@test.com", "p", "U", null, User.Role.CLIENT, true),
                type, null, null, null, packTitle, commerceName, amount, null, null,
                createdAt, null, null);
    }
}
