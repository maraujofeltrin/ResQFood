package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.models.pack.Pack;
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
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Rollback
@Transactional
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestConfig.class)
public class PackFavoriteJdbcDaoTest {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private PackFavoriteJdbcDao packFavoriteDao;

    @Autowired
    private PackJdbcDao packDao;

    @Autowired
    private UserJdbcDao userDao;

    @Autowired
    private CommerceJdbcDao commerceDao;

    @Autowired
    private ClientJdbcDao clientDao;

    private JdbcTemplate jdbcTemplate;

    private long commerceUserId;
    private long clientUserId;
    private Pack packActive;

    @BeforeEach
    public void setUp() {
        jdbcTemplate = new JdbcTemplate(dataSource);
        JdbcTestUtils.deleteFromTables(jdbcTemplate, "commerce_reviews", "bids", "auctions", "reservation_tokens",
                "pack_tags", "reservations", "client_pack_favorites", "packs", "images", "commerces", "clients", "tokens",
                "users");

        commerceUserId = userDao.createUser("c@test.com", "p", "C", "1", User.Role.COMMERCE).getId();
        commerceDao.createCommerce(commerceUserId, "Shop", Commerce.Category.BAKERY, "St", 1, ar.edu.itba.paw.models.pack.Municipality.AVELLANEDA, "P", "1000", "09:00", "18:00");

        clientUserId = userDao.createUser("cl@test.com", "p", "Cl", "2", User.Role.CLIENT).getId();
        clientDao.createClient(clientUserId, "A", "B", true);

        packActive = packDao.createPack(commerceUserId, "P1", "D", 100.0, 80.0, 5, null, null);
    }

    @Test
    public void testFindActiveFavoritePacksForClientWhenTwoActiveFavoritesExist() {
        // 1. Setup
        final Pack p2 = packDao.createPack(commerceUserId, "P2", "D2", 200.0, 150.0, 3, null, null);
        packFavoriteDao.insert(clientUserId, packActive.getId());
        packFavoriteDao.insert(clientUserId, p2.getId());

        // 2. Ejercicio
        final List<Pack> favorites = packFavoriteDao.findActiveFavoritePacksForClient(clientUserId, 1, 10);

        // 3. Asserts
        assertEquals(2, favorites.size());
        assertEquals(2, JdbcTestUtils.countRowsInTable(jdbcTemplate, "client_pack_favorites"));
    }

    @Test
    public void testFindActiveFavoritePacksForClientWhenFavoritePackInactiveReturnsEmpty() {
        // 1. Setup
        packFavoriteDao.insert(clientUserId, packActive.getId());
        packDao.setActive(packActive.getId(), false);

        // 2. Ejercicio
        final List<Pack> favorites = packFavoriteDao.findActiveFavoritePacksForClient(clientUserId, 1, 10);

        // 3. Asserts
        assertTrue(favorites.isEmpty());
        assertEquals(1, JdbcTestUtils.countRowsInTable(jdbcTemplate, "client_pack_favorites"));
    }

    @Test
    public void testCountActiveFavoritePacksForClientWhenTwoFavoritesExist() {
        // 1. Setup
        packFavoriteDao.insert(clientUserId, packActive.getId());
        final Pack p2 = packDao.createPack(commerceUserId, "P2", "D2", 200.0, 150.0, 3, null, null);
        packFavoriteDao.insert(clientUserId, p2.getId());

        // 2. Ejercicio
        final int count = packFavoriteDao.countActiveFavoritePacksForClient(clientUserId);

        // 3. Asserts
        assertEquals(2, count);
        assertEquals(2, JdbcTestUtils.countRowsInTable(jdbcTemplate, "client_pack_favorites"));
    }

    @Test
    public void testFindActiveFavoritePacksForClientWhenPageSizeOneReturnsOnePack() {
        // 1. Setup
        packFavoriteDao.insert(clientUserId, packActive.getId());
        final Pack p2 = packDao.createPack(commerceUserId, "P2", "D2", 200.0, 150.0, 3, null, null);
        packFavoriteDao.insert(clientUserId, p2.getId());

        // 2. Ejercicio
        final List<Pack> page1 = packFavoriteDao.findActiveFavoritePacksForClient(clientUserId, 1, 1);

        // 3. Asserts
        assertEquals(1, page1.size());
        assertEquals(2, JdbcTestUtils.countRowsInTable(jdbcTemplate, "client_pack_favorites"));
    }

    @Test
    public void testExistsWhenFavoriteDoesNotExist() {
        // 1. Setup
        // Client and pack from setUp(); no favorite row.

        // 2. Ejercicio
        final boolean exists = packFavoriteDao.exists(clientUserId, packActive.getId());

        // 3. Asserts
        assertFalse(exists);
        assertEquals(0, JdbcTestUtils.countRowsInTable(jdbcTemplate, "client_pack_favorites"));
    }

    @Test
    public void testInsertWhenFavoriteDoesNotExist() {
        // 1. Setup
        // Client and pack from setUp().

        // 2. Ejercicio
        packFavoriteDao.insert(clientUserId, packActive.getId());

        // 3. Asserts
        assertTrue(packFavoriteDao.exists(clientUserId, packActive.getId()));
        assertEquals(1, JdbcTestUtils.countRowsInTable(jdbcTemplate, "client_pack_favorites"));
    }

    @Test
    public void testDeleteWhenFavoriteExists() {
        // 1. Setup
        packFavoriteDao.insert(clientUserId, packActive.getId());

        // 2. Ejercicio
        packFavoriteDao.delete(clientUserId, packActive.getId());

        // 3. Asserts
        assertFalse(packFavoriteDao.exists(clientUserId, packActive.getId()));
        assertEquals(0, JdbcTestUtils.countRowsInTable(jdbcTemplate, "client_pack_favorites"));
    }
}
