   package ar.edu.itba.paw.services;

import ar.edu.itba.paw.models.Client;
import ar.edu.itba.paw.models.Commerce;
import ar.edu.itba.paw.models.Pack;
import ar.edu.itba.paw.models.PackSortOption;
import ar.edu.itba.paw.models.Reservation;
import ar.edu.itba.paw.models.User;
import ar.edu.itba.paw.persistence.PackDao;
import ar.edu.itba.paw.persistence.ReservationDao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ReservationServiceImplTest {

    private static final String APP_URL = "http://app";

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

        InMemoryPackDao(Pack pack) { this.pack = pack; }

        @Override public Pack createPack(Long commerceId, String title, String description, Double originalPrice, Double finalPrice, Integer stock, java.util.List<ar.edu.itba.paw.models.PackTag> tags, byte[] imageData, String imageContentType) { throw new UnsupportedOperationException(); }
        @Override public Optional<Pack> findById(Long id) { return id.equals(pack.getId()) ? Optional.of(pack) : Optional.empty(); }
        @Override public java.util.List<Pack> findAll() { return Collections.emptyList(); }
        @Override public java.util.List<Pack> findActive() { return Collections.emptyList(); }
        @Override public java.util.List<Pack> searchPacks(String query) { return Collections.emptyList(); }
        @Override public Pack update(Pack pack) { throw new UnsupportedOperationException(); }
        @Override public void setActive(Long id, boolean active) { }
        @Override public Optional<Pack> findImageByPackId(Long id) { return Optional.empty(); }
        @Override public boolean decrementStock(long packId, int quantity) { return true; }
        @Override public java.util.List<Pack> findActiveByTags(java.util.List<ar.edu.itba.paw.models.PackTag> tags) { return Collections.emptyList(); }
        @Override public java.util.List<Pack> searchPacksWithTags(String query, java.util.List<ar.edu.itba.paw.models.PackTag> tags) { return Collections.emptyList(); }
        @Override public java.util.List<Pack> findActive(PackSortOption sort) { return findActive(); }
        @Override public java.util.List<Pack> searchPacks(String query, PackSortOption sort) { return searchPacks(query); }
        @Override public java.util.List<Pack> findActiveByTags(java.util.List<ar.edu.itba.paw.models.PackTag> tags, PackSortOption sort) { return findActiveByTags(tags); }
        @Override public java.util.List<Pack> searchPacksWithTags(String query, java.util.List<ar.edu.itba.paw.models.PackTag> tags, PackSortOption sort) { return searchPacksWithTags(query, tags); }
    }

    static class TestUserService implements UserService {
        private final User user;
        TestUserService(User user) { this.user = user; }
        @Override public User createUser(User user, Client clientProfile, Commerce commerceProfile) { return this.user; }
        @Override public Optional<User> upgradeProvisionalUser(User user, Client clientProfile, Commerce commerceProfile) { return Optional.empty(); }
        @Override public Optional<User> findByEmail(String email) { return Optional.of(user); }
        @Override public Optional<User> findById(Long id) { return Optional.of(user); }
        @Override public void updatePassword(final Long userId, final String encodedPassword) { }
        @Override public void markVerified(final Long userId) { }
    }

    static class InMemoryMailService implements ReservationMailService {
        int sentToCommerce = 0;
        int sentToClient = 0;
        int sentRejected = 0;
        @Override public void sendReservationRequestToCommerce(Reservation reservation, String commerceEmail, String baseUrl, String pickupDateStr) { sentToCommerce++; }
        @Override public void sendReservationCodeToClient(Reservation reservation, String clientEmail, String pickupDateStr) { sentToClient++; }
        @Override public void sendReservationRejectedToClient(Reservation reservation, String clientEmail) { sentRejected++; }
    }

    static class TestCommerceService implements CommerceService {
        @Override public ar.edu.itba.paw.models.Commerce getOrCreateCommerce(String email, String password, String name, String commercialName, ar.edu.itba.paw.models.Commerce.Category category, String street, Integer streetNumber, String city, String province, String postalCode, String openingTime, String closingTime) { throw new UnsupportedOperationException(); }
        @Override public Optional<ar.edu.itba.paw.models.Commerce> findByUserId(Long userId) { return Optional.empty(); }
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
        final Pack pack = new Pack(packId, commerceUserId, "title", "desc", 10.0, 5.0, 5, true, Collections.emptyList());

        final UserService userService = new TestUserService(user);
        final PackDao packDao = new InMemoryPackDao(pack);

        final ReservationServiceImpl svc = new ReservationServiceImpl(userService, new TestClientService(), reservationDao,
            packDao, mailService, new TestCommerceService(), "America/Argentina/Buenos_Aires");

        assertDoesNotThrow(() -> svc.createReservation(packId, user.getId(), 1, 5.0, "pw", APP_URL));

        assertEquals(1, reservationDao.store.size(), "Reservation should be stored in DAO");
        final Reservation created = reservationDao.store.get(0);
        assertEquals(packId, created.getPackId());
        assertEquals(1, mailService.sentToCommerce);
        assertEquals(1, mailService.sentToClient);
    }

    @Test
    public void createReservation_invalidQuantity_noSideEffects() {
        final long packId = 20L;
        final long commerceUserId = 200L;
        final String email = "user2@example.org";

        final User user = new User(2L, email, "pwd", "Test User2", null, User.Role.CLIENT, false);
        final Pack pack = new Pack(packId, commerceUserId, "title", "desc", 10.0, 5.0, 5, true, Collections.emptyList());

        final UserService userService = new TestUserService(user);
        final PackDao packDao = new InMemoryPackDao(pack);

        final ReservationServiceImpl svc = new ReservationServiceImpl(userService, new TestClientService(), reservationDao,
            packDao, mailService, new TestCommerceService(), "America/Argentina/Buenos_Aires");

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
        final Pack pack = new Pack(packId, commerceUserId, "title", "desc", 10.0, 5.0, 5, true, Collections.emptyList());

        final UserService userService = new TestUserService(user);
        final PackDao packDao = new InMemoryPackDao(pack);

        final ReservationServiceImpl svc = new ReservationServiceImpl(userService, new TestClientService(), reservationDao,
            packDao, mailService, new TestCommerceService(), "America/Argentina/Buenos_Aires");

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
        final Client client = new Client(user.getId(), "Test", "User", true);

        final ReservationServiceImpl svc = new ReservationServiceImpl(
                new TestUserService(user),
                new TestClientService(),
                reservationDao,
                new InMemoryPackDao(pack),
                mailService,
                new TestCommerceService(),
                "America/Argentina/Buenos_Aires");

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
        final Client client = new Client(user.getId(), "Test", "User", true);

        final ReservationServiceImpl svc = new ReservationServiceImpl(
                new TestUserService(user),
                new TestClientService(),
                reservationDao,
                new InMemoryPackDao(pack),
                mailService,
                new TestCommerceService(),
                "America/Argentina/Buenos_Aires");

        final List<Reservation> reservations = svc.findByCommerceId(100L);
        assertEquals(2, reservations.size());
        assertTrue(reservations.stream().allMatch(r -> r.getPackId().equals(100L)));
    }
}
