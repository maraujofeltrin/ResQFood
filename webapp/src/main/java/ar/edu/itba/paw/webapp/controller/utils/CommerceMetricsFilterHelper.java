package ar.edu.itba.paw.webapp.controller.utils;

import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;

@Component
public class CommerceMetricsFilterHelper {

    public MetricsFilterResolution resolve(final String fromStr, final String toStr, final Integer days) {
        final LocalDateTime now = LocalDateTime.now();
        try {
            if (days != null && days > 0) {
                return new MetricsFilterResolution(now.minusDays(days).truncatedTo(ChronoUnit.DAYS), now, "", "", days);
            }
            if (fromStr != null && !fromStr.isEmpty() && toStr != null && !toStr.isEmpty()) {
                final LocalDate fromDate = LocalDate.parse(fromStr);
                final LocalDate toDate = LocalDate.parse(toStr);
                return new MetricsFilterResolution(fromDate.atStartOfDay(), toDate.atTime(LocalTime.MAX),
                    fromDate.toString(), toDate.toString(), null);
            }
        } catch (final DateTimeParseException ex) {
            return new MetricsFilterResolution(now.minusDays(7).truncatedTo(ChronoUnit.DAYS), now, "", "", 7);
        }
        return new MetricsFilterResolution(now.minusDays(7).truncatedTo(ChronoUnit.DAYS), now, "", "", 7);
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
