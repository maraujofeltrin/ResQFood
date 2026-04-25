package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.models.pack.Pack;
import ar.edu.itba.paw.models.pack.PackTag;
import ar.edu.itba.paw.models.user.Commerce;
import ar.edu.itba.paw.models.user.User;
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
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestConfig.class)
@Sql("classpath:schema.sql")
public class PackJdbcDaoTest {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private PackJdbcDao packDao;

    @Autowired
    private UserJdbcDao userDao;

    @Autowired
    private CommerceJdbcDao commerceDao;

    private JdbcTemplate jdbcTemplate;

    private Long commerceId;

    @BeforeEach
    public void setUp() {
        jdbcTemplate = new JdbcTemplate(dataSource);
        JdbcTestUtils.deleteFromTables(jdbcTemplate, "bids", "auctions", "reservation_tokens", "pack_tags", "reservations", "packs", "commerces", "clients", "tokens", "users");
        
        commerceId = userDao.createUser("commerce@example.com", "pass", "Commerce", "123", User.Role.COMMERCE).getId();
        commerceDao.createCommerce(commerceId, "Comm", Commerce.Category.BAKERY, "Street", 123, "City", "Prov", "1000", "08:00", "20:00");
    }

    @Test
    public void testCreatePack() {
        // 1. Setup
        List<PackTag> tags = Collections.singletonList(PackTag.VEGAN);

        // 2. Ejercicio
        Pack pack = packDao.createPack(commerceId, "Title", "Desc", 1000.0, 500.0, 10, tags, null, null);

        // 3. Asserts
        assertNotNull(pack);
        assertEquals("Title", pack.getTitle());
        assertEquals(1, pack.getTags().size());
        assertEquals(PackTag.VEGAN, pack.getTags().get(0));
        assertEquals(1, JdbcTestUtils.countRowsInTable(jdbcTemplate, "packs"));
        assertEquals(1, JdbcTestUtils.countRowsInTable(jdbcTemplate, "pack_tags"));
    }

    @Test
    public void testFindById() {
        // 1. Setup
        Pack created = packDao.createPack(commerceId, "Title", "Desc", 1000.0, 500.0, 10, null, null, null);

        // 2. Ejercicio
        Optional<Pack> pack = packDao.findById(created.getId());

        // 3. Asserts
        assertTrue(pack.isPresent());
        assertEquals(created.getId(), pack.get().getId());
    }

    @Test
    public void testFindAll() {
        // 1. Setup
        packDao.createPack(commerceId, "Title1", "Desc1", 1000.0, 500.0, 10, null, null, null);
        packDao.createPack(commerceId, "Title2", "Desc2", 2000.0, 1000.0, 5, null, null, null);

        // 2. Ejercicio
        List<Pack> packs = packDao.findAll();

        // 3. Asserts
        assertEquals(2, packs.size());
    }

    @Test
    public void testFindByCommerceId() {
        // 1. Setup
        packDao.createPack(commerceId, "Title1", "Desc1", 1000.0, 500.0, 10, null, null, null);
        Long otherCommerceId = userDao.createUser("other@example.com", "pass", "Other", "123", User.Role.COMMERCE).getId();
        commerceDao.createCommerce(otherCommerceId, "Other Comm", Commerce.Category.BAKERY, "Street", 123, "City", "Prov", "1000", "08:00", "20:00");
        packDao.createPack(otherCommerceId, "Title2", "Desc2", 2000.0, 1000.0, 5, null, null, null);

        // 2. Ejercicio
        List<Pack> packs = packDao.findByCommerceId(commerceId);

        // 3. Asserts
        assertEquals(1, packs.size());
        assertEquals(commerceId, packs.get(0).getCommerceId());
    }

    @Test
    public void testUpdate() {
        // 1. Setup
        Pack created = packDao.createPack(commerceId, "Title", "Desc", 1000.0, 500.0, 10, null, null, null);
        created.setTitle("New Title");
        created.setStock(5);

        // 2. Ejercicio
        packDao.update(created);

        // 3. Asserts
        Optional<Pack> updated = packDao.findById(created.getId());
        assertTrue(updated.isPresent());
        assertEquals("New Title", updated.get().getTitle());
        assertEquals(5, updated.get().getStock());
    }

    @Test
    public void testSoftDelete() {
        // 1. Setup
        Pack created = packDao.createPack(commerceId, "Title", "Desc", 1000.0, 500.0, 10, null, null, null);

        // 2. Ejercicio
        packDao.softDelete(created.getId());

        // 3. Asserts
        Optional<Pack> deleted = packDao.findById(created.getId());
        assertTrue(deleted.isPresent());
        assertTrue(deleted.get().getDeleted());
    }

    @Test
    public void testDecrementStock() {
        // 1. Setup
        Pack created = packDao.createPack(commerceId, "Title", "Desc", 1000.0, 500.0, 10, null, null, null);

        // 2. Ejercicio
        boolean success = packDao.decrementStock(created.getId(), 3);

        // 3. Asserts
        assertTrue(success);
        Optional<Pack> updated = packDao.findById(created.getId());
        assertTrue(updated.isPresent());
        assertEquals(7, updated.get().getStock());
    }

    @Test
    public void testIncrementStock() {
        // 1. Setup
        Pack created = packDao.createPack(commerceId, "Title", "Desc", 1000.0, 500.0, 10, null, null, null);

        // 2. Ejercicio
        boolean success = packDao.incrementStock(created.getId(), 5);

        // 3. Asserts
        assertTrue(success);
        Optional<Pack> updated = packDao.findById(created.getId());
        assertTrue(updated.isPresent());
        assertEquals(15, updated.get().getStock());
    }
}
