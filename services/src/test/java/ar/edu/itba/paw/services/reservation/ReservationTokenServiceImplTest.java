package ar.edu.itba.paw.services.reservation;

import ar.edu.itba.paw.models.user.Client;
import ar.edu.itba.paw.models.user.Commerce;
import ar.edu.itba.paw.models.pack.Pack;
import ar.edu.itba.paw.models.pack.PackSortOption;
import ar.edu.itba.paw.models.pack.PackTag;
import ar.edu.itba.paw.models.reservation.Reservation;
import ar.edu.itba.paw.models.reservation.ReservationToken;
import ar.edu.itba.paw.persistence.PackDao;
import ar.edu.itba.paw.persistence.ReservationDao;
import ar.edu.itba.paw.persistence.ReservationTokenDao;
import ar.edu.itba.paw.services.user.RegisterResult;
import ar.edu.itba.paw.services.user.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
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
        public List<Reservation> findByCustomerId(Long customerId) {
            return store.values().stream().filter(r -> r.getCustomerId().equals(customerId)).toList();
        }

        @Override
        public List<Reservation> findByCommerceId(Long commerceId) {
            return Collections.emptyList();
        }

        @Override
        public List<Reservation> findByPackId(Long packId) {
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

        @Override
        public List<Reservation> filterReservations(Long commerceId, Long customerId, String query, Reservation.Status status, int page, int pageSize) {
            return Collections.emptyList();
        }

        @Override
        public int countFilteredReservations(Long commerceId, Long customerId, String query, Reservation.Status status) {
            return 0;
        }

        @Override
        public boolean hasActiveReservation(Long packId, Long customerId) {
            return false;
        }

        @Override
        public int countPaidReservationsInPeriod(Long commerceId, LocalDateTime periodStart, LocalDateTime periodEnd) {
            return 0;
        }

        @Override
        public List<Object[]> countPaidReservationsPerDay(Long commerceId, LocalDateTime from, LocalDateTime to) {
            return Collections.emptyList();
        }

        @Override
        public BigDecimal sumRevenueInPeriod(Long commerceId, LocalDateTime from, LocalDateTime to) {
            return BigDecimal.ZERO;
        }

        @Override
        public Optional<Long> findBestSellingPackId(Long commerceId, LocalDateTime from, LocalDateTime to) {
            return Optional.empty();
        }

        @Override
        public long countByStatusInPeriod(Long commerceId, Reservation.Status status, LocalDateTime from, LocalDateTime to) {
            return 0L;
        }
    }

    static class StubReservationService implements ReservationService {
        int rejectCalls = 0;
        private final ReservationDao reservationDao;
        private final PackDao packDao;

        StubReservationService(ReservationDao reservationDao, PackDao packDao) {
            this.reservationDao = reservationDao;
            this.packDao = packDao;
        }

        @Override
        public Reservation rejectReservation(Long reservationId) {
            rejectCalls++;
            final Reservation r = reservationDao.findById(reservationId)
                    .orElseThrow(() -> new IllegalArgumentException("not found"));
            final int qty = r.getQuantity() == null ? 1 : r.getQuantity();
            packDao.incrementStock(r.getPackId(), qty);
            return reservationDao.updateStatus(reservationId, Reservation.Status.CANCELED);
        }

        @Override public Reservation createReservation(long packId, long userId, int quantity, double unitPrice, String pickupWindow, String baseUrl) { throw new UnsupportedOperationException(); }
        @Override public Optional<Reservation> findById(Long id) { return reservationDao.findById(id); }
        @Override public List<Reservation> findByCustomerId(Long customerId) { throw new UnsupportedOperationException(); }
        @Override public List<Reservation> findByCommerceId(Long commerceId) { throw new UnsupportedOperationException(); }
        @Override public List<Reservation> findByPackId(Long packId) { throw new UnsupportedOperationException(); }
        @Override public String computePickupDateStr(Reservation reservation) { throw new UnsupportedOperationException(); }
        @Override public void validateReservationBelongsToCommerce(Long reservationId, Long commerceUserId) { throw new UnsupportedOperationException(); }
        @Override public ar.edu.itba.paw.services.pack.DirectReservationCheck checkDirectPackReservation(long packId, int quantity) { throw new UnsupportedOperationException(); }
        @Override public Reservation confirmPickup(Long id) { throw new UnsupportedOperationException(); }
        @Override public Reservation rejectReservationForCommerce(Long reservationId, Long commerceUserId) { throw new UnsupportedOperationException(); }
        @Override public PickupByCodeResult confirmPickupByCode(String pickupCode, Long commerceUserId) { throw new UnsupportedOperationException(); }
        @Override public List<Reservation> filterReservations(Long commerceId, Long customerId, String query, Reservation.Status status, int page, int pageSize) { throw new UnsupportedOperationException(); }
        @Override public int countFilteredReservations(Long commerceId, Long customerId, String query, Reservation.Status status) { throw new UnsupportedOperationException(); }
        @Override public boolean hasActiveReservation(Long packId, Long customerId) { throw new UnsupportedOperationException(); }
    }

    static class InMemoryPackDao implements PackDao {
        int incrementCalls = 0;

        @Override public Pack createPack(Long commerceId, String title, String description, Double originalPrice, Double finalPrice, Integer stock, List<PackTag> tags, Long imageId) { throw new UnsupportedOperationException(); }
        @Override public Optional<Pack> findById(Long id) { return Optional.empty(); }
        @Override public List<Pack> findAll() { return List.of(); }
        @Override public Pack update(Pack pack) { throw new UnsupportedOperationException(); }
        @Override public void setActive(Long id, boolean active) { }
        @Override public boolean decrementStock(long packId, int quantity) { return true; }
        @Override public boolean incrementStock(long packId, int quantity) { incrementCalls++; return true; }
        @Override public void softDelete(Long id) { }
        @Override public List<Pack> findByCommerceId(Long commerceId) { return List.of(); }
        @Override public List<Pack> filterPacks(String query, List<ar.edu.itba.paw.models.pack.PackTag> tags, String city, List<String> timeRanges, PackSortOption sort, int page, int pageSize) { return List.of(); }
        @Override public int countFilteredPacks(String query, List<ar.edu.itba.paw.models.pack.PackTag> tags, String city, List<String> timeRanges) { return 0; }
        @Override public List<Pack> filterCommercePacks(Long commerceId, Boolean hasAuction, int page, int pageSize) { return List.of(); }
        @Override public int countCommercePacks(Long commerceId, Boolean hasAuction) { return 0; }
    }

    private InMemoryReservationTokenDao tokenDao;
    private InMemoryReservationDao reservationDao;
    private InMemoryPackDao packDao;
    private StubReservationService reservationService;
    private ReservationTokenServiceImpl svc;

    @BeforeEach
    public void setUp() {
        tokenDao = new InMemoryReservationTokenDao();
        reservationDao = new InMemoryReservationDao();
        packDao = new InMemoryPackDao();
        reservationService = new StubReservationService(reservationDao, packDao);
        svc = new ReservationTokenServiceImpl(tokenDao, reservationDao, reservationService);
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
        final Reservation r = reservationDao.createReservation(1L, 1L, LocalDateTime.now(), 5.0, Reservation.Status.RESERVED, "c", null, 2, "pw");
        final String t = createToken(r.getId(), ReservationToken.Action.REJECT, LocalDateTime.now().plusHours(1), "t5");

        final var res = svc.validateAndConsume(t, ReservationToken.Action.REJECT);
        assertEquals(ReservationTokenService.TokenValidationResult.SUCCESS, res);

        final var tokenOpt = tokenDao.findByToken(t);
        assertEquals(true, tokenOpt.isPresent() && tokenOpt.get().isUsed());
        final var resOpt = reservationDao.findById(r.getId());
        assertEquals(Reservation.Status.CANCELED, resOpt.get().getStatus());
        assertEquals(1, packDao.incrementCalls);
        assertEquals(1, reservationService.rejectCalls);
    }

    @Test
    public void findReservationIdByToken_returnsReservationId() {
        final Reservation r = reservationDao.createReservation(1L, 1L, LocalDateTime.now(), 5.0, Reservation.Status.RESERVED, "c", null, 1, "pw");
        final String t = createToken(r.getId(), ReservationToken.Action.ACCEPT, LocalDateTime.now().plusHours(1), "t6");
        final var idOpt = svc.findReservationIdByToken(t);
        assertEquals(true, idOpt.isPresent() && idOpt.get().equals(r.getId()));
    }
}
