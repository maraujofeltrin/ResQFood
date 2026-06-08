package ar.edu.itba.paw.services.notification;

import ar.edu.itba.paw.models.auction.Auction;
import ar.edu.itba.paw.models.notification.ClientNotificationPreference;
import ar.edu.itba.paw.models.notification.Notification;
import ar.edu.itba.paw.models.notification.NotificationType;
import ar.edu.itba.paw.models.pack.Municipality;
import ar.edu.itba.paw.models.pack.Pack;
import ar.edu.itba.paw.models.reservation.Reservation;
import ar.edu.itba.paw.models.user.Client;
import ar.edu.itba.paw.models.user.Commerce;
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
import ar.edu.itba.paw.models.auction.Bid;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.Locale;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
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

    private static Client clientRef(final long id) {
        return new Client(id, "N", "L", true);
    }

    private static Commerce commerceRef(final long userId) {
        return new Commerce(userId, "Shop", Commerce.Category.BAKERY, "St", 1, Municipality.AVELLANEDA, "P", "1000",
                "08:00", "20:00");
    }

    private static Pack packRef(final long id, final long commerceId) {
        return new Pack(id, commerceRef(commerceId), "Pack title", "d", 1.0, 1.0, 1, true, Collections.emptyList());
    }

    private static Reservation reservationRef(final long id, final long clientId, final long packId,
            final long commerceId) {
        return new Reservation(id, clientRef(clientId), packRef(packId, commerceId), LocalDateTime.now(ZoneOffset.UTC),
                50.0, Reservation.Status.RESERVED, "ABC12", null, 1, null);
    }

    private static Notification notificationWithRecipient(final long recipientId, final NotificationType type) {
        final User recipient = new User(recipientId, "u@test.com", "p", "U", null, User.Role.CLIENT, false);
        return new Notification(1L, recipient, type, null, null, null, "Pack", "Shop", 50.0, "ABC12", null,
                LocalDateTime.now(ZoneOffset.UTC), null, null);
    }

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

}
