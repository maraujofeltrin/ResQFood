package ar.edu.itba.paw.services.commerce;

import ar.edu.itba.paw.models.user.CommerceReview;
import ar.edu.itba.paw.models.user.CommerceReviewException;
import ar.edu.itba.paw.persistence.CommerceReviewDao;
import ar.edu.itba.paw.services.reservation.ReservationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class CommerceReviewServiceImpl implements CommerceReviewService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CommerceReviewServiceImpl.class);

    private final CommerceReviewDao commerceReviewDao;
    private final ReservationService reservationService;

    @Autowired
    public CommerceReviewServiceImpl(final CommerceReviewDao commerceReviewDao, final ReservationService reservationService) {
        this.commerceReviewDao = commerceReviewDao;
        this.reservationService = reservationService;
    }

    @Transactional(readOnly = true)
    @Override
    public boolean canClientReviewCommerce(final long clientUserId, final long commerceUserId) {
        return reservationService.hasPaidReservationWithCommerce(clientUserId, commerceUserId);
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<CommerceReview> findClientReview(final long clientUserId, final long commerceUserId) {
        return commerceReviewDao.findByClientAndCommerce(clientUserId, commerceUserId);
    }

    @Transactional(readOnly = true)
    @Override
    public List<CommerceReview> findReviewsForCommerce(final long commerceUserId, final int page, final int pageSize) {
        return commerceReviewDao.findByCommerceId(commerceUserId, Math.max(1, page), Math.max(1, pageSize));
    }

    @Transactional(readOnly = true)
    @Override
    public int countReviewsForCommerce(final long commerceUserId) {
        return commerceReviewDao.countByCommerceId(commerceUserId);
    }

    @Transactional
    @Override
    public CommerceReview upsertReview(final long clientUserId, final long commerceUserId, final int rating,
            final String body) throws CommerceReviewException {
        if (!canClientReviewCommerce(clientUserId, commerceUserId)) {
            LOGGER.warn("Review rejected: clientUserId={} not eligible for commerceUserId={}", clientUserId,
                    commerceUserId);
            throw new CommerceReviewException(CommerceReviewException.Reason.NOT_ELIGIBLE, "Client is not eligible to review this commerce");
        }
        validateRating(rating);
        final String normalizedBody = normalizeBody(body);

        return commerceReviewDao.findByClientAndCommerce(clientUserId, commerceUserId)
                .map(existing -> commerceReviewDao.updateReview(existing.getId(), rating, normalizedBody))
                .orElseGet(() -> commerceReviewDao.createReview(commerceUserId, clientUserId, rating, normalizedBody));
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<Double> averageRatingForCommerce(final long commerceUserId) {
        return Optional.ofNullable(commerceReviewDao.averageRatingByCommerceId(commerceUserId));
    }

    @Transactional(readOnly = true)
    @Override
    public Map<Long, Double> findAverageRatingsForCommerceIds(final List<Long> commerceUserIds) {
        return commerceReviewDao.findAverageRatingsForCommerceIds(commerceUserIds);
    }

    private void validateRating(final int rating) throws CommerceReviewException {
        if (rating < MIN_RATING || rating > MAX_RATING) {
            throw new CommerceReviewException(CommerceReviewException.Reason.INVALID_RATING, "Rating must be between 1 and 5");
        }
    }

    private String normalizeBody(final String body) throws CommerceReviewException {
        final String normalized = body == null ? "" : body.trim();
        if (normalized.isEmpty()) {
            throw new CommerceReviewException(CommerceReviewException.Reason.INVALID_BODY, "Review body is required");
        }
        if (normalized.length() > MAX_BODY_LENGTH) {
            throw new CommerceReviewException(CommerceReviewException.Reason.INVALID_BODY, "Review body is too long");
        }
        return normalized;
    }
}
