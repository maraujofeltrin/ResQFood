package ar.edu.itba.paw.persistence;

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

import ar.edu.itba.paw.models.pack.Pack;
import ar.edu.itba.paw.models.pack.PackTag;
import ar.edu.itba.paw.models.pack.PackSortOption;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

@Repository
public class PackJdbcDao implements PackDao {

    private static final Logger LOGGER = LoggerFactory.getLogger(PackJdbcDao.class);

    private static final String PACK_COLS_NO_IMAGE =
            "id, commerce_id, title, description, original_price, final_price, stock, active, deleted, image_id";

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
                rs.getObject("image_id") != null ? rs.getLong("image_id") : null
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
            rs.getObject("image_id") != null ? rs.getLong("image_id") : null
        );
    }

    @Override
    public Pack createPack(Long commerceId, String title, String description, Double originalPrice,
                           Double finalPrice, Integer stock, List<PackTag> tags,
                           Long imageId) {
        final Map<String, Object> parameters = new HashMap<>();
        parameters.put("commerce_id", commerceId);
        parameters.put("title", title);
        parameters.put("description", description);
        parameters.put("original_price", originalPrice);
        parameters.put("final_price", finalPrice);
        parameters.put("stock", stock);
        parameters.put("active", true);
        parameters.put("deleted", false);
        parameters.put("image_id", imageId);
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
                stock, true, false, tags, imageId);
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
        final int packRowsUpdated = jdbcTemplate.update(
            "UPDATE packs SET title = ?, description = ?, original_price = ?, final_price = ?, stock = ?, image_id = ? WHERE id = ?",
            pack.getTitle(),
            pack.getDescription(),
            pack.getOriginalPrice(),
            pack.getFinalPrice(),
            pack.getStock(),
            pack.getImageId(),
            pack.getId()
        );
        if (packRowsUpdated == 0) {
            LOGGER.warn("update: no packs row matched for id {}", pack.getId());
        }

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
        final int rows = jdbcTemplate.update("UPDATE packs SET deleted = true WHERE id = ?", id);
        if (rows == 0) {
            LOGGER.warn("softDelete: no packs row matched for id {}", id);
        }
    }

    @Override
    public void setActive(final Long id, final boolean active) {
        final int rows = jdbcTemplate.update("UPDATE packs SET active = ? WHERE id = ?", active, id);
        if (rows == 0) {
            LOGGER.warn("setActive: no packs row matched for id {} (active={})", id, active);
        }
    }

    @Override
    public boolean decrementStock(final long packId, final int quantity) {
        if (quantity < 1) {
            return false;
        }
        final int updated = jdbcTemplate.update(
                "UPDATE packs SET stock = stock - ? WHERE id = ? AND stock >= ?",
                quantity, packId, quantity);
        if (updated != 1) {
            LOGGER.debug("decrementStock: expected exactly one updated row, got {} (packId={}, quantity={})", updated,
                    packId, quantity);
        }
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
        if (updated != 1) {
            LOGGER.debug("incrementStock: expected exactly one updated row, got {} (packId={}, quantity={})", updated,
                    packId, quantity);
        }
        return updated == 1;
    }



    private void appendFilterJoinsAndConditions(final StringBuilder sql, final List<Object> params,
                                                final String query, final List<PackTag> tags,
                                                final String city, final List<String> timeRanges,
                                                final boolean requirePositiveStock) {
        final boolean hasQuery = query != null && !query.isBlank();
        final boolean hasTags = tags != null && !tags.isEmpty();
        final boolean hasCity = city != null && !city.isBlank();
        final boolean hasTime = timeRanges != null && !timeRanges.isEmpty();

        sql.append("FROM packs p ")
           .append("JOIN commerces c ON p.commerce_id = c.user_id ");

        if (hasTags) {
            sql.append("JOIN pack_tags pt ON p.id = pt.pack_id ");
        }

        sql.append("WHERE p.active = true ")
           .append("AND p.deleted = false ")
           .append("AND NOT EXISTS (")
           .append("SELECT 1 FROM auctions a ")
           .append("WHERE a.pack_id = p.id AND a.status = 'ACTIVE') ");

        if (requirePositiveStock) {
            sql.append("AND p.stock > 0 ");
        }

        if (hasQuery) {
            final String escapedQuery = query.trim()
                    .replace("\\", "\\\\")
                    .replace("%", "\\%")
                    .replace("_", "\\_");
            final String pattern = "%" + escapedQuery + "%";
            sql.append("AND (p.title ILIKE ? ESCAPE '\\' OR c.commercial_name ILIKE ? ESCAPE '\\') ");
            params.add(pattern);
            params.add(pattern);
        }

        if (hasCity) {
            sql.append("AND c.city = ? ");
            params.add(city);
        }

        if (hasTime) {
            final List<String> timeConditions = new ArrayList<>();
            for (final String range : timeRanges) {
                switch (range) {
                    case "morning":
                        timeConditions.add("CAST(SPLIT_PART(c.opening_time, ':', 1) AS INTEGER) < 12");
                        break;
                    case "afternoon":
                        timeConditions.add("CAST(SPLIT_PART(c.opening_time, ':', 1) AS INTEGER) >= 12 "
                                + "AND CAST(SPLIT_PART(c.opening_time, ':', 1) AS INTEGER) < 17");
                        break;
                    case "evening":
                        timeConditions.add("CAST(SPLIT_PART(c.opening_time, ':', 1) AS INTEGER) >= 17");
                        break;
                    default:
                        break;
                }
            }
            if (!timeConditions.isEmpty()) {
                sql.append("AND (")
                   .append(String.join(" OR ", timeConditions))
                   .append(") ");
            }
        }

        if (hasTags) {
            final String inClause = String.join(", ", Collections.nCopies(tags.size(), "?"));
            sql.append("AND pt.tag IN (").append(inClause).append(") ");
            for (final PackTag tag : tags) {
                params.add(tag.name());
            }
        }
    }

    @Override
    public List<Pack> filterPacks(final String query, final List<PackTag> tags,
                                  final String city, final List<String> timeRanges,
                                  final PackSortOption sort,
                                  final int page, final int pageSize,
                                  final boolean requirePositiveStock) {

        final StringBuilder sql = new StringBuilder();
        sql.append("SELECT p.id, p.commerce_id, p.title, p.description, ")
           .append("p.original_price, p.final_price, p.stock, p.active, p.deleted, p.image_id ");

        final List<Object> params = new ArrayList<>();
        appendFilterJoinsAndConditions(sql, params, query, tags, city, timeRanges, requirePositiveStock);

        if (tags != null && !tags.isEmpty()) {
            sql.append("GROUP BY p.id, p.commerce_id, p.title, p.description, ")
               .append("p.original_price, p.final_price, p.stock, p.active, p.deleted, p.image_id, c.commercial_name ")
               .append("HAVING COUNT(DISTINCT pt.tag) = ? ");
            params.add(tags.size());
        }

        final PackSortOption safeSortOption = sort != null ? sort : PackSortOption.DATE_DESC;
        final String orderBy = safeSortOption.getOrderByClause().replace("packs.", "p.");
        sql.append("ORDER BY ").append(orderBy).append(" ");

        sql.append("LIMIT ? OFFSET ?");
        params.add(pageSize);
        params.add((page - 1) * pageSize);

        return jdbcTemplate.query(sql.toString(), packRowMapperNoTags, params.toArray());
    }

    @Override
    public int countFilteredPacks(final String query, final List<PackTag> tags,
                                  final String city, final List<String> timeRanges,
                                  final boolean requirePositiveStock) {

        final StringBuilder sqlJoinsAndConditions = new StringBuilder();
        final List<Object> params = new ArrayList<>();
        appendFilterJoinsAndConditions(sqlJoinsAndConditions, params, query, tags, city, timeRanges,
                requirePositiveStock);

        if (tags != null && !tags.isEmpty()) {
            // Because we only want the total count of valid packs, we use a subquery to apply HAVING safely
            final StringBuilder wrapperSql = new StringBuilder();
            wrapperSql.append("SELECT COUNT(*) FROM (SELECT p.id ");
            wrapperSql.append(sqlJoinsAndConditions);
            wrapperSql.append("GROUP BY p.id HAVING COUNT(DISTINCT pt.tag) = ?) AS subquery");
            params.add(tags.size());
            Integer count = jdbcTemplate.queryForObject(wrapperSql.toString(), Integer.class, params.toArray());
            return count != null ? count : 0;
        } else {
            final StringBuilder sql = new StringBuilder();
            sql.append("SELECT COUNT(DISTINCT p.id) ");
            sql.append(sqlJoinsAndConditions);
            Integer count = jdbcTemplate.queryForObject(sql.toString(), Integer.class, params.toArray());
            return count != null ? count : 0;
        }
    }

    @Override
    public List<Pack> filterCommercePacks(Long commerceId, Boolean hasAuction, int page, int pageSize) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT id, commerce_id, title, description, original_price, final_price, stock, active, deleted, image_id ");
        sql.append("FROM packs p WHERE p.commerce_id = ? AND p.deleted = false ");
        
        List<Object> params = new ArrayList<>();
        params.add(commerceId);
        
        if (hasAuction != null) {
            if (hasAuction) {
                sql.append("AND EXISTS (SELECT 1 FROM auctions a WHERE a.pack_id = p.id) ");
            } else {
                sql.append("AND NOT EXISTS (SELECT 1 FROM auctions a WHERE a.pack_id = p.id) ");
            }
        }
        
        sql.append("ORDER BY p.id DESC LIMIT ? OFFSET ?");
        params.add(pageSize);
        params.add((page - 1) * pageSize);
        
        return jdbcTemplate.query(sql.toString(), packRowMapperNoTags, params.toArray());
    }

    @Override
    public int countCommercePacks(Long commerceId, Boolean hasAuction) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT COUNT(p.id) FROM packs p WHERE p.commerce_id = ? AND p.deleted = false ");
        
        List<Object> params = new ArrayList<>();
        params.add(commerceId);
        
        if (hasAuction != null) {
            if (hasAuction) {
                sql.append("AND EXISTS (SELECT 1 FROM auctions a WHERE a.pack_id = p.id) ");
            } else {
                sql.append("AND NOT EXISTS (SELECT 1 FROM auctions a WHERE a.pack_id = p.id) ");
            }
        }
        
        Integer count = jdbcTemplate.queryForObject(sql.toString(), Integer.class, params.toArray());
        return count != null ? count : 0;
    }
}
