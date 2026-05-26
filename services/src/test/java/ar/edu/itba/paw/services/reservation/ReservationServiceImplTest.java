package ar.edu.itba.paw.services.reservation;

import ar.edu.itba.paw.models.pack.Pack;
import ar.edu.itba.paw.models.reservation.Reservation;
import ar.edu.itba.paw.models.reservation.ReservationRejectionError;
import ar.edu.itba.paw.models.reservation.ReservationToken;
import ar.edu.itba.paw.models.user.Client;
import ar.edu.itba.paw.models.user.User;
import ar.edu.itba.paw.persistence.AuctionDao;
import ar.edu.itba.paw.persistence.PackDao;
import ar.edu.itba.paw.persistence.ReservationDao;
import ar.edu.itba.paw.persistence.CommerceDao;
import ar.edu.itba.paw.persistence.ReservationTokenDao;
import ar.edu.itba.paw.services.user.ClientService;
import ar.edu.itba.paw.services.user.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReservationServiceImplTest {

    private static final String APP_URL = "http://app";
    private static final ZoneId TEST_ZONE = ZoneId.of("America/Argentina/Buenos_Aires");

    @Mock
    private UserService userService;
    @Mock
    private ClientService clientService;
    @Mock
    private ReservationDao reservationDao;
    @Mock
    private ReservationTokenDao reservationTokenDao;
    @Mock
    private PackDao packDao;
    @Mock
    private ReservationMailService reservationMailService;
    @Mock
    private CommerceDao commerceDao;
    @Mock
    private AuctionDao auctionDao;

    private ReservationServiceImpl reservationService;

    private static Client clientRef(final long id) {
        return new Client(id, "N", "L", true);
    }

    private static Pack packRef(final long id) {
        return new Pack(id, 1L, "t", "d", 1.0, 1.0, 1, true, Collections.emptyList());
    }

    private static Pack packRef(final long id, final long commerceId) {
        return new Pack(id, commerceId, "t", "d", 1.0, 1.0, 1, true, Collections.emptyList());
    }

    private static Reservation reservationRef(final long id) {
        return new Reservation(id, clientRef(1L), packRef(1L), null, null, null, null, null, 1, null);
    }

    @BeforeEach
    void setUp() {
        reservationService = new ReservationServiceImpl(
                userService,
                clientService,
                reservationDao,
                reservationTokenDao,
                packDao,
                reservationMailService,
                commerceDao,
                auctionDao,
                TEST_ZONE);
    }

    @Test
    void testCreateReservationWhenDirectSaleSendsCommerceAndClientMail() {
        // 1. Setup
        final long packId = 10L;
        final long commerceUserId = 100L;
        final User clientUser = new User(1L, "user@example.org", "pwd", "Test User", null, User.Role.CLIENT, false);
        final User commerceUser = new User(commerceUserId, "commerce@example.org", "pwd", "Commerce", null, User.Role.COMMERCE, false);
        final Pack pack = new Pack(packId, commerceUserId, "title", "desc", 10.0, 5.0, 5, true, false,
                Collections.emptyList(), null);
        when(userService.findById(1L)).thenReturn(Optional.of(clientUser));
        when(clientService.findByUserId(1L)).thenReturn(Optional.of(new Client(1L, "Test", "User", true)));
        when(packDao.decrementStock(packId, 1)).thenReturn(true);
        when(reservationDao.findByPickupCode(anyString())).thenReturn(Optional.empty());
        lenient().when(auctionDao.findByPackId(anyLong())).thenReturn(Optional.empty());
        when(reservationDao.createReservation(eq(1L), eq(packId), any(LocalDateTime.class), eq(5.0),
                eq(Reservation.Status.RESERVED), anyString(), isNull(), eq(1), eq("pw")))
                .thenAnswer(inv -> new Reservation(1L, clientRef(1L), packRef(packId), inv.getArgument(2), 5.0, Reservation.Status.RESERVED,
                        inv.getArgument(5), null, 1, "pw"));
        when(packDao.findById(packId)).thenReturn(Optional.of(pack));
        when(userService.findById(commerceUserId)).thenReturn(Optional.of(commerceUser));
        final List<ReservationToken> createdTokens = new CopyOnWriteArrayList<>();
        when(reservationTokenDao.create(anyString(), anyLong(), any(ReservationToken.Action.class),
                any(LocalDateTime.class), any(LocalDateTime.class))).thenAnswer(inv -> {
            final ReservationToken token = new ReservationToken(inv.getArgument(0), reservationRef(inv.getArgument(1)),
                    inv.getArgument(2), false, inv.getArgument(3), inv.getArgument(4));
            createdTokens.add(token);
            return token;
        });
        final AtomicInteger sentToCommerce = new AtomicInteger();
        final AtomicInteger sentToClient = new AtomicInteger();
        doAnswer(inv -> {
            sentToCommerce.incrementAndGet();
            return null;
        }).when(reservationMailService).sendReservationRequestToCommerce(any(Reservation.class), anyString(),
                anyString(), anyString(), anyString(), anyString(), any(Locale.class));
        doAnswer(inv -> {
            sentToClient.incrementAndGet();
            return null;
        }).when(reservationMailService).sendReservationCodeToClient(any(Reservation.class), anyString(), anyString(),
                any(Locale.class));

        // 2. Ejercicio
        final Reservation result = reservationService.createReservation(packId, 1L, 1, 5.0, "pw", APP_URL);

        // 3. Asserts
        assertEquals(packId, result.getPackId());
        assertEquals(2, createdTokens.size());
        assertTrue(createdTokens.stream().anyMatch(t -> t.getAction() == ReservationToken.Action.ACCEPT));
        assertTrue(createdTokens.stream().anyMatch(t -> t.getAction() == ReservationToken.Action.REJECT));
        assertEquals(1, sentToCommerce.get());
        assertEquals(1, sentToClient.get());
    }

    @Test
    void testCreateReservationWhenEmptyBaseUrlSendsAuctionWinnerMails() {
        // 1. Setup
        final long packId = 15L;
        final long commerceUserId = 150L;
        final User clientUser = new User(7L, "winner@example.org", "pwd", "Winning User", null, User.Role.CLIENT, false);
        final User commerceUser = new User(commerceUserId, "commerce150@example.org", "pwd", "C150", null, User.Role.COMMERCE, false);
        final Pack pack = new Pack(packId, commerceUserId, "auction-pack", "desc", 10.0, 5.0, 5, true,
                Collections.emptyList());
        when(userService.findById(7L)).thenReturn(Optional.of(clientUser));
        when(clientService.findByUserId(7L)).thenReturn(Optional.of(new Client(7L, "Winning", "User", true)));
        when(packDao.decrementStock(packId, 1)).thenReturn(true);
        when(reservationDao.findByPickupCode(anyString())).thenReturn(Optional.empty());
        lenient().when(auctionDao.findByPackId(anyLong())).thenReturn(Optional.empty());
        when(reservationDao.createReservation(eq(7L), eq(packId), any(LocalDateTime.class), eq(7.5),
                eq(Reservation.Status.RESERVED), anyString(), isNull(), eq(1), isNull()))
                .thenAnswer(inv -> new Reservation(1L, clientRef(7L), packRef(packId), inv.getArgument(2), 7.5, Reservation.Status.RESERVED,
                        inv.getArgument(5), null, 1, null));
        when(packDao.findById(packId)).thenReturn(Optional.of(pack));
        when(userService.findById(commerceUserId)).thenReturn(Optional.of(commerceUser));
        final AtomicInteger tokenCreates = new AtomicInteger();
        lenient().when(reservationTokenDao.create(anyString(), anyLong(), any(ReservationToken.Action.class),
                any(LocalDateTime.class), any(LocalDateTime.class))).thenAnswer(inv -> {
            tokenCreates.incrementAndGet();
            return new ReservationToken(inv.getArgument(0), reservationRef(inv.getArgument(1)), inv.getArgument(2), false,
                    inv.getArgument(3), inv.getArgument(4));
        });
        final AtomicInteger sentAuctionToClient = new AtomicInteger();
        final AtomicInteger sentAuctionToCommerce = new AtomicInteger();
        doAnswer(inv -> {
            sentAuctionToClient.incrementAndGet();
            return null;
        }).when(reservationMailService).sendAuctionWinnerCodeToClient(any(Reservation.class), anyString(), anyString(),
                any(Locale.class));
        doAnswer(inv -> {
            sentAuctionToCommerce.incrementAndGet();
            return null;
        }).when(reservationMailService).sendAuctionWinnerCodeToCommerce(any(Reservation.class), anyString(),
                anyString(), any(Locale.class));

        // 2. Ejercicio
        final Reservation result = reservationService.createReservation(packId, 7L, 1, 7.5, null, "");

        // 3. Asserts
        assertEquals(packId, result.getPackId());
        assertEquals(0, tokenCreates.get());
        assertEquals(1, sentAuctionToClient.get());
        assertEquals(1, sentAuctionToCommerce.get());
    }

    @Test
    void testCreateReservationWhenQuantityInvalidDoesNotPersistOrSendMail() {
        // 1. Setup

        // 2. Ejercicio
        final IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> reservationService.createReservation(20L, 2L, 0, 5.0, "pw", APP_URL));

        // 3. Asserts
        assertTrue(ex.getMessage().contains("quantity"));
    }

    @Test
    void testCreateReservationWhenPickupWindowTooLongDoesNotPersistOrSendMail() {
        // 1. Setup
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 600; i++) {
            sb.append('x');
        }
        final String longPickup = sb.toString();

        // 2. Ejercicio
        final IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> reservationService.createReservation(30L, 3L, 1, 5.0, longPickup, APP_URL));

        // 3. Asserts
        assertTrue(ex.getMessage().contains("pickup_window"));
    }

    @Test
    void testFindByCustomerIdWhenDataExistsReturnsOnlyThatCustomer() {
        // 1. Setup
        final List<Reservation> forCustomer = new ArrayList<>();
        forCustomer.add(new Reservation(1L, clientRef(1L), packRef(10L), LocalDateTime.now(), 10.0, Reservation.Status.RESERVED, "A",
                null, 1, null));
        forCustomer.add(new Reservation(3L, clientRef(1L), packRef(12L), LocalDateTime.now(), 30.0, Reservation.Status.PAID, "C", null, 3,
                null));
        when(reservationDao.findByCustomerId(1L)).thenReturn(forCustomer);

        // 2. Ejercicio
        final List<Reservation> reservations = reservationService.findByCustomerId(1L);

        // 3. Asserts
        assertEquals(2, reservations.size());
        assertTrue(reservations.stream().allMatch(r -> r.getCustomerId().equals(1L)));
    }

    @Test
    void testFindByCommerceIdWhenDataExistsReturnsMatchingPackIds() {
        // 1. Setup
        final List<Reservation> forCommerce = new ArrayList<>();
        forCommerce.add(new Reservation(1L, clientRef(1L), packRef(100L), LocalDateTime.now(), 10.0, Reservation.Status.RESERVED, "D", null,
                1, null));
        forCommerce.add(new Reservation(3L, clientRef(3L), packRef(100L), LocalDateTime.now(), 30.0, Reservation.Status.PAID, "F", null, 1,
                null));
        when(reservationDao.findByCommerceId(100L)).thenReturn(forCommerce);

        // 2. Ejercicio
        final List<Reservation> reservations = reservationService.findByCommerceId(100L);

        // 3. Asserts
        assertEquals(2, reservations.size());
        assertTrue(reservations.stream().allMatch(r -> r.getPackId().equals(100L)));
    }

    @Test
    void testValidateReservationBelongsToCommerceWhenOwnerMatchesReservationStillPresent() {
        // 1. Setup
        final long reservationId = 5L;
        final Reservation reservation = new Reservation(reservationId, clientRef(1L), packRef(10L), LocalDateTime.now(), 10.0,
                Reservation.Status.RESERVED, "GGGGG", null, 1, null);
        when(reservationDao.findById(reservationId)).thenReturn(Optional.of(reservation));
        when(packDao.findById(10L)).thenReturn(Optional.of(
                new Pack(10L, 77L, "title", "desc", 10.0, 5.0, 5, true, Collections.emptyList())));

        // 2. Ejercicio
        reservationService.validateReservationBelongsToCommerce(reservationId, 77L);

        // 3. Asserts
        assertEquals(reservationId, reservationService.findById(reservationId).map(Reservation::getId).orElseThrow());
    }

    @Test
    void testValidateReservationBelongsToCommerceWhenCommerceMismatchThrows() {
        // 1. Setup
        final long reservationId = 5L;
        final Reservation reservation = new Reservation(reservationId, clientRef(1L), packRef(10L), LocalDateTime.now(), 10.0,
                Reservation.Status.RESERVED, "GGGGG", null, 1, null);
        when(reservationDao.findById(reservationId)).thenReturn(Optional.of(reservation));
        when(packDao.findById(10L)).thenReturn(Optional.of(
                new Pack(10L, 77L, "title", "desc", 10.0, 5.0, 5, true, Collections.emptyList())));

        // 2. Ejercicio
        final IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> reservationService.validateReservationBelongsToCommerce(reservationId, 88L));

        // 3. Asserts
        assertEquals(ReservationRejectionError.WRONG_COMMERCE.name(), ex.getMessage());
        assertEquals(Reservation.Status.RESERVED,
                reservationService.findById(reservationId).orElseThrow().getStatus());
    }

    @Test
    void testValidateReservationBelongsToCommerceWhenReservationIdNullThrows() {
        // 1. Setup

        // 2. Ejercicio
        final IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> reservationService.validateReservationBelongsToCommerce(null, 77L));

        // 3. Asserts
        assertEquals(ReservationRejectionError.INVALID_PARAMS.name(), ex.getMessage());
    }

    @Test
    void testValidateReservationBelongsToCommerceWhenCommerceIdNullThrows() {
        // 1. Setup
        final long reservationId = 5L;
        final Reservation reservation = new Reservation(reservationId, clientRef(1L), packRef(10L), LocalDateTime.now(), 10.0,
                Reservation.Status.RESERVED, "GGGGG", null, 1, null);
        when(reservationDao.findById(reservationId)).thenReturn(Optional.of(reservation));

        // 2. Ejercicio
        final IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> reservationService.validateReservationBelongsToCommerce(reservationId, null));

        // 3. Asserts
        assertEquals(ReservationRejectionError.INVALID_PARAMS.name(), ex.getMessage());
        assertEquals(Reservation.Status.RESERVED,
                reservationService.findById(reservationId).orElseThrow().getStatus());
    }

    @Test
    void testTryRejectReservationForCommerceWhenReservedCancelsRestoresStockAndNotifiesClient() {
        // 1. Setup
        final long packId = 500L;
        final long commerceId = 999L;
        final long reservationId = 42L;
        final LocalDateTime resDate = LocalDateTime.now();
        final Reservation reserved = new Reservation(reservationId, clientRef(101L), packRef(packId), resDate, 25.0,
                Reservation.Status.RESERVED, "HHHHH", null, 3, null);
        final Reservation canceled = new Reservation(reservationId, clientRef(101L), packRef(packId), resDate, 25.0,
                Reservation.Status.CANCELED, "HHHHH", null, 3, null);
        when(reservationDao.findById(reservationId)).thenReturn(Optional.of(reserved));
        when(packDao.findById(packId)).thenReturn(Optional.of(
                new Pack(packId, commerceId, "title", "desc", 10.0, 5.0, 5, true, Collections.emptyList())));
        when(packDao.incrementStock(packId, 3)).thenReturn(true);
        when(reservationDao.updateStatus(reservationId, Reservation.Status.CANCELED)).thenReturn(canceled);
        final User clientUser = new User(101L, "client@example.org", "pwd", "Client", null, User.Role.CLIENT, false);
        when(userService.findById(101L)).thenReturn(Optional.of(clientUser));
        final AtomicInteger sentRejected = new AtomicInteger();
        doAnswer(inv -> {
            sentRejected.incrementAndGet();
            return null;
        }).when(reservationMailService).sendReservationRejectedToClient(any(Reservation.class), anyString(),
                any(Locale.class));

        // 2. Ejercicio
        final ReservationServiceResult<ReservationRejectionError> result =
                reservationService.tryRejectReservationForCommerce(reservationId, commerceId);

        // 3. Asserts
        assertTrue(result.isSuccess());
        assertEquals(Reservation.Status.CANCELED, result.reservation().orElseThrow().getStatus());
        assertEquals(1, sentRejected.get());
    }

    @Test
    void testTryRejectReservationForCommerceWhenWrongCommerceReturnsError() {
        // 1. Setup
        final long packId = 700L;
        final long reservationId = 43L;
        final Reservation reserved = new Reservation(reservationId, clientRef(103L), packRef(packId), LocalDateTime.now(), 25.0,
                Reservation.Status.RESERVED, "JJJJJ", null, 1, null);
        when(reservationDao.findById(reservationId)).thenReturn(Optional.of(reserved));
        when(packDao.findById(packId)).thenReturn(Optional.of(
                new Pack(packId, 222L, "title", "desc", 10.0, 5.0, 5, true, Collections.emptyList())));

        // 2. Ejercicio
        final ReservationServiceResult<ReservationRejectionError> result =
                reservationService.tryRejectReservationForCommerce(reservationId, 333L);

        // 3. Asserts
        assertEquals(ReservationRejectionError.WRONG_COMMERCE, result.error().orElseThrow());
    }

    @Test
    void testTryRejectReservationForCommerceWhenCanceledReturnsError() {
        // 1. Setup
        final long packId = 710L;
        final long commerceId = 711L;
        final long reservationId = 44L;
        final Reservation reservation = new Reservation(reservationId, clientRef(104L), packRef(packId), LocalDateTime.now(), 25.0,
                Reservation.Status.CANCELED, "KKKKK", null, 1, null);
        when(reservationDao.findById(reservationId)).thenReturn(Optional.of(reservation));
        when(packDao.findById(packId)).thenReturn(Optional.of(
                new Pack(packId, commerceId, "title", "desc", 10.0, 5.0, 5, true, Collections.emptyList())));

        // 2. Ejercicio
        final ReservationServiceResult<ReservationRejectionError> result =
                reservationService.tryRejectReservationForCommerce(reservationId, commerceId);

        // 3. Asserts
        assertEquals(ReservationRejectionError.ALREADY_CANCELED, result.error().orElseThrow());
    }

    @Test
    void testTryRejectReservationForCommerceWhenPaidReturnsError() {
        // 1. Setup
        final long packId = 600L;
        final long commerceId = 111L;
        final long reservationId = 45L;
        final Reservation reservation = new Reservation(reservationId, clientRef(102L), packRef(packId), LocalDateTime.now(), 25.0,
                Reservation.Status.PAID, "IIIII", null, 1, null);
        when(reservationDao.findById(reservationId)).thenReturn(Optional.of(reservation));
        when(packDao.findById(packId)).thenReturn(Optional.of(
                new Pack(packId, commerceId, "title", "desc", 10.0, 5.0, 5, true, Collections.emptyList())));

        // 2. Ejercicio
        final ReservationServiceResult<ReservationRejectionError> result =
                reservationService.tryRejectReservationForCommerce(reservationId, commerceId);

        // 3. Asserts
        assertEquals(ReservationRejectionError.ALREADY_COMPLETED, result.error().orElseThrow());
    }

}
