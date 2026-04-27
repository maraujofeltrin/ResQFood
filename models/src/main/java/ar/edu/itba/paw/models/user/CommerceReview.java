package ar.edu.itba.paw.models.user;

import java.time.LocalDateTime;

public class CommerceReview {

    private final Long id;
    private final Long commerceUserId;
    private final Long clientUserId;
    private final Integer rating;
    private final String body;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public CommerceReview(final Long id, final Long commerceUserId, final Long clientUserId, final Integer rating,
            final String body, final LocalDateTime createdAt, final LocalDateTime updatedAt) {
        this.id = id;
        this.commerceUserId = commerceUserId;
        this.clientUserId = clientUserId;
        this.rating = rating;
        this.body = body;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public Long getCommerceUserId() {
        return commerceUserId;
    }

    public Long getClientUserId() {
        return clientUserId;
    }

    public Integer getRating() {
        return rating;
    }

    public String getBody() {
        return body;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
