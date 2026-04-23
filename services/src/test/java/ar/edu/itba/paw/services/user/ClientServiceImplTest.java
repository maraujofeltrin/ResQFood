package ar.edu.itba.paw.services.user;

import ar.edu.itba.paw.models.user.Client;
import ar.edu.itba.paw.persistence.ClientDao;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class ClientServiceImplTest {

    static class InMemoryClientDao implements ClientDao {
        private final Map<Long, Client> store = new HashMap<>();

        @Override
        public Client createClient(Long userId, String name, String lastName, Boolean notificationsVisibilityPreferences) {
            final Client c = new Client(userId, name, lastName, notificationsVisibilityPreferences);
            store.put(userId, c);
            return c;
        }

        @Override
        public Optional<Client> findByUserId(Long userId) {
            return Optional.ofNullable(store.get(userId));
        }

        @Override
        public Client update(Client client) {
            store.put(client.getUserId(), client);
            return client;
        }
    }

    @Test
    public void createClient_and_findByUserId() {
        final InMemoryClientDao dao = new InMemoryClientDao();
        final ClientServiceImpl svc = new ClientServiceImpl(dao);

        final Client c = svc.createClient(10L, "N", "L", true);
        assertEquals(10L, c.getUserId());
        final var maybe = svc.findByUserId(10L);
        assertTrue(maybe.isPresent());
        assertEquals("N", maybe.get().getName());
    }

    @Test
    public void update_modifiesClient() {
        final InMemoryClientDao dao = new InMemoryClientDao();
        final ClientServiceImpl svc = new ClientServiceImpl(dao);

        final Client c = svc.createClient(11L, "A", "B", false);
        c.setName("X");
        c.setNotificationsVisibilityPreferences(true);
        svc.update(c);

        final var maybe = svc.findByUserId(11L);
        assertTrue(maybe.isPresent());
        assertEquals("X", maybe.get().getName());
        assertTrue(maybe.get().getNotificationsVisibilityPreferences());
    }
}
