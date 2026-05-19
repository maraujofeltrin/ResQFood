package ar.edu.itba.paw.persistence.util;

import java.util.ArrayList;
import java.util.List;

/**
 * Shared JPQL predicates for morning / afternoon / evening filters on commerce opening times.
 * Parses the hour as the substring before the first ':' (aligned with JDBC SPLIT_PART behavior).
 * Rows with null or colon-less opening times are excluded when a time range filter is applied.
 */
public final class OpeningTimeFilterJpql {

    private OpeningTimeFilterJpql() {
    }

    public static void appendTimeRangeConditions(final StringBuilder jpql, final String openingTimePath,
                                                 final List<String> timeRanges) {
        if (timeRanges == null || timeRanges.isEmpty()) {
            return;
        }
        final List<String> conditions = new ArrayList<>();
        final String hourExpr = "CAST(SUBSTRING(" + openingTimePath + ", 1, LOCATE(':', " + openingTimePath
                + ") - 1) AS int)";
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
            jpql.append(" AND ").append(openingTimePath).append(" IS NOT NULL AND LOCATE(':', ")
                    .append(openingTimePath).append(") > 0 AND (")
                    .append(String.join(" OR ", conditions))
                    .append(")");
        }
    }
}
