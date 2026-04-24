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
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import org.springframework.jdbc.core.RowMapper;

import ar.edu.itba.paw.models.pack.Pack;
import ar.edu.itba.paw.models.pack.PackTag;
import ar.edu.itba.paw.models.pack.PackSortOption;

@Repository
public class PackJdbcDao implements PackDao {

    private static final String PACK_COLS_NO_IMAGE =
            "id, commerce_id, title, description, original_price, final_price, stock, active, deleted";

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert simpleJdbcInsert;
    private final RowMapper<Pack> packRowMapper;
    private final RowMapper<Pack> packRowMapperNoTags;

    @Autowired
    public PackJdbcDao(final DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.simpleJdbcInsert = new SimpleJdbcInsert(dataSource)
            .withTableName("packs")
            .usingGeneratedKeyColumns("id");

        // Full mapper: loads tags per pack (use only for single-pack queries like findById)
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
                rs.getBoolean("deleted"),
                tags,
                null,
                null
            );
        };

        // Lightweight mapper: skips tags query (use for list/catalog queries)
        this.packRowMapperNoTags = (rs, rowNum) -> new Pack(
            rs.getLong("id"),
            rs.getLong("commerce_id"),
            rs.getString("title"),
            rs.getString("description"),
            rs.getDouble("original_price"),
            rs.getDouble("final_price"),
            rs.getInt("stock"),
            rs.getBoolean("active"),
            rs.getBoolean("deleted"),
            Collections.emptyList(),
            null,
            null
        );
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
        parameters.put("deleted", false);
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
                stock, true, false, tags, imageData, imageContentType);
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
        return jdbcTemplate.query("SELECT " + PACK_COLS_NO_IMAGE + " FROM packs", packRowMapperNoTags);
    }

    @Override
    public List<Pack> findByCommerceId(Long commerceId) {
        return jdbcTemplate.query(
                "SELECT " + PACK_COLS_NO_IMAGE + " FROM packs WHERE commerce_id = ? AND deleted = false ORDER BY id DESC",
                packRowMapperNoTags, commerceId
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
    public void softDelete(final Long id) {
        jdbcTemplate.update("UPDATE packs SET deleted = true WHERE id = ?", id);
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
    public boolean incrementStock(final long packId, final int quantity) {
        if (quantity < 1) {
            return false;
        }
        final int updated = jdbcTemplate.update(
                "UPDATE packs SET stock = stock + ? WHERE id = ?",
                quantity, packId);
        return updated == 1;
    }

    @Override
    public Optional<Pack> findImageByPackId(Long id) {
        return jdbcTemplate.query(
                "SELECT id, commerce_id, title, description, original_price, final_price, stock, active, deleted, image_data, image_content_type FROM packs WHERE id = ?",
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
                            rs.getBoolean("deleted"),
                            Collections.emptyList(),
                            imgData,
                            imgType
                    );
                },
                id
        ).stream().findAny();
    }

    @Override
    public void updateImage(Long packId, byte[] imageData, String imageContentType) {
        jdbcTemplate.update("UPDATE packs SET image_data = ?, image_content_type = ? WHERE id = ?",
                imageData, imageContentType, packId);
    }

    @Override
    public List<Pack> filterPacks(final String query, final List<PackTag> tags,
                                  final String city, final List<String> timeRanges,
                                  final PackSortOption sort) {

        final boolean hasQuery = query != null && !query.isBlank();
        final boolean hasTags = tags != null && !tags.isEmpty();
        final boolean hasCity = city != null && !city.isBlank();
        final boolean hasTime = timeRanges != null && !timeRanges.isEmpty();

        /*
         * Build a dynamic query.  We always JOIN commerces (needed for city,
         * time, and text-search on commercial_name).  When tags are requested
         * we also JOIN pack_tags and use GROUP BY + HAVING.
         */
        final StringBuilder sql = new StringBuilder();
        sql.append("SELECT p.id, p.commerce_id, p.title, p.description, ")
           .append("p.original_price, p.final_price, p.stock, p.active, p.deleted ")
           .append("FROM packs p ")
           .append("JOIN commerces c ON p.commerce_id = c.user_id ");

        if (hasTags) {
            sql.append("JOIN pack_tags pt ON p.id = pt.pack_id ");
        }

        sql.append("WHERE p.active = true ")
           .append("AND p.deleted = false ")
           .append("AND NOT EXISTS (")
           .append("SELECT 1 FROM auctions a ")
           .append("WHERE a.pack_id = p.id AND a.status = 'ACTIVE' AND a.end_time > ?) ");

        final List<Object> params = new ArrayList<>();
        params.add(Timestamp.valueOf(LocalDateTime.now(ZoneOffset.UTC)));

        // --- text search ---
        if (hasQuery) {
            final String pattern = "%" + query.trim() + "%";
            sql.append("AND (p.title ILIKE ? OR c.commercial_name ILIKE ?) ");
            params.add(pattern);
            params.add(pattern);
        }

        // --- city ---
        if (hasCity) {
            sql.append("AND c.city = ? ");
            params.add(city);
        }

        // --- time ranges (safe cast + integer comparison) ---
        if (hasTime) {
            /*
             * Time format in DB: "HH:mm" or "H:mm".
             * We cast the hour portion to integer for safe comparison.
             * morning   = hour in [0, 12)
             * afternoon = hour in [12, 17)
             * evening   = hour in [17, 24)
             */
            final List<String> timeConditions = new ArrayList<>();
            for (final String range : timeRanges) {
                switch (range) {
                    case "morning":
                        timeConditions.add(
                            "CAST(SPLIT_PART(c.opening_time, ':', 1) AS INTEGER) < 12");
                        break;
                    case "afternoon":
                        timeConditions.add(
                            "CAST(SPLIT_PART(c.opening_time, ':', 1) AS INTEGER) >= 12 "
                          + "AND CAST(SPLIT_PART(c.opening_time, ':', 1) AS INTEGER) < 17");
                        break;
                    case "evening":
                        timeConditions.add(
                            "CAST(SPLIT_PART(c.opening_time, ':', 1) AS INTEGER) >= 17");
                        break;
                    default:
                        // unknown range value — silently ignore (whitelist approach)
                        break;
                }
            }
            if (!timeConditions.isEmpty()) {
                sql.append("AND (")
                   .append(String.join(" OR ", timeConditions))
                   .append(") ");
            }
        }

        // --- tags ---
        if (hasTags) {
            final String inClause = String.join(", ", Collections.nCopies(tags.size(), "?"));
            sql.append("AND pt.tag IN (").append(inClause).append(") ");
            for (final PackTag tag : tags) {
                params.add(tag.name());
            }
            sql.append("GROUP BY p.id, p.commerce_id, p.title, p.description, ")
               .append("p.original_price, p.final_price, p.stock, p.active, p.deleted ")
               .append("HAVING COUNT(DISTINCT pt.tag) = ? ");
            params.add(tags.size());
        }

        // --- ordering ---
        final PackSortOption safeSortOption = sort != null ? sort : PackSortOption.DATE_DESC;
        final String orderBy = safeSortOption.getOrderByClause().replace("packs.", "p.");
        sql.append("ORDER BY ").append(orderBy);

        return jdbcTemplate.query(sql.toString(), packRowMapperNoTags, params.toArray());
    }
}
