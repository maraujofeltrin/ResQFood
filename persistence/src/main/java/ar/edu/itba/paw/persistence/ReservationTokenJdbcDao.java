package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.models.reservation.ReservationToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public class ReservationTokenJdbcDao implements ReservationTokenDao {

    private static final RowMapper<ReservationToken> ROW_MAPPER = (rs, rowNum) -> new ReservationToken(
            rs.getString("token"),
            rs.getLong("reservation_id"),
            ReservationToken.Action.valueOf(rs.getString("action")),
            rs.getBoolean("used"),
            rs.getTimestamp("created_at").toLocalDateTime(),
            rs.getTimestamp("expires_at").toLocalDateTime()
    );

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public ReservationTokenJdbcDao(final DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Override
    public ReservationToken create(final String token, final Long reservationId, final ReservationToken.Action action,
            final LocalDateTime createdAt, final LocalDateTime expiresAt) {
        jdbcTemplate.update(
                "INSERT INTO reservation_tokens (token, reservation_id, action, used, created_at, expires_at) VALUES (?, ?, ?, ?, ?, ?)",
                token,
                reservationId,
                action.name(),
                false,
                Timestamp.valueOf(createdAt),
                Timestamp.valueOf(expiresAt));
        return new ReservationToken(token, reservationId, action, false, createdAt, expiresAt);
    }

    @Override
    public Optional<ReservationToken> findByToken(final String token) {
        return jdbcTemplate
                .query("SELECT * FROM reservation_tokens WHERE token = ?", ROW_MAPPER, token)
                .stream()
                .findAny();
    }

    @Override
    public void markAsUsed(final String token) {
        final int rows = jdbcTemplate.update("UPDATE reservation_tokens SET used = true WHERE token = ?", token);
        if (rows <= 0) {
            throw new IllegalArgumentException("Reservation token not found");
        }
    }
}
