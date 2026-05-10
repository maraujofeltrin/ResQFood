package ar.edu.itba.paw.services.metrics;

import ar.edu.itba.paw.models.pack.Pack;
import ar.edu.itba.paw.models.reservation.Reservation;
import ar.edu.itba.paw.persistence.PackDao;
import ar.edu.itba.paw.persistence.ReservationDao;
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
    private final ZoneId displayZone;

    @Autowired
    public CommerceMetricsServiceImpl(final ReservationDao reservationDao, final PackDao packDao,
                                      final ZoneId businessZone) {
        this.reservationDao = reservationDao;
        this.packDao = packDao;
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
            final LocalDate d = (LocalDate) row[0];
            final Long cnt = (Long) row[1];
            countsByDate.put(d, cnt == null ? 0L : cnt);
        }

        final List<CommerceMetrics.DailySalesPoint> daily = new ArrayList<>();
        final LocalDate startDate = from.toLocalDate();
        final int days = (int) ChronoUnit.DAYS.between(startDate, to.toLocalDate());
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

        return new CommerceMetrics(daily, totalRevenue, totalReservations, bestTitle, acceptanceRate);
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

}
