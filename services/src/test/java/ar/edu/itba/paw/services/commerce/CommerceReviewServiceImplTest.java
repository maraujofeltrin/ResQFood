package ar.edu.itba.paw.services.commerce;

import ar.edu.itba.paw.models.user.CommerceReview;
import ar.edu.itba.paw.persistence.CommerceReviewDao;
import ar.edu.itba.paw.persistence.ReservationDao;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommerceReviewServiceImplTest {

    private static final long CLIENT_ID = 1L;
    private static final long COMMERCE_ID = 2L;
    private static final long REVIEW_ID = 3L;
    private static final String BODY = "Muy buena experiencia.";

    @Mock
    private CommerceReviewDao commerceReviewDao;

    @Mock
    private ReservationDao reservationDao;

    @InjectMocks
    private CommerceReviewServiceImpl commerceReviewService;

    @Test
    void testCanClientReviewCommerceWhenPaidReservationExistsReturnsTrue() {
        // 1. Setup
        when(reservationDao.hasPaidReservationWithCommerce(CLIENT_ID, COMMERCE_ID)).thenReturn(true);

        // 2. Ejercicio
        final boolean result = commerceReviewService.canClientReviewCommerce(CLIENT_ID, COMMERCE_ID);

        // 3. Asserts
        assertTrue(result);
    }

    @Test
    void testUpsertReviewWhenEligibleAndNoPreviousReviewReturnsCreatedReview() {
        // 1. Setup
        final CommerceReview created = new CommerceReview(REVIEW_ID, COMMERCE_ID, CLIENT_ID, 5, BODY,
                LocalDateTime.now(), LocalDateTime.now());
        when(reservationDao.hasPaidReservationWithCommerce(CLIENT_ID, COMMERCE_ID)).thenReturn(true);
        when(commerceReviewDao.findByClientAndCommerce(CLIENT_ID, COMMERCE_ID)).thenReturn(Optional.empty());
        when(commerceReviewDao.createReview(COMMERCE_ID, CLIENT_ID, 5, BODY)).thenReturn(created);
        lenient().doThrow(new AssertionError("updateReview no debe invocarse")).when(commerceReviewDao)
                .updateReview(anyLong(), anyInt(), anyString());

        // 2. Ejercicio
        final CommerceReview result = commerceReviewService.upsertReview(CLIENT_ID, COMMERCE_ID, 5,
                "  Muy buena experiencia.  ");

        // 3. Asserts
        assertEquals(created, result);
    }

    @Test
    void testUpsertReviewWhenEligibleAndPreviousReviewExistsReturnsUpdatedReview() {
        // 1. Setup
        final CommerceReview existing = new CommerceReview(REVIEW_ID, COMMERCE_ID, CLIENT_ID, 4, "Antes",
                LocalDateTime.now(), LocalDateTime.now());
        final CommerceReview updated = new CommerceReview(REVIEW_ID, COMMERCE_ID, CLIENT_ID, 3, BODY,
                existing.getCreatedAt(), LocalDateTime.now());
        when(reservationDao.hasPaidReservationWithCommerce(CLIENT_ID, COMMERCE_ID)).thenReturn(true);
        when(commerceReviewDao.findByClientAndCommerce(CLIENT_ID, COMMERCE_ID)).thenReturn(Optional.of(existing));
        when(commerceReviewDao.updateReview(REVIEW_ID, 3, BODY)).thenReturn(updated);
        lenient().doThrow(new AssertionError("createReview no debe invocarse")).when(commerceReviewDao)
                .createReview(anyLong(), anyLong(), anyInt(), anyString());

        // 2. Ejercicio
        final CommerceReview result = commerceReviewService.upsertReview(CLIENT_ID, COMMERCE_ID, 3, BODY);

        // 3. Asserts
        assertEquals(updated, result);
    }

    @Test
    void testUpsertReviewWhenClientNotEligibleThrowsIllegalStateException() {
        // 1. Setup
        when(reservationDao.hasPaidReservationWithCommerce(CLIENT_ID, COMMERCE_ID)).thenReturn(false);
        lenient().doThrow(new AssertionError("commerceReviewDao no debe usarse")).when(commerceReviewDao)
                .findByClientAndCommerce(anyLong(), anyLong());
        lenient().doThrow(new AssertionError("createReview no debe invocarse")).when(commerceReviewDao)
                .createReview(anyLong(), anyLong(), anyInt(), anyString());
        lenient().doThrow(new AssertionError("updateReview no debe invocarse")).when(commerceReviewDao)
                .updateReview(anyLong(), anyInt(), anyString());

        // 2. Ejercicio
        final IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> commerceReviewService.upsertReview(CLIENT_ID, COMMERCE_ID, 5, BODY));

        // 3. Asserts
        assertEquals("Client is not eligible to review this commerce", exception.getMessage());
    }

    @Test
    void testUpsertReviewWhenRatingInvalidThrowsIllegalArgumentException() {
        // 1. Setup
        when(reservationDao.hasPaidReservationWithCommerce(CLIENT_ID, COMMERCE_ID)).thenReturn(true);
        lenient().doThrow(new AssertionError("findByClientAndCommerce no debe invocarse")).when(commerceReviewDao)
                .findByClientAndCommerce(anyLong(), anyLong());

        // 2. Ejercicio
        final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> commerceReviewService.upsertReview(CLIENT_ID, COMMERCE_ID, 6, BODY));

        // 3. Asserts
        assertEquals("Rating must be between 1 and 5", exception.getMessage());
    }

    @Test
    void testUpsertReviewWhenBodyBlankThrowsIllegalArgumentException() {
        // 1. Setup
        when(reservationDao.hasPaidReservationWithCommerce(CLIENT_ID, COMMERCE_ID)).thenReturn(true);
        lenient().doThrow(new AssertionError("findByClientAndCommerce no debe invocarse")).when(commerceReviewDao)
                .findByClientAndCommerce(anyLong(), anyLong());

        // 2. Ejercicio
        final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> commerceReviewService.upsertReview(CLIENT_ID, COMMERCE_ID, 5, "   "));

        // 3. Asserts
        assertEquals("Review body is required", exception.getMessage());
    }

    @Test
    void testAverageRatingForCommerceWhenDaoReturnsValueReturnsOptionalOfRating() {
        // 1. Setup
        when(commerceReviewDao.averageRatingByCommerceId(COMMERCE_ID)).thenReturn(4.5);

        // 2. Ejercicio
        final Optional<Double> result = commerceReviewService.averageRatingForCommerce(COMMERCE_ID);

        // 3. Asserts
        assertTrue(result.isPresent());
        assertEquals(4.5, result.get(), 0.01);
    }

    @Test
    void testAverageRatingForCommerceWhenDaoReturnsNullReturnsEmpty() {
        // 1. Setup
        when(commerceReviewDao.averageRatingByCommerceId(COMMERCE_ID)).thenReturn(null);

        // 2. Ejercicio
        final Optional<Double> result = commerceReviewService.averageRatingForCommerce(COMMERCE_ID);

        // 3. Asserts
        assertFalse(result.isPresent());
    }
}
