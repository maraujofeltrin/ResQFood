package ar.edu.itba.paw.services.commerce;

import ar.edu.itba.paw.models.user.CommerceReview;

import java.util.List;
import java.util.Optional;

public interface CommerceReviewService {

    int MIN_RATING = 1;
    int MAX_RATING = 5;
    int MAX_BODY_LENGTH = 1000;

    boolean canClientReviewCommerce(long clientUserId, long commerceUserId);

    Optional<CommerceReview> findClientReview(long clientUserId, long commerceUserId);

    List<CommerceReview> findReviewsForCommerce(long commerceUserId, int page, int pageSize);

    int countReviewsForCommerce(long commerceUserId);

    CommerceReview upsertReview(long clientUserId, long commerceUserId, int rating, String body);
}
