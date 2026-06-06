package ar.edu.itba.paw.models.user;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class ClientCommerceFavoriteId implements Serializable {

    private static final long serialVersionUID = 1L;

    @Column(name = "client_id")
    private Long clientId;

    @Column(name = "commerce_id")
    private Long commerceId;

    protected ClientCommerceFavoriteId() {}

    public ClientCommerceFavoriteId(final Long clientId, final Long commerceId) {
        this.clientId = clientId;
        this.commerceId = commerceId;
    }

    public Long getClientId() {
        return clientId;
    }

    public Long getCommerceId() {
        return commerceId;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ClientCommerceFavoriteId)) {
            return false;
        }
        final ClientCommerceFavoriteId that = (ClientCommerceFavoriteId) o;
        return Objects.equals(clientId, that.clientId) && Objects.equals(commerceId, that.commerceId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(clientId, commerceId);
    }
}
