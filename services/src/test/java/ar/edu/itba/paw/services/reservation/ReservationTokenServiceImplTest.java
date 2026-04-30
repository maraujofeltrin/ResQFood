package ar.edu.itba.paw.services.reservation;

import ar.edu.itba.paw.models.pack.Pack;
import ar.edu.itba.paw.models.pack.PackSortOption;
import ar.edu.itba.paw.models.pack.PackTag;
import ar.edu.itba.paw.models.reservation.Reservation;
import ar.edu.itba.paw.models.reservation.ReservationToken;
import ar.edu.itba.paw.models.reservation.ReservationTokenActionError;
import ar.edu.itba.paw.persistence.PackDao;
import ar.edu.itba.paw.persistence.ReservationDao;
import ar.edu.itba.paw.persistence.ReservationTokenDao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
        public List<Reservation> filterReservations(Long commerceId, Long customerId, String query, Reservation.Status status, boolean excludeAuctionPacks, int page, int pageSize) {
            return Collections.emptyList();
        }

        @Override
        public int countFilteredReservations(Long commerceId, Long customerId, String query, Reservation.Status status, boolean excludeAuctionPacks) {
            return 0;
        }

        @Override
        public boolean hasActiveReservation(Long packId, Long customerId) {
            return false;
        }

        @Override
        public boolean hasPaidReservationWithCommerce(final Long customerId, final Long commerceId) {
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
        int confirmPickupCalls = 0;
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

        @Override
        public Reservation confirmPickup(Long id) {
            confirmPickupCalls++;
            final Reservation r = reservationDao.findById(id)
                    .orElseThrow(() -> new IllegalStateException("not found"));
            return ((InMemoryReservationDao) reservationDao).confirmPickup(id, LocalDateTime.now(ZoneOffset.UTC));
        }

        @Override public Reservation createReservation(long packId, long userId, int quantity, double unitPrice, String pickupWindow, String baseUrl) { throw new UnsupportedOperationException(); }
        @Override public Optional<Reservation> findById(Long id) { return reservationDao.findById(id); }
        @Override public List<Reservation> findByCustomerId(Long customerId) { throw new UnsupportedOperationException(); }
        @Override public List<Reservation> findByCommerceId(Long commerceId) { throw new UnsupportedOperationException(); }
        @Override public List<Reservation> findByPackId(Long packId) { throw new UnsupportedOperationException(); }
        @Override public String computePickupDateStr(Reservation reservation) { throw new UnsupportedOperationException(); }
        @Override public void validateReservationBelongsToCommerce(Long reservationId, Long commerceUserId) { throw new UnsupportedOperationException(); }
        @Override public ar.edu.itba.paw.services.pack.DirectReservationCheck checkDirectPackReservation(long packId, int quantity) { throw new UnsupportedOperationException(); }
        @Override public ReservationServiceResult<ar.edu.itba.paw.models.reservation.ReservationRejectionError> tryRejectReservationForCommerce(Long reservationId, Long commerceUserId) { throw new UnsupportedOperationException(); }
        @Override public Reservation rejectReservationForCommerce(Long reservationId, Long commerceUserId) { throw new UnsupportedOperationException(); }
        @Override public ReservationServiceResult<ar.edu.itba.paw.models.reservation.PickupByCodeError> confirmPickupByCode(String pickupCode, Long commerceUserId) { throw new UnsupportedOperationException(); }
        @Override public List<Reservation> filterReservations(Long commerceId, Long customerId, String query, Reservation.Status status, boolean excludeAuctionPacks, int page, int pageSize) { throw new UnsupportedOperationException(); }
        @Override public int countFilteredReservations(Long commerceId, Long customerId, String query, Reservation.Status status, boolean excludeAuctionPacks) { throw new UnsupportedOperationException(); }
        @Override public boolean hasActiveReservation(Long packId, Long customerId) { throw new UnsupportedOperationException(); }
    }

    static class InMemoryPackDao implements PackDao {
        final Map<Long, Pack> store = new HashMap<>();
        int incrementCalls = 0;

        void addPack(Pack pack) { store.put(pack.getId(), pack); }

        @Override public Pack createPack(Long commerceId, String title, String description, Double originalPrice, Double finalPrice, Integer stock, List<PackTag> tags, Long imageId) { throw new UnsupportedOperationException(); }
        @Override public Optional<Pack> findById(Long id) { return Optional.ofNullable(store.get(id)); }
        @Override public List<Pack> findAll() { return List.of(); }
        @Override public Pack update(Pack pack) { throw new UnsupportedOperationException(); }
        @Override public void setActive(Long id, boolean active) { }
        @Override public boolean decrementStock(long packId, int quantity) { return true; }
        @Override public boolean incrementStock(long packId, int quantity) { incrementCalls++; return true; }
        @Override public void softDelete(Long id) { }
        @Override public List<Pack> findByCommerceId(Long commerceId) { return List.of(); }
        @Override public List<Pack> filterPacks(String query, List<PackTag> tags, String city, List<String> timeRanges, PackSortOption sort, int page, int pageSize, boolean requirePositiveStock) { return List.of(); }
        @Override public int countFilteredPacks(String query, List<PackTag> tags, String city, List<String> timeRanges, boolean requirePositiveStock) { return 0; }
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
        svc = new ReservationTokenServiceImpl(tokenDao, reservationDao, packDao, reservationService);
    }

    private String createToken(long reservationId, ReservationToken.Action action, LocalDateTime expiresAt, String tokenStr) {
        final LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        tokenDao.create(tokenStr, reservationId, action, now, expiresAt);
        return tokenStr;
    }

    // --- validateOnly tests ---

    @Test
    public void validateOnly_tokenNotFound_returnsNotFound() {
        // 1. Setup (no token created)
        // 2. Ejercicio
        final var res = svc.validateOnly("no-token", ReservationToken.Action.ACCEPT);
        // 3. Asserts
        assertEquals(ReservationTokenService.TokenValidationResult.NOT_FOUND, res);
    }

    @Test
    public void validateOnly_tokenUsed_returnsAlreadyUsed() {
        // 1. Setup
        final Reservation r = reservationDao.createReservation(1L, 1L, LocalDateTime.now(), 5.0, Reservation.Status.RESERVED, "c", null, 1, "pw");
        final String t = createToken(r.getId(), ReservationToken.Action.ACCEPT, LocalDateTime.now(ZoneOffset.UTC).plusHours(1), "t1");
        tokenDao.markAsUsed(t);
        // 2. Ejercicio
        final var res = svc.validateOnly(t, ReservationToken.Action.ACCEPT);
        // 3. Asserts
        assertEquals(ReservationTokenService.TokenValidationResult.ALREADY_USED, res);
    }

    @Test
    public void validateOnly_actionMismatch_returnsNotFound() {
        // 1. Setup
        final Reservation r = reservationDao.createReservation(1L, 1L, LocalDateTime.now(), 5.0, Reservation.Status.RESERVED, "c", null, 1, "pw");
        final String t = createToken(r.getId(), ReservationToken.Action.ACCEPT, LocalDateTime.now(ZoneOffset.UTC).plusHours(1), "t2");
        // 2. Ejercicio
        final var res = svc.validateOnly(t, ReservationToken.Action.REJECT);
        // 3. Asserts
        assertEquals(ReservationTokenService.TokenValidationResult.NOT_FOUND, res);
    }

    @Test
    public void validateOnly_expired_returnsExpired() {
        // 1. Setup
        final Reservation r = reservationDao.createReservation(1L, 1L, LocalDateTime.now(), 5.0, Reservation.Status.RESERVED, "c", null, 1, "pw");
        final String t = createToken(r.getId(), ReservationToken.Action.ACCEPT, LocalDateTime.now(ZoneOffset.UTC).minusMinutes(5), "t3");
        // 2. Ejercicio
        final var res = svc.validateOnly(t, ReservationToken.Action.ACCEPT);
        // 3. Asserts
        assertEquals(ReservationTokenService.TokenValidationResult.EXPIRED, res);
    }

    @Test
    public void findReservationIdByToken_returnsReservationId() {
        // 1. Setup
        final Reservation r = reservationDao.createReservation(1L, 1L, LocalDateTime.now(), 5.0, Reservation.Status.RESERVED, "c", null, 1, "pw");
        final String t = createToken(r.getId(), ReservationToken.Action.ACCEPT, LocalDateTime.now(ZoneOffset.UTC).plusHours(1), "t6");
        // 2. Ejercicio
        final var idOpt = svc.findReservationIdByToken(t);
        // 3. Asserts
        assertTrue(idOpt.isPresent() && idOpt.get().equals(r.getId()));
    }

    // --- acceptReservationTokenWithPickupCode tests ---

    @Test
    public void acceptToken_validCode_marksUsedAndConfirmsPickup() {
        // 1. Setup
        final long packId = 800L;
        final long commerceId = 801L;
        packDao.addPack(new Pack(packId, commerceId, "title", "desc", 10.0, 5.0, 5, true, Collections.emptyList()));
        final Reservation reservation = reservationDao.createReservation(201L, packId, LocalDateTime.now(), 25.0,
            Reservation.Status.RESERVED, "A1B2C", null, 1, null);
        createToken(reservation.getId(), ReservationToken.Action.ACCEPT,
            LocalDateTime.now(ZoneOffset.UTC).plusHours(1), "accept-token");

        // 2. Ejercicio
        final ReservationServiceResult<ReservationTokenActionError> result =
            svc.acceptReservationTokenWithPickupCode("accept-token", "a1b2c", commerceId);

        // 3. Asserts
        assertTrue(result.isSuccess());
        assertEquals(Reservation.Status.PAID, result.reservation().get().getStatus());
        assertTrue(tokenDao.findByToken("accept-token").get().isUsed());
        assertEquals(1, reservationService.confirmPickupCalls);
    }

    @Test
    public void acceptToken_invalidCode_returnsErrorAndDoesNotConsume() {
        // 1. Setup
        final long packId = 810L;
        final long commerceId = 811L;
        packDao.addPack(new Pack(packId, commerceId, "title", "desc", 10.0, 5.0, 5, true, Collections.emptyList()));
        final Reservation reservation = reservationDao.createReservation(202L, packId, LocalDateTime.now(), 25.0,
            Reservation.Status.RESERVED, "Z9Y8X", null, 1, null);
        createToken(reservation.getId(), ReservationToken.Action.ACCEPT,
            LocalDateTime.now(ZoneOffset.UTC).plusHours(1), "bad-code-token");

        // 2. Ejercicio
        final ReservationServiceResult<ReservationTokenActionError> result =
            svc.acceptReservationTokenWithPickupCode("bad-code-token", "WRONG", commerceId);

        // 3. Asserts
        assertEquals(ReservationTokenActionError.INVALID_PICKUP_CODE, result.error().get());
        assertEquals(Reservation.Status.RESERVED, reservationDao.findById(reservation.getId()).get().getStatus());
        assertFalse(tokenDao.findByToken("bad-code-token").get().isUsed());
    }

    @Test
    public void acceptToken_blankToken_returnsInvalidTokenError() {
        // 1. Setup (no token needed)
        // 2. Ejercicio
        final ReservationServiceResult<ReservationTokenActionError> result =
            svc.acceptReservationTokenWithPickupCode("  ", "CODE", 1L);
        // 3. Asserts
        assertEquals(ReservationTokenActionError.INVALID_TOKEN, result.error().get());
    }

    // --- rejectReservationToken tests ---

    @Test
    public void rejectToken_validToken_marksUsedAndCancelsReservation() {
        // 1. Setup
        final long packId = 900L;
        final long commerceId = 901L;
        packDao.addPack(new Pack(packId, commerceId, "title", "desc", 10.0, 5.0, 5, true, Collections.emptyList()));
        final Reservation reservation = reservationDao.createReservation(301L, packId, LocalDateTime.now(), 25.0,
            Reservation.Status.RESERVED, "R1R2R", null, 1, null);
        createToken(reservation.getId(), ReservationToken.Action.REJECT,
            LocalDateTime.now(ZoneOffset.UTC).plusHours(1), "reject-token");

        // 2. Ejercicio
        final ReservationServiceResult<ReservationTokenActionError> result = svc.rejectReservationToken("reject-token", commerceId);

        // 3. Asserts
        assertTrue(result.isSuccess());
        assertEquals(Reservation.Status.CANCELED, result.reservation().get().getStatus());
        assertTrue(tokenDao.findByToken("reject-token").get().isUsed());
        assertEquals(1, packDao.incrementCalls);
        assertEquals(1, reservationService.rejectCalls);
    }

    @Test
    public void rejectToken_wrongCommerce_returnsErrorAndDoesNotConsume() {
        // 1. Setup
        final long packId = 910L;
        final long ownerCommerceId = 911L;
        final long otherCommerceId = 999L;
        packDao.addPack(new Pack(packId, ownerCommerceId, "title", "desc", 10.0, 5.0, 5, true, Collections.emptyList()));
        final Reservation reservation = reservationDao.createReservation(302L, packId, LocalDateTime.now(), 25.0,
            Reservation.Status.RESERVED, "S1S2S", null, 1, null);
        createToken(reservation.getId(), ReservationToken.Action.REJECT,
            LocalDateTime.now(ZoneOffset.UTC).plusHours(1), "reject-wrong-commerce");

        // 2. Ejercicio
        final ReservationServiceResult<ReservationTokenActionError> result = svc.rejectReservationToken("reject-wrong-commerce", otherCommerceId);

        // 3. Asserts
        assertEquals(ReservationTokenActionError.WRONG_COMMERCE, result.error().get());
        assertFalse(tokenDao.findByToken("reject-wrong-commerce").get().isUsed());
        assertEquals(0, packDao.incrementCalls);
    }

    @Test
    public void rejectToken_expiredToken_returnsExpiredError() {
        // 1. Setup
        final long packId = 920L;
        final long commerceId = 921L;
        packDao.addPack(new Pack(packId, commerceId, "title", "desc", 10.0, 5.0, 5, true, Collections.emptyList()));
        final Reservation reservation = reservationDao.createReservation(303L, packId, LocalDateTime.now(), 25.0,
            Reservation.Status.RESERVED, "T1T2T", null, 1, null);
        createToken(reservation.getId(), ReservationToken.Action.REJECT,
            LocalDateTime.now(ZoneOffset.UTC).minusHours(1), "reject-expired");

        // 2. Ejercicio
        final ReservationServiceResult<ReservationTokenActionError> result = svc.rejectReservationToken("reject-expired", commerceId);

        // 3. Asserts
        assertEquals(ReservationTokenActionError.EXPIRED, result.error().get());
        assertFalse(tokenDao.findByToken("reject-expired").get().isUsed());
        assertEquals(0, packDao.incrementCalls);
    }

    @Test
    public void rejectToken_alreadyCanceled_returnsAlreadyUsedError() {
        // 1. Setup
        final long packId = 930L;
        final long commerceId = 931L;
        packDao.addPack(new Pack(packId, commerceId, "title", "desc", 10.0, 5.0, 5, true, Collections.emptyList()));
        final Reservation reservation = reservationDao.createReservation(304L, packId, LocalDateTime.now(), 25.0,
            Reservation.Status.CANCELED, "U1U2U", null, 1, null);
        createToken(reservation.getId(), ReservationToken.Action.REJECT,
            LocalDateTime.now(ZoneOffset.UTC).plusHours(1), "reject-canceled");

        // 2. Ejercicio
        final ReservationServiceResult<ReservationTokenActionError> result = svc.rejectReservationToken("reject-canceled", commerceId);

        // 3. Asserts
        assertEquals(ReservationTokenActionError.ALREADY_USED, result.error().get());
        assertFalse(tokenDao.findByToken("reject-canceled").get().isUsed());
        assertEquals(0, packDao.incrementCalls);
    }

    @Test
    public void rejectToken_blankToken_returnsInvalidTokenError() {
        // 1. Setup (no token needed)
        // 2. Ejercicio
        final ReservationServiceResult<ReservationTokenActionError> result = svc.rejectReservationToken("  ", 1L);
        // 3. Asserts
        assertEquals(ReservationTokenActionError.INVALID_TOKEN, result.error().get());
    }
}
