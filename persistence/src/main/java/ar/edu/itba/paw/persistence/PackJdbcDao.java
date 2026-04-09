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
import ar.edu.itba.paw.models.PackSortOption;

@Repository
public class PackJdbcDao implements PackDao {

    private static final String PACK_COLS_NO_IMAGE =
            "id, commerce_id, title, description, original_price, final_price, stock, active";

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
    public Pack createPack(Long commerceId, String title, String description, Double originalPrice,
                           Double finalPrice, Integer stock, List<PackTag> tags,
                           byte[] imageData, String imageContentType) {
        final Map<String, Object> parameters = new HashMap<>();
        parameters.put("commerce_id", commerceId);
        parameters.put("title", title);
        parameters.put("description", description);
        parameters.put("original_price", originalPrice);
        parameters.put("final_price", finalPrice);
        parameters.put("stock", stock);
        parameters.put("active", true);
        parameters.put("image_data", imageData);
        parameters.put("image_content_type", imageContentType);
        final Number id = simpleJdbcInsert.executeAndReturnKey(parameters);

        if (tags != null && !tags.isEmpty()) {
            List<Object[]> batchParams = new ArrayList<>();
            for (PackTag tag : tags) {
                batchParams.add(new Object[]{id.longValue(), tag.name()});
            }
            jdbcTemplate.batchUpdate("INSERT INTO pack_tags (pack_id, tag) VALUES (?, ?)", batchParams);
        } else {
            tags = Collections.emptyList();
        }

        return new Pack(id.longValue(), commerceId, title, description, originalPrice, finalPrice,
                stock, true, tags, imageData, imageContentType);
    }

    @Override
    public Optional<Pack> findById(final Long id) {
        return jdbcTemplate.query(
                "SELECT " + PACK_COLS_NO_IMAGE + " FROM packs WHERE id = ?",
                packRowMapper, id
        ).stream().findAny();
    }

    @Override
    public List<Pack> findAll() {
        return jdbcTemplate.query("SELECT " + PACK_COLS_NO_IMAGE + " FROM packs", packRowMapper);
    }

    @Override
    public List<Pack> findActive() {
        return findActive(PackSortOption.DATE_DESC);
    }

    @Override
    public List<Pack> findActive(PackSortOption sort) {
        return jdbcTemplate.query(
                "SELECT " + PACK_COLS_NO_IMAGE + " FROM packs WHERE active = true ORDER BY " + sort.getOrderByClause(),
                packRowMapper
        );
    }

    @Override
    public List<Pack> searchPacks(String query) {
        return searchPacks(query, PackSortOption.DATE_DESC);
    }

    @Override
    public List<Pack> searchPacks(String query, PackSortOption sort) {
        final String pattern = "%" + query + "%";
        return jdbcTemplate.query(
            "SELECT packs.id, packs.commerce_id, packs.title, packs.description, packs.original_price, packs.final_price, packs.stock, packs.active " +
            "FROM packs JOIN commerces ON packs.commerce_id = commerces.user_id " +
            "WHERE packs.active = true AND (packs.title ILIKE ? OR commerces.commercial_name ILIKE ?) ORDER BY " + sort.getOrderByClause(),
            packRowMapper,
            pattern,
            pattern
        );
    }

    @Override
    public List<Pack> findActiveByTags(final List<PackTag> tags) {
        return findActiveByTags(tags, PackSortOption.DATE_DESC);
    }

    @Override
    public List<Pack> findActiveByTags(final List<PackTag> tags, PackSortOption sort) {
        if (tags == null || tags.isEmpty()) {
            return findActive(sort);
        }
        final String inClause = String.join(", ", Collections.nCopies(tags.size(), "?"));
        final List<Object> params = new ArrayList<>();
        for (final PackTag tag : tags) {
            params.add(tag.name());
        }
        params.add(tags.size());
        
        // Since we are grouping, we must alias in order by. PackSortOption uses 'packs.' alias. Let's make sure alias matches.
        // Or simply GROUP BY fields ... ORDER BY MIN(packs.id). But for tags, if we use 'p.' we need to adjust sort string.
        // Let's replace 'packs.' with 'p.' in orderBy string or use 'packs' instead of 'p' in query.
        String orderBy = sort.getOrderByClause().replace("packs.", "p.");
        
        return jdbcTemplate.query(
            "SELECT p.id, p.commerce_id, p.title, p.description, p.original_price, p.final_price, p.stock, p.active " +
            "FROM packs p JOIN pack_tags pt ON p.id = pt.pack_id " +
            "WHERE p.active = true AND pt.tag IN (" + inClause + ") " +
            "GROUP BY p.id, p.commerce_id, p.title, p.description, p.original_price, p.final_price, p.stock, p.active " +
            "HAVING COUNT(DISTINCT pt.tag) = ? ORDER BY " + orderBy,
            packRowMapper,
            params.toArray()
        );
    }

    @Override
    public List<Pack> searchPacksWithTags(final String query, final List<PackTag> tags) {
        return searchPacksWithTags(query, tags, PackSortOption.DATE_DESC);
    }

    @Override
    public List<Pack> searchPacksWithTags(final String query, final List<PackTag> tags, PackSortOption sort) {
        if (tags == null || tags.isEmpty()) {
            return searchPacks(query, sort);
        }
        final String pattern = "%" + query + "%";
        final String inClause = String.join(", ", Collections.nCopies(tags.size(), "?"));
        final List<Object> params = new ArrayList<>();
        params.add(pattern);
        params.add(pattern);
        for (final PackTag tag : tags) {
            params.add(tag.name());
        }
        params.add(tags.size());
        return jdbcTemplate.query(
            "SELECT packs.id, packs.commerce_id, packs.title, packs.description, packs.original_price, packs.final_price, packs.stock, packs.active " +
            "FROM packs " +
            "JOIN commerces ON packs.commerce_id = commerces.user_id " +
            "JOIN pack_tags pt ON packs.id = pt.pack_id " +
            "WHERE packs.active = true AND (packs.title ILIKE ? OR commerces.commercial_name ILIKE ?) AND pt.tag IN (" + inClause + ") " +
            "GROUP BY packs.id, packs.commerce_id, packs.title, packs.description, packs.original_price, packs.final_price, packs.stock, packs.active " +
            "HAVING COUNT(DISTINCT pt.tag) = ? ORDER BY " + sort.getOrderByClause(),
            packRowMapper,
            params.toArray()
        );
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

    @Override
    public boolean decrementStock(final long packId, final int quantity) {
        if (quantity < 1) {
            return false;
        }
        final int updated = jdbcTemplate.update(
                "UPDATE packs SET stock = stock - ? WHERE id = ? AND stock >= ?",
                quantity, packId, quantity);
        return updated == 1;
    }

    @Override
    public Optional<Pack> findImageByPackId(Long id) {
        return jdbcTemplate.query(
                "SELECT id, commerce_id, title, description, original_price, final_price, stock, active, image_data, image_content_type FROM packs WHERE id = ?",
                (rs, rowNum) -> {
                    byte[] imgData = rs.getBytes("image_data");
                    String imgType = rs.getString("image_content_type");
                    return new Pack(
                            rs.getLong("id"),
                            rs.getLong("commerce_id"),
                            rs.getString("title"),
                            rs.getString("description"),
                            rs.getDouble("original_price"),
                            rs.getDouble("final_price"),
                            rs.getInt("stock"),
                            rs.getBoolean("active"),
                            Collections.emptyList(),
                            imgData,
                            imgType
                    );
                },
                id
        ).stream().findAny();
    }
}
