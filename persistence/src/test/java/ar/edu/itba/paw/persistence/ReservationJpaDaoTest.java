package ar.edu.itba.paw.persistence;

import javax.persistence.PersistenceContext;
import ar.edu.itba.paw.models.auction.Auction;
import ar.edu.itba.paw.models.image.Image;
import ar.edu.itba.paw.models.pack.Pack;
import ar.edu.itba.paw.models.reservation.Reservation;
import ar.edu.itba.paw.models.user.Client;
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
import java.math.BigDecimal;
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

    private static final LocalDateTime EXPIRED_AUCTION_END = LocalDateTime.of(2020, 1, 1, 0, 0);
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
    private AuctionDao auctionDao;

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
    public void testFindByIdWithPackAndCommerceReturnsPackAndCommerceData() {
        // 1. Setup
        final Reservation created = reservationDao.createReservation(clientId, packId, RESERVATION_DATE, 500.0,
                Reservation.Status.RESERVED, "CODE", null, 1, null);
        em.flush();
        em.clear();

        // 2. Ejercicio
        final Optional<Reservation> found = reservationDao.findByIdWithPackAndCommerce(created.getId());

        // 3. Asserts
        assertTrue(found.isPresent());
        assertEquals("Pack", found.get().getPack().getTitle());
        assertEquals("Comm", found.get().getPack().getCommerce().getCommercialName());
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
    public void testFindByPickupCodeReturnsReservationWithCustomerPackAndCommerce() {
        // 1. Setup
        reservationDao.createReservation(clientId, packId, RESERVATION_DATE, 500.0,
                Reservation.Status.RESERVED, "PICKUP1", null, 1, null);
        em.flush();

        // 2. Ejercicio
        final Optional<Reservation> found = reservationDao.findByPickupCode("PICKUP1");

        // 3. Asserts
        assertTrue(found.isPresent());
        assertEquals("Client Last", found.get().getCustomer().getFullName());
        assertEquals("Pack", found.get().getPack().getTitle());
        assertEquals("Comm", found.get().getPack().getCommerce().getCommercialName());
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
    public void testHasActiveReservationWhenReservedReservationExistsReturnsTrue() {
        // 1. Setup
        reservationDao.createReservation(clientId, packId, RESERVATION_DATE, 500.0, Reservation.Status.RESERVED,
                "ACTIVE_CODE", null, 1, null);
        em.flush();

        // 2. Ejercicio
        final boolean result = reservationDao.hasActiveReservation(packId, clientId);

        // 3. Asserts
        assertTrue(result);
        assertEquals(1, JdbcTestUtils.countRowsInTable(jdbcTemplate, "reservations"));
    }

    @Test
    public void testHasActiveReservationWhenOnlyPaidReservationExistsReturnsFalse() {
        // 1. Setup
        reservationDao.createReservation(clientId, packId, RESERVATION_DATE, 500.0, Reservation.Status.PAID,
                "PAID_ACTIVE_CODE", PICKUP_CONFIRMED_AT, 1, null);
        em.flush();

        // 2. Ejercicio
        final boolean result = reservationDao.hasActiveReservation(packId, clientId);

        // 3. Asserts
        assertFalse(result);
        assertEquals(1, JdbcTestUtils.countRowsInTable(jdbcTemplate, "reservations"));
    }

    @Test
    public void testCountPaidReservationsInPeriodWhenPaidPickupInsidePeriodReturnsCount() {
        // 1. Setup
        final LocalDateTime from = LocalDateTime.of(2030, 3, 1, 0, 0);
        final LocalDateTime to = LocalDateTime.of(2030, 3, 31, 0, 0);
        reservationDao.createReservation(clientId, packId, RESERVATION_DATE, 500.0, Reservation.Status.PAID,
                "PAID_IN_PERIOD", PICKUP_CONFIRMED_AT, 1, null);
        reservationDao.createReservation(clientId, packId, RESERVATION_DATE, 300.0, Reservation.Status.RESERVED,
                "RESERVED_IN_PERIOD", null, 1, null);
        em.flush();

        // 2. Ejercicio
        final int count = reservationDao.countPaidReservationsInPeriod(commerceId, from, to);

        // 3. Asserts
        assertEquals(1, count);
        assertEquals(2, JdbcTestUtils.countRowsInTable(jdbcTemplate, "reservations"));
    }

    @Test
    public void testSumRevenueInPeriodWhenPaidPickupInsidePeriodReturnsRevenue() {
        // 1. Setup
        final LocalDateTime from = LocalDateTime.of(2030, 3, 1, 0, 0);
        final LocalDateTime to = LocalDateTime.of(2030, 3, 31, 0, 0);
        reservationDao.createReservation(clientId, packId, RESERVATION_DATE, 500.0, Reservation.Status.PAID,
                "PAID_REVENUE", PICKUP_CONFIRMED_AT, 1, null);
        reservationDao.createReservation(clientId, packId, RESERVATION_DATE, 300.0, Reservation.Status.RESERVED,
                "RESERVED_REVENUE", null, 1, null);
        em.flush();

        // 2. Ejercicio
        final BigDecimal revenue = reservationDao.sumRevenueInPeriod(commerceId, from, to);

        // 3. Asserts
        assertEquals(0, BigDecimal.valueOf(500.0).compareTo(revenue));
        assertEquals(2, JdbcTestUtils.countRowsInTable(jdbcTemplate, "reservations"));
    }




    @Test
    public void testFilterReservationsReturnsCustomerPackAndCommerceData() {
        // 1. Setup
        reservationDao.createReservation(clientId, packId, RESERVATION_DATE, 500.0,
                Reservation.Status.RESERVED, "CODE", null, 1, null);
        em.flush();

        // 2. Ejercicio
        final List<Reservation> results = reservationDao.filterReservations(commerceId, null, null, null, false, 1, 10);

        // 3. Asserts
        assertEquals(1, results.size());
        assertEquals("Client Last", results.get(0).getCustomer().getFullName());
        assertEquals("Pack", results.get(0).getPack().getTitle());
        assertEquals("Comm", results.get(0).getPack().getCommerce().getCommercialName());
        assertEquals(1, JdbcTestUtils.countRowsInTable(jdbcTemplate, "reservations"));
    }

    @Test
    public void testFilterReservationsWhenSecondPageRequestedReturnsOldestReservation() {
        // 1. Setup
        final LocalDateTime oldest = LocalDateTime.of(2030, 1, 1, 10, 0);
        final LocalDateTime middle = LocalDateTime.of(2030, 2, 1, 10, 0);
        final LocalDateTime newest = LocalDateTime.of(2030, 3, 1, 10, 0);

        final Reservation oldestReservation = reservationDao.createReservation(clientId, packId, oldest, 500.0,
                Reservation.Status.RESERVED, "OLD", null, 1, null);
        reservationDao.createReservation(clientId, packId, middle, 500.0,
                Reservation.Status.RESERVED, "MID", null, 1, null);
        reservationDao.createReservation(clientId, packId, newest, 500.0,
                Reservation.Status.RESERVED, "NEW", null, 1, null);
        em.flush();

        // 2. Ejercicio
        final List<Reservation> pageTwo = reservationDao.filterReservations(commerceId, null, null, null, false, 2, 2);

        // 3. Asserts
        assertEquals(1, pageTwo.size());
        assertEquals(oldest, pageTwo.get(0).getReservationDate());
        assertEquals(oldestReservation.getId(), pageTwo.get(0).getId());
        assertEquals(3, JdbcTestUtils.countRowsInTable(jdbcTemplate, "reservations"));
    }


    @Test
    public void testFilterReservationsReturnsAuctionWhenPackIsAuction() {
        // 1. Setup
        final Pack auctionPack = packDao.createPack(commerceId, "Auction", "Desc", 100.0, 50.0, 5, null, null);
        em.flush();
        auctionDao.createAuction(auctionPack.getId(), 10.0, 1.0, EXPIRED_AUCTION_END);
        em.flush();
        reservationDao.createReservation(clientId, auctionPack.getId(), LocalDateTime.now(),
                50.0, Reservation.Status.RESERVED, "code-auction", null, 1, null);
        em.flush();
        em.clear();

        // 2. Ejercicio
        final List<Reservation> reservations = reservationDao.filterReservations(
                commerceId, null, null, null, false, 1, 10);

        // 3. Asserts
        assertEquals(1, reservations.size());
        assertEquals(auctionPack.getId(), reservations.get(0).getPack().getId());
        assertNotNull(reservations.get(0).getPack().getAuction());
        assertEquals(Auction.Status.ACTIVE, reservations.get(0).getPack().getAuction().getStatus());
        assertEquals(1, JdbcTestUtils.countRowsInTable(jdbcTemplate, "reservations"));
        assertEquals(1, JdbcTestUtils.countRowsInTable(jdbcTemplate, "auctions"));
    }

    @Test
    public void testFilterReservationsLeavesAuctionNullWhenPackIsDirect() {
        // 1. Setup
        final Pack directPack = packDao.createPack(commerceId, "Direct", "Desc", 100.0, 50.0, 5, null, null);
        em.flush();
        reservationDao.createReservation(clientId, directPack.getId(), LocalDateTime.now(),
                50.0, Reservation.Status.RESERVED, "code-direct", null, 1, null);
        em.flush();
        em.clear();

        // 2. Ejercicio
        final List<Reservation> reservations = reservationDao.filterReservations(
                commerceId, null, null, null, false, 1, 10);

        // 3. Asserts
        assertEquals(1, reservations.size());
        assertEquals(directPack.getId(), reservations.get(0).getPack().getId());
        assertNull(reservations.get(0).getPack().getAuction());
        assertEquals(1, JdbcTestUtils.countRowsInTable(jdbcTemplate, "reservations"));
    }



    @Test
    public void testFindByIdWithDetailsReturnsCustomerUserAndCommerceUser() {
        // 1. Setup
        final Reservation created = reservationDao.createReservation(clientId, packId, RESERVATION_DATE, 500.0,
                Reservation.Status.RESERVED, "DETAIL_CODE", null, 1, null);
        em.flush();
        em.clear();

        // 2. Ejercicio
        final Optional<Reservation> found = reservationDao.findByIdWithDetails(created.getId());

        // 3. Asserts
        assertTrue(found.isPresent());
        assertEquals("client@example.com", found.get().getCustomer().getUser().getEmail());
        assertEquals("commerce@example.com", found.get().getPack().getCommerce().getUser().getEmail());
        assertEquals(1, JdbcTestUtils.countRowsInTable(jdbcTemplate, "reservations"));
    }

    @Test
    public void testFindByPackIdReturnsReservationsWithCustomerData() {
        // 1. Setup
        reservationDao.createReservation(clientId, packId, RESERVATION_DATE, 500.0,
                Reservation.Status.RESERVED, "CODE", null, 1, null);
        em.flush();

        // 2. Ejercicio
        final List<Reservation> results = reservationDao.findByPackId(packId, 1, 10);

        // 3. Asserts
        assertEquals(1, results.size());
        assertEquals("Client Last", results.get(0).getCustomer().getFullName());
        assertEquals(1, JdbcTestUtils.countRowsInTable(jdbcTemplate, "reservations"));
    }

    @Test
    public void testFindByPackIdWhenSecondPageRequestedReturnsNextReservation() {
        // 1. Setup
        final LocalDateTime newer = LocalDateTime.of(2030, 6, 10, 12, 0);
        final LocalDateTime older = LocalDateTime.of(2030, 6, 9, 12, 0);
        reservationDao.createReservation(clientId, packId, newer, 500.0,
                Reservation.Status.RESERVED, "CODE1", null, 1, null);
        reservationDao.createReservation(clientId, packId, older, 500.0,
                Reservation.Status.RESERVED, "CODE2", null, 1, null);
        em.flush();

        // 2. Ejercicio
        final List<Reservation> pageTwo = reservationDao.findByPackId(packId, 2, 1);

        // 3. Asserts
        assertEquals(1, pageTwo.size());
        assertEquals(older, pageTwo.get(0).getReservationDate());
    }

    @Test
    public void testFindTopSellingPacksReturnsPackEntityAndUnitsSoldOrdered() {
        // 1. Setup
        final LocalDateTime periodStart = LocalDateTime.of(2030, 3, 1, 0, 0);
        final LocalDateTime periodEnd = LocalDateTime.of(2030, 3, 10, 0, 0);
        final Pack lowSeller = packDao.createPack(commerceId, "Low Seller", "Desc", 100.0, 50.0, 10,
                Collections.emptyList(), null);
        final Pack topSeller = packDao.createPack(commerceId, "Top Seller", "Desc", 100.0, 50.0, 10,
                Collections.emptyList(), null);
        reservationDao.createReservation(clientId, lowSeller.getId(), RESERVATION_DATE, 100.0,
                Reservation.Status.PAID, "LOW1", PICKUP_CONFIRMED_AT, 2, null);
        reservationDao.createReservation(clientId, topSeller.getId(), RESERVATION_DATE, 250.0,
                Reservation.Status.PAID, "TOP1", PICKUP_CONFIRMED_AT, 5, null);
        em.flush();

        // 2. Ejercicio
        final List<Object[]> rows = reservationDao.findTopSellingPacks(commerceId, periodStart, periodEnd, 3);

        // 3. Asserts
        assertEquals(2, rows.size());
        assertInstanceOf(Pack.class, rows.get(0)[0]);
        assertEquals(topSeller.getId(), ((Pack) rows.get(0)[0]).getId());
        assertEquals("Top Seller", ((Pack) rows.get(0)[0]).getTitle());
        assertEquals(5L, ((Number) rows.get(0)[1]).longValue());
        assertInstanceOf(Pack.class, rows.get(1)[0]);
        assertEquals(lowSeller.getId(), ((Pack) rows.get(1)[0]).getId());
        assertEquals(2L, ((Number) rows.get(1)[1]).longValue());
    }

    @Test
    public void testFindTopClientsByPaidReservationsReturnsClientEntityAndCountOrdered() {
        // 1. Setup
        final LocalDateTime periodStart = LocalDateTime.of(2030, 3, 1, 0, 0);
        final LocalDateTime periodEnd = LocalDateTime.of(2030, 3, 10, 0, 0);
        final Long secondClientId = userDao.createUser("client2@example.com", "pass", "Second", "123", User.Role.CLIENT)
                .getId();
        clientDao.createClient(secondClientId, "Second", "Client", true);
        reservationDao.createReservation(clientId, packId, RESERVATION_DATE, 500.0,
                Reservation.Status.PAID, "C1", PICKUP_CONFIRMED_AT, 1, null);
        reservationDao.createReservation(secondClientId, packId, RESERVATION_DATE, 500.0,
                Reservation.Status.PAID, "C2A", PICKUP_CONFIRMED_AT, 1, null);
        reservationDao.createReservation(secondClientId, packId, RESERVATION_DATE, 500.0,
                Reservation.Status.PAID, "C2B", PICKUP_CONFIRMED_AT, 1, null);
        em.flush();

        // 2. Ejercicio
        final List<Object[]> rows = reservationDao.findTopClientsByPaidReservations(commerceId, periodStart, periodEnd, 3);

        // 3. Asserts
        assertEquals(2, rows.size());
        assertInstanceOf(Client.class, rows.get(0)[0]);
        assertEquals(secondClientId, ((Client) rows.get(0)[0]).getUserId());
        assertEquals(2L, ((Number) rows.get(0)[1]).longValue());
        assertInstanceOf(Client.class, rows.get(1)[0]);
        assertEquals(clientId, ((Client) rows.get(1)[0]).getUserId());
        assertEquals(1L, ((Number) rows.get(1)[1]).longValue());
    }
}
