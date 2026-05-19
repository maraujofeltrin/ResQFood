package ar.edu.itba.paw.persistence.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PaginationTest {

    @Test
    void offset_firstPageIsZero() {
        // 1. Setup
        // 2. Ejercicio
        final int offset = Pagination.offset(1, 10);
        // 3. Asserts
        assertEquals(0, offset);
    }

    @Test
    void offset_secondPage() {
        // 1. Setup
        // 2. Ejercicio
        final int offset = Pagination.offset(2, 10);
        // 3. Asserts
        assertEquals(10, offset);
    }

    @Test
    void offset_nonPositivePageClampedToZero() {
        // 1. Setup
        // 2. Ejercicio
        final int offset = Pagination.offset(0, 10);
        // 3. Asserts
        assertEquals(0, offset);
    }
}
