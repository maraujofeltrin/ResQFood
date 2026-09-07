package ar.edu.itba.paw.services.user;

import ar.edu.itba.paw.models.user.Client;
import java.util.Optional;

public interface ClientService {

    Client createClient(final Long userId, final String name, final String lastName,
            final Boolean notificationsVisibilityPreferences);

    Optional<Client> findByUserId(final Long userId);

    Client update(final Client client);
}
