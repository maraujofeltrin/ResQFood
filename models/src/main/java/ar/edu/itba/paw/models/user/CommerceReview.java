package ar.edu.itba.paw.models.user;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name = "commerce_reviews")
public class CommerceReview {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "commerce_reviews_id_seq")
    @SequenceGenerator(sequenceName = "commerce_reviews_id_seq", name = "commerce_reviews_id_seq", allocationSize = 1)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "commerce_user_id")
    private Commerce commerce;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "client_user_id")
    private Client client;

    @Column(nullable = false)
    private Integer rating;

    @Column
    private String body;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected CommerceReview() {}

    public CommerceReview(final Long id, final Commerce commerce, final Client client, final Integer rating,
            final String body, final LocalDateTime createdAt, final LocalDateTime updatedAt) {
        this.id = id;
        this.commerce = commerce;
        this.client = client;
        this.rating = rating;
        this.body = body;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public Commerce getCommerce() {
        return commerce;
    }

    public Client getClient() {
        return client;
    }

    public Long getCommerceUserId() {
        return commerce.getUserId();
    }

    public Long getClientUserId() {
        return client.getUserId();
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

    @Override
    public String toString() {
        return "CommerceReview [id=" + id + ", commerceUserId=" + getCommerceUserId() + ", clientUserId="
                + getClientUserId() + ", rating=" + rating + ", body=" + body + ", createdAt=" + createdAt
                + ", updatedAt=" + updatedAt + "]";
    }
}
