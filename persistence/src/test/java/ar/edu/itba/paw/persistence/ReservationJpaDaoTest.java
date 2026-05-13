package ar.edu.itba.paw.persistence;

import javax.persistence.PersistenceContext;
import ar.edu.itba.paw.models.pack.Pack;
import ar.edu.itba.paw.models.reservation.Reservation;
import ar.edu.itba.paw.models.user.Commerce;
import ar.edu.itba.paw.models.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
}
