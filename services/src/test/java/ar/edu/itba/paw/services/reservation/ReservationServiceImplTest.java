package ar.edu.itba.paw.services.reservation;

import ar.edu.itba.paw.models.pack.Municipality;
import ar.edu.itba.paw.models.pack.Pack;
import ar.edu.itba.paw.models.reservation.Reservation;
import ar.edu.itba.paw.models.reservation.ReservationCreationException;
import ar.edu.itba.paw.models.reservation.ReservationRejectionError;
import ar.edu.itba.paw.models.reservation.ReservationToken;
import ar.edu.itba.paw.models.user.Client;
import ar.edu.itba.paw.models.user.Commerce;
import ar.edu.itba.paw.models.user.User;
import ar.edu.itba.paw.persistence.ReservationDao;
import ar.edu.itba.paw.services.notification.NotificationService;
import ar.edu.itba.paw.services.pack.PackService;
import ar.edu.itba.paw.services.auction.AuctionService;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
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
    private ReservationTokenService reservationTokenService;
    @Mock
    private PackService packService;
    @Mock
    private NotificationService notificationService;
    @Mock
    private AuctionService auctionService;

    private ReservationServiceImpl reservationService;

    private static Client clientRef(final long id) {
        return new Client(id, "N", "L", true);
    }

    private static Commerce commerceRef(final long userId) {
        return new Commerce(userId, "Comm", Commerce.Category.BAKERY, "St", 1, Municipality.AVELLANEDA, "P", "1000",
                "08:00", "20:00");
    }

    private static Pack packRef(final long id) {
        return new Pack(id, commerceRef(1L), "t", "d", 1.0, 1.0, 1, true, Collections.emptyList());
    }



    private static Pack newPack(final long id, final long commerceId, final String title, final String desc,
            final double originalPrice, final double finalPrice, final int stock, final boolean active,
            final boolean deleted, final List<ar.edu.itba.paw.models.pack.PackTag> tags, final Long imageId) {
        return new Pack(id, commerceRef(commerceId), title, desc, originalPrice, finalPrice, stock, active, deleted,
                tags, imageId != null ? new ar.edu.itba.paw.models.image.Image(imageId, new byte[0], "image/png")
                        : null);
    }

    private static Pack newPack(final long id, final long commerceId, final String title, final String desc,
            final double originalPrice, final double finalPrice, final int stock, final boolean active,
            final List<ar.edu.itba.paw.models.pack.PackTag> tags) {
        return new Pack(id, commerceRef(commerceId), title, desc, originalPrice, finalPrice, stock, active, tags);
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
                reservationTokenService,
                packService,
                notificationService,
                auctionService,
                TEST_ZONE);
    }

    @Test
    void testCreateReservationWhenDirectSaleSendsCommerceAndClientMail() {
        // 1. Setup
        final long packId = 10L;
        final long commerceUserId = 100L;
        final User clientUser = new User(1L, "user@example.org", "pwd", "Test User", null, User.Role.CLIENT, false);
        final User commerceUser = new User(commerceUserId, "commerce@example.org", "pwd", "Commerce", null, User.Role.COMMERCE, false);
        final Pack pack = newPack(packId, commerceUserId, "title", "desc", 10.0, 5.0, 5, true, false,
                Collections.emptyList(), null);
        when(userService.findById(1L)).thenReturn(Optional.of(clientUser));
        when(clientService.findByUserId(1L)).thenReturn(Optional.of(new Client(1L, "Test", "User", true)));
        when(packService.decrementStock(packId, 1)).thenReturn(true);
        when(reservationDao.findByPickupCode(anyString())).thenReturn(Optional.empty());
        lenient().when(auctionService.findByPackId(anyLong())).thenReturn(Optional.empty());
        final Reservation createdReservation = new Reservation(1L, clientRef(1L), pack, null, 5.0,
                Reservation.Status.RESERVED, "CODE1", null, 1, "pw");
        when(reservationDao.createReservation(eq(1L), eq(packId), any(LocalDateTime.class), eq(5.0),
                eq(Reservation.Status.RESERVED), anyString(), isNull(), eq(1), eq("pw")))
                .thenReturn(createdReservation);
        when(reservationDao.findByIdWithDetails(1L)).thenReturn(Optional.of(createdReservation));
        when(packService.findById(packId)).thenReturn(Optional.of(pack));
        when(userService.findById(commerceUserId)).thenReturn(Optional.of(commerceUser));
        when(reservationTokenService.create(anyString(), anyLong(), any(ReservationToken.Action.class),
                any(LocalDateTime.class), any(LocalDateTime.class))).thenAnswer(inv -> {
            final ReservationToken token = new ReservationToken(inv.getArgument(0), reservationRef(inv.getArgument(1)),
                    inv.getArgument(2), false, inv.getArgument(3), inv.getArgument(4));
            return token;
        });

        // 2. Ejercicio
        final Reservation result = reservationService.createReservation(packId, 1L, 1, 5.0, "pw", false);

        // 3. Asserts
        assertEquals(packId, result.getPackId());
        assertEquals(Reservation.Status.RESERVED, result.getStatus());
    }

    @Test
    void testCreateReservationWhenIsAuctionSendsAuctionWinnerMails() {
        // 1. Setup
        final long packId = 15L;
        final long commerceUserId = 150L;
        final User clientUser = new User(7L, "winner@example.org", "pwd", "Winning User", null, User.Role.CLIENT, false);
        final User commerceUser = new User(commerceUserId, "commerce150@example.org", "pwd", "C150", null, User.Role.COMMERCE, false);
        final Pack pack = newPack(packId, commerceUserId, "auction-pack", "desc", 10.0, 5.0, 5, true,
                Collections.emptyList());
        when(userService.findById(7L)).thenReturn(Optional.of(clientUser));
        when(clientService.findByUserId(7L)).thenReturn(Optional.of(new Client(7L, "Winning", "User", true)));
        when(packService.decrementStock(packId, 1)).thenReturn(true);
        when(reservationDao.findByPickupCode(anyString())).thenReturn(Optional.empty());
        lenient().when(auctionService.findByPackId(anyLong())).thenReturn(Optional.empty());
        final Reservation auctionReservation = new Reservation(1L, clientRef(7L), pack, null, 7.5,
                Reservation.Status.RESERVED, "CODE2", null, 1, null);
        when(reservationDao.createReservation(eq(7L), eq(packId), any(LocalDateTime.class), eq(7.5),
                eq(Reservation.Status.RESERVED), anyString(), isNull(), eq(1), isNull()))
                .thenReturn(auctionReservation);
        when(reservationDao.findByIdWithDetails(1L)).thenReturn(Optional.of(auctionReservation));
        when(packService.findById(packId)).thenReturn(Optional.of(pack));
        when(userService.findById(commerceUserId)).thenReturn(Optional.of(commerceUser));
        // 2. Ejercicio
        final Reservation result = reservationService.createReservation(packId, 7L, 1, 7.5, null, true);

        // 3. Asserts
        assertEquals(packId, result.getPackId());
        assertEquals(Reservation.Status.RESERVED, result.getStatus());
    }

    @Test
    void testCreateReservationWhenQuantityInvalidDoesNotPersistOrSendMail() {
        // 1. Setup

        // 2. Ejercicio
        final ReservationCreationException ex = assertThrows(ReservationCreationException.class,
                () -> reservationService.createReservation(20L, 2L, 0, 5.0, "pw", false));

        // 3. Asserts
        assertEquals(ReservationCreationException.Reason.INVALID_QUANTITY, ex.getReason());
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
        final ReservationCreationException ex = assertThrows(ReservationCreationException.class,
                () -> reservationService.createReservation(30L, 3L, 1, 5.0, longPickup, false));

        // 3. Asserts
        assertEquals(ReservationCreationException.Reason.PICKUP_WINDOW_TOO_LONG, ex.getReason());
    }

    @Test
    void testTryRejectReservationWhenReservedCancelsRestoresStockAndNotifiesClient() {
        // 1. Setup
        final long packId = 500L;
        final long reservationId = 42L;
        final LocalDateTime resDate = LocalDateTime.now();
        final Pack pack = newPack(packId, 100L, "t", "d", 1.0, 1.0, 1, true, false, Collections.emptyList(), null);
        final Reservation reserved = new Reservation(reservationId, clientRef(101L), pack, resDate, 25.0,
                Reservation.Status.RESERVED, "HHHHH", null, 3, null);
        final Reservation canceled = new Reservation(reservationId, clientRef(101L), pack, resDate, 25.0,
                Reservation.Status.CANCELED, "HHHHH", null, 3, null);
        when(reservationDao.findById(reservationId)).thenReturn(Optional.of(reserved));
        when(packService.incrementStock(packId, 3)).thenReturn(true);
        when(reservationDao.updateStatus(reservationId, Reservation.Status.CANCELED)).thenReturn(canceled);
        when(reservationDao.findByIdWithDetails(reservationId)).thenReturn(Optional.of(canceled));
        final User clientUser = new User(101L, "client@example.org", "pwd", "Client", null, User.Role.CLIENT, false);
        when(userService.findById(101L)).thenReturn(Optional.of(clientUser));

        // 2. Ejercicio
        final ReservationServiceResult<ReservationRejectionError> result =
                reservationService.tryRejectReservation(reservationId);

        // 3. Asserts
        assertTrue(result.isSuccess());
        assertEquals(Reservation.Status.CANCELED, result.reservation().orElseThrow().getStatus());
    }

    @Test
    void testTryRejectReservationWhenCanceledReturnsError() {
        // 1. Setup
        final long packId = 710L;
        final long reservationId = 44L;
        final Reservation reservation = new Reservation(reservationId, clientRef(104L), packRef(packId), LocalDateTime.now(), 25.0,
                Reservation.Status.CANCELED, "KKKKK", null, 1, null);
        when(reservationDao.findById(reservationId)).thenReturn(Optional.of(reservation));

        // 2. Ejercicio
        final ReservationServiceResult<ReservationRejectionError> result =
                reservationService.tryRejectReservation(reservationId);

        // 3. Asserts
        assertEquals(ReservationRejectionError.ALREADY_CANCELED, result.error().orElseThrow());
    }

    @Test
    void testTryRejectReservationWhenPaidReturnsError() {
        // 1. Setup
        final long packId = 600L;
        final long reservationId = 45L;
        final Reservation reservation = new Reservation(reservationId, clientRef(102L), packRef(packId), LocalDateTime.now(), 25.0,
                Reservation.Status.PAID, "IIIII", null, 1, null);
        when(reservationDao.findById(reservationId)).thenReturn(Optional.of(reservation));

        // 2. Ejercicio
        final ReservationServiceResult<ReservationRejectionError> result =
                reservationService.tryRejectReservation(reservationId);

        // 3. Asserts
        assertEquals(ReservationRejectionError.ALREADY_COMPLETED, result.error().orElseThrow());
    }
}
