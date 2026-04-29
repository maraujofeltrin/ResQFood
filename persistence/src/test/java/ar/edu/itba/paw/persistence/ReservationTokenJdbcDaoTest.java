package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.models.pack.Pack;
import ar.edu.itba.paw.models.reservation.Reservation;
import ar.edu.itba.paw.models.reservation.ReservationToken;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestConfig.class)
@Sql("classpath:schema.sql")
public class ReservationTokenJdbcDaoTest {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private ReservationTokenJdbcDao reservationTokenDao;

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

    private Long reservationId;

    @BeforeEach
    public void setUp() {
        jdbcTemplate = new JdbcTemplate(dataSource);
        JdbcTestUtils.deleteFromTables(jdbcTemplate, "bids", "auctions", "reservation_tokens", "pack_tags", "reservations", "client_pack_favorites", "packs", "images", "commerces", "clients", "tokens", "users");
        
        Long commerceId = userDao.createUser("commerce@example.com", "pass", "Commerce", "123", User.Role.COMMERCE).getId();
        commerceDao.createCommerce(commerceId, "Comm", Commerce.Category.BAKERY, "Street", 123, "City", "Prov", "1000", "08:00", "20:00");
        
        Long clientId = userDao.createUser("client@example.com", "pass", "Client", "123", User.Role.CLIENT).getId();
        clientDao.createClient(clientId, "Client", "Last", true);

        Pack pack = packDao.createPack(commerceId, "Pack", "Desc", 1000.0, 500.0, 10, Collections.emptyList(), null);
        Long packId = pack.getId();

        Reservation reservation = reservationDao.createReservation(clientId, packId, LocalDateTime.now(), 500.0, Reservation.Status.RESERVED, "CODE", null, 1, null);
        reservationId = reservation.getId();
    }

    @Test
    public void testCreate() {
        // 1. Setup
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expires = now.plusDays(1);

        // 2. Ejercicio
        ReservationToken token = reservationTokenDao.create("token123", reservationId, ReservationToken.Action.ACCEPT, now, expires);

        // 3. Asserts
        assertNotNull(token);
        assertEquals("token123", token.getToken());
        assertEquals(reservationId, token.getReservationId());
        assertEquals(ReservationToken.Action.ACCEPT, token.getAction());
        assertFalse(token.isUsed());
        assertEquals(1, JdbcTestUtils.countRowsInTable(jdbcTemplate, "reservation_tokens"));
    }

    @Test
    public void testFindByToken() {
        // 1. Setup
        LocalDateTime now = LocalDateTime.now();
        reservationTokenDao.create("token123", reservationId, ReservationToken.Action.ACCEPT, now, now.plusDays(1));

        // 2. Ejercicio
        Optional<ReservationToken> found = reservationTokenDao.findByToken("token123");

        // 3. Asserts
        assertTrue(found.isPresent());
        assertEquals(reservationId, found.get().getReservationId());
        assertEquals(ReservationToken.Action.ACCEPT, found.get().getAction());
    }

    @Test
    public void testMarkAsUsed() {
        // 1. Setup
        LocalDateTime now = LocalDateTime.now();
        reservationTokenDao.create("token123", reservationId, ReservationToken.Action.REJECT, now, now.plusDays(1));

        // 2. Ejercicio
        reservationTokenDao.markAsUsed("token123");

        // 3. Asserts
        Optional<ReservationToken> found = reservationTokenDao.findByToken("token123");
        assertTrue(found.isPresent());
        assertTrue(found.get().isUsed());
    }
}
