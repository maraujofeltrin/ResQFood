package ar.edu.itba.paw.persistence;

import org.springframework.stereotype.Repository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;
import java.util.List;
import org.springframework.jdbc.core.RowMapper;

import ar.edu.itba.paw.models.Pack;

@Repository
public class PackJdbcDao implements PackDao {

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert simpleJdbcInsert;
    private final static RowMapper<Pack> PACK_ROW_MAPPER = (rs, rowNum) -> new Pack(
        rs.getLong("id"),
        rs.getString("title"),
        rs.getString("description"),
        rs.getDouble("original_price"),
        rs.getDouble("final_price"),
        rs.getInt("stock"),
        rs.getBoolean("active")
    );

    @Autowired
    public PackJdbcDao(final DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.simpleJdbcInsert = new SimpleJdbcInsert(dataSource)
            .withTableName("packs")
            .usingGeneratedKeyColumns("id");
    }

    @Override
    public Pack createPack(String title, String description, Double originalPrice, Double finalPrice, Integer stock) {
        final Map<String, Object> parameters = new HashMap<>();
        parameters.put("title", title);
        parameters.put("description", description);
        parameters.put("original_price", originalPrice);
        parameters.put("final_price", finalPrice);
        parameters.put("stock", stock);
        parameters.put("active", false);
        final Number id = simpleJdbcInsert.executeAndReturnKey(parameters);
        return new Pack(id.longValue(), title, description, originalPrice, finalPrice, stock, false);
    }

    @Override
    public Optional<Pack> findById(final Long id) {
        return jdbcTemplate.query("SELECT * FROM packs WHERE id = ?", PACK_ROW_MAPPER, id).stream().findAny();
    }

    @Override
    public List<Pack> findAll() {
        return jdbcTemplate.query("SELECT * FROM packs", PACK_ROW_MAPPER);
    }

    @Override
    public List<Pack> findActive() {
        return jdbcTemplate.query("SELECT * FROM packs WHERE active = true", PACK_ROW_MAPPER);
    }

    @Override
    public Pack update(Pack pack) {
        jdbcTemplate.update(
            "UPDATE packs SET title = ?, description = ?, original_price = ?, final_price = ?, stock = ? WHERE id = ?",
            pack.getTitle(),
            pack.getDescription(),
            pack.getOriginalPrice(),
            pack.getFinalPrice(),
            pack.getStock(),
            pack.getId()
        );
        return pack;
    }

    @Override
    public void setActive(final Long id, final boolean active) {
        jdbcTemplate.update("UPDATE packs SET active = ? WHERE id = ?", active, id);
    }
}
