package ar.edu.itba.paw.persistence;

import javax.persistence.PersistenceContext;
import ar.edu.itba.paw.models.pack.Pack;
import ar.edu.itba.paw.models.reservation.Reservation;
import ar.edu.itba.paw.models.reservation.ReservationToken;
import ar.edu.itba.paw.models.user.Commerce;
import ar.edu.itba.paw.models.user.User;
import org.hibernate.Hibernate;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@Rollback
@Transactional
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestConfig.class)
public class ReservationTokenJpaDaoTest {

    private static final LocalDateTime RESERVATION_DATE = LocalDateTime.of(2030, 3, 1, 12, 0);
    private static final LocalDateTime TOKEN_CREATED = LocalDateTime.of(2030, 3, 10, 9, 0);
    private static final LocalDateTime TOKEN_EXPIRES = LocalDateTime.of(2030, 3, 11, 9, 0);

    @Autowired
    private DataSource dataSource;

    @Autowired
    
    private ReservationTokenDao reservationTokenDao;

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

    private Long reservationId;

    @BeforeEach
    public void setUp() {
        jdbcTemplate = new JdbcTemplate(dataSource);
        JdbcTestUtils.deleteFromTables(jdbcTemplate, "commerce_reviews", "bids", "auctions", "reservation_tokens",
                "pack_tags", "reservations", "client_pack_favorites", "packs", "images", "commerces", "clients",
                "tokens",
                "users");

        final Long commerceId = userDao
                .createUser("commerce@example.com", "pass", "Commerce", "123", User.Role.COMMERCE).getId();
        commerceDao.createCommerce(commerceId, "Comm", Commerce.Category.BAKERY, "Street", 123,
                ar.edu.itba.paw.models.pack.Municipality.AVELLANEDA, "Prov", "1000", "08:00", "20:00");

        final Long clientId = userDao.createUser("client@example.com", "pass", "Client", "123", User.Role.CLIENT)
                .getId();
        clientDao.createClient(clientId, "Client", "Last", true);

        final Pack pack = packDao.createPack(commerceId, "Pack", "Desc", 1000.0, 500.0, 10, Collections.emptyList(),
                null);
        final Long packId = pack.getId();

        final Reservation reservation = reservationDao.createReservation(clientId, packId, RESERVATION_DATE, 500.0,
                Reservation.Status.RESERVED, "CODE", null, 1, null);
        reservationId = reservation.getId();
        em.flush();
    }

    @Test
    public void testCreateWhenReservationExists() {
        // 1. Setup
        // reservationId from setUp().

        // 2. Ejercicio
        final ReservationToken token = reservationTokenDao.create("token123", reservationId,
                ReservationToken.Action.ACCEPT, TOKEN_CREATED, TOKEN_EXPIRES);
        em.flush();

        // 3. Asserts
        assertNotNull(token);
        assertEquals("token123", token.getToken());
        assertEquals(reservationId, token.getReservationId());
        assertEquals(ReservationToken.Action.ACCEPT, token.getAction());
        assertFalse(token.isUsed());
        assertEquals(1, JdbcTestUtils.countRowsInTable(jdbcTemplate, "reservation_tokens"));
    }

    @Test
    public void testFindByTokenWhenTokenExists() {
        // 1. Setup
        reservationTokenDao.create("token123", reservationId, ReservationToken.Action.ACCEPT, TOKEN_CREATED,
                TOKEN_EXPIRES);
        em.flush();

        // 2. Ejercicio
        final Optional<ReservationToken> found = reservationTokenDao.findByToken("token123");

        // 3. Asserts
        assertTrue(found.isPresent());
        assertEquals(reservationId, found.get().getReservationId());
        assertEquals(ReservationToken.Action.ACCEPT, found.get().getAction());
        assertEquals(1, JdbcTestUtils.countRowsInTable(jdbcTemplate, "reservation_tokens"));
    }

    @Test
    public void testFindByTokenEagerlyLoadsReservationAndPack() {
        // 1. Setup
        reservationTokenDao.create("token123", reservationId, ReservationToken.Action.ACCEPT, TOKEN_CREATED,
                TOKEN_EXPIRES);
        em.flush();
        em.clear();

        // 2. Ejercicio
        final Optional<ReservationToken> found = reservationTokenDao.findByToken("token123");

        // 3. Asserts
        assertTrue(found.isPresent());
        assertTrue(Hibernate.isInitialized(found.get().getReservation()));
        assertNotNull(found.get().getReservation().getPack());
        assertTrue(Hibernate.isInitialized(found.get().getReservation().getPack()));
        assertNotNull(found.get().getReservation().getPack().getCommerce());
        assertTrue(Hibernate.isInitialized(found.get().getReservation().getPack().getCommerce()));
        assertEquals(1, JdbcTestUtils.countRowsInTable(jdbcTemplate, "reservation_tokens"));
    }

    @Test
    public void testMarkAsUsedWhenTokenExists() {
        // 1. Setup
        reservationTokenDao.create("token123", reservationId, ReservationToken.Action.REJECT, TOKEN_CREATED,
                TOKEN_EXPIRES);

        // 2. Ejercicio
        reservationTokenDao.markAsUsed("token123");
        em.flush();
        em.clear();

        // 3. Asserts
        final Optional<ReservationToken> found = reservationTokenDao.findByToken("token123");
        assertTrue(found.isPresent());
        assertTrue(found.get().isUsed());
        assertEquals(1, JdbcTestUtils.countRowsInTable(jdbcTemplate, "reservation_tokens"));
    }
}
