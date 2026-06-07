package ar.edu.itba.paw.persistence;

import javax.persistence.PersistenceContext;
import ar.edu.itba.paw.models.image.Image;
import ar.edu.itba.paw.models.pack.Pack;
import ar.edu.itba.paw.models.reservation.Reservation;
import ar.edu.itba.paw.models.user.Commerce;
import ar.edu.itba.paw.models.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.jdbc.JdbcTestUtils;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@Rollback
@Transactional
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestConfig.class)
public class ReservationJpaDaoTest {

    private static final LocalDateTime RESERVATION_DATE = LocalDateTime.of(2030, 3, 1, 12, 0);
    private static final LocalDateTime PICKUP_CONFIRMED_AT = LocalDateTime.of(2030, 3, 5, 18, 0);
    private static final LocalDateTime CONFIRM_PICKUP_AT = LocalDateTime.of(2030, 4, 1, 10, 30);

    @Autowired
    private DataSource dataSource;

    @Autowired
    
    private ReservationDao reservationDao;

    @Autowired
    
    private UserDao userDao;

    @Autowired
    
    private ClientDao clientDao;

    @Autowired
    
    private CommerceDao commerceDao;

    @Autowired
    private PackDao packDao;

    @Autowired
    private ImageDao imageDao;

    @PersistenceContext
    private javax.persistence.EntityManager em;

    private JdbcTemplate jdbcTemplate;

    private Long commerceId;
    private Long clientId;
    private Long packId;

    @BeforeEach
    public void setUp() {
        jdbcTemplate = new JdbcTemplate(dataSource);
        JdbcTestUtils.deleteFromTables(jdbcTemplate, "commerce_reviews", "bids", "auctions", "reservation_tokens",
                "pack_tags", "reservations", "client_pack_favorites", "packs", "images", "commerces", "clients",
                "tokens", "users");

        commerceId = userDao.createUser("commerce@example.com", "pass", "Commerce", "123", User.Role.COMMERCE).getId();
        commerceDao.createCommerce(commerceId, "Comm", Commerce.Category.BAKERY, "Street", 123,
                ar.edu.itba.paw.models.pack.Municipality.AVELLANEDA, "Prov", "1000", "08:00", "20:00");

        clientId = userDao.createUser("client@example.com", "pass", "Client", "123", User.Role.CLIENT).getId();
        clientDao.createClient(clientId, "Client", "Last", true);

        final Pack pack = packDao.createPack(commerceId, "Pack", "Desc", 1000.0, 500.0, 10, Collections.emptyList(),
                null);
        packId = pack.getId();
    }

    @Test
    public void testCreateReservationWhenPackAndClientExist() {
        // 1. Setup
        // packId and clientId from setUp().

        // 2. Ejercicio
        final Reservation reservation = reservationDao.createReservation(clientId, packId, RESERVATION_DATE, 500.0,
                Reservation.Status.RESERVED, "123456", null, 2, "Window");
        em.flush();

        // 3. Asserts
        assertNotNull(reservation);
        assertEquals(clientId, reservation.getCustomerId());
        assertEquals(packId, reservation.getPackId());
        assertEquals(Reservation.Status.RESERVED, reservation.getStatus());
        assertEquals("123456", reservation.getPickupCode());
        assertEquals(2, reservation.getQuantity());
        assertEquals(1, JdbcTestUtils.countRowsInTable(jdbcTemplate, "reservations"));
    }

    @Test
    public void testFindByIdWhenReservationExists() {
        // 1. Setup
        final Reservation created = reservationDao.createReservation(clientId, packId, RESERVATION_DATE, 500.0,
                Reservation.Status.RESERVED, "CODE", null, 1, null);
        em.flush();

        // 2. Ejercicio
        final Optional<Reservation> found = reservationDao.findById(created.getId());

        // 3. Asserts
        assertTrue(found.isPresent());
        assertEquals(created.getId(), found.get().getId());
        assertEquals(1, JdbcTestUtils.countRowsInTable(jdbcTemplate, "reservations"));
    }

