package ar.edu.itba.paw.services.commerce;

import ar.edu.itba.paw.models.user.CommerceReview;
import ar.edu.itba.paw.persistence.CommerceReviewDao;
import ar.edu.itba.paw.persistence.ReservationDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class CommerceReviewServiceImpl implements CommerceReviewService {

    private final CommerceReviewDao commerceReviewDao;
    private final ReservationDao reservationDao;

    @Autowired
    public CommerceReviewServiceImpl(final CommerceReviewDao commerceReviewDao, final ReservationDao reservationDao) {
        this.commerceReviewDao = commerceReviewDao;
        this.reservationDao = reservationDao;
    }

    @Override
    public boolean canClientReviewCommerce(final long clientUserId, final long commerceUserId) {
        return reservationDao.hasPaidReservationWithCommerce(clientUserId, commerceUserId);
    }

    @Override
    public Optional<CommerceReview> findClientReview(final long clientUserId, final long commerceUserId) {
        return commerceReviewDao.findByClientAndCommerce(clientUserId, commerceUserId);
    }

    @Override
    public List<CommerceReview> findReviewsForCommerce(final long commerceUserId, final int page, final int pageSize) {
        return commerceReviewDao.findByCommerceId(commerceUserId, Math.max(1, page), Math.max(1, pageSize));
    }

    @Override
    public int countReviewsForCommerce(final long commerceUserId) {
        return commerceReviewDao.countByCommerceId(commerceUserId);
    }

    @Transactional
    @Override
    public CommerceReview upsertReview(final long clientUserId, final long commerceUserId, final int rating,
            final String body) {
        if (!canClientReviewCommerce(clientUserId, commerceUserId)) {
            throw new IllegalStateException("Client is not eligible to review this commerce");
        }
        validateRating(rating);
        final String normalizedBody = normalizeBody(body);

        return commerceReviewDao.findByClientAndCommerce(clientUserId, commerceUserId)
                .map(existing -> commerceReviewDao.updateReview(existing.getId(), rating, normalizedBody))
                .orElseGet(() -> commerceReviewDao.createReview(commerceUserId, clientUserId, rating, normalizedBody));
    }

    private void validateRating(final int rating) {
        if (rating < MIN_RATING || rating > MAX_RATING) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }
    }

    private String normalizeBody(final String body) {
        final String normalized = body == null ? "" : body.trim();
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException("Review body is required");
        }
        if (normalized.length() > MAX_BODY_LENGTH) {
            throw new IllegalArgumentException("Review body is too long");
        }
        return normalized;
    }
}
