package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.models.image.Image;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@Rollback
@Transactional
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestConfig.class)
public class ImageJdbcDaoTest {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private ImageJdbcDao imageDao;

    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    public void setUp() {
        jdbcTemplate = new JdbcTemplate(dataSource);
        JdbcTestUtils.deleteFromTables(jdbcTemplate, "commerce_reviews", "bids", "auctions", "reservation_tokens",
                "pack_tags", "reservations", "client_pack_favorites", "packs", "images", "commerces", "clients", "tokens",
                "users");
    }

    @Test
    public void testSaveImageWhenDataAndContentTypeProvided() {
        // 1. Setup
        final byte[] data = new byte[] {3, 4, 5};
        final String contentType = "image/png";

        // 2. Ejercicio
        final Image saved = imageDao.saveImage(data, contentType);

        // 3. Asserts
        assertNotNull(saved.getId());
        assertArrayEquals(data, saved.getData());
        assertEquals(contentType, saved.getContentType());
        assertEquals(1, JdbcTestUtils.countRowsInTable(jdbcTemplate, "images"));
    }

    @Test
    public void testGetImageWhenImageExists() {
        // 1. Setup
        final Image saved = imageDao.saveImage(new byte[] {7, 8}, "image/jpeg");

        // 2. Ejercicio
        final Optional<Image> loaded = imageDao.getImage(saved.getId());

        // 3. Asserts
        assertTrue(loaded.isPresent());
        assertEquals(saved.getId(), loaded.get().getId());
        assertEquals("image/jpeg", loaded.get().getContentType());
        assertArrayEquals(new byte[] {7, 8}, loaded.get().getData());
        assertEquals(1, JdbcTestUtils.countRowsInTable(jdbcTemplate, "images"));
    }
}
