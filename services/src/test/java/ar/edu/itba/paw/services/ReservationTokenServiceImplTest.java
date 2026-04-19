package ar.edu.itba.paw.services;

import ar.edu.itba.paw.models.Client;
import ar.edu.itba.paw.models.Commerce;
import ar.edu.itba.paw.models.Reservation;
import ar.edu.itba.paw.models.ReservationToken;
import ar.edu.itba.paw.persistence.ReservationDao;
import ar.edu.itba.paw.persistence.ReservationTokenDao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ReservationTokenServiceImplTest {

    static class InMemoryReservationTokenDao implements ReservationTokenDao {
        final Map<String, ReservationToken> store = new HashMap<>();

        @Override
        public ReservationToken create(String token, Long reservationId, ReservationToken.Action action, LocalDateTime createdAt, LocalDateTime expiresAt) {
            final ReservationToken rt = new ReservationToken(token, reservationId, action, false, createdAt, expiresAt);
            store.put(token, rt);
            return rt;
        }

        @Override
        public Optional<ReservationToken> findByToken(String token) {
            return Optional.ofNullable(store.get(token));
        }

        @Override
        public void markAsUsed(String token) {
            final ReservationToken old = store.get(token);
            if (old != null) {
                final ReservationToken nw = new ReservationToken(old.getToken(), old.getReservationId(), old.getAction(), true, old.getCreatedAt(), old.getExpiresAt());
                store.put(token, nw);
            }
        }
    }

    static class InMemoryReservationDao implements ReservationDao {
        final Map<Long, Reservation> store = new HashMap<>();

        @Override
        public Reservation createReservation(Long customerId, Long packId, LocalDateTime reservationDate, Double finalPrice, Reservation.Status status, String pickupCode, LocalDateTime pickupConfirmationDate, Integer quantity, String pickupWindow) {
            final long id = store.size() + 1;
            final Reservation r = new Reservation(id, customerId, packId, reservationDate, finalPrice, status, pickupCode, pickupConfirmationDate, quantity, pickupWindow);
            store.put(id, r);
            return r;
        }

        @Override
        public Optional<Reservation> findById(Long id) {
            return Optional.ofNullable(store.get(id));
        }

        @Override
        public java.util.List<Reservation> findByCustomerId(Long customerId) {
            return store.values().stream().filter(r -> r.getCustomerId().equals(customerId)).toList();
        }

        @Override
        public java.util.List<Reservation> findByCommerceId(Long commerceId) {
            return java.util.Collections.emptyList();
        }

        @Override
        public java.util.List<Reservation> findByPackId(Long packId) {
            return store.values().stream().filter(r -> r.getPackId().equals(packId)).toList();
        }

        @Override
        public Reservation updateStatus(Long id, Reservation.Status status) {
            final Reservation old = store.get(id);
            if (old == null) throw new IllegalStateException("not found");
            final Reservation updated = new Reservation(old.getId(), old.getCustomerId(), old.getPackId(), old.getReservationDate(), old.getFinalPrice(), status, old.getPickupCode(), old.getPickupConfirmationDate(), old.getQuantity(), old.getPickupWindow());
            store.put(id, updated);
            return updated;
        }

        @Override
        public Optional<Reservation> findByPickupCode(String pickupCode) {
            return store.values().stream().filter(r -> r.getPickupCode().equals(pickupCode)).findFirst();
        }

        @Override
        public Reservation confirmPickup(Long id, LocalDateTime pickupConfirmationDate) {
            final Reservation old = store.get(id);
            if (old == null) throw new IllegalStateException("not found");
            final Reservation updated = new Reservation(old.getId(), old.getCustomerId(), old.getPackId(), old.getReservationDate(), old.getFinalPrice(), Reservation.Status.PAID, old.getPickupCode(), pickupConfirmationDate, old.getQuantity(), old.getPickupWindow());
            store.put(id, updated);
            return updated;
        }
    }

    static class TestUserService implements UserService {
        private final ar.edu.itba.paw.models.User user;
        TestUserService(ar.edu.itba.paw.models.User user) { this.user = user; }
        @Override public ar.edu.itba.paw.models.User createUser(ar.edu.itba.paw.models.User user, Client clientProfile, Commerce commerceProfile) { return this.user; }
        @Override public Optional<ar.edu.itba.paw.models.User> upgradeProvisionalUser(ar.edu.itba.paw.models.User user, Client clientProfile, Commerce commerceProfile) { return Optional.empty(); }
        @Override public Optional<ar.edu.itba.paw.models.User> findByEmail(String email) { return Optional.of(user); }
        @Override public Optional<ar.edu.itba.paw.models.User> findById(Long id) { return Optional.of(user); }
        @Override public void updatePassword(final Long userId, final String encodedPassword) { }
        @Override public void markVerified(final Long userId) { }
    }

    static class InMemoryMailService implements ReservationMailService {
        int sentRejected = 0;
        @Override public void sendReservationRequestToCommerce(Reservation reservation, String commerceEmail, String baseUrl, String pickupDateStr) { }
        @Override public void sendReservationCodeToClient(Reservation reservation, String clientEmail, String pickupDateStr) { }
        @Override public void sendReservationRejectedToClient(Reservation reservation, String clientEmail) { sentRejected++; }
    }

    private InMemoryReservationTokenDao tokenDao;
    private InMemoryReservationDao reservationDao;
    private TestUserService userService;
    private InMemoryMailService mailService;
    private ReservationTokenServiceImpl svc;

    @BeforeEach
    public void setUp() {
        tokenDao = new InMemoryReservationTokenDao();
        reservationDao = new InMemoryReservationDao();
        userService = new TestUserService(new ar.edu.itba.paw.models.User(1L, "user@test.com", "pwd", "Test User"));
        mailService = new InMemoryMailService();
        svc = new ReservationTokenServiceImpl(tokenDao, reservationDao, userService, mailService);
    }

    // Helper to create a reservation and token
    private String createToken(long reservationId, ReservationToken.Action action, LocalDateTime expiresAt, String tokenStr) {
        final LocalDateTime now = LocalDateTime.now();
        tokenDao.create(tokenStr, reservationId, action, now, expiresAt);
        return tokenStr;
    }

    @Test
    public void validateOnly_tokenNotFound_returnsNotFound() {
        final var res = svc.validateOnly("no-token", ReservationToken.Action.ACCEPT);
        assertEquals(ReservationTokenService.TokenValidationResult.NOT_FOUND, res);
    }

    @Test
    public void validateOnly_tokenUsed_returnsAlreadyUsed() {
        final Reservation r = reservationDao.createReservation(1L, 1L, LocalDateTime.now(), 5.0, Reservation.Status.RESERVED, "c", null, 1, "pw");
        final String t = createToken(r.getId(), ReservationToken.Action.ACCEPT, LocalDateTime.now().plusHours(1), "t1");
        tokenDao.markAsUsed(t);
        final var res = svc.validateOnly(t, ReservationToken.Action.ACCEPT);
        assertEquals(ReservationTokenService.TokenValidationResult.ALREADY_USED, res);
    }

    @Test
    public void validateOnly_actionMismatch_returnsNotFound() {
        final Reservation r = reservationDao.createReservation(1L, 1L, LocalDateTime.now(), 5.0, Reservation.Status.RESERVED, "c", null, 1, "pw");
        final String t = createToken(r.getId(), ReservationToken.Action.ACCEPT, LocalDateTime.now().plusHours(1), "t2");
        final var res = svc.validateOnly(t, ReservationToken.Action.REJECT);
        assertEquals(ReservationTokenService.TokenValidationResult.NOT_FOUND, res);
    }

    @Test
    public void validateOnly_expired_returnsExpired() {
        final Reservation r = reservationDao.createReservation(1L, 1L, LocalDateTime.now(), 5.0, Reservation.Status.RESERVED, "c", null, 1, "pw");
        final String t = createToken(r.getId(), ReservationToken.Action.ACCEPT, LocalDateTime.now().minusMinutes(5), "t3");
        final var res = svc.validateOnly(t, ReservationToken.Action.ACCEPT);
        assertEquals(ReservationTokenService.TokenValidationResult.EXPIRED, res);
    }

    @Test
    public void validateAndConsume_accept_success_marksUsed_and_returnsSuccess() {
        final Reservation r = reservationDao.createReservation(1L, 1L, LocalDateTime.now(), 5.0, Reservation.Status.RESERVED, "c", null, 1, "pw");
        final String t = createToken(r.getId(), ReservationToken.Action.ACCEPT, LocalDateTime.now().plusHours(1), "t4");

        final var res = svc.validateAndConsume(t, ReservationToken.Action.ACCEPT);
        assertEquals(ReservationTokenService.TokenValidationResult.SUCCESS, res);

        final var tokenOpt = tokenDao.findByToken(t);
        assertEquals(true, tokenOpt.isPresent() && tokenOpt.get().isUsed());
        // reservation status should remain RESERVED for ACCEPT (controller will confirm pickup)
        final var resOpt = reservationDao.findById(r.getId());
        assertEquals(Reservation.Status.RESERVED, resOpt.get().getStatus());
    }

    @Test
    public void validateAndConsume_reject_success_marksUsed_and_cancelsReservation() {
        final Reservation r = reservationDao.createReservation(1L, 1L, LocalDateTime.now(), 5.0, Reservation.Status.RESERVED, "c", null, 1, "pw");
        final String t = createToken(r.getId(), ReservationToken.Action.REJECT, LocalDateTime.now().plusHours(1), "t5");

        final var res = svc.validateAndConsume(t, ReservationToken.Action.REJECT);
        assertEquals(ReservationTokenService.TokenValidationResult.SUCCESS, res);

        final var tokenOpt = tokenDao.findByToken(t);
        assertEquals(true, tokenOpt.isPresent() && tokenOpt.get().isUsed());
        final var resOpt = reservationDao.findById(r.getId());
        assertEquals(Reservation.Status.CANCELED, resOpt.get().getStatus());
    }

    @Test
    public void findReservationIdByToken_returnsReservationId() {
        final Reservation r = reservationDao.createReservation(1L, 1L, LocalDateTime.now(), 5.0, Reservation.Status.RESERVED, "c", null, 1, "pw");
        final String t = createToken(r.getId(), ReservationToken.Action.ACCEPT, LocalDateTime.now().plusHours(1), "t6");
        final var idOpt = svc.findReservationIdByToken(t);
        assertEquals(true, idOpt.isPresent() && idOpt.get().equals(r.getId()));
    }
}
