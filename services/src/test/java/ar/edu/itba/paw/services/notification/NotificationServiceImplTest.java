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
import ar.edu.itba.paw.persistence.CommerceDao;
import ar.edu.itba.paw.persistence.CommerceFavoriteDao;
import ar.edu.itba.paw.persistence.NotificationDao;
import ar.edu.itba.paw.persistence.PackFavoriteDao;
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
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doAnswer;
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
    private AuctionDao auctionDao;
    @Mock
    private CommerceDao commerceDao;
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
                auctionDao,
                commerceDao,
                userService,
                packFavoriteDao,
                commerceFavoriteDao,
                bidDao,
                BUSINESS_ZONE);
    }

    @Test
    void testNotifyReservationCodeIssuedWhenClientMailDisabledCreatesWebNotificationOnly() {
        // 1. Setup
        final Reservation reservation = reservationRef(1L, 5L, 10L, 100L);
        when(commerceDao.findByUserId(100L)).thenReturn(Optional.of(commerceRef(100L)));
        when(clientNotificationPreferenceDao.findByClientAndType(eq(5L), eq(NotificationType.RESERVATION_CODE_CLIENT)))
                .thenReturn(Optional.of(new ClientNotificationPreference(1L, clientRef(5L),
                        NotificationType.RESERVATION_CODE_CLIENT, false)));
        final AtomicReference<NotificationType> savedType = new AtomicReference<>();
        doAnswer(inv -> {
            savedType.set(inv.getArgument(1));
            return notificationWithRecipient(5L, NotificationType.RESERVATION_CODE_CLIENT);
        }).when(notificationDao).create(eq(5L), eq(NotificationType.RESERVATION_CODE_CLIENT),
                any(), any(), any(), any(), any(), any(), any(), any(), any());

        // 2. Ejercicio
        notificationService.notifyReservationCodeIssued(reservation, "c@test.com", "01/01/2026",
                Locale.forLanguageTag("es"));

        // 3. Asserts
        assertEquals(NotificationType.RESERVATION_CODE_CLIENT, savedType.get());
    }

    @Test
    void testNotifyReservationCodeIssuedWhenNoPreferenceSendsMail() {
        // 1. Setup
        final Reservation reservation = reservationRef(1L, 5L, 10L, 100L);
        when(commerceDao.findByUserId(100L)).thenReturn(Optional.of(commerceRef(100L)));
        when(clientNotificationPreferenceDao.findByClientAndType(anyLong(), any())).thenReturn(Optional.empty());
        doAnswer(inv -> notificationWithRecipient(5L, NotificationType.RESERVATION_CODE_CLIENT))
                .when(notificationDao).create(anyLong(), eq(NotificationType.RESERVATION_CODE_CLIENT),
                        any(), any(), any(), any(), any(), any(), any(), any(), any());
        final AtomicInteger mailSent = new AtomicInteger();
        doAnswer(inv -> {
            mailSent.incrementAndGet();
            return null;
        }).when(reservationMailService).sendReservationCodeToClient(any(), anyString(), anyString(), any());

        // 2. Ejercicio
        notificationService.notifyReservationCodeIssued(reservation, "c@test.com", "01/01/2026",
                Locale.forLanguageTag("es"));

        // 3. Asserts
        assertEquals(1, mailSent.get());
    }

    @Test
    void testNotifyReservationRequestedAlwaysSendsCommerceMail() {
        // 1. Setup
        final Reservation reservation = reservationRef(1L, 5L, 10L, 100L);
        when(commerceDao.findByUserId(100L)).thenReturn(Optional.of(commerceRef(100L)));
        doAnswer(inv -> notificationWithRecipient(100L, NotificationType.RESERVATION_REQUESTED_COMMERCE))
                .when(notificationDao).create(eq(100L), eq(NotificationType.RESERVATION_REQUESTED_COMMERCE),
                        any(), any(), any(), any(), any(), any(), any(), any(), any());
        final AtomicInteger mailSent = new AtomicInteger();
        doAnswer(inv -> {
            mailSent.incrementAndGet();
            return null;
        }).when(reservationMailService).sendReservationRequestToCommerce(any(), anyString(),
                anyString(), anyString(), anyString(), any());

        // 2. Ejercicio
        notificationService.notifyReservationRequested(reservation, "shop@test.com", "a", "r",
                "01/01/2026", Locale.forLanguageTag("es"));

        // 3. Asserts
        assertEquals(1, mailSent.get());
    }

    @Test
    void testNotifyAuctionOutbidWhenPreviousBidderExistsCreatesNotificationForBidder() {
        // 1. Setup
        final Pack pack = packRef(8L, 100L);
        final Auction auction = new Auction(3L, pack, 100.0, 5.0, 120.0, 7L,
                LocalDateTime.now(ZoneOffset.UTC).plusHours(2), Auction.Status.ACTIVE, LocalDateTime.now());
        when(auctionDao.findById(3L)).thenReturn(Optional.of(auction));
        when(commerceDao.findByUserId(100L)).thenReturn(Optional.of(commerceRef(100L)));
        when(userService.findById(7L)).thenReturn(Optional.of(
                new User(7L, "bidder@test.com", "p", "B", null, User.Role.CLIENT, false)));
        when(clientNotificationPreferenceDao.findByClientAndType(eq(7L), eq(NotificationType.AUCTION_OUTBID_CLIENT)))
                .thenReturn(Optional.empty());
        final AtomicLong recipientId = new AtomicLong();
        doAnswer(inv -> {
            recipientId.set(inv.getArgument(0));
            return notificationWithRecipient(7L, NotificationType.AUCTION_OUTBID_CLIENT);
        }).when(notificationDao).create(eq(7L), eq(NotificationType.AUCTION_OUTBID_CLIENT), isNull(), eq(3L), eq(8L),
                any(), any(), eq(120.0), isNull(), isNull(), any());

        // 2. Ejercicio
        notificationService.notifyAuctionOutbid(7L, 3L, 120.0);

        // 3. Asserts
        assertEquals(7L, recipientId.get());
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
    void testNotifyPackRestockedCreatesNotificationAndSendsMail() {
        // 1. Setup
        final Pack pack = packRef(10L, 100L);
        when(packFavoriteDao.findClientIdsByPack(10L)).thenReturn(List.of(5L));
        when(commerceDao.findByUserId(100L)).thenReturn(Optional.of(commerceRef(100L)));
        when(clientNotificationPreferenceDao.findByClientAndType(eq(5L), eq(NotificationType.FAVORITE_PACK_RESTOCKED)))
                .thenReturn(Optional.of(new ClientNotificationPreference(1L, clientRef(5L),
                        NotificationType.FAVORITE_PACK_RESTOCKED, true)));
        when(userService.findById(5L)).thenReturn(Optional.of(
                new User(5L, "c@test.com", "p", "C", null, User.Role.CLIENT, false)));

        final AtomicReference<NotificationType> savedType = new AtomicReference<>();
        doAnswer(inv -> {
            savedType.set(inv.getArgument(1));
            return notificationWithRecipient(5L, NotificationType.FAVORITE_PACK_RESTOCKED);
        }).when(notificationDao).create(eq(5L), eq(NotificationType.FAVORITE_PACK_RESTOCKED),
                any(), any(), eq(10L), any(), any(), any(), any(), any(), any());

        final AtomicInteger mailSent = new AtomicInteger();
        doAnswer(inv -> {
            mailSent.incrementAndGet();
            return null;
        }).when(reservationMailService).sendFavoritePackRestockedToClient(any(), any(), any(), any());

        // 2. Ejercicio
        notificationService.notifyPackRestocked(pack);

        // 3. Asserts
        assertEquals(NotificationType.FAVORITE_PACK_RESTOCKED, savedType.get());
        assertEquals(1, mailSent.get());
    }

    @Test
    void testNotifyPackPublishedCreatesNotificationAndSendsMail() {
        // 1. Setup
        final Pack pack = packRef(10L, 100L);
        when(commerceFavoriteDao.findClientIdsByCommerce(100L)).thenReturn(List.of(5L));
        when(commerceDao.findByUserId(100L)).thenReturn(Optional.of(commerceRef(100L)));
        when(clientNotificationPreferenceDao.findByClientAndType(eq(5L), eq(NotificationType.FAVORITE_COMMERCE_NEW_PACK)))
                .thenReturn(Optional.of(new ClientNotificationPreference(1L, clientRef(5L),
                        NotificationType.FAVORITE_COMMERCE_NEW_PACK, true)));
        when(userService.findById(5L)).thenReturn(Optional.of(
                new User(5L, "c@test.com", "p", "C", null, User.Role.CLIENT, false)));

        final AtomicReference<NotificationType> savedType = new AtomicReference<>();
        doAnswer(inv -> {
            savedType.set(inv.getArgument(1));
            return notificationWithRecipient(5L, NotificationType.FAVORITE_COMMERCE_NEW_PACK);
        }).when(notificationDao).create(eq(5L), eq(NotificationType.FAVORITE_COMMERCE_NEW_PACK),
                any(), any(), eq(10L), any(), any(), any(), any(), any(), any());

        final AtomicInteger mailSent = new AtomicInteger();
        doAnswer(inv -> {
            mailSent.incrementAndGet();
            return null;
        }).when(reservationMailService).sendFavoriteCommerceNewPackToClient(any(), any(), any(), any());

        // 2. Ejercicio
        notificationService.notifyPackPublished(pack);

        // 3. Asserts
        assertEquals(NotificationType.FAVORITE_COMMERCE_NEW_PACK, savedType.get());
        assertEquals(1, mailSent.get());
    }

    @Test
    void testNotifyAuctionFinishedCreatesLostNotificationsAndSendsMail() {
        // 1. Setup
        final Pack pack = packRef(10L, 100L);
        final Auction auction = new Auction(3L, pack, 100.0, 5.0, 120.0, 7L,
                LocalDateTime.now(ZoneOffset.UTC).plusHours(2), Auction.Status.ACTIVE, LocalDateTime.now());
        final Bid winnerBid = new Bid(1L, auction, clientRef(7L), 120.0, LocalDateTime.now());
        final Bid loserBid = new Bid(2L, auction, clientRef(5L), 110.0, LocalDateTime.now());
        
        when(auctionDao.findById(3L)).thenReturn(Optional.of(auction));
        when(commerceDao.findByUserId(100L)).thenReturn(Optional.of(commerceRef(100L)));
        when(bidDao.findByAuctionId(3L)).thenReturn(List.of(winnerBid, loserBid));
        when(clientNotificationPreferenceDao.findByClientAndType(eq(5L), eq(NotificationType.AUCTION_LOST_CLIENT)))
                .thenReturn(Optional.of(new ClientNotificationPreference(1L, clientRef(5L),
                        NotificationType.AUCTION_LOST_CLIENT, true)));
        when(userService.findById(5L)).thenReturn(Optional.of(
                new User(5L, "loser@test.com", "p", "L", null, User.Role.CLIENT, false)));

        final AtomicReference<NotificationType> savedType = new AtomicReference<>();
        doAnswer(inv -> {
            savedType.set(inv.getArgument(1));
            return notificationWithRecipient(5L, NotificationType.AUCTION_LOST_CLIENT);
        }).when(notificationDao).create(eq(5L), eq(NotificationType.AUCTION_LOST_CLIENT),
                any(), eq(3L), eq(10L), any(), any(), eq(120.0), any(), any(), any());

        final AtomicInteger mailSent = new AtomicInteger();
        doAnswer(inv -> {
            mailSent.incrementAndGet();
            return null;
        }).when(reservationMailService).sendAuctionFinishedLostToClient(any(), any(), any(), any());

        // 2. Ejercicio
        notificationService.notifyAuctionFinished(3L);

        // 3. Asserts
        assertEquals(NotificationType.AUCTION_LOST_CLIENT, savedType.get());
        assertEquals(1, mailSent.get());
    }
}
