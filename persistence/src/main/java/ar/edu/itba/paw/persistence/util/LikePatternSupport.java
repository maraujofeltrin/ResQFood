package ar.edu.itba.paw.persistence.util;

import java.util.Optional;

/**
 * Escapes LIKE wildcards and builds a case-insensitive search pattern for JPQL.
 */
public final class LikePatternSupport {

    private LikePatternSupport() {
    }

    public static Optional<String> escapeAndWrap(final String query) {
        if (query == null || query.isBlank()) {
            return Optional.empty();
        }
        final String escaped = query.trim()
                .replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
        return Optional.of("%" + escaped + "%");
    }

    public static void appendEscapedLike(final StringBuilder jpql, final String fieldExpression,
                                           final String parameterReference) {
        jpql.append("LOWER(").append(fieldExpression).append(") LIKE LOWER(")
                .append(parameterReference).append(") ESCAPE '\\'");
    }

    public static void appendEscapedLikeNative(final StringBuilder sql, final String columnExpression,
                                               final String parameterReference) {
        sql.append("LOWER(").append(columnExpression).append(") LIKE LOWER(")
                .append(parameterReference).append(") ESCAPE '\\'");
    }
}
