package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.models.notification.Notification;
import ar.edu.itba.paw.models.notification.NotificationType;
import ar.edu.itba.paw.models.user.Commerce;
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
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@Rollback
@Transactional
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestConfig.class)
public class NotificationJpaDaoTest {

    private static final long NON_EXISTENT_NOTIFICATION_ID = 99_999L;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private NotificationDao notificationDao;

    @Autowired
    private UserDao userDao;

    @Autowired
    private ClientDao clientDao;

    @Autowired
    private CommerceDao commerceDao;

    @Autowired
    private PackDao packDao;

    private JdbcTemplate jdbcTemplate;

    private long clientUserId;
    private long otherUserId;
    private long packId;

    @BeforeEach
    public void setUp() {
        jdbcTemplate = new JdbcTemplate(dataSource);
        JdbcTestUtils.deleteFromTables(jdbcTemplate, "client_notification_preferences", "notifications",
                "commerce_reviews", "bids", "auctions", "reservation_tokens", "pack_tags", "reservations",
                "client_pack_favorites", "client_commerce_favorites", "packs", "images", "commerces", "clients",
                "tokens", "users");

        final long commerceUserId = userDao.createUser("c1@test.com", "p", "C1", "1", User.Role.COMMERCE).getId();
        commerceDao.createCommerce(commerceUserId, "Shop 1", Commerce.Category.BAKERY, "St", 1,
                ar.edu.itba.paw.models.pack.Municipality.AVELLANEDA, "P", "1000", "09:00", "18:00");
        packId = packDao.createPack(commerceUserId, "Pack title", "desc", 100.0, 50.0, 5,
                java.util.Collections.emptyList(), null).getId();

        clientUserId = userDao.createUser("cl@test.com", "p", "Cl", "3", User.Role.CLIENT).getId();
        clientDao.createClient(clientUserId, "A", "B", true);

        otherUserId = userDao.createUser("other@test.com", "p", "Ot", "4", User.Role.CLIENT).getId();
        clientDao.createClient(otherUserId, "O", "T", true);
    }

    @Test
    public void testCreateWhenRecipientExistsPersistsNotification() {
        // 1. Setup
        final LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);

        // 2. Ejercicio
        final Notification created = notificationDao.create(
                clientUserId, NotificationType.RESERVATION_CODE_CLIENT,
                null, null, packId, "Pack title", "Shop", 50.0, "ABC12", now, now);