    @Test
    public void testFindByCustomerIdWhenTwoReservationsExist() {
        // 1. Setup
        reservationDao.createReservation(clientId, packId, RESERVATION_DATE, 500.0, Reservation.Status.RESERVED,
                "CODE1", null, 1, null);
        reservationDao.createReservation(clientId, packId, RESERVATION_DATE, 500.0, Reservation.Status.RESERVED,
                "CODE2", null, 1, null);
        em.flush();

        // 2. Ejercicio
        final List<Reservation> reservations = reservationDao.findByCustomerId(clientId);

        // 3. Asserts
        assertEquals(2, reservations.size());
        assertEquals(2, JdbcTestUtils.countRowsInTable(jdbcTemplate, "reservations"));
    }

    @Test
    public void testFindByCommerceIdWhenOneReservationExists() {
        // 1. Setup
        reservationDao.createReservation(clientId, packId, RESERVATION_DATE, 500.0, Reservation.Status.RESERVED, "CODE",
                null, 1, null);
        em.flush();

        // 2. Ejercicio
        final List<Reservation> reservations = reservationDao.findByCommerceId(commerceId);

        // 3. Asserts
        assertEquals(1, reservations.size());
        assertEquals(packId, reservations.get(0).getPackId());
        assertEquals(1, JdbcTestUtils.countRowsInTable(jdbcTemplate, "reservations"));
    }

    @Test
    public void testUpdateStatusWhenReservationExists() {
        // 1. Setup
        final Reservation created = reservationDao.createReservation(clientId, packId, RESERVATION_DATE, 500.0,
                Reservation.Status.RESERVED, "CODE", null, 1, null);

        // 2. Ejercicio
        final Reservation updated = reservationDao.updateStatus(created.getId(), Reservation.Status.CANCELED);
        em.flush();
        em.clear();

        // 3. Asserts
        assertEquals(Reservation.Status.CANCELED, updated.getStatus());
        assertEquals(1, JdbcTestUtils.countRowsInTable(jdbcTemplate, "reservations"));
    }

    @Test
    public void testConfirmPickupWhenReservationReserved() {
        // 1. Setup
        final Reservation created = reservationDao.createReservation(clientId, packId, RESERVATION_DATE, 500.0,
                Reservation.Status.RESERVED, "CODE", null, 1, null);

        // 2. Ejercicio
        final Reservation updated = reservationDao.confirmPickup(created.getId(), CONFIRM_PICKUP_AT);
        em.flush();
        em.clear();

        // 3. Asserts
        assertEquals(Reservation.Status.PAID, updated.getStatus());
        assertNotNull(updated.getPickupConfirmationDate());
        assertEquals(CONFIRM_PICKUP_AT, updated.getPickupConfirmationDate());
        assertEquals(1, JdbcTestUtils.countRowsInTable(jdbcTemplate, "reservations"));
    }

    @Test
    public void testFindByPickupCodeWhenReservationExists() {
        // 1. Setup
        final Reservation created = reservationDao.createReservation(clientId, packId, RESERVATION_DATE, 500.0,
                Reservation.Status.RESERVED, "UNIQUE_CODE", null, 1, null);
        em.flush();

        // 2. Ejercicio
        final Optional<Reservation> found = reservationDao.findByPickupCode("UNIQUE_CODE");

        // 3. Asserts
        assertTrue(found.isPresent());
        assertEquals(created.getId(), found.get().getId());
        assertEquals(1, JdbcTestUtils.countRowsInTable(jdbcTemplate, "reservations"));
    }

    @Test
    public void testHasPaidReservationWithCommerceWhenPaidReservationExists() {
        // 1. Setup
        reservationDao.createReservation(clientId, packId, RESERVATION_DATE, 500.0, Reservation.Status.PAID,
                "PAID_CODE", PICKUP_CONFIRMED_AT, 1, null);
        em.flush();

        // 2. Ejercicio
        final boolean result = reservationDao.hasPaidReservationWithCommerce(clientId, commerceId);

        // 3. Asserts
        assertTrue(result);
        assertEquals(1, JdbcTestUtils.countRowsInTable(jdbcTemplate, "reservations"));
    }

    @Test
    public void testFilterReservationsByCommerceIdAndPackTitleQuery() {
        // 1. Setup
        reservationDao.createReservation(clientId, packId, RESERVATION_DATE, 500.0,
                Reservation.Status.RESERVED, "CODE1", null, 1, null);
        em.flush();

        // 2. Ejercicio
        final List<Reservation> filtered = reservationDao.filterReservations(commerceId, null, "Pack", null,
                false, 1, 10);

        // 3. Asserts
        assertEquals(1, filtered.size());
        assertEquals(packId, filtered.get(0).getPackId());
        assertEquals(1, JdbcTestUtils.countRowsInTable(jdbcTemplate, "reservations"));
    }

