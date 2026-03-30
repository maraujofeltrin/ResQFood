package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.models.Reservation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class ReservationJdbcDao implements ReservationDao {

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert simpleJdbcInsert;

    private static final RowMapper<Reservation> RESERVATION_ROW_MAPPER = (rs, rowNum) -> new Reservation(
            rs.getLong("id"),
            rs.getLong("customer_id"),
            rs.getLong("pack_id"),
            rs.getTimestamp("reservation_date").toLocalDateTime(),
            rs.getDouble("final_price"),
            rs.getString("status") == null ? null : Reservation.Status.valueOf(rs.getString("status")),
            rs.getString("pickup_code"),
            rs.getTimestamp("pickup_confirmation_date") == null ? null
                    : rs.getTimestamp("pickup_confirmation_date").toLocalDateTime()
    );

    @Autowired
    public ReservationJdbcDao(final DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.simpleJdbcInsert = new SimpleJdbcInsert(dataSource)
                .withTableName("reservations")
                .usingGeneratedKeyColumns("id");
    }

    @Override
    public Reservation createReservation(final Long customerId, final Long packId, final LocalDateTime reservationDate,
            final Double finalPrice, final Reservation.Status status, final String pickupCode,
            final LocalDateTime pickupConfirmationDate) {
        final Map<String, Object> parameters = new HashMap<>();
        parameters.put("customer_id", customerId);
        parameters.put("pack_id", packId);
        parameters.put("reservation_date", reservationDate == null ? null : Timestamp.valueOf(reservationDate));
        parameters.put("final_price", finalPrice);
        parameters.put("status", status == null ? null : status.name());
        parameters.put("pickup_code", pickupCode);
        parameters.put("pickup_confirmation_date",
                pickupConfirmationDate == null ? null : Timestamp.valueOf(pickupConfirmationDate));

        final Number id = simpleJdbcInsert.executeAndReturnKey(parameters);
        return new Reservation(id.longValue(), customerId, packId, reservationDate, finalPrice, status, pickupCode,
                pickupConfirmationDate);
    }

    @Override
    public Optional<Reservation> findById(final Long id) {
        return jdbcTemplate.query("SELECT * FROM reservations WHERE id = ?", RESERVATION_ROW_MAPPER, id)
                .stream()
                .findAny();
    }

    @Override
    public List<Reservation> findByCustomerId(final Long customerId) {
        return jdbcTemplate.query("SELECT * FROM reservations WHERE customer_id = ?", RESERVATION_ROW_MAPPER,
                customerId);
    }

    @Override
    public List<Reservation> findByPackId(final Long packId) {
        return jdbcTemplate.query("SELECT * FROM reservations WHERE pack_id = ?", RESERVATION_ROW_MAPPER, packId);
    }

    @Override
    public Reservation updateStatus(final Long id, final Reservation.Status status) {
        jdbcTemplate.update("UPDATE reservations SET status = ? WHERE id = ?", status == null ? null : status.name(),
                id);
        return findById(id).orElseThrow(() -> new IllegalStateException("Reservation not found: " + id));
    }

        @Override
        public Reservation confirmPickup(final Long id, final java.time.LocalDateTime pickupConfirmationDate) {
                jdbcTemplate.update("UPDATE reservations SET status = ?, pickup_confirmation_date = ? WHERE id = ?",
                                Reservation.Status.PAID.name(),
                                pickupConfirmationDate == null ? null : java.sql.Timestamp.valueOf(pickupConfirmationDate),
                                id);
                return findById(id).orElseThrow(() -> new IllegalStateException("Reservation not found: " + id));
        }

    @Override
    public Optional<Reservation> findByPickupCode(final String pickupCode) {
        return jdbcTemplate.query(
                "SELECT * FROM reservations WHERE pickup_code = ?",
                RESERVATION_ROW_MAPPER, pickupCode
                ).stream().findAny();
    }

}

