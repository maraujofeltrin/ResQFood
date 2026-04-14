package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.models.PasswordResetToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public class PasswordResetTokenJdbcDao implements PasswordResetTokenDao {

    private static final RowMapper<PasswordResetToken> ROW_MAPPER = (rs, rowNum) -> new PasswordResetToken(
            rs.getString("token"),
            rs.getLong("user_id"),
            rs.getBoolean("used"),
            rs.getTimestamp("created_at").toLocalDateTime(),
            rs.getTimestamp("expires_at").toLocalDateTime()
    );

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public PasswordResetTokenJdbcDao(final DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Override
    public PasswordResetToken create(final String token, final Long userId, final LocalDateTime createdAt,
            final LocalDateTime expiresAt) {
        jdbcTemplate.update(
                "INSERT INTO password_reset_tokens (token, user_id, used, created_at, expires_at) VALUES (?, ?, ?, ?, ?)",
                token,
                userId,
                false,
                Timestamp.valueOf(createdAt),
                Timestamp.valueOf(expiresAt));
        return new PasswordResetToken(token, userId, false, createdAt, expiresAt);
    }

    @Override
    public Optional<PasswordResetToken> findByToken(final String token) {
        return jdbcTemplate
                .query("SELECT * FROM password_reset_tokens WHERE token = ?", ROW_MAPPER, token)
                .stream()
                .findAny();
    }

    @Override
    public void markAsUsed(final String token) {
        final int rows = jdbcTemplate.update("UPDATE password_reset_tokens SET used = true WHERE token = ?", token);
        if (rows <= 0) {
            throw new IllegalArgumentException("Token not found: " + token);
        }
    }
}