        // 3. Asserts
        assertNotNull(created.getId());
        assertEquals(NotificationType.RESERVATION_CODE_CLIENT, created.getType());
        assertEquals(clientUserId, created.getRecipientId());
        assertEquals(1, JdbcTestUtils.countRowsInTable(jdbcTemplate, "notifications"));
    }

    @Test
    public void testFindByIdWhenNotificationExistsReturnsNotification() {
        // 1. Setup
        final LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        final Notification created = notificationDao.create(
                clientUserId, NotificationType.RESERVATION_CODE_CLIENT,
                null, null, packId, "Pack title", "Shop", 50.0, "ABC12", now, now);

        // 2. Ejercicio
        final Optional<Notification> found = notificationDao.findById(created.getId());

        // 3. Asserts
        assertTrue(found.isPresent());
        assertEquals(created.getId(), found.get().getId());
        assertEquals(NotificationType.RESERVATION_CODE_CLIENT, found.get().getType());
        assertEquals(clientUserId, found.get().getRecipientId());
        assertEquals(1, JdbcTestUtils.countRowsInTable(jdbcTemplate, "notifications"));
    }

    @Test
    public void testFindByIdWhenNotificationDoesNotExistReturnsEmpty() {
        // 1. Setup — sin notificaciones

        // 2. Ejercicio
        final Optional<Notification> found = notificationDao.findById(NON_EXISTENT_NOTIFICATION_ID);

        // 3. Asserts
        assertTrue(found.isEmpty());
        assertEquals(0, JdbcTestUtils.countRowsInTable(jdbcTemplate, "notifications"));
    }

    @Test
    public void testFindRecentByRecipientExcludesSoftDeleted() {
        // 1. Setup
        final LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        final Notification active = notificationDao.create(clientUserId, NotificationType.RESERVATION_CODE_CLIENT,
                null, null, packId, "t", "s", 1.0, null, null, now);
        final Notification deleted = notificationDao.create(clientUserId, NotificationType.AUCTION_OUTBID_CLIENT,
                null, null, packId, "t2", "s2", 2.0, null, null, now.minusHours(1));
        notificationDao.softDelete(deleted.getId(), now);

        // 2. Ejercicio
        final List<Notification> recent = notificationDao.findRecentByRecipient(clientUserId, 10);

        // 3. Asserts
        assertEquals(1, recent.size());
        assertEquals(active.getId(), recent.get(0).getId());
        assertEquals(2, JdbcTestUtils.countRowsInTable(jdbcTemplate, "notifications"));
    }

    @Test
    public void testCountUnreadExcludesReadAndDeleted() {
        // 1. Setup
        final LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        notificationDao.create(clientUserId, NotificationType.RESERVATION_CODE_CLIENT,
                null, null, packId, "t", "s", 1.0, null, null, now);
        final Notification read = notificationDao.create(clientUserId, NotificationType.AUCTION_OUTBID_CLIENT,
                null, null, packId, "t2", "s2", 2.0, null, null, now);
        notificationDao.markRead(read.getId(), now);
        final Notification deleted = notificationDao.create(clientUserId, NotificationType.RESERVATION_REJECTED_CLIENT,
                null, null, packId, "t3", "s3", 3.0, null, null, now);
        notificationDao.softDelete(deleted.getId(), now);

        // 2. Ejercicio
        final int unread = notificationDao.countUnread(clientUserId);

        // 3. Asserts
        assertEquals(1, unread);
        assertEquals(3, JdbcTestUtils.countRowsInTable(jdbcTemplate, "notifications"));
    }

    @Test
    public void testMarkReadSetsReadAt() {
        // 1. Setup
        final LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        final Notification created = notificationDao.create(clientUserId, NotificationType.RESERVATION_CODE_CLIENT,
                null, null, packId, "t", "s", 1.0, null, null, now);

        // 2. Ejercicio
        final Optional<Notification> result = notificationDao.markRead(created.getId(), now);

        // 3. Asserts
        assertTrue(result.isPresent());
        assertNotNull(result.get().getReadAt());
        assertEquals(1, JdbcTestUtils.countRowsInTable(jdbcTemplate, "notifications"));
    }

    @Test
    public void testMarkReadWhenNotificationDoesNotExistReturnsEmpty() {
        // 1. Setup
        final LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);

        // 2. Ejercicio
        final Optional<Notification> result = notificationDao.markRead(NON_EXISTENT_NOTIFICATION_ID, now);

        // 3. Asserts
        assertTrue(result.isEmpty());
        assertEquals(0, JdbcTestUtils.countRowsInTable(jdbcTemplate, "notifications"));
    }

    @Test
    public void testMarkReadWhenNotificationAlreadyDeletedReturnsEmpty() {
        // 1. Setup
        final LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        final Notification created = notificationDao.create(clientUserId, NotificationType.RESERVATION_CODE_CLIENT,
                null, null, packId, "t", "s", 1.0, null, null, now);
        notificationDao.softDelete(created.getId(), now);

        // 2. Ejercicio
        final Optional<Notification> result = notificationDao.markRead(created.getId(), now);

        // 3. Asserts
        assertTrue(result.isEmpty());
        assertEquals(1, JdbcTestUtils.countRowsInTable(jdbcTemplate, "notifications"));
    }

    @Test
    public void testMarkUnreadClearsReadAt() {
        // 1. Setup
        final LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        final Notification created = notificationDao.create(clientUserId, NotificationType.RESERVATION_CODE_CLIENT,
                null, null, packId, "t", "s", 1.0, null, null, now);
        notificationDao.markRead(created.getId(), now);

        // 2. Ejercicio
        final Optional<Notification> result = notificationDao.markUnread(created.getId());

        // 3. Asserts
        assertTrue(result.isPresent());
        assertNull(result.get().getReadAt());
        assertEquals(1, JdbcTestUtils.countRowsInTable(jdbcTemplate, "notifications"));
    }

    @Test
    public void testMarkUnreadWhenNotificationDoesNotExistReturnsEmpty() {
        // 1. Setup — sin notificaciones

        // 2. Ejercicio
        final Optional<Notification> result = notificationDao.markUnread(NON_EXISTENT_NOTIFICATION_ID);

        // 3. Asserts
        assertTrue(result.isEmpty());
        assertEquals(0, JdbcTestUtils.countRowsInTable(jdbcTemplate, "notifications"));
    }

    @Test
    public void testMarkAllReadUpdatesOnlyUnreadForRecipient() {
        // 1. Setup
        final LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        notificationDao.create(clientUserId, NotificationType.RESERVATION_CODE_CLIENT,
                null, null, packId, "t", "s", 1.0, null, null, now);
        notificationDao.create(clientUserId, NotificationType.AUCTION_OUTBID_CLIENT,
                null, null, packId, "t2", "s2", 2.0, null, null, now);
        notificationDao.create(otherUserId, NotificationType.RESERVATION_CODE_CLIENT,
                null, null, packId, "t3", "s3", 3.0, null, null, now);

        // 2. Ejercicio
        final int updated = notificationDao.markAllRead(clientUserId, now);

        // 3. Asserts
        assertEquals(2, updated);
        assertEquals(0, notificationDao.countUnread(clientUserId));
        assertEquals(1, notificationDao.countUnread(otherUserId));
        assertEquals(3, JdbcTestUtils.countRowsInTable(jdbcTemplate, "notifications"));
    }

    @Test
    public void testSoftDeleteWhenNotificationExistsSetsDeletedAt() {
        // 1. Setup
        final LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        final Notification created = notificationDao.create(clientUserId, NotificationType.RESERVATION_CODE_CLIENT,
                null, null, packId, "t", "s", 1.0, null, null, now);

        // 2. Ejercicio
        final Optional<Notification> result = notificationDao.softDelete(created.getId(), now);

        // 3. Asserts
        assertTrue(result.isPresent());
        assertNotNull(result.get().getDeletedAt());
        assertEquals(1, JdbcTestUtils.countRowsInTable(jdbcTemplate, "notifications"));
    }

    @Test
    public void testSoftDeleteWhenNotificationDoesNotExistReturnsEmpty() {
        // 1. Setup
        final LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);

        // 2. Ejercicio
        final Optional<Notification> result = notificationDao.softDelete(NON_EXISTENT_NOTIFICATION_ID, now);

        // 3. Asserts
        assertTrue(result.isEmpty());
        assertEquals(0, JdbcTestUtils.countRowsInTable(jdbcTemplate, "notifications"));
    }

    @Test
    public void testSoftDeleteWhenNotificationAlreadyDeletedReturnsEmpty() {
        // 1. Setup
        final LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        final Notification created = notificationDao.create(clientUserId, NotificationType.RESERVATION_CODE_CLIENT,
                null, null, packId, "t", "s", 1.0, null, null, now);
        notificationDao.softDelete(created.getId(), now);

        // 2. Ejercicio
        final Optional<Notification> result = notificationDao.softDelete(created.getId(), now);

        // 3. Asserts
        assertTrue(result.isEmpty());
        assertEquals(1, JdbcTestUtils.countRowsInTable(jdbcTemplate, "notifications"));
    }

    @Test
    public void testBelongsToRecipientWhenOwnerReturnsTrue() {
        // 1. Setup
        final LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        final Notification created = notificationDao.create(clientUserId, NotificationType.RESERVATION_CODE_CLIENT,
                null, null, packId, "t", "s", 1.0, null, null, now);

        // 2. Ejercicio
        final boolean belongs = notificationDao.belongsToRecipient(created.getId(), clientUserId);

        // 3. Asserts
        assertTrue(belongs);
        assertEquals(1, JdbcTestUtils.countRowsInTable(jdbcTemplate, "notifications"));
    }

    @Test
    public void testBelongsToRecipientWhenNotOwnerReturnsFalse() {
        // 1. Setup
        final LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        final Notification created = notificationDao.create(clientUserId, NotificationType.RESERVATION_CODE_CLIENT,
                null, null, packId, "t", "s", 1.0, null, null, now);

        // 2. Ejercicio
        final boolean belongs = notificationDao.belongsToRecipient(created.getId(), otherUserId);

        // 3. Asserts
        assertFalse(belongs);
        assertEquals(1, JdbcTestUtils.countRowsInTable(jdbcTemplate, "notifications"));
    }

    @Test
    public void testBelongsToRecipientWhenNotificationDoesNotExistReturnsFalse() {
        // 1. Setup — sin notificaciones

        // 2. Ejercicio
        final boolean belongs = notificationDao.belongsToRecipient(NON_EXISTENT_NOTIFICATION_ID, clientUserId);

        // 3. Asserts
        assertFalse(belongs);
        assertEquals(0, JdbcTestUtils.countRowsInTable(jdbcTemplate, "notifications"));
    }
}
