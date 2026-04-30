package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.models.pack.Pack;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.Collections;
import java.util.List;

@Repository
public class PackFavoriteJdbcDao implements PackFavoriteDao {

    private static final String PACK_COLS =
            "p.id, p.commerce_id, p.title, p.description, p.original_price, p.final_price, p.stock, p.active, p.deleted, p.image_id";

    private final JdbcTemplate jdbcTemplate;
    private final RowMapper<Pack> packRowMapperNoTags;

    @Autowired
    public PackFavoriteJdbcDao(final DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
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
    public List<Pack> findActiveFavoritePacksForClient(final long clientId, final int page, final int pageSize) {
        final int safePage = Math.max(1, page);
        final int safeSize = Math.max(1, pageSize);
        final int offset = (safePage - 1) * safeSize;
        return jdbcTemplate.query(
                "SELECT " + PACK_COLS + " FROM client_pack_favorites f "
                        + "INNER JOIN packs p ON p.id = f.pack_id "
                        + "WHERE f.client_id = ? AND p.active = TRUE AND p.deleted = FALSE "
                        + "ORDER BY f.created_at DESC LIMIT ? OFFSET ?",
                packRowMapperNoTags,
                clientId,
                safeSize,
                offset);
    }

    @Override
    public int countActiveFavoritePacksForClient(final long clientId) {
        final Integer n = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM client_pack_favorites f INNER JOIN packs p ON p.id = f.pack_id "
                        + "WHERE f.client_id = ? AND p.active = TRUE AND p.deleted = FALSE",
                Integer.class,
                clientId);
        return n != null ? n : 0;
    }

    @Override
    public boolean exists(final long clientId, final long packId) {
        final Integer n = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM client_pack_favorites WHERE client_id = ? AND pack_id = ?",
                Integer.class,
                clientId,
                packId);
        return n != null && n > 0;
    }

    @Override
    public void insert(final long clientId, final long packId) {
        jdbcTemplate.update(
                "INSERT INTO client_pack_favorites (client_id, pack_id) VALUES (?, ?)",
                clientId,
                packId);
    }

    @Override
    public void delete(final long clientId, final long packId) {
        jdbcTemplate.update(
                "DELETE FROM client_pack_favorites WHERE client_id = ? AND pack_id = ?",
                clientId,
                packId);
    }
}
