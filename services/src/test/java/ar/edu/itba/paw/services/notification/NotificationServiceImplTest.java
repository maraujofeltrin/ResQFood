package ar.edu.itba.paw.services.notification;

import ar.edu.itba.paw.models.notification.ClientNotificationPreference;
import ar.edu.itba.paw.models.notification.Notification;
import ar.edu.itba.paw.models.notification.NotificationType;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
    @Mock
    private AuctionDao auctionDao;
    @Mock
    private UserService userService;
    @Mock
    private PackFavoriteDao packFavoriteDao;
    @Mock
    private CommerceFavoriteDao commerceFavoriteDao;
    @Mock
    private BidDao bidDao;

    private NotificationServiceImpl notificationService;

    @BeforeEach
    void setUp() {
        notificationService = new NotificationServiceImpl(
                notificationDao,
                clientNotificationPreferenceDao,
                reservationMailService,
                auctionMailService,
                favoriteMailService,
                auctionDao,
                userService,
                packFavoriteDao,
                commerceFavoriteDao,
                bidDao,
                BUSINESS_ZONE);
    }

    @Test
    void testMarkReadWhenNotificationExistsReturnsReadItem() {
        // 1. Setup
        final Notification read = new Notification(1L,
                new User(1L, "u@test.com", "p", "U", null, User.Role.CLIENT, false),
                NotificationType.RESERVATION_CODE_CLIENT, null, null, null, "Pack", "Shop", 50.0, null, null,
                LocalDateTime.now(ZoneOffset.UTC), LocalDateTime.now(ZoneOffset.UTC), null);
        when(notificationDao.markRead(eq(1L), any())).thenReturn(Optional.of(read));

        // 2. Ejercicio
        final Optional<NotificationItemView> result = notificationService.markRead(1L);

        // 3. Asserts
        assertTrue(result.isPresent());
        assertTrue(result.get().isRead());
    }

    @Test
    void testSoftDeleteWhenNotificationExistsReturnsDeletedState() {
        // 1. Setup
        final Notification deleted = new Notification(1L,
                new User(1L, "u@test.com", "p", "U", null, User.Role.CLIENT, false),
                NotificationType.RESERVATION_CODE_CLIENT, null, null, null, "Pack", "Shop", 50.0, null, null,
                LocalDateTime.now(ZoneOffset.UTC), null, LocalDateTime.now(ZoneOffset.UTC));
        when(notificationDao.softDelete(eq(1L), any())).thenReturn(Optional.of(deleted));

        // 2. Ejercicio
        final Optional<NotificationItemView> result = notificationService.softDelete(1L);

        // 3. Asserts
        assertTrue(result.isPresent());
    }

    @Test
    void testMarkReadWhenNotificationMissingReturnsEmpty() {
        // 1. Setup
        when(notificationDao.markRead(eq(99L), any())).thenReturn(Optional.empty());

        // 2. Ejercicio
        final Optional<NotificationItemView> result = notificationService.markRead(99L);

        // 3. Asserts
        assertTrue(result.isEmpty());
    }

    @Test
    void testMarkUnreadWhenNotificationExistsReturnsUnreadItem() {
        // 1. Setup
        final Notification unread = new Notification(2L,
                new User(1L, "u@test.com", "p", "U", null, User.Role.CLIENT, false),
                NotificationType.AUCTION_OUTBID_CLIENT, null, null, null, "Pack", "Shop", 80.0, null, null,
                LocalDateTime.now(ZoneOffset.UTC), null, null);
        when(notificationDao.markUnread(eq(2L))).thenReturn(Optional.of(unread));

        // 2. Ejercicio
        final Optional<NotificationItemView> result = notificationService.markUnread(2L);

        // 3. Asserts
        assertTrue(result.isPresent());
        assertFalse(result.get().isRead());
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
    void testFindRecentForUserWhenNotificationsExistReturnsMappedViews() {
        // 1. Setup
        final Notification notification = new Notification(10L,
                new User(5L, "u@test.com", "p", "U", null, User.Role.CLIENT, false),
                NotificationType.FAVORITE_PACK_RESTOCKED, null, null, null, "Surplus Box", "Panadería", 120.0,
                "CODE1", LocalDateTime.now(ZoneOffset.UTC), LocalDateTime.now(ZoneOffset.UTC), null, null);
        when(notificationDao.findRecentByRecipient(5L, 10)).thenReturn(List.of(notification));

        // 2. Ejercicio
        final List<NotificationItemView> result = notificationService.findRecentForUser(5L, 10);

        // 3. Asserts
        assertEquals(1, result.size());
        assertEquals(10L, result.get(0).getId());
        assertEquals(NotificationType.FAVORITE_PACK_RESTOCKED, result.get(0).getType());
        assertEquals("Surplus Box", result.get(0).getPackTitle());
        assertFalse(result.get(0).isRead());
    }

    @Test
    void testGetClientMailPreferencesWhenNoStoredDefaultsMailEnabled() {
        // 1. Setup
        when(clientNotificationPreferenceDao.findByClient(3L)).thenReturn(Collections.emptyList());

        // 2. Ejercicio
        final List<ClientMailPreferenceView> preferences = notificationService.getClientMailPreferences(3L);

        // 3. Asserts
        assertEquals(7, preferences.size());
        assertTrue(preferences.stream().allMatch(ClientMailPreferenceView::isMailEnabled));
    }

    @Test
    void testGetClientMailPreferencesWhenStoredDisabledReturnsDisabled() {
        // 1. Setup
        final ClientNotificationPreference disabled = new ClientNotificationPreference(1L, null,
                NotificationType.RESERVATION_CODE_CLIENT, false);
        when(clientNotificationPreferenceDao.findByClient(3L)).thenReturn(List.of(disabled));

        // 2. Ejercicio
        final List<ClientMailPreferenceView> preferences = notificationService.getClientMailPreferences(3L);

        // 3. Asserts
        final Optional<ClientMailPreferenceView> reservationCode = preferences.stream()
                .filter(p -> p.getType() == NotificationType.RESERVATION_CODE_CLIENT)
                .findFirst();
        assertTrue(reservationCode.isPresent());
        assertFalse(reservationCode.get().isMailEnabled());
    }

    @Test
    void testNotifyAuctionOutbidWhenAuctionNotFoundThrowsIllegalStateException() {
        // 1. Setup
        when(auctionDao.findById(99L)).thenReturn(Optional.empty());

        // 2. Ejercicio
        final IllegalStateException thrown = assertThrows(IllegalStateException.class,
                () -> notificationService.notifyAuctionOutbid(7L, 99L, 150.0));

        // 3. Asserts
        assertTrue(thrown.getMessage().contains("99"));
    }
}
