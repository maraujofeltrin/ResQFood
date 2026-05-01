package ar.edu.itba.paw.services.metrics;

import java.time.LocalDateTime;

public interface CommerceMetricsService {

    CommerceMetrics getCommerceMetrics(Long commerceId, LocalDateTime from, LocalDateTime to);

    /**
     * Counts reservations confirmed (PAID) today for packs belonging to the given commerce.
     * "Today" is defined in the application's business timezone.
     */
    int countSoldToday(Long commerceId);
}