    @Test
    public void testCountFilteredReservationsByCommerceIdAndPackTitleQuery() {
        // 1. Setup
        reservationDao.createReservation(clientId, packId, RESERVATION_DATE, 500.0,
                Reservation.Status.RESERVED, "CODE1", null, 1, null);
        em.flush();

        // 2. Ejercicio
        final int count = reservationDao.countFilteredReservations(commerceId, null, "Pack", null, false);

        // 3. Asserts
        assertEquals(1, count);
        assertEquals(1, JdbcTestUtils.countRowsInTable(jdbcTemplate, "reservations"));
    }

    @Test
    public void testFilterReservationsByCommerceIdAndClientNameQuery() {
        // 1. Setup
        reservationDao.createReservation(clientId, packId, RESERVATION_DATE, 500.0,
                Reservation.Status.RESERVED, "CODE2", null, 1, null);
        em.flush();

        // 2. Ejercicio
        final List<Reservation> filtered = reservationDao.filterReservations(commerceId, null, "Client Last", null,
                false, 1, 10);

        // 3. Asserts
        assertEquals(1, filtered.size());
        assertEquals(clientId, filtered.get(0).getCustomerId());
        assertEquals(1, JdbcTestUtils.countRowsInTable(jdbcTemplate, "reservations"));
    }

    @Test
    public void testCountFilteredReservationsByCommerceIdAndClientNameQuery() {
        // 1. Setup
        reservationDao.createReservation(clientId, packId, RESERVATION_DATE, 500.0,
                Reservation.Status.RESERVED, "CODE2", null, 1, null);
        em.flush();

        // 2. Ejercicio
        final int count = reservationDao.countFilteredReservations(commerceId, null, "Client Last", null, false);

        // 3. Asserts
        assertEquals(1, count);
        assertEquals(1, JdbcTestUtils.countRowsInTable(jdbcTemplate, "reservations"));
    }

    @Test
    public void testHasPaidReservationWithCommerceWhenOnlyReservedExists() {
        // 1. Setup
        reservationDao.createReservation(clientId, packId, RESERVATION_DATE, 500.0, Reservation.Status.RESERVED,
                "RESERVED_CODE", null, 1, null);
        em.flush();

        // 2. Ejercicio
        final boolean result = reservationDao.hasPaidReservationWithCommerce(clientId, commerceId);

        // 3. Asserts
        assertFalse(result);
        assertEquals(1, JdbcTestUtils.countRowsInTable(jdbcTemplate, "reservations"));
    }

    @Test
    public void testFilterReservationsEagerlyLoadsPackImage() {
        // 1. Setup
        final Image image = imageDao.saveImage(new byte[] {1, 2, 3}, "image/png");
        em.flush();
        final Pack packWithImage = packDao.createPack(commerceId, "Pack With Image", "Desc", 1000.0, 500.0, 10,
                Collections.emptyList(), image.getId());
        em.flush();
        reservationDao.createReservation(clientId, packWithImage.getId(), RESERVATION_DATE, 500.0,
                Reservation.Status.RESERVED, "IMG-CODE", null, 1, null);
        em.flush();
        em.clear();

        // 2. Ejercicio
        final List<Reservation> results = reservationDao.filterReservations(commerceId, null, null, null, false, 1, 10);

        // 3. Asserts
        assertEquals(1, results.size());
        final Reservation r = results.get(0);
        assertTrue(Hibernate.isInitialized(r.getPack().getImage()));
        assertNotNull(r.getPack().getImageId());
        assertEquals(image.getId(), r.getPack().getImageId());
        assertEquals(1, JdbcTestUtils.countRowsInTable(jdbcTemplate, "reservations"));
        assertEquals(1, JdbcTestUtils.countRowsInTable(jdbcTemplate, "images"));
    }

