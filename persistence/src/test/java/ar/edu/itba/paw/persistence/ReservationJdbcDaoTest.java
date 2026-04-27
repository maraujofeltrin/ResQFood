package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.models.pack.Pack;
import ar.edu.itba.paw.models.reservation.Reservation;
import ar.edu.itba.paw.models.user.Commerce;
import ar.edu.itba.paw.models.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.jdbc.JdbcTestUtils;

import javax.sql.DataSource;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestConfig.class)
@Sql("classpath:schema.sql")
public class ReservationJdbcDaoTest {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private ReservationJdbcDao reservationDao;

    @Autowired
    private UserJdbcDao userDao;

    @Autowired
    private ClientJdbcDao clientDao;

    @Autowired
    private CommerceJdbcDao commerceDao;

    @Autowired
    private PackJdbcDao packDao;

    private JdbcTemplate jdbcTemplate;

    private Long commerceId;
    private Long clientId;
    private Long packId;

    @BeforeEach
    public void setUp() {
        jdbcTemplate = new JdbcTemplate(dataSource);
        JdbcTestUtils.deleteFromTables(jdbcTemplate, "commerce_reviews", "bids", "auctions", "reservation_tokens", "pack_tags", "reservations", "packs", "images", "commerces", "clients", "tokens", "users");
        
        commerceId = userDao.createUser("commerce@example.com", "pass", "Commerce", "123", User.Role.COMMERCE).getId();
        commerceDao.createCommerce(commerceId, "Comm", Commerce.Category.BAKERY, "Street", 123, "City", "Prov", "1000", "08:00", "20:00");
        
        clientId = userDao.createUser("client@example.com", "pass", "Client", "123", User.Role.CLIENT).getId();
        clientDao.createClient(clientId, "Client", "Last", true);

        Pack pack = packDao.createPack(commerceId, "Pack", "Desc", 1000.0, 500.0, 10, Collections.emptyList(), null);
        packId = pack.getId();
    }

    @Test
    public void testCreateReservation() {
        // 1. Setup
        LocalDateTime now = LocalDateTime.now();

        // 2. Ejercicio
        Reservation reservation = reservationDao.createReservation(clientId, packId, now, 500.0, Reservation.Status.RESERVED, "123456", null, 2, "Window");

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
    public void testFindById() {
        // 1. Setup
        Reservation created = reservationDao.createReservation(clientId, packId, LocalDateTime.now(), 500.0, Reservation.Status.RESERVED, "CODE", null, 1, null);

        // 2. Ejercicio
        Optional<Reservation> found = reservationDao.findById(created.getId());

        // 3. Asserts
        assertTrue(found.isPresent());
        assertEquals(created.getId(), found.get().getId());
    }

    @Test
    public void testFindByCustomerId() {
        // 1. Setup
        reservationDao.createReservation(clientId, packId, LocalDateTime.now(), 500.0, Reservation.Status.RESERVED, "CODE1", null, 1, null);
        reservationDao.createReservation(clientId, packId, LocalDateTime.now(), 500.0, Reservation.Status.RESERVED, "CODE2", null, 1, null);

        // 2. Ejercicio
        List<Reservation> reservations = reservationDao.findByCustomerId(clientId);

        // 3. Asserts
        assertEquals(2, reservations.size());
    }

    @Test
    public void testFindByCommerceId() {
        // 1. Setup
        reservationDao.createReservation(clientId, packId, LocalDateTime.now(), 500.0, Reservation.Status.RESERVED, "CODE", null, 1, null);

        // 2. Ejercicio
        List<Reservation> reservations = reservationDao.findByCommerceId(commerceId);

        // 3. Asserts
        assertEquals(1, reservations.size());
        assertEquals(packId, reservations.get(0).getPackId());
    }

    @Test
    public void testUpdateStatus() {
        // 1. Setup
        Reservation created = reservationDao.createReservation(clientId, packId, LocalDateTime.now(), 500.0, Reservation.Status.RESERVED, "CODE", null, 1, null);

        // 2. Ejercicio
        Reservation updated = reservationDao.updateStatus(created.getId(), Reservation.Status.CANCELED);

        // 3. Asserts
        assertEquals(Reservation.Status.CANCELED, updated.getStatus());
        Optional<Reservation> found = reservationDao.findById(created.getId());
        assertTrue(found.isPresent());
        assertEquals(Reservation.Status.CANCELED, found.get().getStatus());
    }

    @Test
    public void testConfirmPickup() {
        // 1. Setup
        Reservation created = reservationDao.createReservation(clientId, packId, LocalDateTime.now(), 500.0, Reservation.Status.RESERVED, "CODE", null, 1, null);
        LocalDateTime confirmTime = LocalDateTime.now();

        // 2. Ejercicio
        Reservation updated = reservationDao.confirmPickup(created.getId(), confirmTime);

        // 3. Asserts
        assertEquals(Reservation.Status.PAID, updated.getStatus());
        assertNotNull(updated.getPickupConfirmationDate());
    }

    @Test
    public void testFindByPickupCode() {
        // 1. Setup
        Reservation created = reservationDao.createReservation(clientId, packId, LocalDateTime.now(), 500.0, Reservation.Status.RESERVED, "UNIQUE_CODE", null, 1, null);

        // 2. Ejercicio
        Optional<Reservation> found = reservationDao.findByPickupCode("UNIQUE_CODE");

        // 3. Asserts
        assertTrue(found.isPresent());
        assertEquals(created.getId(), found.get().getId());
    }

    @Test
    public void testHasPaidReservationWithCommerce_WhenPaidReservationExists() {
        // 1. Setup
        reservationDao.createReservation(clientId, packId, LocalDateTime.now(), 500.0, Reservation.Status.PAID, "PAID_CODE", LocalDateTime.now(), 1, null);

        // 2. Ejercicio
        final boolean result = reservationDao.hasPaidReservationWithCommerce(clientId, commerceId);

        // 3. Asserts
        assertTrue(result);
    }

    @Test
    public void testHasPaidReservationWithCommerce_WhenOnlyReservedReservationExists() {
        // 1. Setup
        reservationDao.createReservation(clientId, packId, LocalDateTime.now(), 500.0, Reservation.Status.RESERVED, "RESERVED_CODE", null, 1, null);

        // 2. Ejercicio
        final boolean result = reservationDao.hasPaidReservationWithCommerce(clientId, commerceId);

        // 3. Asserts
        assertFalse(result);
    }
}
