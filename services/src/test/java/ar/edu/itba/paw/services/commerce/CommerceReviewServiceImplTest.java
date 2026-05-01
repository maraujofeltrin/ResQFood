package ar.edu.itba.paw.services.commerce;

import ar.edu.itba.paw.models.user.CommerceReview;
import ar.edu.itba.paw.persistence.CommerceReviewDao;
import ar.edu.itba.paw.persistence.ReservationDao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CommerceReviewServiceImplTest {

    private static final long CLIENT_ID = 1L;
    private static final long COMMERCE_ID = 2L;
    private static final long REVIEW_ID = 3L;
    private static final String BODY = "Muy buena experiencia.";

    @Mock
    private CommerceReviewDao commerceReviewDao;

    @Mock
    private ReservationDao reservationDao;

    private CommerceReviewServiceImpl commerceReviewService;

    @BeforeEach
    public void setUp() {
        commerceReviewService = new CommerceReviewServiceImpl(commerceReviewDao, reservationDao);
    }

    @Test
    public void testCanClientReviewCommerce_WhenPaidReservationExists() {
        // 1. Setup
        when(reservationDao.hasPaidReservationWithCommerce(CLIENT_ID, COMMERCE_ID)).thenReturn(true);

        // 2. Ejercicio
        final boolean result = commerceReviewService.canClientReviewCommerce(CLIENT_ID, COMMERCE_ID);

        // 3. Asserts
        assertTrue(result);
        verify(reservationDao).hasPaidReservationWithCommerce(CLIENT_ID, COMMERCE_ID);
    }

    @Test
    public void testUpsertReview_WhenEligibleAndNoPreviousReview_CreatesReview() {
        // 1. Setup
        final CommerceReview created = new CommerceReview(REVIEW_ID, COMMERCE_ID, CLIENT_ID, 5, BODY,
                LocalDateTime.now(), LocalDateTime.now());
        when(reservationDao.hasPaidReservationWithCommerce(CLIENT_ID, COMMERCE_ID)).thenReturn(true);
        when(commerceReviewDao.findByClientAndCommerce(CLIENT_ID, COMMERCE_ID)).thenReturn(Optional.empty());
        when(commerceReviewDao.createReview(COMMERCE_ID, CLIENT_ID, 5, BODY)).thenReturn(created);

        // 2. Ejercicio
        final CommerceReview result = commerceReviewService.upsertReview(CLIENT_ID, COMMERCE_ID, 5,
                "  Muy buena experiencia.  ");

        // 3. Asserts
        assertEquals(created, result);
        verify(commerceReviewDao).createReview(COMMERCE_ID, CLIENT_ID, 5, BODY);
        verify(commerceReviewDao, never()).updateReview(anyLong(), anyInt(), anyString());
    }

    @Test
    public void testUpsertReview_WhenEligibleAndPreviousReviewExists_UpdatesReview() {
        // 1. Setup
        final CommerceReview existing = new CommerceReview(REVIEW_ID, COMMERCE_ID, CLIENT_ID, 4, "Antes",
                LocalDateTime.now(), LocalDateTime.now());
        final CommerceReview updated = new CommerceReview(REVIEW_ID, COMMERCE_ID, CLIENT_ID, 3, BODY,
                existing.getCreatedAt(), LocalDateTime.now());
        when(reservationDao.hasPaidReservationWithCommerce(CLIENT_ID, COMMERCE_ID)).thenReturn(true);
        when(commerceReviewDao.findByClientAndCommerce(CLIENT_ID, COMMERCE_ID)).thenReturn(Optional.of(existing));
        when(commerceReviewDao.updateReview(REVIEW_ID, 3, BODY)).thenReturn(updated);

        // 2. Ejercicio
        final CommerceReview result = commerceReviewService.upsertReview(CLIENT_ID, COMMERCE_ID, 3, BODY);

        // 3. Asserts
        assertEquals(updated, result);
        verify(commerceReviewDao).updateReview(REVIEW_ID, 3, BODY);
        verify(commerceReviewDao, never()).createReview(anyLong(), anyLong(), anyInt(), anyString());
    }

    @Test
    public void testUpsertReview_WhenClientIsNotEligible_Throws() {
        // 1. Setup
        when(reservationDao.hasPaidReservationWithCommerce(CLIENT_ID, COMMERCE_ID)).thenReturn(false);

        // 2. Ejercicio
        final IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> commerceReviewService.upsertReview(CLIENT_ID, COMMERCE_ID, 5, BODY));

        // 3. Asserts
        assertEquals("Client is not eligible to review this commerce", exception.getMessage());
        verifyNoInteractions(commerceReviewDao);
    }

    @Test
    public void testUpsertReview_WhenRatingIsInvalid_Throws() {
        // 1. Setup
        when(reservationDao.hasPaidReservationWithCommerce(CLIENT_ID, COMMERCE_ID)).thenReturn(true);

        // 2. Ejercicio
        final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> commerceReviewService.upsertReview(CLIENT_ID, COMMERCE_ID, 6, BODY));

        // 3. Asserts
        assertEquals("Rating must be between 1 and 5", exception.getMessage());
        verify(commerceReviewDao, never()).findByClientAndCommerce(anyLong(), anyLong());
    }

    @Test
    public void testUpsertReview_WhenBodyIsBlank_Throws() {
        // 1. Setup
        when(reservationDao.hasPaidReservationWithCommerce(CLIENT_ID, COMMERCE_ID)).thenReturn(true);

        // 2. Ejercicio
        final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> commerceReviewService.upsertReview(CLIENT_ID, COMMERCE_ID, 5, "   "));

        // 3. Asserts
        assertEquals("Review body is required", exception.getMessage());
        verify(commerceReviewDao, never()).findByClientAndCommerce(anyLong(), anyLong());
    }

    @Test
    public void testAverageRatingForCommerce_WhenReviewsExist() {
        // 1. Setup
        when(commerceReviewDao.averageRatingByCommerceId(COMMERCE_ID)).thenReturn(4.5);

        // 2. Ejercicio
        final Optional<Double> result = commerceReviewService.averageRatingForCommerce(COMMERCE_ID);

        // 3. Asserts
        assertTrue(result.isPresent());
        assertEquals(4.5, result.get(), 0.01);
        verify(commerceReviewDao).averageRatingByCommerceId(COMMERCE_ID);
    }

    @Test
    public void testAverageRatingForCommerce_WhenNoReviews() {
        // 1. Setup
        when(commerceReviewDao.averageRatingByCommerceId(COMMERCE_ID)).thenReturn(null);

        // 2. Ejercicio
        final Optional<Double> result = commerceReviewService.averageRatingForCommerce(COMMERCE_ID);

        // 3. Asserts
        assertFalse(result.isPresent());
        verify(commerceReviewDao).averageRatingByCommerceId(COMMERCE_ID);
    }
}
