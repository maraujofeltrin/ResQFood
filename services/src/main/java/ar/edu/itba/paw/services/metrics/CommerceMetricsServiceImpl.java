package ar.edu.itba.paw.services.metrics;

import ar.edu.itba.paw.models.pack.Pack;
import ar.edu.itba.paw.models.reservation.Reservation;
import ar.edu.itba.paw.models.user.Client;
import ar.edu.itba.paw.persistence.PackDao;
import ar.edu.itba.paw.persistence.ReservationDao;
import ar.edu.itba.paw.services.user.ClientService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

    private final ReservationDao reservationDao;
    private final PackDao packDao;
    private final ClientService clientService;
    private final ZoneId displayZone;

    @Autowired
    public CommerceMetricsServiceImpl(final ReservationDao reservationDao, final PackDao packDao,
                                      final ClientService clientService, final ZoneId businessZone) {
        this.reservationDao = reservationDao;
        this.packDao = packDao;
        this.clientService = clientService;
        this.displayZone = businessZone;
    }

    /**
     * Backwards-compatible constructor used by tests that do not provide a ClientService.
     */
    public CommerceMetricsServiceImpl(final ReservationDao reservationDao, final PackDao packDao,
                                      final ZoneId businessZone) {
        this.reservationDao = reservationDao;
        this.packDao = packDao;
        this.clientService = null;
        this.displayZone = businessZone;
    }

    @Override
    public CommerceMetrics getCommerceMetrics(final Long commerceId, final LocalDateTime from, final LocalDateTime to) {
        if (from.isAfter(to)) {
            throw new IllegalArgumentException("from must be <= to");
        }
        LOGGER.debug("getCommerceMetrics commerceId={}", commerceId);
        final List<Object[]> rows = reservationDao.countPaidReservationsPerDay(commerceId, from, to);
        final Map<LocalDate, Long> countsByDate = new HashMap<>();
        for (final Object[] row : rows) {
            final Object dayObj = row[0];
            final LocalDate d;
            if (dayObj instanceof java.time.LocalDate) {
                d = (LocalDate) dayObj;
            } else if (dayObj instanceof java.sql.Date) {
                d = ((java.sql.Date) dayObj).toLocalDate();
            } else if (dayObj instanceof java.sql.Timestamp) {
                d = ((java.sql.Timestamp) dayObj).toLocalDateTime().toLocalDate();
            } else if (dayObj instanceof java.util.Date) {
                d = new java.sql.Date(((java.util.Date) dayObj).getTime()).toLocalDate();
            } else {
                throw new IllegalStateException("Unsupported date type: " + (dayObj == null ? "null" : dayObj.getClass()));
            }
            final Number cntNum = (Number) row[1];
            final Long cnt = cntNum == null ? 0L : cntNum.longValue();
            countsByDate.put(d, cnt);
        }

        final List<CommerceMetrics.DailySalesPoint> daily = new ArrayList<>();
        final LocalDate startDate = from.toLocalDate();
        final LocalDate endDate = to.toLocalDate();
        final int days = (int) ChronoUnit.DAYS.between(startDate, endDate) + 1;
        for (int i = 0; i < days; i++) {
            final LocalDate d = startDate.plusDays(i);
            final long cnt = countsByDate.getOrDefault(d, 0L);
            daily.add(new CommerceMetrics.DailySalesPoint(d.toString(), cnt));
        }

        final int totalReservations = reservationDao.countPaidReservationsInPeriod(commerceId, from, to);
        final BigDecimal totalRevenue = reservationDao.sumRevenueInPeriod(commerceId, from, to);

        final Optional<Long> bestPackId = reservationDao.findBestSellingPackId(commerceId, from, to);
        final String bestTitle = bestPackId.flatMap(id -> packDao.findById(id).map(Pack::getTitle)).orElse(null);

        final long paidCount = reservationDao.countByStatusInPeriod(commerceId, Reservation.Status.PAID, from, to);
        final long canceledCount = reservationDao.countByStatusInPeriod(commerceId, Reservation.Status.CANCELED, from, to);
        final long denom = paidCount + canceledCount;
        final int acceptanceRate = denom == 0L ? 0 : (int) Math.round((double) paidCount / (double) denom * 100.0);

        final long canceledReservations = reservationDao.countCanceledReservationsInPeriod(commerceId, from, to);
        final BigDecimal averageTicket = reservationDao.averageTicketInPeriod(commerceId, from, to);
        final long uniqueClients = reservationDao.countUniqueClientsInPeriod(commerceId, from, to);

        final List<TopPackEntry> topPacks = buildTopPacks(commerceId, from, to);
        final List<TopClientEntry> topClients = buildTopClients(commerceId, from, to);
        final ClientRetention clientRetention = buildClientRetention(commerceId, from, to, uniqueClients);

        return new CommerceMetrics(daily, totalRevenue, totalReservations, bestTitle, acceptanceRate,
                canceledReservations, averageTicket, uniqueClients, topPacks, topClients, clientRetention);
    }

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
        return reservationDao.countPaidReservationsInPeriod(commerceId, dayStartUtc, dayEndUtc);
    }

    private List<TopPackEntry> buildTopPacks(final Long commerceId, final LocalDateTime from,
            final LocalDateTime to) {
        final List<Object[]> rows = reservationDao.findTopSellingPacks(commerceId, from, to, 3);
        final List<TopPackEntry> result = new ArrayList<>();
        for (final Object[] row : rows) {
            final Long packId = ((Number) row[0]).longValue();
            final long unitsSold = ((Number) row[1]).longValue();
            final Optional<Pack> pack = packDao.findById(packId);
            final String packTitle = pack.map(Pack::getTitle).orElse("Pack #" + packId);
            final Long imageId = pack.map(Pack::getImageId).orElse(null);
            result.add(new TopPackEntry(packId, packTitle, imageId, unitsSold));
        }
        return result;
    }

    private List<TopClientEntry> buildTopClients(final Long commerceId, final LocalDateTime from,
            final LocalDateTime to) {
        final List<Object[]> rows = reservationDao.findTopClientsByPaidReservations(commerceId, from, to, 3);
        final List<TopClientEntry> result = new ArrayList<>();
        for (final Object[] row : rows) {
            final Long clientId = ((Number) row[0]).longValue();
            final long reservationCount = ((Number) row[1]).longValue();
            final Optional<Client> client = clientService == null ? Optional.empty() : clientService.findByUserId(clientId);
            final String clientName = client.map(c -> c.getName() + " " + c.getLastName()).orElse("Cliente");
            result.add(new TopClientEntry(clientId, clientName, reservationCount));
        }
        return result;
    }

    private ClientRetention buildClientRetention(final Long commerceId, final LocalDateTime from,
            final LocalDateTime to, final long uniqueClients) {
        final long newClients = reservationDao.countNewClientsInPeriod(commerceId, from, to);
        final long returningClients = Math.max(0, uniqueClients - newClients);
        final long total = newClients + returningClients;
        final int newPercent = total == 0 ? 0 : (int) Math.round((double) newClients / (double) total * 100.0);
        final int returningPercent = total == 0 ? 0 : 100 - newPercent;
        return new ClientRetention(newClients, returningClients, newPercent, returningPercent);
    }

}
