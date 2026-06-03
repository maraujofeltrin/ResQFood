package ar.edu.itba.paw.models.pack;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class ClientPackFavoriteId implements Serializable {

    private static final long serialVersionUID = 1L;

    @Column(name = "client_id")
    private Long clientId;

    @Column(name = "pack_id")
    private Long packId;

    protected ClientPackFavoriteId() {}

    public ClientPackFavoriteId(final Long clientId, final Long packId) {
        this.clientId = clientId;
        this.packId = packId;
    }

    public Long getClientId() {
        return clientId;
    }

    public Long getPackId() {
        return packId;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ClientPackFavoriteId)) {
            return false;
        }
        final ClientPackFavoriteId that = (ClientPackFavoriteId) o;
        return Objects.equals(clientId, that.clientId) && Objects.equals(packId, that.packId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(clientId, packId);
    }
}
