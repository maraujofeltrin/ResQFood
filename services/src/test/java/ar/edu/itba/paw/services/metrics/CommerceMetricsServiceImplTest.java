package ar.edu.itba.paw.services.metrics;

import ar.edu.itba.paw.models.pack.Municipality;
import ar.edu.itba.paw.models.pack.Pack;
import ar.edu.itba.paw.models.user.Client;
import ar.edu.itba.paw.models.user.Commerce;
import ar.edu.itba.paw.models.reservation.Reservation;
import ar.edu.itba.paw.services.pack.PackService;
import ar.edu.itba.paw.services.reservation.ReservationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommerceMetricsServiceImplTest {

    private static final Long COMMERCE_ID = 10L;
    private static final ZoneId BUSINESS_ZONE = ZoneId.of("UTC");

    @Mock
    private ReservationService reservationService;

    @Mock
    private PackService packService;

    private CommerceMetricsServiceImpl commerceMetricsService;

    @BeforeEach
    void setUp() {
        commerceMetricsService = new CommerceMetricsServiceImpl(reservationService, packService, BUSINESS_ZONE);
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
                Collections.singletonList(new Object[] {java.sql.Timestamp.valueOf(LocalDateTime.of(2030, 1, 1, 0, 0)), 5L}),
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
        when(packService.findById(100L)).thenReturn(Optional.of(pack));

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
    void testGetCommerceMetricsWhenTopPacksReturnsEntitiesWithoutPackServiceLookup() {
        // 1. Setup
        final LocalDateTime from = LocalDateTime.of(2030, 10, 1, 0, 0);
        final LocalDateTime to = LocalDateTime.of(2030, 10, 2, 0, 0);
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
        final Pack topPack = new Pack(50L, new Commerce(COMMERCE_ID, "Comm", Commerce.Category.BAKERY, "St", 1,
                Municipality.AVELLANEDA, "P", "1000", "08:00", "20:00"), "Top Pack", "d", 1.0, 1.0, 1, true, null);
        when(reservationService.findTopSellingPacks(eq(COMMERCE_ID), eq(from), eq(to), eq(3)))
                .thenReturn(Collections.singletonList(new Object[] {topPack, 7L}));

        // 2. Ejercicio
        final CommerceMetrics result = commerceMetricsService.getCommerceMetrics(COMMERCE_ID, from, to);

        // 3. Asserts
        assertEquals(1, result.getTopPacks().size());
        assertEquals("Top Pack", result.getTopPacks().get(0).getPackTitle());
        assertEquals(7L, result.getTopPacks().get(0).getUnitsSold());
    }

    @Test
    void testGetCommerceMetricsWhenTopClientsReturnsEntitiesWithoutClientServiceLookup() {
        // 1. Setup
        final LocalDateTime from = LocalDateTime.of(2030, 11, 1, 0, 0);
        final LocalDateTime to = LocalDateTime.of(2030, 11, 2, 0, 0);
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
        final Client topClient = new Client(20L, "Ana", "Garcia", true);
        when(reservationService.findTopClientsByPaidReservations(eq(COMMERCE_ID), eq(from), eq(to), eq(3)))
                .thenReturn(Collections.singletonList(new Object[] {topClient, 4L}));

        // 2. Ejercicio
        final CommerceMetrics result = commerceMetricsService.getCommerceMetrics(COMMERCE_ID, from, to);

        // 3. Asserts
        assertEquals(1, result.getTopClients().size());
        assertEquals("Ana Garcia", result.getTopClients().get(0).getClientName());
        assertEquals(4L, result.getTopClients().get(0).getReservationCount());
        assertEquals(20L, result.getTopClients().get(0).getClientId());
    }

    @Test
    void testCountSoldTodayWhenDaoReturnsCount() {
        // 1. Setup
        when(reservationService.countPaidReservationsInPeriod(eq(COMMERCE_ID), any(LocalDateTime.class), any(LocalDateTime.class)))
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
        when(reservationService.countPaidReservationsPerDay(eq(commerceId), eq(from), eq(to))).thenReturn(perDay);
        when(reservationService.countPaidReservationsInPeriod(eq(commerceId), eq(from), eq(to))).thenReturn(paidCountInPeriod);
        when(reservationService.sumRevenueInPeriod(eq(commerceId), eq(from), eq(to))).thenReturn(revenue);
        when(reservationService.findBestSellingPackId(eq(commerceId), eq(from), eq(to))).thenReturn(bestPackId);
        when(reservationService.countByStatusInPeriod(eq(commerceId), eq(Reservation.Status.PAID), eq(from), eq(to)))
                .thenReturn(paidStatusCount);
        when(reservationService.countByStatusInPeriod(eq(commerceId), eq(Reservation.Status.CANCELED), eq(from), eq(to)))
                .thenReturn(canceledStatusCount);
        when(reservationService.averageTicketInPeriod(eq(commerceId), eq(from), eq(to))).thenReturn(BigDecimal.ZERO);
        when(reservationService.countUniqueClientsInPeriod(eq(commerceId), eq(from), eq(to))).thenReturn(0L);
        when(reservationService.countNewClientsInPeriod(eq(commerceId), eq(from), eq(to))).thenReturn(0L);
        lenient().when(reservationService.findTopSellingPacks(eq(commerceId), eq(from), eq(to), anyInt()))
                .thenReturn(Collections.emptyList());
        lenient().when(reservationService.findTopClientsByPaidReservations(eq(commerceId), eq(from), eq(to), anyInt()))
                .thenReturn(Collections.emptyList());
    }
}
