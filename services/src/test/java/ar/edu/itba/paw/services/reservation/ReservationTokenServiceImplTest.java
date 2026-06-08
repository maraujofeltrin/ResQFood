package ar.edu.itba.paw.services.reservation;

import ar.edu.itba.paw.models.pack.Municipality;
import ar.edu.itba.paw.models.pack.Pack;
import ar.edu.itba.paw.models.user.Client;
import ar.edu.itba.paw.models.user.Commerce;
import ar.edu.itba.paw.models.reservation.Reservation;
import ar.edu.itba.paw.models.reservation.ReservationToken;
import ar.edu.itba.paw.models.reservation.ReservationTokenActionError;
import ar.edu.itba.paw.persistence.ReservationTokenDao;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReservationTokenServiceImplTest {

    @Mock
    private ReservationTokenDao reservationTokenDao;
    @Mock
    private ReservationService reservationService;

    @InjectMocks
    private ReservationTokenServiceImpl svc;

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

    private static Reservation reservationRef(final long id) {
        return new Reservation(id, clientRef(1L), packRef(1L), null, null, null, null, null, 1, null);
    }

    @Test
    void testValidateOnlyWhenTokenNotFoundReturnsNotFound() {
        // 1. Setup
        when(reservationTokenDao.findByToken("no-token")).thenReturn(Optional.empty());

        // 2. Ejercicio
        final ReservationTokenService.TokenValidationResult res =
                svc.validateOnly("no-token", ReservationToken.Action.ACCEPT);

        // 3. Asserts
        assertEquals(ReservationTokenService.TokenValidationResult.NOT_FOUND, res);
    }

    @Test
    void testValidateOnlyWhenTokenUsedReturnsAlreadyUsed() {
        // 1. Setup
        final LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        final Reservation reservation = new Reservation(1L, clientRef(1L), packRef(1L), now, 5.0, Reservation.Status.RESERVED, "c", null,
                1, "pw");
        final ReservationToken usedToken = new ReservationToken("t1", reservation, ReservationToken.Action.ACCEPT, true, now,
                now.plusHours(1));
        when(reservationTokenDao.findByToken("t1")).thenReturn(Optional.of(usedToken));

        // 2. Ejercicio
        final ReservationTokenService.TokenValidationResult res =
                svc.validateOnly("t1", ReservationToken.Action.ACCEPT);

        // 3. Asserts
        assertEquals(ReservationTokenService.TokenValidationResult.ALREADY_USED, res);
    }

    @Test
    void testValidateOnlyWhenActionMismatchReturnsNotFound() {
        // 1. Setup
        final LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        final Reservation reservation = new Reservation(1L, clientRef(1L), packRef(1L), now, 5.0, Reservation.Status.RESERVED, "c", null,
                1, "pw");
        final ReservationToken token = new ReservationToken("t2", reservation, ReservationToken.Action.ACCEPT, false, now,
                now.plusHours(1));
        when(reservationTokenDao.findByToken("t2")).thenReturn(Optional.of(token));

        // 2. Ejercicio
        final ReservationTokenService.TokenValidationResult res =
                svc.validateOnly("t2", ReservationToken.Action.REJECT);

        // 3. Asserts
        assertEquals(ReservationTokenService.TokenValidationResult.NOT_FOUND, res);
    }

    @Test
    void testValidateOnlyWhenExpiredReturnsExpired() {
        // 1. Setup
        final LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        final Reservation reservation = new Reservation(1L, clientRef(1L), packRef(1L), now, 5.0, Reservation.Status.RESERVED, "c", null,
                1, "pw");
        final ReservationToken token = new ReservationToken("t3", reservation, ReservationToken.Action.ACCEPT, false, now,
                now.minusMinutes(5));
        when(reservationTokenDao.findByToken("t3")).thenReturn(Optional.of(token));

        // 2. Ejercicio
        final ReservationTokenService.TokenValidationResult res =
                svc.validateOnly("t3", ReservationToken.Action.ACCEPT);

        // 3. Asserts
        assertEquals(ReservationTokenService.TokenValidationResult.EXPIRED, res);
    }

    @Test
    void testFindReservationIdByTokenWhenTokenExistsReturnsId() {
        // 1. Setup
        final LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        final ReservationToken token = new ReservationToken("t6", reservationRef(7L), ReservationToken.Action.ACCEPT, false, now,
                now.plusHours(1));
        when(reservationTokenDao.findByToken("t6")).thenReturn(Optional.of(token));

        // 2. Ejercicio
        final Optional<Long> idOpt = svc.findReservationIdByToken("t6");

        // 3. Asserts
        assertTrue(idOpt.isPresent());
        assertEquals(7L, idOpt.get());
    }

    @Test
    void testAcceptReservationTokenWhenValidCodeMarksUsedAndConfirmsPickup() {
        // 1. Setup
        final long packId = 800L;
        final LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        final Reservation reserved = new Reservation(201L, clientRef(201L), packRef(packId), now, 25.0, Reservation.Status.RESERVED,
                "A1B2C", null, 1, null);
        final Reservation paid = new Reservation(201L, clientRef(201L), packRef(packId), now, 25.0, Reservation.Status.PAID, "A1B2C",
                now, 1, null);
        final ReservationToken unused = new ReservationToken("accept-token", reserved, ReservationToken.Action.ACCEPT,
                false, now, now.plusHours(1));
        when(reservationTokenDao.findByToken("accept-token")).thenReturn(Optional.of(unused));
        when(reservationService.confirmPickup(201L)).thenReturn(paid);

        // 2. Ejercicio
        final ReservationServiceResult<ReservationTokenActionError> result =
                svc.acceptReservationTokenWithPickupCode("accept-token", "a1b2c");

        // 3. Asserts
        assertTrue(result.isSuccess());
        assertEquals(Reservation.Status.PAID, result.reservation().orElseThrow().getStatus());
    }

    @Test
    void testAcceptReservationTokenWhenInvalidPickupCodeReturnsErrorAndDoesNotConsumeToken() {
        // 1. Setup
        final long packId = 810L;
        final LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        final Reservation reserved = new Reservation(202L, clientRef(202L), packRef(packId), now, 25.0, Reservation.Status.RESERVED,
                "Z9Y8X", null, 1, null);
        final ReservationToken token = new ReservationToken("bad-code-token", reserved, ReservationToken.Action.ACCEPT,
                false, now, now.plusHours(1));
        when(reservationTokenDao.findByToken("bad-code-token")).thenReturn(Optional.of(token));

        // 2. Ejercicio
        final ReservationServiceResult<ReservationTokenActionError> result =
                svc.acceptReservationTokenWithPickupCode("bad-code-token", "WRONG");

        // 3. Asserts
        assertEquals(ReservationTokenActionError.INVALID_PICKUP_CODE, result.error().orElseThrow());
        assertEquals(Reservation.Status.RESERVED, result.reservation().orElseThrow().getStatus());
    }

    @Test
    void testAcceptReservationTokenWhenBlankTokenReturnsInvalidTokenError() {
        // 1. Setup

        // 2. Ejercicio
        final ReservationServiceResult<ReservationTokenActionError> result =
                svc.acceptReservationTokenWithPickupCode("  ", "CODE");

        // 3. Asserts
        assertEquals(ReservationTokenActionError.INVALID_TOKEN, result.error().orElseThrow());
    }

    @Test
    void testRejectReservationTokenWhenValidTokenMarksUsedAndCancelsReservation() {
        // 1. Setup
        final long packId = 900L;
        final LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        final Reservation reserved = new Reservation(301L, clientRef(301L), packRef(packId), now, 25.0, Reservation.Status.RESERVED,
                "R1R2R", null, 1, null);
        final Reservation canceled = new Reservation(301L, clientRef(301L), packRef(packId), now, 25.0, Reservation.Status.CANCELED,
                "R1R2R", null, 1, null);
        final ReservationToken unused = new ReservationToken("reject-token", reserved, ReservationToken.Action.REJECT,
                false, now, now.plusHours(1));
        when(reservationTokenDao.findByToken("reject-token")).thenReturn(Optional.of(unused));
        when(reservationService.rejectReservation(301L)).thenReturn(canceled);

        // 2. Ejercicio
        final ReservationServiceResult<ReservationTokenActionError> result =
                svc.rejectReservationToken("reject-token");

        // 3. Asserts
        assertTrue(result.isSuccess());
        assertEquals(Reservation.Status.CANCELED, result.reservation().orElseThrow().getStatus());
    }

    @Test
    void testRejectReservationTokenWhenExpiredReturnsErrorAndDoesNotConsumeToken() {
        // 1. Setup
        final long packId = 920L;
        final LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        final Reservation reserved = new Reservation(303L, clientRef(303L), packRef(packId), now, 25.0, Reservation.Status.RESERVED,
                "T1T2T", null, 1, null);
        final ReservationToken token = new ReservationToken("reject-expired", reserved, ReservationToken.Action.REJECT,
                false, now, now.minusHours(1));
        when(reservationTokenDao.findByToken("reject-expired")).thenReturn(Optional.of(token));

        // 2. Ejercicio
        final ReservationServiceResult<ReservationTokenActionError> result =
                svc.rejectReservationToken("reject-expired");

        // 3. Asserts
        assertEquals(ReservationTokenActionError.EXPIRED, result.error().orElseThrow());
    }

    @Test
    void testRejectReservationTokenWhenReservationAlreadyCanceledReturnsAlreadyUsedError() {
        // 1. Setup
        final long packId = 930L;
        final LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        final Reservation canceledReservation = new Reservation(304L, clientRef(304L), packRef(packId), now, 25.0,
                Reservation.Status.CANCELED, "U1U2U", null, 1, null);
        final ReservationToken token = new ReservationToken("reject-canceled", canceledReservation, ReservationToken.Action.REJECT,
                false, now, now.plusHours(1));
        when(reservationTokenDao.findByToken("reject-canceled")).thenReturn(Optional.of(token));

        // 2. Ejercicio
        final ReservationServiceResult<ReservationTokenActionError> result =
                svc.rejectReservationToken("reject-canceled");

        // 3. Asserts
        assertEquals(ReservationTokenActionError.ALREADY_USED, result.error().orElseThrow());
    }

    @Test
    void testRejectReservationTokenWhenBlankTokenReturnsInvalidTokenError() {
        // 1. Setup

        // 2. Ejercicio
        final ReservationServiceResult<ReservationTokenActionError> result =
                svc.rejectReservationToken("  ");

        // 3. Asserts
        assertEquals(ReservationTokenActionError.INVALID_TOKEN, result.error().orElseThrow());
    }
}
