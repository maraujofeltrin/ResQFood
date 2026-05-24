package ar.edu.itba.paw.persistence;

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
public class CommerceFavoriteJpaDaoTest {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private CommerceFavoriteDao commerceFavoriteDao;

    @Autowired
    private UserDao userDao;

    @Autowired
    private CommerceDao commerceDao;

    @Autowired
    private ClientDao clientDao;

    private JdbcTemplate jdbcTemplate;

    private long commerceUserId1;
    private long commerceUserId2;
    private long clientUserId;

    @BeforeEach
    public void setUp() {
        jdbcTemplate = new JdbcTemplate(dataSource);
        JdbcTestUtils.deleteFromTables(jdbcTemplate, "commerce_reviews", "bids", "auctions", "reservation_tokens",
                "pack_tags", "reservations", "client_pack_favorites", "client_commerce_favorites", "packs", "images", "commerces", "clients",
                "tokens", "users");

        commerceUserId1 = userDao.createUser("c1@test.com", "p", "C1", "1", User.Role.COMMERCE).getId();
        commerceDao.createCommerce(commerceUserId1, "Shop 1", Commerce.Category.BAKERY, "St", 1,
                ar.edu.itba.paw.models.pack.Municipality.AVELLANEDA, "P", "1000", "09:00", "18:00");

        commerceUserId2 = userDao.createUser("c2@test.com", "p", "C2", "2", User.Role.COMMERCE).getId();
        commerceDao.createCommerce(commerceUserId2, "Shop 2", Commerce.Category.RESTAURANT, "St", 2,
                ar.edu.itba.paw.models.pack.Municipality.AVELLANEDA, "P", "1000", "09:00", "18:00");

        clientUserId = userDao.createUser("cl@test.com", "p", "Cl", "3", User.Role.CLIENT).getId();
        clientDao.createClient(clientUserId, "A", "B", true);
    }

    @Test
    public void testFindFavoriteCommercesForClientWhenTwoFavoritesExist() {
        // 1. Setup
        commerceFavoriteDao.insert(clientUserId, commerceUserId1);
        commerceFavoriteDao.insert(clientUserId, commerceUserId2);

        // 2. Ejercicio
        final List<Commerce> favorites = commerceFavoriteDao.findFavoriteCommercesForClient(clientUserId, 1, 10);

        // 3. Asserts
        assertEquals(2, favorites.size());
        assertEquals(2, JdbcTestUtils.countRowsInTable(jdbcTemplate, "client_commerce_favorites"));
    }

    @Test
    public void testCountFavoriteCommercesForClientWhenTwoFavoritesExist() {
        // 1. Setup
        commerceFavoriteDao.insert(clientUserId, commerceUserId1);
        commerceFavoriteDao.insert(clientUserId, commerceUserId2);

        // 2. Ejercicio
        final int count = commerceFavoriteDao.countFavoriteCommercesForClient(clientUserId);

        // 3. Asserts
        assertEquals(2, count);
        assertEquals(2, JdbcTestUtils.countRowsInTable(jdbcTemplate, "client_commerce_favorites"));
    }

    @Test
    public void testFindFavoriteCommercesForClientWhenPageSizeOneReturnsOneCommerce() {
        // 1. Setup
        commerceFavoriteDao.insert(clientUserId, commerceUserId1);
        commerceFavoriteDao.insert(clientUserId, commerceUserId2);

        // 2. Ejercicio
        final List<Commerce> page1 = commerceFavoriteDao.findFavoriteCommercesForClient(clientUserId, 1, 1);

        // 3. Asserts
        assertEquals(1, page1.size());
        assertEquals(2, JdbcTestUtils.countRowsInTable(jdbcTemplate, "client_commerce_favorites"));
    }

    @Test
    public void testExistsWhenFavoriteDoesNotExist() {
        // 1. Setup
        // Client and commerce from setUp(); no favorite row.

        // 2. Ejercicio
        final boolean exists = commerceFavoriteDao.exists(clientUserId, commerceUserId1);

        // 3. Asserts
        assertFalse(exists);
        assertEquals(0, JdbcTestUtils.countRowsInTable(jdbcTemplate, "client_commerce_favorites"));
    }

    @Test
    public void testInsertWhenFavoriteDoesNotExist() {
        // 1. Setup
        // Client and commerce from setUp().

        // 2. Ejercicio
        commerceFavoriteDao.insert(clientUserId, commerceUserId1);

        // 3. Asserts
        assertEquals(1, JdbcTestUtils.countRowsInTable(jdbcTemplate, "client_commerce_favorites"));
    }

    @Test
    public void testDeleteWhenFavoriteExists() {
        // 1. Setup
        commerceFavoriteDao.insert(clientUserId, commerceUserId1);

        // 2. Ejercicio
        commerceFavoriteDao.delete(clientUserId, commerceUserId1);

        // 3. Asserts
        assertEquals(0, JdbcTestUtils.countRowsInTable(jdbcTemplate, "client_commerce_favorites"));
    }
}
