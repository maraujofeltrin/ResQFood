package ar.edu.itba.paw.services;

import ar.edu.itba.paw.models.Client;
import java.util.Optional;

public interface ClientService {

    Client createClient(final Long userId, final String name, final String lastName,
            final Boolean notificationsVisibilityPreferences);

    Optional<Client> findByUserId(final Long userId);

    Client update(final Client client);
}
