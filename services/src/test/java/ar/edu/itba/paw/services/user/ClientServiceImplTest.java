package ar.edu.itba.paw.services.user;

import ar.edu.itba.paw.models.user.Client;
import ar.edu.itba.paw.persistence.ClientDao;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class ClientServiceImplTest {

    @Mock
    private ClientDao clientDao;

    @InjectMocks
    private ClientServiceImpl clientService;

    @Test
    public void testCreateClient() {
        // 1. Setup
        final Long userId = 10L;
        final String name = "Test";
        final String lastName = "User";
        final Boolean notifPref = true;
        final Client expectedClient = new Client(userId, name, lastName, notifPref);
        
        Mockito.when(clientDao.createClient(userId, name, lastName, notifPref))
               .thenReturn(expectedClient);

        // 2. Ejercicio
        final Client client = clientService.createClient(userId, name, lastName, notifPref);

        // 3. Asserts
        assertNotNull(client);
        assertEquals(userId, client.getUserId());
        assertEquals(name, client.getName());
    }

    @Test
    public void testFindByUserId_WhenClientExists() {
        // 1. Setup
        final Long userId = 10L;
        final Client expectedClient = new Client(userId, "Test", "User", true);
        Mockito.when(clientDao.findByUserId(userId)).thenReturn(Optional.of(expectedClient));

        // 2. Ejercicio
        final Optional<Client> client = clientService.findByUserId(userId);

        // 3. Asserts
        assertTrue(client.isPresent());
        assertEquals(userId, client.get().getUserId());
    }

    @Test
    public void testFindByUserId_WhenClientDoesNotExist() {
        // 1. Setup
        final Long userId = 10L;
        Mockito.when(clientDao.findByUserId(userId)).thenReturn(Optional.empty());

        // 2. Ejercicio
        final Optional<Client> client = clientService.findByUserId(userId);

        // 3. Asserts
        assertFalse(client.isPresent());
    }

    @Test
    public void testUpdateClient() {
        // 1. Setup
        final Client clientToUpdate = new Client(11L, "NewName", "User", false);
        Mockito.when(clientDao.update(clientToUpdate)).thenReturn(clientToUpdate);

        // 2. Ejercicio
        final Client result = clientService.update(clientToUpdate);

        // 3. Asserts
        assertNotNull(result);
        assertEquals("NewName", result.getName());
    }
}
