package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.models.user.CommerceReview;

import java.util.List;
import java.util.Optional;

public interface CommerceReviewDao {

    CommerceReview createReview(Long commerceUserId, Long clientUserId, Integer rating, String body);

    CommerceReview updateReview(Long id, Integer rating, String body);

    Optional<CommerceReview> findByClientAndCommerce(Long clientUserId, Long commerceUserId);

    List<CommerceReview> findByCommerceId(Long commerceUserId, int page, int pageSize);

    int countByCommerceId(Long commerceUserId);

    Double averageRatingByCommerceId(Long commerceUserId);
}
