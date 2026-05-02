package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.models.security.Token;
import ar.edu.itba.paw.models.security.TokenType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Repository
public class TokenJdbcDao implements TokenDao {

    private static final Logger LOGGER = LoggerFactory.getLogger(TokenJdbcDao.class);

    private static final RowMapper<Token> ROW_MAPPER = (rs, rowNum) -> new Token(
            rs.getString("token"),
            rs.getLong("user_id"),
            rs.getBoolean("used"),
            TokenType.valueOf(rs.getString("type")),
            rs.getTimestamp("created_at").toLocalDateTime(),
            rs.getTimestamp("expires_at").toLocalDateTime()
    );

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public TokenJdbcDao(final DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Override
    public Token create(final String token, final Long userId, final TokenType type, final LocalDateTime createdAt,
            final LocalDateTime expiresAt) {
        jdbcTemplate.update(
                "INSERT INTO tokens (token, user_id, used, type, created_at, expires_at) VALUES (?, ?, ?, ?, ?, ?)",
                token,
                userId,
                false,
                type.name(),
                Timestamp.valueOf(createdAt),
                Timestamp.valueOf(expiresAt));
        return new Token(token, userId, false, type, createdAt, expiresAt);
    }

    @Override
    public Optional<Token> findByTokenAndType(final String token, final TokenType type) {
        return jdbcTemplate
                .query("SELECT * FROM tokens WHERE token = ? AND type = ?", ROW_MAPPER, token, type.name())
                .stream()
                .findAny();
    }

    @Override
    public void markAsUsed(final String token, final TokenType type) {
        final int rows = jdbcTemplate.update("UPDATE tokens SET used = true WHERE token = ? AND type = ?", token,
                type.name());
        if (rows <= 0) {
            LOGGER.warn("markAsUsed token: zero rows updated for type {}", type);
            throw new IllegalArgumentException("Token not found");
        }
    }
}
