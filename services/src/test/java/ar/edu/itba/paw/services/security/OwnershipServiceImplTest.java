package ar.edu.itba.paw.services.security;

import ar.edu.itba.paw.models.auction.Auction;
import ar.edu.itba.paw.models.pack.Municipality;
import ar.edu.itba.paw.models.pack.Pack;
import ar.edu.itba.paw.models.pack.PackTag;
import ar.edu.itba.paw.models.reservation.Reservation;
import ar.edu.itba.paw.models.reservation.ReservationToken;
import ar.edu.itba.paw.models.user.Client;
import ar.edu.itba.paw.models.user.Commerce;
import ar.edu.itba.paw.persistence.AuctionDao;
import ar.edu.itba.paw.persistence.NotificationDao;
import ar.edu.itba.paw.persistence.PackDao;
import ar.edu.itba.paw.persistence.ReservationDao;
import ar.edu.itba.paw.persistence.ReservationTokenDao;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OwnershipServiceImplTest {

    private static final long COMMERCE_USER_ID = 10L;
    private static final long OTHER_USER_ID = 99L;

    @Mock
    private PackDao packDao;

    @Mock
    private AuctionDao auctionDao;

    @Mock
    private ReservationDao reservationDao;

    @Mock
    private ReservationTokenDao reservationTokenDao;

    @Mock
    private NotificationDao notificationDao;

    @InjectMocks
    private OwnershipServiceImpl ownershipService;

    private static Commerce commerceRef(final long userId) {
        return new Commerce(userId, "Comm", Commerce.Category.BAKERY, "St", 1, Municipality.AVELLANEDA, "P", "1000",
                "08:00", "20:00");
    }

    private static Pack pack(final long id, final long commerceUserId, final boolean deleted) {
        return new Pack(id, commerceRef(commerceUserId), "Title", "Desc", 100.0, 50.0, 1, true, deleted,
                Collections.singletonList(PackTag.SWEET), null);
    }

    @Test
    void canWritePack_whenOwner_returnsTrue() {
        // 1. Setup
        when(packDao.findById(1L)).thenReturn(Optional.of(pack(1L, COMMERCE_USER_ID, false)));

        // 2. Ejercicio
        final boolean result = ownershipService.canWritePack(1L, COMMERCE_USER_ID);

        // 3. Asserts
        assertTrue(result);
    }

    @Test
    void canWritePack_whenNotOwner_returnsFalse() {
        // 1. Setup
        when(packDao.findById(1L)).thenReturn(Optional.of(pack(1L, COMMERCE_USER_ID, false)));

        // 2. Ejercicio
        final boolean result = ownershipService.canWritePack(1L, OTHER_USER_ID);

        // 3. Asserts
        assertFalse(result);
    }

    @Test
    void canWritePack_whenMissing_throwsNotFound() {
        // 1. Setup
        when(packDao.findById(1L)).thenReturn(Optional.empty());

        // 2. Ejercicio
        final OwnershipResourceNotFoundException thrown = assertThrows(OwnershipResourceNotFoundException.class,
                () -> ownershipService.canWritePack(1L, COMMERCE_USER_ID));

        // 3. Asserts
        assertNotNull(thrown);
    }

    @Test
    void canWriteAuction_whenOwner_returnsTrue() {
        // 1. Setup
        final Pack ownedPack = pack(5L, COMMERCE_USER_ID, false);
        final Auction auction = new Auction(3L, ownedPack, 40.0, 5.0, null, null,
                LocalDateTime.now().plusHours(2), Auction.Status.ACTIVE, LocalDateTime.now());
        when(auctionDao.findById(3L)).thenReturn(Optional.of(auction));

        // 2. Ejercicio
        final boolean result = ownershipService.canWriteAuction(3L, COMMERCE_USER_ID);

        // 3. Asserts
        assertTrue(result);
    }

    @Test
    void canWriteAuction_whenMissing_throwsNotFound() {
        // 1. Setup
        when(auctionDao.findById(3L)).thenReturn(Optional.empty());

        // 2. Ejercicio
        final OwnershipResourceNotFoundException thrown = assertThrows(OwnershipResourceNotFoundException.class,
                () -> ownershipService.canWriteAuction(3L, COMMERCE_USER_ID));

        // 3. Asserts
        assertTrue(thrown.getMessage().contains("Auction"));
    }

    @Test
    void canWriteAuction_whenNotOwner_returnsFalse() {
        // 1. Setup
        final Pack ownedPack = pack(5L, COMMERCE_USER_ID, false);
        final Auction auction = new Auction(3L, ownedPack, 40.0, 5.0, null, null,
                LocalDateTime.now().plusHours(2), Auction.Status.ACTIVE, LocalDateTime.now());
        when(auctionDao.findById(3L)).thenReturn(Optional.of(auction));

        // 2. Ejercicio
        final boolean result = ownershipService.canWriteAuction(3L, OTHER_USER_ID);

        // 3. Asserts
        assertFalse(result);
    }

    @Test
    void canWriteReservation_whenCommerceOwner_returnsTrue() {
        // 1. Setup
        final Pack ownedPack = pack(7L, COMMERCE_USER_ID, false);
        final Client client = new Client(OTHER_USER_ID, "Client", "Last", true);
        final Reservation reservation = new Reservation(20L, client, ownedPack, LocalDateTime.now(), 50.0,
                Reservation.Status.RESERVED, "ABC123", null, 1, null);
        when(reservationDao.findById(20L)).thenReturn(Optional.of(reservation));

        // 2. Ejercicio
        final boolean result = ownershipService.canWriteReservation(20L, COMMERCE_USER_ID);

        // 3. Asserts
        assertTrue(result);
    }

    @Test
    void canWriteReservation_whenMissing_throwsNotFound() {
        // 1. Setup
        when(reservationDao.findById(20L)).thenReturn(Optional.empty());

        // 2. Ejercicio
        final OwnershipResourceNotFoundException thrown = assertThrows(OwnershipResourceNotFoundException.class,
                () -> ownershipService.canWriteReservation(20L, COMMERCE_USER_ID));

        // 3. Asserts
        assertTrue(thrown.getMessage().contains("Reservation"));
    }

    @Test
    void canWriteReservation_whenNotOwner_returnsFalse() {
        // 1. Setup
        final Pack ownedPack = pack(7L, COMMERCE_USER_ID, false);
        final Client client = new Client(OTHER_USER_ID, "Client", "Last", true);
        final Reservation reservation = new Reservation(20L, client, ownedPack, LocalDateTime.now(), 50.0,
                Reservation.Status.RESERVED, "ABC123", null, 1, null);
        when(reservationDao.findById(20L)).thenReturn(Optional.of(reservation));

        // 2. Ejercicio
        final boolean result = ownershipService.canWriteReservation(20L, OTHER_USER_ID);

        // 3. Asserts
        assertFalse(result);
    }

    @Test
    void canWriteToken_whenCommerceOwner_returnsTrue() {
        // 1. Setup
        final Pack ownedPack = pack(8L, COMMERCE_USER_ID, false);
        final Client client = new Client(OTHER_USER_ID, "Client", "Last", true);
        final Reservation reservation = new Reservation(30L, client, ownedPack, LocalDateTime.now(), 50.0,
                Reservation.Status.RESERVED, "ABC123", null, 1, null);
        final ReservationToken token = new ReservationToken("tok", reservation, ReservationToken.Action.ACCEPT, false,
                LocalDateTime.now(), LocalDateTime.now().plusDays(1));
        when(reservationTokenDao.findByToken("tok")).thenReturn(Optional.of(token));
        when(reservationDao.findById(30L)).thenReturn(Optional.of(reservation));

        // 2. Ejercicio
        final boolean result = ownershipService.canWriteToken("tok", COMMERCE_USER_ID);

        // 3. Asserts
        assertTrue(result);
    }

    @Test
    void canWriteToken_whenMissing_throwsNotFound() {
        // 1. Setup
        when(reservationTokenDao.findByToken("tok")).thenReturn(Optional.empty());

        // 2. Ejercicio
        final OwnershipResourceNotFoundException thrown = assertThrows(OwnershipResourceNotFoundException.class,
                () -> ownershipService.canWriteToken("tok", COMMERCE_USER_ID));

        // 3. Asserts
        assertTrue(thrown.getMessage().contains("Token"));
    }

    @Test
    void canWriteToken_whenNotOwner_returnsFalse() {
        // 1. Setup
        final Pack ownedPack = pack(8L, COMMERCE_USER_ID, false);
        final Client client = new Client(OTHER_USER_ID, "Client", "Last", true);
        final Reservation reservation = new Reservation(30L, client, ownedPack, LocalDateTime.now(), 50.0,
                Reservation.Status.RESERVED, "ABC123", null, 1, null);
        final ReservationToken token = new ReservationToken("tok", reservation, ReservationToken.Action.ACCEPT, false,
                LocalDateTime.now(), LocalDateTime.now().plusDays(1));
        when(reservationTokenDao.findByToken("tok")).thenReturn(Optional.of(token));
        when(reservationDao.findById(30L)).thenReturn(Optional.of(reservation));

        // 2. Ejercicio
        final boolean result = ownershipService.canWriteToken("tok", OTHER_USER_ID);

        // 3. Asserts
        assertFalse(result);
    }

    @Test
    void canWriteNotification_whenRecipient_returnsTrue() {
        // 1. Setup
        when(notificationDao.findById(5L)).thenReturn(Optional.of(
                new ar.edu.itba.paw.models.notification.Notification(
                        5L,
                        new ar.edu.itba.paw.models.user.User(10L, "u@test.com", "p", "U", null,
                                ar.edu.itba.paw.models.user.User.Role.CLIENT, false),
                        ar.edu.itba.paw.models.notification.NotificationType.RESERVATION_CODE_CLIENT,
                        null, null, null, null, null, null, null, null,
                        java.time.LocalDateTime.now(), null, null)));
        when(notificationDao.belongsToRecipient(5L, 10L)).thenReturn(true);

        // 2. Ejercicio
        final boolean result = ownershipService.canWriteNotification(5L, 10L);

        // 3. Asserts
        assertTrue(result);
    }

    @Test
    void canWriteNotification_whenNotRecipient_returnsFalse() {
        // 1. Setup
        when(notificationDao.findById(5L)).thenReturn(Optional.of(
                new ar.edu.itba.paw.models.notification.Notification(
                        5L,
                        new ar.edu.itba.paw.models.user.User(10L, "u@test.com", "p", "U", null,
                                ar.edu.itba.paw.models.user.User.Role.CLIENT, false),
                        ar.edu.itba.paw.models.notification.NotificationType.RESERVATION_CODE_CLIENT,
                        null, null, null, null, null, null, null, null,
                        java.time.LocalDateTime.now(), null, null)));
        when(notificationDao.belongsToRecipient(5L, 10L)).thenReturn(false);

        // 2. Ejercicio
        final boolean result = ownershipService.canWriteNotification(5L, 10L);

        // 3. Asserts
        assertFalse(result);
    }

    @Test
    void canWriteNotification_whenMissing_throwsNotFound() {
        // 1. Setup
        when(notificationDao.findById(5L)).thenReturn(Optional.empty());

        // 2. Ejercicio
        final OwnershipResourceNotFoundException thrown = assertThrows(OwnershipResourceNotFoundException.class,
                () -> ownershipService.canWriteNotification(5L, 10L));

        // 3. Asserts
        assertNotNull(thrown);
    }
}
