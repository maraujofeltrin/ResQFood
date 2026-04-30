package ar.edu.itba.paw.webapp.controller.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;

@Component
public class CommerceMetricsFilterHelper {

    private final ZoneId businessZone;

    @Autowired
    public CommerceMetricsFilterHelper(final ZoneId businessZone) {
        this.businessZone = businessZone;
    }

    public MetricsFilterResolution resolve(final String fromStr, final String toStr, final Integer days) {
        final ZonedDateTime now = ZonedDateTime.now(businessZone);
        try {
            if (days != null && days > 0) {
                final LocalDate fromDate = now.toLocalDate().minusDays(days);
                return new MetricsFilterResolution(toUtcStartOfDay(fromDate),
                    now.withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime(), "", "", days);
            }
            if (fromStr != null && !fromStr.isEmpty() && toStr != null && !toStr.isEmpty()) {
                final LocalDate fromDate = LocalDate.parse(fromStr);
                final LocalDate toDate = LocalDate.parse(toStr);
                return new MetricsFilterResolution(toUtcStartOfDay(fromDate), toUtcEndOfDay(toDate),
                    fromDate.toString(), toDate.toString(), null);
            }
        } catch (final DateTimeParseException ex) {
            return defaultResolution(now);
        }
        return defaultResolution(now);
    }

    private MetricsFilterResolution defaultResolution(final ZonedDateTime now) {
        final LocalDate fromDate = now.toLocalDate().minusDays(7);
        return new MetricsFilterResolution(toUtcStartOfDay(fromDate),
            now.withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime(), "", "", 7);
    }

    private LocalDateTime toUtcStartOfDay(final LocalDate date) {
        return date.atStartOfDay(businessZone)
            .withZoneSameInstant(ZoneOffset.UTC)
            .toLocalDateTime();
    }

    private LocalDateTime toUtcEndOfDay(final LocalDate date) {
        return date.atTime(LocalTime.MAX)
            .atZone(businessZone)
            .withZoneSameInstant(ZoneOffset.UTC)
            .toLocalDateTime();
    }

    public static final class MetricsFilterResolution {
        private final LocalDateTime from;
        private final LocalDateTime to;
        private final String fromValue;
        private final String toValue;
        private final Integer daysValue;

        private MetricsFilterResolution(final LocalDateTime from, final LocalDateTime to, final String fromValue,
                                        final String toValue, final Integer daysValue) {
            this.from = from;
            this.to = to;
            this.fromValue = fromValue;
            this.toValue = toValue;
            this.daysValue = daysValue;
        }

        public LocalDateTime getFrom() {
            return from;
        }

        public LocalDateTime getTo() {
            return to;
        }

        public String getFromValue() {
            return fromValue;
        }

        public String getToValue() {
            return toValue;
        }

        public Integer getDaysValue() {
            return daysValue;
        }
    }
}
