package ar.edu.itba.paw.services.commerce;

import ar.edu.itba.paw.models.pack.Municipality;
import ar.edu.itba.paw.models.user.Client;
import ar.edu.itba.paw.models.user.Commerce;
import ar.edu.itba.paw.models.user.CommerceReview;
import ar.edu.itba.paw.models.user.CommerceReviewException;
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

    private static Commerce commerceRef(final long id) {
        return new Commerce(id, "Shop", Commerce.Category.BAKERY, "St", 1, Municipality.AVELLANEDA, "P", "1000",
                "08:00", "20:00");
    }

    private static Client clientRef(final long id) {
        return new Client(id, "N", "L", true);
    }

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
        final CommerceReview created = new CommerceReview(REVIEW_ID, commerceRef(COMMERCE_ID), clientRef(CLIENT_ID),
                5, BODY, LocalDateTime.now(), LocalDateTime.now());
        when(reservationDao.hasPaidReservationWithCommerce(CLIENT_ID, COMMERCE_ID)).thenReturn(true);
        when(commerceReviewDao.findByClientAndCommerce(CLIENT_ID, COMMERCE_ID)).thenReturn(Optional.empty());
        when(commerceReviewDao.createReview(COMMERCE_ID, CLIENT_ID, 5, BODY)).thenReturn(created);

        // 2. Ejercicio
        final CommerceReview result = commerceReviewService.upsertReview(CLIENT_ID, COMMERCE_ID, 5,
                "  Muy buena experiencia.  ");

        // 3. Asserts
        assertEquals(created, result);
    }

    @Test
    void testUpsertReviewWhenEligibleAndPreviousReviewExistsReturnsUpdatedReview() {
        // 1. Setup
        final CommerceReview existing = new CommerceReview(REVIEW_ID, commerceRef(COMMERCE_ID), clientRef(CLIENT_ID),
                4, "Antes", LocalDateTime.now(), LocalDateTime.now());
        final CommerceReview updated = new CommerceReview(REVIEW_ID, commerceRef(COMMERCE_ID), clientRef(CLIENT_ID), 3,
                BODY, existing.getCreatedAt(), LocalDateTime.now());
        when(reservationDao.hasPaidReservationWithCommerce(CLIENT_ID, COMMERCE_ID)).thenReturn(true);
        when(commerceReviewDao.findByClientAndCommerce(CLIENT_ID, COMMERCE_ID)).thenReturn(Optional.of(existing));
        when(commerceReviewDao.updateReview(REVIEW_ID, 3, BODY)).thenReturn(updated);

        // 2. Ejercicio
        final CommerceReview result = commerceReviewService.upsertReview(CLIENT_ID, COMMERCE_ID, 3, BODY);

        // 3. Asserts
        assertEquals(updated, result);
    }

    @Test
    void testUpsertReviewWhenClientNotEligibleThrowsIllegalStateException() {
        // 1. Setup
        when(reservationDao.hasPaidReservationWithCommerce(CLIENT_ID, COMMERCE_ID)).thenReturn(false);

        // 2. Ejercicio
        final CommerceReviewException exception = assertThrows(CommerceReviewException.class,
                () -> commerceReviewService.upsertReview(CLIENT_ID, COMMERCE_ID, 5, BODY));

        // 3. Asserts
        assertEquals(CommerceReviewException.Reason.NOT_ELIGIBLE, exception.getReason());
    }

    @Test
    void testUpsertReviewWhenRatingInvalidThrowsIllegalArgumentException() {
        // 1. Setup
        when(reservationDao.hasPaidReservationWithCommerce(CLIENT_ID, COMMERCE_ID)).thenReturn(true);

        // 2. Ejercicio
        final CommerceReviewException exception = assertThrows(CommerceReviewException.class,
                () -> commerceReviewService.upsertReview(CLIENT_ID, COMMERCE_ID, 6, BODY));

        // 3. Asserts
        assertEquals(CommerceReviewException.Reason.INVALID_RATING, exception.getReason());
    }

    @Test
    void testUpsertReviewWhenBodyBlankThrowsIllegalArgumentException() {
        // 1. Setup
        when(reservationDao.hasPaidReservationWithCommerce(CLIENT_ID, COMMERCE_ID)).thenReturn(true);

        // 2. Ejercicio
        final CommerceReviewException exception = assertThrows(CommerceReviewException.class,
                () -> commerceReviewService.upsertReview(CLIENT_ID, COMMERCE_ID, 5, "   "));

        // 3. Asserts
        assertEquals(CommerceReviewException.Reason.INVALID_BODY, exception.getReason());
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
