package ar.edu.itba.paw.services.commerce;

import ar.edu.itba.paw.models.user.CommerceReview;
import ar.edu.itba.paw.models.user.CommerceReviewException;

import java.util.List;
import java.util.Optional;

public interface CommerceReviewService {

    int MIN_RATING = 1;
    int MAX_RATING = 5;
    int MAX_BODY_LENGTH = 500;

    boolean canClientReviewCommerce(long clientUserId, long commerceUserId);

    Optional<CommerceReview> findClientReview(long clientUserId, long commerceUserId);

    List<CommerceReview> findReviewsForCommerce(long commerceUserId, int page, int pageSize);

    int countReviewsForCommerce(long commerceUserId);

    /**
     * @throws CommerceReviewException if the client is not eligible or the payload is invalid
     */
    CommerceReview upsertReview(long clientUserId, long commerceUserId, int rating, String body) throws CommerceReviewException;

    Optional<Double> averageRatingForCommerce(long commerceUserId);
}
