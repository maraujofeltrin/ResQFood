package ar.edu.itba.paw.models.pack;

import ar.edu.itba.paw.models.user.Client;

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
@Table(name = "client_pack_favorites")
public class ClientPackFavorite {

    @EmbeddedId
    private ClientPackFavoriteId id;

    @MapsId("clientId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @MapsId("packId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pack_id", nullable = false)
    private Pack pack;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    protected ClientPackFavorite() {}

    public ClientPackFavorite(final Client client, final Pack pack, final LocalDateTime createdAt) {
        this.client = client;
        this.pack = pack;
        this.id = new ClientPackFavoriteId(client.getUserId(), pack.getId());
        this.createdAt = createdAt;
    }

    public ClientPackFavoriteId getId() {
        return id;
    }

    public Client getClient() {
        return client;
    }

    public Pack getPack() {
        return pack;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public Long getClientId() {
        return id != null ? id.getClientId() : (client != null ? client.getUserId() : null);
    }

    public Long getPackId() {
        return id != null ? id.getPackId() : (pack != null ? pack.getId() : null);
    }
}
