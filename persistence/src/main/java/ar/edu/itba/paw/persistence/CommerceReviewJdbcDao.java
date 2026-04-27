package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.models.user.CommerceReview;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class CommerceReviewJdbcDao implements CommerceReviewDao {

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert simpleJdbcInsert;

    private static final RowMapper<CommerceReview> COMMERCE_REVIEW_ROW_MAPPER = (rs, rowNum) -> {
        final Timestamp createdAt = rs.getTimestamp("created_at");
        final Timestamp updatedAt = rs.getTimestamp("updated_at");
        return new CommerceReview(
                rs.getLong("id"),
                rs.getLong("commerce_user_id"),
                rs.getLong("client_user_id"),
                rs.getInt("rating"),
                rs.getString("body"),
                createdAt == null ? null : createdAt.toLocalDateTime(),
                updatedAt == null ? null : updatedAt.toLocalDateTime());
    };

    @Autowired
    public CommerceReviewJdbcDao(final DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.simpleJdbcInsert = new SimpleJdbcInsert(dataSource)
                .withTableName("commerce_reviews")
                .usingGeneratedKeyColumns("id");
    }

    @Override
    public CommerceReview createReview(final Long commerceUserId, final Long clientUserId, final Integer rating,
            final String body) {
        final LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        final Map<String, Object> parameters = new HashMap<>();
        parameters.put("commerce_user_id", commerceUserId);
        parameters.put("client_user_id", clientUserId);
        parameters.put("rating", rating);
        parameters.put("body", body);
        parameters.put("created_at", Timestamp.valueOf(now));
        parameters.put("updated_at", Timestamp.valueOf(now));

        final Number id = simpleJdbcInsert.executeAndReturnKey(parameters);
        return new CommerceReview(id.longValue(), commerceUserId, clientUserId, rating, body, now, now);
    }

    @Override
    public CommerceReview updateReview(final Long id, final Integer rating, final String body) {
        final LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        jdbcTemplate.update("UPDATE commerce_reviews SET rating = ?, body = ?, updated_at = ? WHERE id = ?",
                rating, body, Timestamp.valueOf(now), id);
        return jdbcTemplate.query(
                "SELECT id, commerce_user_id, client_user_id, rating, body, created_at, updated_at FROM commerce_reviews WHERE id = ?",
                COMMERCE_REVIEW_ROW_MAPPER, id).stream()
                .findAny()
                .orElseThrow(() -> new IllegalStateException("Commerce review not found: " + id));
    }

    @Override
    public Optional<CommerceReview> findByClientAndCommerce(final Long clientUserId, final Long commerceUserId) {
        return jdbcTemplate.query(
                "SELECT id, commerce_user_id, client_user_id, rating, body, created_at, updated_at FROM commerce_reviews WHERE client_user_id = ? AND commerce_user_id = ?",
                COMMERCE_REVIEW_ROW_MAPPER, clientUserId, commerceUserId).stream()
                .findAny();
    }

    @Override
    public List<CommerceReview> findByCommerceId(final Long commerceUserId, final int page, final int pageSize) {
        return jdbcTemplate.query(
                "SELECT id, commerce_user_id, client_user_id, rating, body, created_at, updated_at FROM commerce_reviews WHERE commerce_user_id = ? ORDER BY updated_at DESC, id DESC LIMIT ? OFFSET ?",
                COMMERCE_REVIEW_ROW_MAPPER, commerceUserId, pageSize, (page - 1) * pageSize);
    }

    @Override
    public int countByCommerceId(final Long commerceUserId) {
        final Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM commerce_reviews WHERE commerce_user_id = ?",
                Integer.class, commerceUserId);
        return count == null ? 0 : count;
    }
}
