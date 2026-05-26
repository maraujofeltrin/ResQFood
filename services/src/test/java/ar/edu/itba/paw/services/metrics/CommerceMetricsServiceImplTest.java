package ar.edu.itba.paw.services.metrics;

import ar.edu.itba.paw.models.pack.Municipality;
import ar.edu.itba.paw.models.pack.Pack;
import ar.edu.itba.paw.models.user.Commerce;
import ar.edu.itba.paw.models.reservation.Reservation;
import ar.edu.itba.paw.persistence.PackDao;
import ar.edu.itba.paw.persistence.ReservationDao;
import ar.edu.itba.paw.services.user.ClientService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommerceMetricsServiceImplTest {

    private static final Long COMMERCE_ID = 10L;
    private static final ZoneId BUSINESS_ZONE = ZoneId.of("UTC");

    @Mock
    private ReservationDao reservationDao;

    @Mock
    private PackDao packDao;

    @Mock
    private ClientService clientService;

    private CommerceMetricsServiceImpl commerceMetricsService;

    @BeforeEach
    void setUp() {
        commerceMetricsService = new CommerceMetricsServiceImpl(reservationDao, packDao, clientService, BUSINESS_ZONE);
    }

    @Test
    void testGetCommerceMetricsWhenMultiDayPeriodBuildsDailySeriesWithGapsFilled() {
        // 1. Setup
        final LocalDateTime from = LocalDateTime.of(2030, 1, 1, 0, 0);
        final LocalDateTime to = LocalDateTime.of(2030, 1, 3, 0, 0);
        stubGetCommerceMetrics(
                COMMERCE_ID,
                from,
                to,
                Collections.singletonList(new Object[] {LocalDate.of(2030, 1, 1), 5L}),
                0,
                BigDecimal.ZERO,
                Optional.empty(),
                0L,
                0L);

        // 2. Ejercicio
        final CommerceMetrics result = commerceMetricsService.getCommerceMetrics(COMMERCE_ID, from, to);

        // 3. Asserts
        assertEquals(3, result.getDailySales().size());
        assertEquals("2030-01-01", result.getDailySales().get(0).getDate());
        assertEquals(5L, result.getDailySales().get(0).getCount());
        assertEquals("2030-01-02", result.getDailySales().get(1).getDate());
        assertEquals(0L, result.getDailySales().get(1).getCount());
        assertEquals("2030-01-03", result.getDailySales().get(2).getDate());
        assertEquals(0L, result.getDailySales().get(2).getCount());
    }

    @Test
    void testGetCommerceMetricsWhenDaoReturnsTotals() {
        // 1. Setup
        final LocalDateTime from = LocalDateTime.of(2030, 5, 1, 12, 0);
        final LocalDateTime to = LocalDateTime.of(2030, 5, 2, 12, 0);
        stubGetCommerceMetrics(
                COMMERCE_ID,
                from,
                to,
                Collections.emptyList(),
                9,
                new BigDecimal("12.34"),
                Optional.empty(),
                0L,
                0L);

        // 2. Ejercicio
        final CommerceMetrics result = commerceMetricsService.getCommerceMetrics(COMMERCE_ID, from, to);

        // 3. Asserts
        assertEquals(9, result.getTotalReservations());
        assertEquals(0, new BigDecimal("12.34").compareTo(result.getTotalRevenue()));
    }

    @Test
    void testGetCommerceMetricsWhenBestSellingPackExists() {
        // 1. Setup
        final LocalDateTime from = LocalDateTime.of(2030, 6, 1, 0, 0);
        final LocalDateTime to = LocalDateTime.of(2030, 6, 2, 0, 0);
        stubGetCommerceMetrics(
                COMMERCE_ID,
                from,
                to,
                Collections.emptyList(),
                0,
                BigDecimal.ZERO,
                Optional.of(100L),
                0L,
                0L);
        final Pack pack = new Pack(100L, new Commerce(COMMERCE_ID, "Comm", Commerce.Category.BAKERY, "St", 1,
                Municipality.AVELLANEDA, "P", "1000", "08:00", "20:00"), "Star Pack", "d", 1.0, 1.0, 1, true, null);
        when(packDao.findById(100L)).thenReturn(Optional.of(pack));

        // 2. Ejercicio
        final CommerceMetrics result = commerceMetricsService.getCommerceMetrics(COMMERCE_ID, from, to);

        // 3. Asserts
        assertEquals("Star Pack", result.getBestSellingPackTitle());
    }

    @Test
    void testGetCommerceMetricsWhenBestSellingPackIdAbsent() {
        // 1. Setup
        final LocalDateTime from = LocalDateTime.of(2030, 7, 1, 0, 0);
        final LocalDateTime to = LocalDateTime.of(2030, 7, 2, 0, 0);
        stubGetCommerceMetrics(
                COMMERCE_ID,
                from,
                to,
                Collections.emptyList(),
                0,
                BigDecimal.ZERO,
                Optional.empty(),
                0L,
                0L);

        // 2. Ejercicio
        final CommerceMetrics result = commerceMetricsService.getCommerceMetrics(COMMERCE_ID, from, to);

        // 3. Asserts
        assertNull(result.getBestSellingPackTitle());
    }

    @Test
    void testGetCommerceMetricsWhenAcceptanceRateZeroDenominator() {
        // 1. Setup
        final LocalDateTime from = LocalDateTime.of(2030, 8, 1, 0, 0);
        final LocalDateTime to = LocalDateTime.of(2030, 8, 2, 0, 0);
        stubGetCommerceMetrics(
                COMMERCE_ID,
                from,
                to,
                Collections.emptyList(),
                0,
                BigDecimal.ZERO,
                Optional.empty(),
                0L,
                0L);

        // 2. Ejercicio
        final CommerceMetrics result = commerceMetricsService.getCommerceMetrics(COMMERCE_ID, from, to);

        // 3. Asserts
        assertEquals(0, result.getAcceptanceRatePercent());
    }

    @Test
    void testGetCommerceMetricsWhenAcceptanceRateRoundedFromPaidAndCanceled() {
        // 1. Setup
        final LocalDateTime from = LocalDateTime.of(2030, 9, 1, 0, 0);
        final LocalDateTime to = LocalDateTime.of(2030, 9, 2, 0, 0);
        stubGetCommerceMetrics(
                COMMERCE_ID,
                from,
                to,
                Collections.emptyList(),
                0,
                BigDecimal.ZERO,
                Optional.empty(),
                2L,
                1L);

        // 2. Ejercicio
        final CommerceMetrics result = commerceMetricsService.getCommerceMetrics(COMMERCE_ID, from, to);

        // 3. Asserts
        assertEquals(67, result.getAcceptanceRatePercent());
    }

    @Test
    void testCountSoldTodayWhenDaoReturnsCount() {
        // 1. Setup
        when(reservationDao.countPaidReservationsInPeriod(eq(COMMERCE_ID), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(11);

        // 2. Ejercicio
        final int sold = commerceMetricsService.countSoldToday(COMMERCE_ID);

        // 3. Asserts
        assertEquals(11, sold);
    }

    private void stubGetCommerceMetrics(
            final Long commerceId,
            final LocalDateTime from,
            final LocalDateTime to,
            final List<Object[]> perDay,
            final int paidCountInPeriod,
            final BigDecimal revenue,
            final Optional<Long> bestPackId,
            final long paidStatusCount,
            final long canceledStatusCount) {
        when(reservationDao.countPaidReservationsPerDay(eq(commerceId), eq(from), eq(to))).thenReturn(perDay);
        when(reservationDao.countPaidReservationsInPeriod(eq(commerceId), eq(from), eq(to))).thenReturn(paidCountInPeriod);
        when(reservationDao.sumRevenueInPeriod(eq(commerceId), eq(from), eq(to))).thenReturn(revenue);
        when(reservationDao.findBestSellingPackId(eq(commerceId), eq(from), eq(to))).thenReturn(bestPackId);
        when(reservationDao.countByStatusInPeriod(eq(commerceId), eq(Reservation.Status.PAID), eq(from), eq(to)))
                .thenReturn(paidStatusCount);
        when(reservationDao.countByStatusInPeriod(eq(commerceId), eq(Reservation.Status.CANCELED), eq(from), eq(to)))
                .thenReturn(canceledStatusCount);
    }
}
