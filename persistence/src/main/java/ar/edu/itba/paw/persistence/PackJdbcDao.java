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
import java.util.ArrayList;
import java.util.Collections;
import org.springframework.jdbc.core.RowMapper;

import ar.edu.itba.paw.models.Pack;
import ar.edu.itba.paw.models.PackTag;

@Repository
public class PackJdbcDao implements PackDao {

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert simpleJdbcInsert;
    private final RowMapper<Pack> packRowMapper;

    @Autowired
    public PackJdbcDao(final DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.simpleJdbcInsert = new SimpleJdbcInsert(dataSource)
            .withTableName("packs")
            .usingGeneratedKeyColumns("id");
            
        this.packRowMapper = (rs, rowNum) -> {
            Long packId = rs.getLong("id");
            List<PackTag> tags = jdbcTemplate.query("SELECT tag FROM pack_tags WHERE pack_id = ?", 
                (rs1, rowNum1) -> PackTag.valueOf(rs1.getString("tag")), packId);
                
            return new Pack(
                packId,
                rs.getLong("commerce_id"),
                rs.getString("title"),
                rs.getString("description"),
                rs.getDouble("original_price"),
                rs.getDouble("final_price"),
                rs.getInt("stock"),
                rs.getBoolean("active"),
                tags
            );
        };
    }

    @Override
    public Pack createPack(Long commerceId, String title, String description, Double originalPrice, Double finalPrice, Integer stock, List<PackTag> tags) {
        final Map<String, Object> parameters = new HashMap<>();
        parameters.put("commerce_id", commerceId);
        parameters.put("title", title);
        parameters.put("description", description);
        parameters.put("original_price", originalPrice);
        parameters.put("final_price", finalPrice);
        parameters.put("stock", stock);
        parameters.put("active", false);
        final Number id = simpleJdbcInsert.executeAndReturnKey(parameters);
        
        // Insert tags
        if (tags != null && !tags.isEmpty()) {
            List<Object[]> batchParams = new ArrayList<>();
            for (PackTag tag : tags) {
                batchParams.add(new Object[]{id.longValue(), tag.name()});
            }
            jdbcTemplate.batchUpdate("INSERT INTO pack_tags (pack_id, tag) VALUES (?, ?)", batchParams);
        } else {
            tags = Collections.emptyList();
        }
        
        return new Pack(id.longValue(), commerceId, title, description, originalPrice, finalPrice, stock, false, tags);
    }

    @Override
    public Optional<Pack> findById(final Long id) {
        return jdbcTemplate.query("SELECT * FROM packs WHERE id = ?", packRowMapper, id).stream().findAny();
    }

    @Override
    public List<Pack> findAll() {
        return jdbcTemplate.query("SELECT * FROM packs", packRowMapper);
    }

    @Override
    public List<Pack> findActive() {
        return jdbcTemplate.query("SELECT * FROM packs WHERE active = true", packRowMapper);
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
        
        // Update tags: simple approach, delete all and re-insert
        jdbcTemplate.update("DELETE FROM pack_tags WHERE pack_id = ?", pack.getId());
        if (pack.getTags() != null && !pack.getTags().isEmpty()) {
            List<Object[]> batchParams = new ArrayList<>();
            for (PackTag tag : pack.getTags()) {
                batchParams.add(new Object[]{pack.getId(), tag.name()});
            }
            jdbcTemplate.batchUpdate("INSERT INTO pack_tags (pack_id, tag) VALUES (?, ?)", batchParams);
        }
        
        return pack;
    }

    @Override
    public void setActive(final Long id, final boolean active) {
        jdbcTemplate.update("UPDATE packs SET active = ? WHERE id = ?", active, id);
    }
}
