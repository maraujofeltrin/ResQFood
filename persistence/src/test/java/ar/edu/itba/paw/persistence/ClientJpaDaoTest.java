package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.models.user.Client;
import ar.edu.itba.paw.models.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.jdbc.JdbcTestUtils;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.sql.DataSource;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@Rollback
@Transactional
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestConfig.class)
public class ClientJpaDaoTest {

    private static final String EMAIL = "test@example.com";
    private static final String PASSWORD = "password123";
    private static final String NAME = "Test";
    private static final String LAST_NAME = "Client";
    private static final String PHONE = "123456789";

    @Autowired
    private DataSource dataSource;

    @Autowired
    private ClientJpaDao clientDao;

    @Autowired
    private UserJpaDao userDao;

    @PersistenceContext
    private EntityManager em;

    private JdbcTemplate jdbcTemplate;

    private Long userId;

    @BeforeEach
    public void setUp() {
        jdbcTemplate = new JdbcTemplate(dataSource);
        JdbcTestUtils.deleteFromTables(jdbcTemplate, "bids", "auctions", "reservation_tokens", "pack_tags", "reservations", "client_pack_favorites", "packs", "images", "commerces", "clients", "tokens", "users");

        userId = userDao.createUser(EMAIL, PASSWORD, NAME, PHONE, User.Role.CLIENT).getId();
        em.flush();
    }

    @Test
    public void testCreateClientWhenUserExists() {
        // 1. Setup
        // Base user prepared in setUp().

        // 2. Ejercicio
        final Client client = clientDao.createClient(userId, NAME, LAST_NAME, true);
        em.flush();

        // 3. Asserts
        assertNotNull(client);
        assertEquals(userId, client.getUserId());
        assertEquals(NAME, client.getName());
        assertEquals(LAST_NAME, client.getLastName());
        assertTrue(client.getNotificationsVisibilityPreferences());
        assertEquals(1, JdbcTestUtils.countRowsInTable(jdbcTemplate, "clients"));
    }

    @Test
    public void testFindByUserIdWhenClientExists() {
        // 1. Setup
        clientDao.createClient(userId, NAME, LAST_NAME, true);
        em.flush();

        // 2. Ejercicio
        final Optional<Client> client = clientDao.findByUserId(userId);

        // 3. Asserts
        assertTrue(client.isPresent());
        assertEquals(userId, client.get().getUserId());
        assertEquals(NAME, client.get().getName());
        assertEquals(LAST_NAME, client.get().getLastName());
        assertEquals(1, JdbcTestUtils.countRowsInTable(jdbcTemplate, "clients"));
    }

    @Test
    public void testFindByUserIdWhenClientDoesNotExist() {
        // 1. Setup
        // No client row for the user created in setUp().

        // 2. Ejercicio
        final Optional<Client> client = clientDao.findByUserId(userId);

        // 3. Asserts
        assertTrue(client.isEmpty());
        assertEquals(0, JdbcTestUtils.countRowsInTable(jdbcTemplate, "clients"));
    }

    @Test
    public void testUpdateWhenClientExists() {
        // 1. Setup
        final Client client = clientDao.createClient(userId, NAME, LAST_NAME, true);
        em.flush();
        em.clear();

        client.setName("UpdatedName");
        client.setLastName("UpdatedLastName");
        client.setNotificationsVisibilityPreferences(false);

        // 2. Ejercicio
        final Client updatedClient = clientDao.update(client);
        em.flush();

        // 3. Asserts
        assertNotNull(updatedClient);
        assertEquals("UpdatedName", updatedClient.getName());
        assertEquals("UpdatedLastName", updatedClient.getLastName());
        assertFalse(updatedClient.getNotificationsVisibilityPreferences());
        assertEquals(1, JdbcTestUtils.countRowsInTable(jdbcTemplate, "clients"));
    }
}
