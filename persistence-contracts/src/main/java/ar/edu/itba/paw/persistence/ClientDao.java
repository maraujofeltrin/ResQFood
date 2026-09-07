package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.models.user.Client;
import java.util.Optional;

public interface ClientDao {
    Client createClient(final Long userId, final String name, final String lastName,
            final Boolean notificationsVisibilityPreferences);

    Optional<Client> findByUserId(final Long userId);

    Client update(final Client client);
}

