package ar.edu.itba.paw.services.metrics;

import ar.edu.itba.paw.models.CommerceMetrics;

import java.time.LocalDateTime;

public interface CommerceMetricsService {

    CommerceMetrics getCommerceMetrics(Long commerceId, LocalDateTime from, LocalDateTime to);
}
