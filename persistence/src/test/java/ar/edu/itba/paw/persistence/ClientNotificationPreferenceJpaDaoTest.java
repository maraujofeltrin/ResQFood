package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.models.notification.ClientNotificationPreference;
import ar.edu.itba.paw.models.notification.NotificationType;
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

import javax.sql.DataSource;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@Rollback
@Transactional
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestConfig.class)
public class ClientNotificationPreferenceJpaDaoTest {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private ClientNotificationPreferenceDao preferenceDao;

    @Autowired
    private UserDao userDao;

    @Autowired
    private ClientDao clientDao;

    private JdbcTemplate jdbcTemplate;

    private long clientUserId;

    @BeforeEach
    public void setUp() {
        jdbcTemplate = new JdbcTemplate(dataSource);
        JdbcTestUtils.deleteFromTables(jdbcTemplate, "client_notification_preferences", "notifications",
                "commerce_reviews", "bids", "auctions", "reservation_tokens", "pack_tags", "reservations",
                "client_pack_favorites", "client_commerce_favorites", "packs", "images", "commerces", "clients",
                "tokens", "users");

        clientUserId = userDao.createUser("cl@test.com", "p", "Cl", "3", User.Role.CLIENT).getId();
        clientDao.createClient(clientUserId, "A", "B", true);
    }

    @Test
    public void testUpsertWhenPreferenceDoesNotExistCreatesRow() {
        // 1. Setup — clientUserId existente

        // 2. Ejercicio
        final ClientNotificationPreference pref = preferenceDao.upsert(
                clientUserId, NotificationType.AUCTION_OUTBID_CLIENT, false);

        // 3. Asserts
        assertEquals(false, pref.isMailEnabled());
        assertEquals(1, JdbcTestUtils.countRowsInTable(jdbcTemplate, "client_notification_preferences"));
    }

    @Test
    public void testUpsertWhenPreferenceExistsUpdatesMailEnabled() {
        // 1. Setup
        preferenceDao.upsert(clientUserId, NotificationType.AUCTION_OUTBID_CLIENT, true);

        // 2. Ejercicio
        final ClientNotificationPreference updated = preferenceDao.upsert(
                clientUserId, NotificationType.AUCTION_OUTBID_CLIENT, false);

        // 3. Asserts
        assertEquals(false, updated.isMailEnabled());
        assertEquals(1, JdbcTestUtils.countRowsInTable(jdbcTemplate, "client_notification_preferences"));
    }

    @Test
    public void testFindByClientAndTypeWhenExistsReturnsPreference() {
        // 1. Setup
        preferenceDao.upsert(clientUserId, NotificationType.RESERVATION_CODE_CLIENT, true);

        // 2. Ejercicio
        final Optional<ClientNotificationPreference> found = preferenceDao.findByClientAndType(clientUserId,
                NotificationType.RESERVATION_CODE_CLIENT);

        // 3. Asserts
        assertTrue(found.isPresent());
        assertTrue(found.get().isMailEnabled());
    }

    @Test
    public void testFindByClientAndTypeWhenPreferenceDoesNotExistReturnsEmpty() {
        // 1. Setup — clientUserId existente, sin preferencias

        // 2. Ejercicio
        final Optional<ClientNotificationPreference> found = preferenceDao.findByClientAndType(clientUserId,
                NotificationType.RESERVATION_CODE_CLIENT);

        // 3. Asserts
        assertTrue(found.isEmpty());
    }

    @Test
    public void testFindByClientReturnsAllPreferencesForClient() {
        // 1. Setup
        preferenceDao.upsert(clientUserId, NotificationType.RESERVATION_CODE_CLIENT, true);
        preferenceDao.upsert(clientUserId, NotificationType.AUCTION_OUTBID_CLIENT, false);

        // 2. Ejercicio
        final List<ClientNotificationPreference> prefs = preferenceDao.findByClient(clientUserId);

        // 3. Asserts
        assertEquals(2, prefs.size());
    }
}
