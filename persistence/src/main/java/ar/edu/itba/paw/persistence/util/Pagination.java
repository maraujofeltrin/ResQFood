package ar.edu.itba.paw.persistence.util;

/**
 * Defensive pagination helpers for JPA {@code setFirstResult}.
 */
public final class Pagination {

    private Pagination() {
    }

    /**
     * Zero-based offset for the given page (1-based) and page size.
     */
    public static int offset(final int page, final int pageSize) {
        return Math.max(0, page - 1) * pageSize;
    }
}
