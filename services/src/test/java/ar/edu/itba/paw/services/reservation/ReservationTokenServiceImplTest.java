package ar.edu.itba.paw.services.reservation;

import ar.edu.itba.paw.models.pack.Pack;
import ar.edu.itba.paw.models.reservation.Reservation;
import ar.edu.itba.paw.models.reservation.ReservationToken;
import ar.edu.itba.paw.models.reservation.ReservationTokenActionError;
import ar.edu.itba.paw.persistence.PackDao;
import ar.edu.itba.paw.persistence.ReservationDao;
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
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReservationTokenServiceImplTest {

    @Mock
    private ReservationTokenDao reservationTokenDao;
    @Mock
    private ReservationDao reservationDao;
    @Mock
    private PackDao packDao;
    @Mock
    private ReservationService reservationService;

    @InjectMocks
    private ReservationTokenServiceImpl svc;

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
        final Reservation reservation = new Reservation(1L, 1L, 1L, now, 5.0, Reservation.Status.RESERVED, "c", null,
                1, "pw");
        final ReservationToken usedToken = new ReservationToken("t1", 1L, ReservationToken.Action.ACCEPT, true, now,
                now.plusHours(1));
        when(reservationTokenDao.findByToken("t1")).thenReturn(Optional.of(usedToken));
        when(reservationDao.findById(1L)).thenReturn(Optional.of(reservation));

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
        final Reservation reservation = new Reservation(1L, 1L, 1L, now, 5.0, Reservation.Status.RESERVED, "c", null,
                1, "pw");
        final ReservationToken token = new ReservationToken("t2", 1L, ReservationToken.Action.ACCEPT, false, now,
                now.plusHours(1));
        when(reservationTokenDao.findByToken("t2")).thenReturn(Optional.of(token));
        when(reservationDao.findById(1L)).thenReturn(Optional.of(reservation));

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
        final Reservation reservation = new Reservation(1L, 1L, 1L, now, 5.0, Reservation.Status.RESERVED, "c", null,
                1, "pw");
        final ReservationToken token = new ReservationToken("t3", 1L, ReservationToken.Action.ACCEPT, false, now,
                now.minusMinutes(5));
        when(reservationTokenDao.findByToken("t3")).thenReturn(Optional.of(token));
        when(reservationDao.findById(1L)).thenReturn(Optional.of(reservation));

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
        final ReservationToken token = new ReservationToken("t6", 7L, ReservationToken.Action.ACCEPT, false, now,
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
        final long commerceId = 801L;
        final LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        final Reservation reserved = new Reservation(201L, 201L, packId, now, 25.0, Reservation.Status.RESERVED,
                "A1B2C", null, 1, null);
        final Reservation paid = new Reservation(201L, 201L, packId, now, 25.0, Reservation.Status.PAID, "A1B2C",
                now, 1, null);
        final ReservationToken unused = new ReservationToken("accept-token", 201L, ReservationToken.Action.ACCEPT,
                false, now, now.plusHours(1));
        final AtomicReference<ReservationToken> tokenRef = new AtomicReference<>(unused);
        when(reservationTokenDao.findByToken("accept-token")).thenAnswer(inv -> Optional.of(tokenRef.get()));
        doAnswer(inv -> {
            final ReservationToken cur = tokenRef.get();
            tokenRef.set(new ReservationToken(cur.getToken(), cur.getReservationId(), cur.getAction(), true,
                    cur.getCreatedAt(), cur.getExpiresAt()));
            return null;
        }).when(reservationTokenDao).markAsUsed("accept-token");
        when(reservationDao.findById(201L)).thenReturn(Optional.of(reserved));
        when(packDao.findById(packId)).thenReturn(Optional.of(
                new Pack(packId, commerceId, "title", "desc", 10.0, 5.0, 5, true, Collections.emptyList())));
        when(reservationService.confirmPickup(201L)).thenReturn(paid);

        // 2. Ejercicio
        final ReservationServiceResult<ReservationTokenActionError> result =
                svc.acceptReservationTokenWithPickupCode("accept-token", "a1b2c", commerceId);

        // 3. Asserts
        assertTrue(result.isSuccess());
        assertEquals(Reservation.Status.PAID, result.reservation().orElseThrow().getStatus());
        assertTrue(tokenRef.get().isUsed());
    }

    @Test
    void testAcceptReservationTokenWhenInvalidPickupCodeReturnsErrorAndDoesNotConsumeToken() {
        // 1. Setup
        final long packId = 810L;
        final long commerceId = 811L;
        final LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        final Reservation reserved = new Reservation(202L, 202L, packId, now, 25.0, Reservation.Status.RESERVED,
                "Z9Y8X", null, 1, null);
        final ReservationToken token = new ReservationToken("bad-code-token", 202L, ReservationToken.Action.ACCEPT,
                false, now, now.plusHours(1));
        final AtomicReference<ReservationToken> tokenRef = new AtomicReference<>(token);
        when(reservationTokenDao.findByToken("bad-code-token")).thenAnswer(inv -> Optional.of(tokenRef.get()));
        when(reservationDao.findById(202L)).thenReturn(Optional.of(reserved));
        when(packDao.findById(packId)).thenReturn(Optional.of(
                new Pack(packId, commerceId, "title", "desc", 10.0, 5.0, 5, true, Collections.emptyList())));

        // 2. Ejercicio
        final ReservationServiceResult<ReservationTokenActionError> result =
                svc.acceptReservationTokenWithPickupCode("bad-code-token", "WRONG", commerceId);

        // 3. Asserts
        assertEquals(ReservationTokenActionError.INVALID_PICKUP_CODE, result.error().orElseThrow());
        assertEquals(Reservation.Status.RESERVED, result.reservation().orElseThrow().getStatus());
        assertFalse(tokenRef.get().isUsed());
    }

    @Test
    void testAcceptReservationTokenWhenBlankTokenReturnsInvalidTokenError() {
        // 1. Setup

        // 2. Ejercicio
        final ReservationServiceResult<ReservationTokenActionError> result =
                svc.acceptReservationTokenWithPickupCode("  ", "CODE", 1L);

        // 3. Asserts
        assertEquals(ReservationTokenActionError.INVALID_TOKEN, result.error().orElseThrow());
    }

    @Test
    void testRejectReservationTokenWhenValidTokenMarksUsedAndCancelsReservation() {
        // 1. Setup
        final long packId = 900L;
        final long commerceId = 901L;
        final LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        final Reservation reserved = new Reservation(301L, 301L, packId, now, 25.0, Reservation.Status.RESERVED,
                "R1R2R", null, 1, null);
        final Reservation canceled = new Reservation(301L, 301L, packId, now, 25.0, Reservation.Status.CANCELED,
                "R1R2R", null, 1, null);
        final ReservationToken unused = new ReservationToken("reject-token", 301L, ReservationToken.Action.REJECT,
                false, now, now.plusHours(1));
        final AtomicReference<ReservationToken> tokenRef = new AtomicReference<>(unused);
        when(reservationTokenDao.findByToken("reject-token")).thenAnswer(inv -> Optional.of(tokenRef.get()));
        doAnswer(inv -> {
            final ReservationToken cur = tokenRef.get();
            tokenRef.set(new ReservationToken(cur.getToken(), cur.getReservationId(), cur.getAction(), true,
                    cur.getCreatedAt(), cur.getExpiresAt()));
            return null;
        }).when(reservationTokenDao).markAsUsed("reject-token");
        when(reservationDao.findById(301L)).thenReturn(Optional.of(reserved), Optional.of(reserved),
                Optional.of(canceled));
        when(packDao.findById(packId)).thenReturn(Optional.of(
                new Pack(packId, commerceId, "title", "desc", 10.0, 5.0, 5, true, Collections.emptyList())));
        when(reservationService.rejectReservation(301L)).thenReturn(canceled);

        // 2. Ejercicio
        final ReservationServiceResult<ReservationTokenActionError> result =
                svc.rejectReservationToken("reject-token", commerceId);

        // 3. Asserts
        assertTrue(result.isSuccess());
        assertEquals(Reservation.Status.CANCELED, result.reservation().orElseThrow().getStatus());
        assertTrue(tokenRef.get().isUsed());
    }

    @Test
    void testRejectReservationTokenWhenWrongCommerceReturnsErrorAndDoesNotConsumeToken() {
        // 1. Setup
        final long packId = 910L;
        final long ownerCommerceId = 911L;
        final long otherCommerceId = 999L;
        final LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        final Reservation reserved = new Reservation(302L, 302L, packId, now, 25.0, Reservation.Status.RESERVED,
                "S1S2S", null, 1, null);
        final ReservationToken token = new ReservationToken("reject-wrong-commerce", 302L,
                ReservationToken.Action.REJECT, false, now, now.plusHours(1));
        final AtomicReference<ReservationToken> tokenRef = new AtomicReference<>(token);
        when(reservationTokenDao.findByToken("reject-wrong-commerce")).thenAnswer(inv -> Optional.of(tokenRef.get()));
        when(reservationDao.findById(302L)).thenReturn(Optional.of(reserved));
        when(packDao.findById(packId)).thenReturn(Optional.of(
                new Pack(packId, ownerCommerceId, "title", "desc", 10.0, 5.0, 5, true, Collections.emptyList())));

        // 2. Ejercicio
        final ReservationServiceResult<ReservationTokenActionError> result =
                svc.rejectReservationToken("reject-wrong-commerce", otherCommerceId);

        // 3. Asserts
        assertEquals(ReservationTokenActionError.WRONG_COMMERCE, result.error().orElseThrow());
        assertFalse(tokenRef.get().isUsed());
    }

    @Test
    void testRejectReservationTokenWhenExpiredReturnsErrorAndDoesNotConsumeToken() {
        // 1. Setup
        final long packId = 920L;
        final long commerceId = 921L;
        final LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        final Reservation reserved = new Reservation(303L, 303L, packId, now, 25.0, Reservation.Status.RESERVED,
                "T1T2T", null, 1, null);
        final ReservationToken token = new ReservationToken("reject-expired", 303L, ReservationToken.Action.REJECT,
                false, now, now.minusHours(1));
        final AtomicReference<ReservationToken> tokenRef = new AtomicReference<>(token);
        when(reservationTokenDao.findByToken("reject-expired")).thenAnswer(inv -> Optional.of(tokenRef.get()));
        when(reservationDao.findById(303L)).thenReturn(Optional.of(reserved));

        // 2. Ejercicio
        final ReservationServiceResult<ReservationTokenActionError> result =
                svc.rejectReservationToken("reject-expired", commerceId);

        // 3. Asserts
        assertEquals(ReservationTokenActionError.EXPIRED, result.error().orElseThrow());
        assertFalse(tokenRef.get().isUsed());
    }

    @Test
    void testRejectReservationTokenWhenReservationAlreadyCanceledReturnsAlreadyUsedError() {
        // 1. Setup
        final long packId = 930L;
        final long commerceId = 931L;
        final LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        final Reservation canceledReservation = new Reservation(304L, 304L, packId, now, 25.0,
                Reservation.Status.CANCELED, "U1U2U", null, 1, null);
        final ReservationToken token = new ReservationToken("reject-canceled", 304L, ReservationToken.Action.REJECT,
                false, now, now.plusHours(1));
        final AtomicReference<ReservationToken> tokenRef = new AtomicReference<>(token);
        when(reservationTokenDao.findByToken("reject-canceled")).thenAnswer(inv -> Optional.of(tokenRef.get()));
        when(reservationDao.findById(304L)).thenReturn(Optional.of(canceledReservation));

        // 2. Ejercicio
        final ReservationServiceResult<ReservationTokenActionError> result =
                svc.rejectReservationToken("reject-canceled", commerceId);

        // 3. Asserts
        assertEquals(ReservationTokenActionError.ALREADY_USED, result.error().orElseThrow());
        assertFalse(tokenRef.get().isUsed());
    }

    @Test
    void testRejectReservationTokenWhenBlankTokenReturnsInvalidTokenError() {
        // 1. Setup

        // 2. Ejercicio
        final ReservationServiceResult<ReservationTokenActionError> result =
                svc.rejectReservationToken("  ", 1L);

        // 3. Asserts
        assertEquals(ReservationTokenActionError.INVALID_TOKEN, result.error().orElseThrow());
    }
}
