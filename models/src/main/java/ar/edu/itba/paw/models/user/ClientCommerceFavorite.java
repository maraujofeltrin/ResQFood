package ar.edu.itba.paw.models.user;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "client_commerce_favorites")
public class ClientCommerceFavorite {

    @EmbeddedId
    private ClientCommerceFavoriteId id;

    @MapsId("clientId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @MapsId("commerceId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "commerce_id", nullable = false, referencedColumnName = "user_id")
    private Commerce commerce;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    protected ClientCommerceFavorite() {}

    public ClientCommerceFavorite(final Client client, final Commerce commerce, final LocalDateTime createdAt) {
        this.client = client;
        this.commerce = commerce;
        this.id = new ClientCommerceFavoriteId(client.getUserId(), commerce.getUserId());
        this.createdAt = createdAt;
    }

    public ClientCommerceFavoriteId getId() {
        return id;
    }

    public Client getClient() {
        return client;
    }

    public Commerce getCommerce() {
        return commerce;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public Long getClientId() {
        return id != null ? id.getClientId() : (client != null ? client.getUserId() : null);
    }

    public Long getCommerceId() {
        return id != null ? id.getCommerceId() : (commerce != null ? commerce.getUserId() : null);
    }
}
