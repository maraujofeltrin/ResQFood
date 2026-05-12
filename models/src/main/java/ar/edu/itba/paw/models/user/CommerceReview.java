package ar.edu.itba.paw.models.user;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name = "commerce_reviews")
public class CommerceReview {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "commerce_reviews_id_seq")
    @SequenceGenerator(sequenceName = "commerce_reviews_id_seq", name = "commerce_reviews_id_seq", allocationSize = 1)
    private Long id;
    @Column(name = "commerce_user_id", nullable = false)
    private Long commerceUserId;
    @Column(name = "client_user_id", nullable = false)
    private Long clientUserId;
    @Column(nullable = false)
    private Integer rating;
    @Column
    private String body;
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected CommerceReview() {}

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
