package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.models.user.Client;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.jdbc.JdbcTestUtils;

import javax.sql.DataSource;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestConfig.class)
@Sql("classpath:schema.sql")
public class ClientJdbcDaoTest {

    private static final String EMAIL = "test@example.com";
    private static final String PASSWORD = "password123";
    private static final String NAME = "Test";
    private static final String LAST_NAME = "Client";
    private static final String PHONE = "123456789";

    @Autowired
    private DataSource dataSource;

    @Autowired
    private ClientJdbcDao clientDao;

    @Autowired
    private UserJdbcDao userDao;

    private JdbcTemplate jdbcTemplate;

    private Long userId;

    @BeforeEach
    public void setUp() {
        jdbcTemplate = new JdbcTemplate(dataSource);
        JdbcTestUtils.deleteFromTables(jdbcTemplate, "clients", "users");
        
        // Create an underlying user for foreign key constraints
        userId = userDao.createUser(EMAIL, PASSWORD, NAME, PHONE, ar.edu.itba.paw.models.user.User.Role.CLIENT).getId();
    }

    @Test
    public void testCreateClient() {
        // 1. Setup
        // Clean table with user ready

        // 2. Ejercicio
        final Client client = clientDao.createClient(userId, NAME, LAST_NAME, true);

        // 3. Asserts
        assertNotNull(client);
        assertEquals(userId, client.getUserId());
        assertEquals(NAME, client.getName());
        assertEquals(LAST_NAME, client.getLastName());
        assertTrue(client.getNotificationsVisibilityPreferences());
        assertEquals(1, JdbcTestUtils.countRowsInTable(jdbcTemplate, "clients"));
    }

    @Test
    public void testFindByUserId_WhenClientExists() {
        // 1. Setup
        clientDao.createClient(userId, NAME, LAST_NAME, true);

        // 2. Ejercicio
        final Optional<Client> client = clientDao.findByUserId(userId);

        // 3. Asserts
        assertTrue(client.isPresent());
        assertEquals(userId, client.get().getUserId());
        assertEquals(NAME, client.get().getName());
        assertEquals(LAST_NAME, client.get().getLastName());
    }

    @Test
    public void testFindByUserId_WhenClientDoesNotExist() {
        // 1. Setup
        // Client not created for existing user

        // 2. Ejercicio
        final Optional<Client> client = clientDao.findByUserId(userId);

        // 3. Asserts
        assertFalse(client.isPresent());
    }

    @Test
    public void testUpdate() {
        // 1. Setup
        Client client = clientDao.createClient(userId, NAME, LAST_NAME, true);
        client.setName("UpdatedName");
        client.setLastName("UpdatedLastName");
        client.setNotificationsVisibilityPreferences(false);

        // 2. Ejercicio
        Client updatedClient = clientDao.update(client);

        // 3. Asserts
        assertNotNull(updatedClient);
        assertEquals("UpdatedName", updatedClient.getName());
        assertEquals("UpdatedLastName", updatedClient.getLastName());
        assertFalse(updatedClient.getNotificationsVisibilityPreferences());
        
        Optional<Client> dbClient = clientDao.findByUserId(userId);
        assertTrue(dbClient.isPresent());
        assertEquals("UpdatedName", dbClient.get().getName());
        assertEquals("UpdatedLastName", dbClient.get().getLastName());
        assertFalse(dbClient.get().getNotificationsVisibilityPreferences());
    }
}
