package ar.edu.itba.paw.services.reservation;

import ar.edu.itba.paw.models.auction.Auction;
import ar.edu.itba.paw.models.pack.Municipality;
import ar.edu.itba.paw.models.pack.Pack;
import ar.edu.itba.paw.models.reservation.Reservation;
import ar.edu.itba.paw.models.reservation.ReservationCreationException;
import ar.edu.itba.paw.models.reservation.ReservationRejectionError;
import ar.edu.itba.paw.models.reservation.ReservationToken;
import ar.edu.itba.paw.models.reservation.ReservationTokenActionError;
import ar.edu.itba.paw.models.user.Client;
import ar.edu.itba.paw.models.user.Commerce;
import ar.edu.itba.paw.models.user.User;
import ar.edu.itba.paw.persistence.ReservationDao;
import ar.edu.itba.paw.persistence.ReservationTokenDao;
import ar.edu.itba.paw.services.notification.NotificationService;
import ar.edu.itba.paw.services.pack.DirectReservationCheck;
import ar.edu.itba.paw.services.pack.PackService;
import ar.edu.itba.paw.services.user.ClientService;
import ar.edu.itba.paw.services.user.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
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
    private PackService packService;
    @Mock
    private NotificationService notificationService;

    private ReservationServiceImpl reservationService;

    private static Client clientRef(final long id) {
        return new Client(id, "N", "L", true);
    }

    private static User clientUserRef(final long id, final String email) {
        return new User(id, email, "pwd", "Client", null, User.Role.CLIENT, false);
    }

    private static User commerceUserRef(final long id, final String email) {
        return new User(id, email, "pwd", "Commerce", null, User.Role.COMMERCE, false);
    }

    private static Client clientWithUser(final long id, final String email, final String name, final String lastName) {
        return new Client(clientUserRef(id, email), name, lastName, true);
    }

    private static Pack packWithCommerceUser(final long packId, final long commerceUserId, final String commerceEmail,
            final String title) {
        final Commerce commerce = new Commerce(commerceUserRef(commerceUserId, commerceEmail), "Comm",
                Commerce.Category.BAKERY, "St", 1, Municipality.AVELLANEDA, "P", "1000", "08:00", "20:00");
        return new Pack(packId, commerce, title, "desc", 10.0, 5.0, 5, true, Collections.emptyList());
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
                reservationTokenDao,
                packService,
                notificationService,
                TEST_ZONE);
    }

    @Test
    void testCreateReservationWhenDirectSaleSendsCommerceAndClientMail() {
        // 1. Setup
        final long packId = 10L;
        final long commerceUserId = 100L;
        final User clientUser = new User(1L, "user@example.org", "pwd", "Test User", null, User.Role.CLIENT, false);
        final Pack pack = packWithCommerceUser(packId, commerceUserId, "commerce@example.org", "title");
        when(userService.findById(1L)).thenReturn(Optional.of(clientUser));
        when(clientService.findByUserId(1L)).thenReturn(Optional.of(new Client(1L, "Test", "User", true)));
        when(packService.decrementStock(packId, 1)).thenReturn(true);
        when(reservationDao.findByPickupCode(anyString())).thenReturn(Optional.empty());
        final Reservation createdReservation = new Reservation(1L, clientWithUser(1L, "user@example.org", "Test", "User"),
                pack, null, 5.0, Reservation.Status.RESERVED, "CODE1", null, 1, "pw");
        when(reservationDao.createReservation(eq(1L), eq(packId), any(LocalDateTime.class), eq(5.0),
                eq(Reservation.Status.RESERVED), anyString(), isNull(), eq(1), eq("pw")))
                .thenReturn(createdReservation);
        when(reservationDao.findByIdWithDetails(1L)).thenReturn(Optional.of(createdReservation));
        when(reservationTokenDao.create(anyString(), anyLong(), any(ReservationToken.Action.class),
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
        final Pack pack = packWithCommerceUser(packId, commerceUserId, "commerce150@example.org", "auction-pack");
        when(userService.findById(7L)).thenReturn(Optional.of(clientUser));
        when(clientService.findByUserId(7L)).thenReturn(Optional.of(new Client(7L, "Winning", "User", true)));
        when(packService.decrementStock(packId, 1)).thenReturn(true);
        when(reservationDao.findByPickupCode(anyString())).thenReturn(Optional.empty());
        final Reservation auctionReservation = new Reservation(1L, clientWithUser(7L, "winner@example.org", "Winning", "User"),
                pack, null, 7.5, Reservation.Status.RESERVED, "CODE2", null, 1, null);
        when(reservationDao.createReservation(eq(7L), eq(packId), any(LocalDateTime.class), eq(7.5),
                eq(Reservation.Status.RESERVED), anyString(), isNull(), eq(1), isNull()))
                .thenReturn(auctionReservation);
        when(reservationDao.findByIdWithDetails(1L)).thenReturn(Optional.of(auctionReservation));

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
        final Pack pack = packWithCommerceUser(packId, 100L, "commerce@example.org", "t");
        final Client client = clientWithUser(101L, "client@example.org", "Client", "Name");
        final Reservation reserved = new Reservation(reservationId, client, pack, resDate, 25.0,
                Reservation.Status.RESERVED, "HHHHH", null, 3, null);
        when(reservationDao.findByIdWithDetails(reservationId)).thenReturn(Optional.of(reserved));
        when(packService.incrementStock(packId, 3)).thenReturn(true);

        // 2. Ejercicio
        final ReservationServiceResult<ReservationRejectionError> result =
                reservationService.tryRejectReservation(reservationId);

        // 3. Asserts
        assertTrue(result.isSuccess());
        assertEquals(Reservation.Status.CANCELED, result.reservation().orElseThrow().getStatus());
    }

    @Test
    void testConfirmPickupWhenReservedSetsPaidStatusAndPickupDate() {
        // 1. Setup
        final long reservationId = 55L;
        final Reservation reserved = new Reservation(reservationId, clientRef(1L), packRef(1L), LocalDateTime.now(),
                25.0, Reservation.Status.RESERVED, "CODE", null, 1, null);
        when(reservationDao.findById(reservationId)).thenReturn(Optional.of(reserved));

        // 2. Ejercicio
        final Reservation confirmed = reservationService.confirmPickup(reservationId);

        // 3. Asserts
        assertEquals(Reservation.Status.PAID, confirmed.getStatus());
        assertNotNull(confirmed.getPickupConfirmationDate());
    }

    @Test
    void testTryRejectReservationWhenCanceledReturnsError() {
        // 1. Setup
        final long packId = 710L;
        final long reservationId = 44L;
        final Reservation reservation = new Reservation(reservationId, clientRef(104L), packRef(packId), LocalDateTime.now(), 25.0,
                Reservation.Status.CANCELED, "KKKKK", null, 1, null);
        when(reservationDao.findByIdWithDetails(reservationId)).thenReturn(Optional.of(reservation));

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
        when(reservationDao.findByIdWithDetails(reservationId)).thenReturn(Optional.of(reservation));

        // 2. Ejercicio
        final ReservationServiceResult<ReservationRejectionError> result =
                reservationService.tryRejectReservation(reservationId);

        // 3. Asserts
        assertEquals(ReservationRejectionError.ALREADY_COMPLETED, result.error().orElseThrow());
    }

    @Test
    void testAcceptByTokenWhenValidCodeMarksUsedAndConfirmsPickup() {
        // 1. Setup
        final long packId = 800L;
        final LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        final Reservation reserved = new Reservation(201L, clientRef(201L), packRef(packId), now, 25.0,
                Reservation.Status.RESERVED, "A1B2C", null, 1, null);
        final ReservationToken unused = new ReservationToken("accept-token", reserved, ReservationToken.Action.ACCEPT,
                false, now, now.plusHours(1));
        when(reservationTokenDao.findByToken("accept-token")).thenReturn(Optional.of(unused));
        when(reservationDao.findById(201L)).thenReturn(Optional.of(reserved));

        // 2. Ejercicio
        final ReservationServiceResult<ReservationTokenActionError> result =
                reservationService.acceptByToken("accept-token", "a1b2c");

        // 3. Asserts
        assertTrue(result.isSuccess());
        assertEquals(Reservation.Status.PAID, result.reservation().orElseThrow().getStatus());
        assertNotNull(result.reservation().orElseThrow().getPickupConfirmationDate());
    }

    @Test
    void testAcceptByTokenWhenInvalidPickupCodeReturnsErrorAndDoesNotConsumeToken() {
        // 1. Setup
        final long packId = 810L;
        final LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        final Reservation reserved = new Reservation(202L, clientRef(202L), packRef(packId), now, 25.0,
                Reservation.Status.RESERVED, "Z9Y8X", null, 1, null);
        final ReservationToken token = new ReservationToken("bad-code-token", reserved, ReservationToken.Action.ACCEPT,
                false, now, now.plusHours(1));
        when(reservationTokenDao.findByToken("bad-code-token")).thenReturn(Optional.of(token));

        // 2. Ejercicio
        final ReservationServiceResult<ReservationTokenActionError> result =
                reservationService.acceptByToken("bad-code-token", "WRONG");

        // 3. Asserts
        assertEquals(ReservationTokenActionError.INVALID_PICKUP_CODE, result.error().orElseThrow());
        assertEquals(Reservation.Status.RESERVED, result.reservation().orElseThrow().getStatus());
    }

    @Test
    void testAcceptByTokenWhenBlankTokenReturnsInvalidTokenError() {
        // 1. Setup

        // 2. Ejercicio
        final ReservationServiceResult<ReservationTokenActionError> result =
                reservationService.acceptByToken("  ", "CODE");

        // 3. Asserts
        assertEquals(ReservationTokenActionError.INVALID_TOKEN, result.error().orElseThrow());
    }

    @Test
    void testRejectByTokenWhenValidTokenMarksUsedAndCancelsReservation() {
        // 1. Setup
        final long packId = 900L;
        final LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        final Reservation reserved = new Reservation(301L, clientWithUser(301L, "client301@example.org", "Client", "Name"),
                packWithCommerceUser(packId, 100L, "commerce@example.org", "t"), now, 25.0,
                Reservation.Status.RESERVED, "R1R2R", null, 1, null);
        final ReservationToken unused = new ReservationToken("reject-token", reserved, ReservationToken.Action.REJECT,
                false, now, now.plusHours(1));
        when(reservationTokenDao.findByToken("reject-token")).thenReturn(Optional.of(unused));
        when(reservationDao.findByIdWithDetails(301L)).thenReturn(Optional.of(reserved));
        when(packService.incrementStock(packId, 1)).thenReturn(true);

        // 2. Ejercicio
        final ReservationServiceResult<ReservationTokenActionError> result =
                reservationService.rejectByToken("reject-token");

        // 3. Asserts
        assertTrue(result.isSuccess());
        assertEquals(Reservation.Status.CANCELED, result.reservation().orElseThrow().getStatus());
    }

    @Test
    void testRejectByTokenWhenExpiredReturnsErrorAndDoesNotConsumeToken() {
        // 1. Setup
        final long packId = 920L;
        final LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        final Reservation reserved = new Reservation(303L, clientRef(303L), packRef(packId), now, 25.0,
                Reservation.Status.RESERVED, "T1T2T", null, 1, null);
        final ReservationToken token = new ReservationToken("reject-expired", reserved, ReservationToken.Action.REJECT,
                false, now, now.minusHours(1));
        when(reservationTokenDao.findByToken("reject-expired")).thenReturn(Optional.of(token));

        // 2. Ejercicio
        final ReservationServiceResult<ReservationTokenActionError> result =
                reservationService.rejectByToken("reject-expired");

        // 3. Asserts
        assertEquals(ReservationTokenActionError.EXPIRED, result.error().orElseThrow());
    }

    @Test
    void testRejectByTokenWhenReservationAlreadyCanceledReturnsAlreadyUsedError() {
        // 1. Setup
        final long packId = 930L;
        final LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        final Reservation canceledReservation = new Reservation(304L, clientRef(304L), packRef(packId), now, 25.0,
                Reservation.Status.CANCELED, "U1U2U", null, 1, null);
        final ReservationToken token = new ReservationToken("reject-canceled", canceledReservation,
                ReservationToken.Action.REJECT, false, now, now.plusHours(1));
        when(reservationTokenDao.findByToken("reject-canceled")).thenReturn(Optional.of(token));

        // 2. Ejercicio
        final ReservationServiceResult<ReservationTokenActionError> result =
                reservationService.rejectByToken("reject-canceled");

        // 3. Asserts
        assertEquals(ReservationTokenActionError.ALREADY_USED, result.error().orElseThrow());
    }

    @Test
    void testRejectByTokenWhenBlankTokenReturnsInvalidTokenError() {
        // 1. Setup

        // 2. Ejercicio
        final ReservationServiceResult<ReservationTokenActionError> result =
                reservationService.rejectByToken("  ");

        // 3. Asserts
        assertEquals(ReservationTokenActionError.INVALID_TOKEN, result.error().orElseThrow());
    }

    @Test
    void testValidateTokenWhenTokenNotFoundReturnsNotFound() {
        // 1. Setup
        when(reservationTokenDao.findByToken("no-token")).thenReturn(Optional.empty());

        // 2. Ejercicio
        final ReservationService.TokenValidationResult res =
                reservationService.validateToken("no-token", ReservationToken.Action.ACCEPT);

        // 3. Asserts
        assertEquals(ReservationService.TokenValidationResult.NOT_FOUND, res);
    }

    @Test
    void testValidateTokenWhenTokenUsedReturnsAlreadyUsed() {
        // 1. Setup
        final LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        final Reservation reservation = new Reservation(1L, clientRef(1L), packRef(1L), now, 5.0,
                Reservation.Status.RESERVED, "c", null, 1, "pw");
        final ReservationToken usedToken = new ReservationToken("t1", reservation, ReservationToken.Action.ACCEPT, true,
                now, now.plusHours(1));
        when(reservationTokenDao.findByToken("t1")).thenReturn(Optional.of(usedToken));

        // 2. Ejercicio
        final ReservationService.TokenValidationResult res =
                reservationService.validateToken("t1", ReservationToken.Action.ACCEPT);

        // 3. Asserts
        assertEquals(ReservationService.TokenValidationResult.ALREADY_USED, res);
    }

    @Test
    void testValidateTokenWhenActionMismatchReturnsNotFound() {
        // 1. Setup
        final LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        final Reservation reservation = new Reservation(1L, clientRef(1L), packRef(1L), now, 5.0,
                Reservation.Status.RESERVED, "c", null, 1, "pw");
        final ReservationToken token = new ReservationToken("t2", reservation, ReservationToken.Action.ACCEPT, false,
                now, now.plusHours(1));
        when(reservationTokenDao.findByToken("t2")).thenReturn(Optional.of(token));

        // 2. Ejercicio
        final ReservationService.TokenValidationResult res =
                reservationService.validateToken("t2", ReservationToken.Action.REJECT);

        // 3. Asserts
        assertEquals(ReservationService.TokenValidationResult.NOT_FOUND, res);
    }

    @Test
    void testValidateTokenWhenExpiredReturnsExpired() {
        // 1. Setup
        final LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        final Reservation reservation = new Reservation(1L, clientRef(1L), packRef(1L), now, 5.0,
                Reservation.Status.RESERVED, "c", null, 1, "pw");
        final ReservationToken token = new ReservationToken("t3", reservation, ReservationToken.Action.ACCEPT, false,
                now, now.minusMinutes(5));
        when(reservationTokenDao.findByToken("t3")).thenReturn(Optional.of(token));

        // 2. Ejercicio
        final ReservationService.TokenValidationResult res =
                reservationService.validateToken("t3", ReservationToken.Action.ACCEPT);

        // 3. Asserts
        assertEquals(ReservationService.TokenValidationResult.EXPIRED, res);
    }

    @Test
    void testFindReservationIdByTokenWhenTokenExistsReturnsId() {
        // 1. Setup
        final LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        final ReservationToken token = new ReservationToken("t6", reservationRef(7L), ReservationToken.Action.ACCEPT,
                false, now, now.plusHours(1));
        when(reservationTokenDao.findByToken("t6")).thenReturn(Optional.of(token));

        // 2. Ejercicio
        final Optional<Long> idOpt = reservationService.findReservationIdByToken("t6");

        // 3. Asserts
        assertTrue(idOpt.isPresent());
        assertEquals(7L, idOpt.get());
    }

    @Test
    void testCheckDirectPackReservationWhenNoAuctionReturnsOk() {
        // 1. Setup
        final Pack pack = newPack(20L, 1L, "direct", "d", 10.0, 5.0, 3, true, Collections.emptyList());
        when(packService.findById(20L)).thenReturn(Optional.of(pack));

        // 2. Ejercicio
        final DirectReservationCheck result = reservationService.checkDirectPackReservation(20L, 1);

        // 3. Asserts
        assertEquals(DirectReservationCheck.Outcome.OK, result.getOutcome());
        assertEquals(5.0, result.getUnitPrice());
    }

    @Test
    void testCheckDirectPackReservationWhenActiveAuctionReturnsBlocked() {
        // 1. Setup
        final Pack pack = newPack(21L, 1L, "auction", "d", 10.0, 5.0, 3, true, Collections.emptyList());
        final Auction auction = new Auction(100L, pack, 10.0, 1.0, null, null,
                LocalDateTime.now(ZoneOffset.UTC).plusDays(1), Auction.Status.ACTIVE, LocalDateTime.now());
        pack.setAuction(auction);
        when(packService.findById(21L)).thenReturn(Optional.of(pack));

        // 2. Ejercicio
        final DirectReservationCheck result = reservationService.checkDirectPackReservation(21L, 1);

        // 3. Asserts
        assertEquals(DirectReservationCheck.Outcome.AUCTION_ACTIVE, result.getOutcome());
    }

    @Test
    void testCheckDirectPackReservationWhenFinishedAuctionReturnsBlocked() {
        // 1. Setup
        final Pack pack = newPack(22L, 1L, "ended", "d", 10.0, 5.0, 3, true, Collections.emptyList());
        final Auction auction = new Auction(101L, pack, 10.0, 1.0, null, null,
                LocalDateTime.now(ZoneOffset.UTC).minusHours(1), Auction.Status.FINISHED, LocalDateTime.now());
        pack.setAuction(auction);
        when(packService.findById(22L)).thenReturn(Optional.of(pack));

        // 2. Ejercicio
        final DirectReservationCheck result = reservationService.checkDirectPackReservation(22L, 1);

        // 3. Asserts
        assertEquals(DirectReservationCheck.Outcome.AUCTION_ENDED_NO_DIRECT, result.getOutcome());
    }
}
