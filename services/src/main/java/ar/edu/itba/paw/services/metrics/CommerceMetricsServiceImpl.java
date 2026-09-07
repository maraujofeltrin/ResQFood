package ar.edu.itba.paw.services.metrics;

import ar.edu.itba.paw.models.pack.Pack;
import ar.edu.itba.paw.models.reservation.MetricsQueryException;
import ar.edu.itba.paw.models.reservation.Reservation;
import ar.edu.itba.paw.models.user.Client;
import ar.edu.itba.paw.services.pack.PackService;
import ar.edu.itba.paw.services.reservation.ReservationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class CommerceMetricsServiceImpl implements CommerceMetricsService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CommerceMetricsServiceImpl.class);

    private final ReservationService reservationService;
    private final PackService packService;
    private final ZoneId displayZone;

    @Autowired
    public CommerceMetricsServiceImpl(final ReservationService reservationService, final PackService packService,
                                      final ZoneId businessZone) {
        this.reservationService = reservationService;
        this.packService = packService;
        this.displayZone = businessZone;
    }


    @Transactional(readOnly = true)
    @Override
    public CommerceMetrics getCommerceMetrics(final Long commerceId, final LocalDateTime from, final LocalDateTime to) {
        if (from.isAfter(to)) {
            throw new MetricsQueryException(MetricsQueryException.Reason.INVALID_DATE_RANGE);
        }
        LOGGER.debug("getCommerceMetrics commerceId={}", commerceId);
        final List<Object[]> rows = reservationService.countPaidReservationsPerDay(commerceId, from, to);
        final Map<LocalDate, Long> countsByDate = new HashMap<>();
        for (final Object[] row : rows) {
            final LocalDate d = ((java.sql.Timestamp) row[0]).toLocalDateTime().toLocalDate();
            final Number cntNum = (Number) row[1];
            final Long cnt = cntNum == null ? 0L : cntNum.longValue();
            countsByDate.put(d, cnt);
        }

        final List<CommerceMetrics.DailySalesPoint> daily = new ArrayList<>();
        final LocalDate startDate = from.atZone(ZoneOffset.UTC).withZoneSameInstant(displayZone).toLocalDate();
        final LocalDate endDate = to.atZone(ZoneOffset.UTC).withZoneSameInstant(displayZone).toLocalDate();
        final int days = (int) ChronoUnit.DAYS.between(startDate, endDate) + 1;
        for (int i = 0; i < days; i++) {
            final LocalDate d = startDate.plusDays(i);
            final long cnt = countsByDate.getOrDefault(d, 0L);
            daily.add(new CommerceMetrics.DailySalesPoint(d.toString(), cnt));
        }

        final int totalReservations = reservationService.countPaidReservationsInPeriod(commerceId, from, to);
        final BigDecimal totalRevenue = reservationService.sumRevenueInPeriod(commerceId, from, to);

        final Optional<Long> bestPackId = reservationService.findBestSellingPackId(commerceId, from, to);
        final String bestTitle = bestPackId.flatMap(id -> packService.findById(id).map(Pack::getTitle)).orElse(null);

        final long paidCount = reservationService.countByStatusInPeriod(commerceId, Reservation.Status.PAID, from, to);
        final long canceledCount = reservationService.countByStatusInPeriod(commerceId, Reservation.Status.CANCELED, from, to);
        final long denom = paidCount + canceledCount;
        final int acceptanceRate = denom == 0L ? 0 : (int) Math.round((double) paidCount / (double) denom * 100.0);

        final BigDecimal averageTicket = reservationService.averageTicketInPeriod(commerceId, from, to);
        final long uniqueClients = reservationService.countUniqueClientsInPeriod(commerceId, from, to);

        final List<TopPackEntry> topPacks = buildTopPacks(commerceId, from, to);
        final List<TopClientEntry> topClients = buildTopClients(commerceId, from, to);
        final ClientRetention clientRetention = buildClientRetention(commerceId, from, to, uniqueClients);

        return new CommerceMetrics(daily, totalRevenue, totalReservations, bestTitle, acceptanceRate,
                canceledCount, averageTicket, uniqueClients, topPacks, topClients, clientRetention);
    }

    @Transactional(readOnly = true)
    @Override
    public int countSoldToday(final Long commerceId) {
        final ZonedDateTime nowInBiz = ZonedDateTime.now(displayZone);
        final LocalDateTime dayStartUtc = nowInBiz.toLocalDate()
                .atStartOfDay(displayZone)
                .withZoneSameInstant(ZoneOffset.UTC)
                .toLocalDateTime();
        final LocalDateTime dayEndUtc = nowInBiz.toLocalDate()
                .plusDays(1)
                .atStartOfDay(displayZone)
                .withZoneSameInstant(ZoneOffset.UTC)
                .toLocalDateTime();
        return reservationService.countPaidReservationsInPeriod(commerceId, dayStartUtc, dayEndUtc);
    }

    private List<TopPackEntry> buildTopPacks(final Long commerceId, final LocalDateTime from,
            final LocalDateTime to) {
        final List<Object[]> rows = reservationService.findTopSellingPacks(commerceId, from, to, 3);
        final List<TopPackEntry> result = new ArrayList<>();
        for (final Object[] row : rows) {
            final Pack pack = (Pack) row[0];
            final long unitsSold = ((Number) row[1]).longValue();
            final String packTitle = pack.getTitle() != null ? pack.getTitle() : "Pack #" + pack.getId();
            final Long imageId = pack.getImageId();
            result.add(new TopPackEntry(pack.getId(), packTitle, imageId, unitsSold));
        }
        return result;
    }

    private List<TopClientEntry> buildTopClients(final Long commerceId, final LocalDateTime from,
            final LocalDateTime to) {
        final List<Object[]> rows = reservationService.findTopClientsByPaidReservations(commerceId, from, to, 3);
        final List<TopClientEntry> result = new ArrayList<>();
        for (final Object[] row : rows) {
            final Client client = (Client) row[0];
            final long reservationCount = ((Number) row[1]).longValue();
            final String rawName = (client.getName() == null ? "" : client.getName())
                    + (client.getLastName() == null ? "" : (" " + client.getLastName()));
            final String clientName = rawName.trim().isEmpty() ? "Cliente" : rawName.trim();
            result.add(new TopClientEntry(client.getUserId(), clientName, reservationCount));
        }
        return result;
    }

    private ClientRetention buildClientRetention(final Long commerceId, final LocalDateTime from,
            final LocalDateTime to, final long uniqueClients) {
        final long newClients = reservationService.countNewClientsInPeriod(commerceId, from, to);
        final long returningClients = Math.max(0, uniqueClients - newClients);
        final long total = newClients + returningClients;
        final int newPercent = total == 0 ? 0 : (int) Math.round((double) newClients / (double) total * 100.0);
        final int returningPercent = total == 0 ? 0 : 100 - newPercent;
        return new ClientRetention(newClients, returningClients, newPercent, returningPercent);
    }
}
