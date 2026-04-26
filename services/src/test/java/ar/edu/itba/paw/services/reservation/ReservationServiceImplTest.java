package ar.edu.itba.paw.services.reservation;

import ar.edu.itba.paw.models.user.Client;
import ar.edu.itba.paw.models.user.Commerce;
import ar.edu.itba.paw.models.pack.Pack;
import ar.edu.itba.paw.models.pack.PackSortOption;
import ar.edu.itba.paw.models.reservation.Reservation;
import ar.edu.itba.paw.models.user.User;
import ar.edu.itba.paw.models.auction.Auction;
import ar.edu.itba.paw.persistence.AuctionDao;
import ar.edu.itba.paw.persistence.PackDao;
import ar.edu.itba.paw.persistence.ReservationDao;
import ar.edu.itba.paw.services.commerce.CommerceService;
import ar.edu.itba.paw.services.user.ClientService;
import ar.edu.itba.paw.services.user.RegisterResult;
import ar.edu.itba.paw.services.user.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ReservationServiceImplTest {

    private static final String APP_URL = "http://app";
    private static final ZoneId TEST_ZONE = ZoneId.of("America/Argentina/Buenos_Aires");

    private static final class NoopAuctionDao implements AuctionDao {
        @Override
        public Auction createAuction(final long packId, final double initialPrice, final LocalDateTime endTime) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Optional<Auction> findById(final long id) {
            return Optional.empty();
        }

        @Override
        public Optional<Auction> findByPackId(final long packId) {
            return Optional.empty();
        }

        @Override
        public List<Auction> findActive() {
            return Collections.emptyList();
        }

        @Override
        public List<Auction> findActive(final ar.edu.itba.paw.models.auction.AuctionSortOption sort) {
            return Collections.emptyList();
        }

        @Override
        public List<Auction> searchActive(final String query, final ar.edu.itba.paw.models.auction.AuctionSortOption sort) {
            return Collections.emptyList();
        }

        @Override
        public List<Auction> findActiveByTags(final List<ar.edu.itba.paw.models.pack.PackTag> tags,
                final ar.edu.itba.paw.models.auction.AuctionSortOption sort) {
            return Collections.emptyList();
        }

        @Override
        public List<Auction> searchActiveWithTags(final String query, final List<ar.edu.itba.paw.models.pack.PackTag> tags,
                final ar.edu.itba.paw.models.auction.AuctionSortOption sort) {
            return Collections.emptyList();
        }

        @Override
        public List<Auction> findByCommerceId(final long commerceId) {
            return Collections.emptyList();
        }

        @Override
        public List<Auction> findByStatus(final Auction.Status status) {
            return Collections.emptyList();
        }

        @Override
        public void updateStatus(final long auctionId, final Auction.Status status) {
        }

        @Override
        public void updateCurrentBid(final long auctionId, final double amount, final long bidderId) {
        }

        @Override
        public List<Auction> findExpiredActive() {
            return Collections.emptyList();
        }
    }

    private static final NoopAuctionDao NO_AUCTIONS = new NoopAuctionDao();

    static class InMemoryReservationDao implements ReservationDao {
        final List<Reservation> store = new ArrayList<>();

        @Override
        public Reservation createReservation(Long customerId, Long packId, LocalDateTime reservationDate, Double finalPrice, Reservation.Status status, String pickupCode, LocalDateTime pickupConfirmationDate, Integer quantity, String pickupWindow) {
            final Reservation r = new Reservation((long) (store.size() + 1), customerId, packId, reservationDate, finalPrice, status, pickupCode, pickupConfirmationDate, quantity, pickupWindow);
            store.add(r);
            return r;
        }

        @Override
        public Optional<Reservation> findById(Long id) {
            return store.stream().filter(r -> r.getId().equals(id)).findFirst();
        }

        @Override
        public List<Reservation> findByCustomerId(Long customerId) {
            return store.stream().filter(r -> r.getCustomerId().equals(customerId)).toList();
        }

        @Override
        public List<Reservation> findByCommerceId(Long commerceId) {
            // Test double: maps commerce id to pack id with same numeric value.
            return store.stream().filter(r -> r.getPackId().equals(commerceId)).toList();
        }

        @Override
        public List<Reservation> findByPackId(Long packId) {
            return store.stream().filter(r -> r.getPackId().equals(packId)).toList();
        }

        @Override
        public Reservation updateStatus(Long id, Reservation.Status status) {
            final var opt = findById(id);
            if (opt.isEmpty()) throw new IllegalStateException("not found");
            final Reservation old = opt.get();
            final Reservation updated = new Reservation(old.getId(), old.getCustomerId(), old.getPackId(), old.getReservationDate(), old.getFinalPrice(), status, old.getPickupCode(), old.getPickupConfirmationDate(), old.getQuantity(), old.getPickupWindow());
            store.remove(old);
            store.add(updated);
            return updated;
        }

        @Override
        public Optional<Reservation> findByPickupCode(String pickupCode) {
            return store.stream().filter(r -> r.getPickupCode().equals(pickupCode)).findFirst();
        }

        @Override
        public Reservation confirmPickup(Long id, LocalDateTime pickupConfirmationDate) {
            final var opt = findById(id);
            if (opt.isEmpty()) throw new IllegalStateException("not found");
            final Reservation old = opt.get();
            final Reservation updated = new Reservation(old.getId(), old.getCustomerId(), old.getPackId(), old.getReservationDate(), old.getFinalPrice(), Reservation.Status.PAID, old.getPickupCode(), pickupConfirmationDate, old.getQuantity(), old.getPickupWindow());
            store.remove(old);
            store.add(updated);
            return updated;
        }
    }

    static class InMemoryPackDao implements PackDao {
        private final Pack pack;
        int incrementCalls = 0;

        InMemoryPackDao(Pack pack) { this.pack = pack; }

        @Override public Pack createPack(Long commerceId, String title, String description, Double originalPrice, Double finalPrice, Integer stock, java.util.List<ar.edu.itba.paw.models.pack.PackTag> tags, byte[] imageData, String imageContentType) { throw new UnsupportedOperationException(); }
        @Override public Optional<Pack> findById(Long id) { return id.equals(pack.getId()) ? Optional.of(pack) : Optional.empty(); }
        @Override public java.util.List<Pack> findAll() { return Collections.emptyList(); }
        @Override public List<Pack> findByCommerceId(Long commerceId) { return pack.getCommerceId().equals(commerceId) ? List.of(pack) : Collections.emptyList(); }
        @Override public Pack update(Pack pack) { throw new UnsupportedOperationException(); }
        @Override public void softDelete(Long id) { }
        @Override public void setActive(Long id, boolean active) { }
        @Override public Optional<Pack> findImageByPackId(Long id) { return Optional.empty(); }
        @Override public boolean decrementStock(long packId, int quantity) { return true; }
        @Override public boolean incrementStock(long packId, int quantity) { incrementCalls++; return true; }
        @Override public void updateImage(Long packId, byte[] imageData, String imageContentType) { }
        @Override public java.util.List<Pack> filterPacks(String query, java.util.List<ar.edu.itba.paw.models.pack.PackTag> tags, String city, java.util.List<String> timeRanges, PackSortOption sort, int page, int pageSize) { return Collections.emptyList(); }
        @Override public int countFilteredPacks(String query, java.util.List<ar.edu.itba.paw.models.pack.PackTag> tags, String city, java.util.List<String> timeRanges) { return 0; }
    }

    static class TestUserService implements UserService {
        private final User user;
        TestUserService(User user) { this.user = user; }
        @Override public User createUser(User user, Client clientProfile, Commerce commerceProfile) { return this.user; }

        @Override public Optional<User> findByEmail(String email) { return Optional.of(user); }
        @Override public Optional<User> findById(Long id) { return Optional.of(user); }
        @Override public void updatePassword(final Long userId, final String encodedPassword) { }
        @Override public void markVerified(final Long userId) { }
        @Override
        public RegisterResult tryRegister(final User u, final Client c, final Commerce com, final String appBaseUrl) {
            return RegisterResult.duplicateEmail();
        }
    }

    static class InMemoryMailService implements ReservationMailService {
        int sentToCommerce = 0;
        int sentToClient = 0;
        int sentAuctionToCommerce = 0;
        int sentAuctionToClient = 0;
        int sentRejected = 0;
        @Override public void sendReservationRequestToCommerce(Reservation reservation, String commerceEmail, String baseUrl, String pickupDateStr, Locale locale) { sentToCommerce++; }
        @Override public void sendReservationCodeToClient(Reservation reservation, String clientEmail, String pickupDateStr, Locale locale) { sentToClient++; }
        @Override public void sendAuctionWinnerCodeToClient(Reservation reservation, String clientEmail, String pickupDateStr, Locale locale) { sentAuctionToClient++; }
        @Override public void sendAuctionWinnerCodeToCommerce(Reservation reservation, String commerceEmail, String pickupDateStr, Locale locale) { sentAuctionToCommerce++; }
        @Override public void sendReservationRejectedToClient(Reservation reservation, String clientEmail, Locale locale) { sentRejected++; }
    }

    static class TestCommerceService implements CommerceService {
        @Override public Optional<ar.edu.itba.paw.models.user.Commerce> findByUserId(Long userId) { return Optional.empty(); }
    }

    static class TestClientService implements ClientService {
        @Override public Client createClient(Long userId, String name, String lastName, Boolean notificationsVisibilityPreferences) { throw new UnsupportedOperationException(); }
        @Override public Optional<Client> findByUserId(Long userId) { return Optional.of(new Client(userId, "Test", "User", true)); }
        @Override public Client update(Client client) { return client; }
    }

    private InMemoryReservationDao reservationDao;
    private InMemoryMailService mailService;

    @BeforeEach
    public void setUp() {
        reservationDao = new InMemoryReservationDao();
        mailService = new InMemoryMailService();
    }

    @Test
    public void createReservation_createsDbEntry_and_sendsEmails() {
        final long packId = 10L;
        final long commerceUserId = 100L;
        final String email = "user@example.org";

        final User user = new User(1L, email, "pwd", "Test User", null, User.Role.CLIENT, false);
        final Pack pack = new Pack(packId, commerceUserId, "title", "desc", 10.0, 5.0, 5, true, false, Collections.emptyList(), null, null);

        final UserService userService = new TestUserService(user);
        final PackDao packDao = new InMemoryPackDao(pack);

        final ReservationServiceImpl svc = new ReservationServiceImpl(userService, new TestClientService(), reservationDao,
            packDao, mailService, new TestCommerceService(), NO_AUCTIONS, TEST_ZONE);

        assertDoesNotThrow(() -> svc.createReservation(packId, user.getId(), 1, 5.0, "pw", APP_URL));

        assertEquals(1, reservationDao.store.size(), "Reservation should be stored in DAO");
        final Reservation created = reservationDao.store.get(0);
        assertEquals(packId, created.getPackId());
        assertEquals(1, mailService.sentToCommerce);
        assertEquals(1, mailService.sentToClient);
    }

    @Test
    public void createReservation_emptyBaseUrl_createsDbEntry_and_sendsWinnerEmails() {
        final long packId = 15L;
        final long commerceUserId = 150L;
        final String email = "winner@example.org";

        final User user = new User(7L, email, "pwd", "Winning User", null, User.Role.CLIENT, false);
        final Pack pack = new Pack(packId, commerceUserId, "auction-pack", "desc", 10.0, 5.0, 5, true, Collections.emptyList());

        final UserService userService = new TestUserService(user);
        final PackDao packDao = new InMemoryPackDao(pack);

        final ReservationServiceImpl svc = new ReservationServiceImpl(userService, new TestClientService(), reservationDao,
                packDao, mailService, new TestCommerceService(), NO_AUCTIONS, TEST_ZONE);

        assertDoesNotThrow(() -> svc.createReservation(packId, user.getId(), 1, 7.5, null, ""));

        assertEquals(1, reservationDao.store.size(), "Reservation should be stored in DAO");
        final Reservation created = reservationDao.store.get(0);
        assertEquals(packId, created.getPackId());
        assertEquals(0, mailService.sentToCommerce);
        assertEquals(0, mailService.sentToClient);
        assertEquals(1, mailService.sentAuctionToClient);
        assertEquals(1, mailService.sentAuctionToCommerce);
    }

    @Test
    public void createReservation_invalidQuantity_noSideEffects() {
        final long packId = 20L;
        final long commerceUserId = 200L;
        final String email = "user2@example.org";

        final User user = new User(2L, email, "pwd", "Test User2", null, User.Role.CLIENT, false);
        final Pack pack = new Pack(packId, commerceUserId, "title", "desc", 10.0, 5.0, 5, true, false, Collections.emptyList(), null, null);

        final UserService userService = new TestUserService(user);
        final PackDao packDao = new InMemoryPackDao(pack);

        final ReservationServiceImpl svc = new ReservationServiceImpl(userService, new TestClientService(), reservationDao,
            packDao, mailService, new TestCommerceService(), NO_AUCTIONS, TEST_ZONE);

        assertThrows(IllegalArgumentException.class, () -> svc.createReservation(packId, user.getId(), 0, 5.0, "pw", APP_URL));
        assertEquals(0, reservationDao.store.size(), "No reservation should be created");
        assertEquals(0, mailService.sentToCommerce);
        assertEquals(0, mailService.sentToClient);
    }

    @Test
    public void createReservation_pickupWindowTooLong_throws_and_noSideEffects() {
        final long packId = 30L;
        final long commerceUserId = 300L;
        final String email = "user3@example.org";

        final User user = new User(3L, email, "pwd", "Test User3", null, User.Role.CLIENT, false);
        final Pack pack = new Pack(packId, commerceUserId, "title", "desc", 10.0, 5.0, 5, true, false, Collections.emptyList(), null, null);

        final UserService userService = new TestUserService(user);
        final PackDao packDao = new InMemoryPackDao(pack);

        final ReservationServiceImpl svc = new ReservationServiceImpl(userService, new TestClientService(), reservationDao,
            packDao, mailService, new TestCommerceService(), NO_AUCTIONS, TEST_ZONE);

        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 600; i++) sb.append('x');
        final String longPickup = sb.toString();

        assertThrows(IllegalArgumentException.class, () -> svc.createReservation(packId, user.getId(), 1, 5.0, longPickup, APP_URL));
        assertEquals(0, reservationDao.store.size(), "No reservation should be created");
        assertEquals(0, mailService.sentToCommerce);
        assertEquals(0, mailService.sentToClient);
    }

    @Test
    public void findByCustomerId_returnsOnlyCustomerReservations() {
        reservationDao.createReservation(1L, 10L, LocalDateTime.now(), 10.0, Reservation.Status.RESERVED, "AAAAA", null, 1, null);
        reservationDao.createReservation(2L, 11L, LocalDateTime.now(), 20.0, Reservation.Status.RESERVED, "BBBBB", null, 2, null);
        reservationDao.createReservation(1L, 12L, LocalDateTime.now(), 30.0, Reservation.Status.PAID, "CCCCC", null, 3, null);

        final User user = new User(1L, "user@example.org", "pwd", "Test User");
        final Pack pack = new Pack(10L, 100L, "title", "desc", 10.0, 5.0, 5, true, Collections.emptyList());

        final ReservationServiceImpl svc = new ReservationServiceImpl(
                new TestUserService(user),
                new TestClientService(),
                reservationDao,
                new InMemoryPackDao(pack),
                mailService,
                new TestCommerceService(),
                NO_AUCTIONS,
                TEST_ZONE);

        final List<Reservation> reservations = svc.findByCustomerId(1L);
        assertEquals(2, reservations.size());
        assertTrue(reservations.stream().allMatch(r -> r.getCustomerId().equals(1L)));
    }

    @Test
    public void findByCommerceId_delegatesToDaoFilter() {
        reservationDao.createReservation(1L, 100L, LocalDateTime.now(), 10.0, Reservation.Status.RESERVED, "DDDDD", null, 1, null);
        reservationDao.createReservation(2L, 200L, LocalDateTime.now(), 20.0, Reservation.Status.RESERVED, "EEEEE", null, 1, null);
        reservationDao.createReservation(3L, 100L, LocalDateTime.now(), 30.0, Reservation.Status.PAID, "FFFFF", null, 1, null);

        final User user = new User(1L, "user@example.org", "pwd", "Test User");
        final Pack pack = new Pack(100L, 100L, "title", "desc", 10.0, 5.0, 5, true, Collections.emptyList());

        final ReservationServiceImpl svc = new ReservationServiceImpl(
                new TestUserService(user),
                new TestClientService(),
                reservationDao,
                new InMemoryPackDao(pack),
                mailService,
                new TestCommerceService(),
                NO_AUCTIONS,
                TEST_ZONE);

        final List<Reservation> reservations = svc.findByCommerceId(100L);
        assertEquals(2, reservations.size());
        assertTrue(reservations.stream().allMatch(r -> r.getPackId().equals(100L)));
    }

    @Test
    public void validateReservationBelongsToCommerce_validatesOwnershipAndThrowsOnMismatch() {
        final long reservationId = reservationDao.createReservation(1L, 10L, LocalDateTime.now(), 10.0,
                Reservation.Status.RESERVED, "GGGGG", null, 1, null).getId();

        final Pack ownedPack = new Pack(10L, 77L, "title", "desc", 10.0, 5.0, 5, true, Collections.emptyList());
        final Pack otherPack = new Pack(11L, 88L, "title2", "desc2", 10.0, 5.0, 5, true, Collections.emptyList());

        final ReservationServiceImpl svc = new ReservationServiceImpl(
                new TestUserService(new User(1L, "user@example.org", "pwd", "Test User")),
                new TestClientService(),
                reservationDao,
                new PackDao() {
                    @Override public Pack createPack(Long commerceId, String title, String description, Double originalPrice, Double finalPrice, Integer stock, java.util.List<ar.edu.itba.paw.models.pack.PackTag> tags, byte[] imageData, String imageContentType) { throw new UnsupportedOperationException(); }
                    @Override public Optional<Pack> findById(Long id) { return id.equals(ownedPack.getId()) ? Optional.of(ownedPack) : id.equals(otherPack.getId()) ? Optional.of(otherPack) : Optional.empty(); }
                    @Override public java.util.List<Pack> findAll() { return Collections.emptyList(); }
                    @Override public Pack update(Pack pack) { throw new UnsupportedOperationException(); }
                    @Override public void setActive(Long id, boolean active) { }
                    @Override public Optional<Pack> findImageByPackId(Long id) { return Optional.empty(); }
                    @Override public boolean decrementStock(long packId, int quantity) { return true; }
                    @Override public boolean incrementStock(long packId, int quantity) { return true; }
                    @Override public java.util.List<Pack> findByCommerceId(Long commerceId) { return Collections.emptyList(); }
                    @Override public void softDelete(Long id) { }
                    @Override public void updateImage(Long packId, byte[] imageData, String imageContentType) { }
                    @Override public java.util.List<Pack> filterPacks(String query, java.util.List<ar.edu.itba.paw.models.pack.PackTag> tags, String city, java.util.List<String> timeRanges, PackSortOption sort, int page, int pageSize) { return Collections.emptyList(); }
                    @Override public int countFilteredPacks(String query, java.util.List<ar.edu.itba.paw.models.pack.PackTag> tags, String city, java.util.List<String> timeRanges) { return 0; }
                },
                mailService,
                new TestCommerceService(),
                NO_AUCTIONS,
                TEST_ZONE);

        assertDoesNotThrow(() -> svc.validateReservationBelongsToCommerce(reservationId, 77L));
        assertThrows(IllegalArgumentException.class, () -> svc.validateReservationBelongsToCommerce(reservationId, 88L));
        assertThrows(IllegalArgumentException.class, () -> svc.validateReservationBelongsToCommerce(null, 77L));
        assertThrows(IllegalArgumentException.class, () -> svc.validateReservationBelongsToCommerce(reservationId, null));
    }

        @Test
        public void rejectReservationForCommerce_reserved_rejectsRestoresStockAndNotifiesClient() {
        final long packId = 500L;
        final long commerceId = 999L;
        final long reservationId = reservationDao.createReservation(101L, packId, LocalDateTime.now(), 25.0,
            Reservation.Status.RESERVED, "HHHHH", null, 3, null).getId();

        final InMemoryPackDao packDao = new InMemoryPackDao(
            new Pack(packId, commerceId, "title", "desc", 10.0, 5.0, 5, true, Collections.emptyList()));

        final ReservationServiceImpl svc = new ReservationServiceImpl(
            new TestUserService(new User(101L, "client@example.org", "pwd", "Client", null, User.Role.CLIENT, false)),
            new TestClientService(),
            reservationDao,
            packDao,
            mailService,
            new TestCommerceService(),
            NO_AUCTIONS,
            TEST_ZONE);

        final Reservation rejected = svc.rejectReservationForCommerce(reservationId, commerceId);
        assertEquals(Reservation.Status.CANCELED, rejected.getStatus());
        assertEquals(1, packDao.incrementCalls);
        assertEquals(1, mailService.sentRejected);
        }

        @Test
        public void rejectReservationForCommerce_paid_throwsAndDoesNotRestoreStock() {
        final long packId = 600L;
        final long commerceId = 111L;
        final long reservationId = reservationDao.createReservation(102L, packId, LocalDateTime.now(), 25.0,
            Reservation.Status.PAID, "IIIII", null, 1, null).getId();

        final InMemoryPackDao packDao = new InMemoryPackDao(
            new Pack(packId, commerceId, "title", "desc", 10.0, 5.0, 5, true, Collections.emptyList()));

        final ReservationServiceImpl svc = new ReservationServiceImpl(
            new TestUserService(new User(102L, "client2@example.org", "pwd", "Client2", null, User.Role.CLIENT, false)),
            new TestClientService(),
            reservationDao,
            packDao,
            mailService,
            new TestCommerceService(),
            NO_AUCTIONS,
            TEST_ZONE);

        assertThrows(IllegalStateException.class, () -> svc.rejectReservationForCommerce(reservationId, commerceId));
        assertEquals(0, packDao.incrementCalls);
        }

        @Test
        public void rejectReservationForCommerce_wrongCommerce_throws() {
        final long packId = 700L;
        final long ownerCommerceId = 222L;
        final long otherCommerceId = 333L;
        final long reservationId = reservationDao.createReservation(103L, packId, LocalDateTime.now(), 25.0,
            Reservation.Status.RESERVED, "JJJJJ", null, 1, null).getId();

        final InMemoryPackDao packDao = new InMemoryPackDao(
            new Pack(packId, ownerCommerceId, "title", "desc", 10.0, 5.0, 5, true, Collections.emptyList()));

        final ReservationServiceImpl svc = new ReservationServiceImpl(
            new TestUserService(new User(103L, "client3@example.org", "pwd", "Client3", null, User.Role.CLIENT, false)),
            new TestClientService(),
            reservationDao,
            packDao,
            mailService,
            new TestCommerceService(),
            NO_AUCTIONS,
            TEST_ZONE);

        assertThrows(IllegalArgumentException.class,
            () -> svc.rejectReservationForCommerce(reservationId, otherCommerceId));
        }
}
