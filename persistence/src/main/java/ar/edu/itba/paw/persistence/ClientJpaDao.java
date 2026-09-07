package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.models.user.Client;
import ar.edu.itba.paw.models.user.User;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Optional;

@Primary
@Repository
public class ClientJpaDao implements ClientDao {

    @PersistenceContext
    private EntityManager em;

    @Override
    public Client createClient(final Long userId, final String name, final String lastName, final Boolean notificationsVisibilityPreferences) {
        final User user = em.getReference(User.class, userId);
        final Client client = new Client(user, name, lastName, notificationsVisibilityPreferences);
        em.persist(client);
        return client;
    }

    @Override
    public Optional<Client> findByUserId(final Long userId) {
        return Optional.ofNullable(em.find(Client.class, userId));
    }

    @Override
    public Client update(final Client client) {
        return client;
    }
}
