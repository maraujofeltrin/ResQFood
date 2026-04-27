package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.models.image.Image;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Repository
public class ImageJdbcDao implements ImageDao {

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert simpleJdbcInsert;

    @Autowired
    public ImageJdbcDao(final DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.simpleJdbcInsert = new SimpleJdbcInsert(dataSource)
                .withTableName("images")
                .usingGeneratedKeyColumns("id");
    }

    @Override
    public Image saveImage(byte[] data, String contentType) {
        final Map<String, Object> parameters = new HashMap<>();
        parameters.put("data", data);
        parameters.put("content_type", contentType);

        final Number id = simpleJdbcInsert.executeAndReturnKey(parameters);
        return new Image(id.longValue(), data, contentType);
    }

    @Override
    public Optional<Image> getImage(long id) {
        return jdbcTemplate.query("SELECT id, data, content_type FROM images WHERE id = ?",
                (rs, rowNum) -> new Image(
                        rs.getLong("id"),
                        rs.getBytes("data"),
                        rs.getString("content_type")
                ), id).stream().findAny();
    }
}
