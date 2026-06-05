package ar.edu.itba.paw.persistence.util;

import java.util.ArrayList;
import java.util.List;

/**
 * Shared native SQL predicates for morning / afternoon / evening filters on commerce opening times.
 * Parses the hour as the substring before the first ':' (aligned with {@link OpeningTimeFilterJpql}).
 * Rows with null or colon-less opening times are excluded when a time range filter is applied.
 */
public final class OpeningTimeFilterSql {

    private OpeningTimeFilterSql() {
    }

    public static void appendTimeRangeConditions(final StringBuilder sql, final String openingTimeColumn,
                                                 final List<String> timeRanges) {
        if (timeRanges == null || timeRanges.isEmpty()) {
            return;
        }
        final List<String> conditions = new ArrayList<>();
        final String hourExpr = "CAST(SUBSTRING(" + openingTimeColumn + ", 1, LOCATE(':', " + openingTimeColumn
                + ") - 1) AS INTEGER)";
        for (final String range : timeRanges) {
            if ("morning".equals(range)) {
                conditions.add(hourExpr + " < 12");
            } else if ("afternoon".equals(range)) {
                conditions.add("(" + hourExpr + " >= 12 AND " + hourExpr + " < 17)");
            } else if ("evening".equals(range)) {
                conditions.add(hourExpr + " >= 17");
            }
        }
        if (!conditions.isEmpty()) {
            sql.append(" AND ").append(openingTimeColumn).append(" IS NOT NULL AND LOCATE(':', ")
                    .append(openingTimeColumn).append(") > 0 AND (")
                    .append(String.join(" OR ", conditions))
                    .append(")");
        }
    }
}