    @Test
    public void testFilterReservationsEagerlyLoadsCustomerAndPack() {
        // 1. Setup
        reservationDao.createReservation(clientId, packId, RESERVATION_DATE, 500.0,
                Reservation.Status.RESERVED, "CODE", null, 1, null);
        em.flush();

        // 2. Ejercicio
        final List<Reservation> results = reservationDao.filterReservations(commerceId, null, null, null, false, 1, 10);

        // 3. Asserts
        assertFalse(results.isEmpty());
        final Reservation r = results.get(0);
        assertTrue(Hibernate.isInitialized(r.getCustomer()));
        assertNotNull(r.getCustomer().getFullName());
        assertTrue(Hibernate.isInitialized(r.getPack()));
        assertNotNull(r.getPack().getTitle());
        assertTrue(Hibernate.isInitialized(r.getPack().getCommerce()));
        assertNotNull(r.getPack().getCommerce().getCommercialName());
        assertEquals(1, JdbcTestUtils.countRowsInTable(jdbcTemplate, "reservations"));
    }

    @Test
    public void testFindByPickupCodeEagerlyLoadsCustomerPackAndCommerce() {
        // 1. Setup
        reservationDao.createReservation(clientId, packId, RESERVATION_DATE, 500.0,
                Reservation.Status.RESERVED, "PICKUP1", null, 1, null);
        em.flush();

        // 2. Ejercicio
        final Optional<Reservation> found = reservationDao.findByPickupCode("PICKUP1");

        // 3. Asserts
        assertTrue(found.isPresent());
        final Reservation r = found.get();
        assertTrue(Hibernate.isInitialized(r.getCustomer()));
        assertNotNull(r.getCustomer().getFullName());
        assertTrue(Hibernate.isInitialized(r.getPack()));
        assertNotNull(r.getPack().getTitle());
        assertTrue(Hibernate.isInitialized(r.getPack().getCommerce()));
        assertNotNull(r.getPack().getCommerce().getCommercialName());
        assertEquals(1, JdbcTestUtils.countRowsInTable(jdbcTemplate, "reservations"));
    }

    @Test
    public void testFilterReservationsReturnsPageInQueryOneOrder() {
        // 1. Setup
        final LocalDateTime oldest = LocalDateTime.of(2030, 1, 1, 10, 0);
        final LocalDateTime middle = LocalDateTime.of(2030, 2, 1, 10, 0);
        final LocalDateTime newest = LocalDateTime.of(2030, 3, 1, 10, 0);

        final Reservation oldestReservation = reservationDao.createReservation(clientId, packId, oldest, 500.0,
                Reservation.Status.RESERVED, "OLD", null, 1, null);
        final Reservation middleReservation = reservationDao.createReservation(clientId, packId, middle, 500.0,
                Reservation.Status.RESERVED, "MID", null, 1, null);
        reservationDao.createReservation(clientId, packId, newest, 500.0,
                Reservation.Status.RESERVED, "NEW", null, 1, null);
        em.flush();

        // 2. Ejercicio
        final List<Reservation> pageOne = reservationDao.filterReservations(commerceId, null, null, null, false, 1, 2);
        final List<Reservation> pageTwo = reservationDao.filterReservations(commerceId, null, null, null, false, 2, 2);

        // 3. Asserts
        assertEquals(2, pageOne.size());
        assertEquals(newest, pageOne.get(0).getReservationDate());
        assertEquals(middle, pageOne.get(1).getReservationDate());

        assertEquals(1, pageTwo.size());
        assertEquals(oldest, pageTwo.get(0).getReservationDate());
        assertEquals(oldestReservation.getId(), pageTwo.get(0).getId());
        assertNotEquals(middleReservation.getId(), pageTwo.get(0).getId());
        assertEquals(3, JdbcTestUtils.countRowsInTable(jdbcTemplate, "reservations"));
    }

    @Test
    public void testFindByPackIdEagerlyLoadsCustomer() {
        // 1. Setup
        reservationDao.createReservation(clientId, packId, RESERVATION_DATE, 500.0,
                Reservation.Status.RESERVED, "CODE", null, 1, null);
        em.flush();

        // 2. Ejercicio
        final List<Reservation> results = reservationDao.findByPackId(packId);

        // 3. Asserts
        assertFalse(results.isEmpty());
        final Reservation r = results.get(0);
        assertTrue(Hibernate.isInitialized(r.getCustomer()));
        assertNotNull(r.getCustomer().getFullName());
        assertEquals(1, JdbcTestUtils.countRowsInTable(jdbcTemplate, "reservations"));
    }
}
