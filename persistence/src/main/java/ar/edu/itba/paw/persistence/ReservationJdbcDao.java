package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.models.reservation.Reservation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;

@Repository
public class ReservationJdbcDao implements ReservationDao {

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert simpleJdbcInsert;

    private static Integer readQuantity(final ResultSet rs) throws SQLException {
        final int v = rs.getInt("quantity");
        return rs.wasNull() ? 1 : v;
    }

    private static String readString(final ResultSet rs, final String col) throws SQLException {
        final String v = rs.getString(col);
        return rs.wasNull() ? null : v;
    }

    private static Long readNullableLong(final ResultSet rs, final String col) throws SQLException {
        final long v = rs.getLong(col);
        return rs.wasNull() ? null : v;
    }

    private static LocalDateTime readNullableDateTime(final ResultSet rs, final String col) throws SQLException {
        final Timestamp t = rs.getTimestamp(col);
        return t == null ? null : t.toLocalDateTime();
    }

    private static Double readNullableDouble(final ResultSet rs, final String col) throws SQLException {
        final double v = rs.getDouble(col);
        return rs.wasNull() ? null : v;
    }

    private static final RowMapper<Reservation> RESERVATION_ROW_MAPPER = (rs, rowNum) -> new Reservation(
            rs.getLong("id"),
            readNullableLong(rs, "customer_id"),
            readNullableLong(rs, "pack_id"),
            readNullableDateTime(rs, "reservation_date"),
            readNullableDouble(rs, "final_price"),
            rs.getString("status") == null ? null : Reservation.Status.valueOf(rs.getString("status")),
            readString(rs, "pickup_code"),
            readNullableDateTime(rs, "pickup_confirmation_date"),
            readQuantity(rs),
            readString(rs, "pickup_window"));

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
            final LocalDateTime pickupConfirmationDate, final Integer quantity, final String pickupWindow) {
        final Map<String, Object> parameters = new HashMap<>();
        parameters.put("customer_id", customerId);
        parameters.put("pack_id", packId);
        parameters.put("reservation_date", reservationDate == null ? null : Timestamp.valueOf(reservationDate));
        parameters.put("final_price", finalPrice);
        parameters.put("status", status == null ? null : status.name());
        parameters.put("pickup_code", pickupCode);
        parameters.put("pickup_confirmation_date",
                pickupConfirmationDate == null ? null : Timestamp.valueOf(pickupConfirmationDate));
        parameters.put("quantity", quantity == null ? 1 : quantity);
        parameters.put("pickup_window", pickupWindow);

        final Number id = simpleJdbcInsert.executeAndReturnKey(parameters);
        return new Reservation(id.longValue(), customerId, packId, reservationDate, finalPrice, status, pickupCode,
                pickupConfirmationDate, quantity == null ? 1 : quantity, pickupWindow);
    }

    @Override
    public Optional<Reservation> findById(final Long id) {
        return jdbcTemplate.query("SELECT * FROM reservations WHERE id = ?", RESERVATION_ROW_MAPPER, id).stream()
                .findAny();
    }

    @Override
    public List<Reservation> findByCustomerId(final Long customerId) {
        return jdbcTemplate.query("SELECT * FROM reservations WHERE customer_id = ?", RESERVATION_ROW_MAPPER,
                customerId);
    }

    @Override
    public List<Reservation> findByCommerceId(final Long commerceId) {
        return jdbcTemplate.query(
                "SELECT r.* FROM reservations r JOIN packs p ON r.pack_id = p.id WHERE p.commerce_id = ?",
                RESERVATION_ROW_MAPPER,
                commerceId);
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
    public Reservation confirmPickup(final Long id, final LocalDateTime pickupConfirmationDate) {
        jdbcTemplate.update("UPDATE reservations SET status = ?, pickup_confirmation_date = ? WHERE id = ?",
                Reservation.Status.PAID.name(),
                pickupConfirmationDate == null ? null : Timestamp.valueOf(pickupConfirmationDate),
                id);
        return findById(id).orElseThrow(() -> new IllegalStateException("Reservation not found: " + id));
    }

    @Override
    public Optional<Reservation> findByPickupCode(final String pickupCode) {
        return jdbcTemplate.query("SELECT * FROM reservations WHERE pickup_code = ?", RESERVATION_ROW_MAPPER,
                pickupCode).stream().findAny();
    }

    private void appendReservationFilters(StringBuilder sql, List<Object> params, Long commerceId, Long customerId, String query, Reservation.Status status, boolean excludeAuctionPacks) {
        sql.append("FROM reservations r ");
        sql.append("LEFT JOIN packs p ON r.pack_id = p.id ");
        sql.append("LEFT JOIN commerces c ON p.commerce_id = c.user_id ");
        sql.append("LEFT JOIN clients cl ON r.customer_id = cl.user_id ");
        sql.append("WHERE 1=1 ");

        if (commerceId != null) {
            sql.append("AND p.commerce_id = ? ");
            params.add(commerceId);
        }
        if (customerId != null) {
            sql.append("AND r.customer_id = ? ");
            params.add(customerId);
        }
        if (status != null) {
            sql.append("AND r.status = ? ");
            params.add(status.name());
        }

        if (query != null && !query.isBlank()) {
            final String escapedQuery = query.trim()
                    .replace("\\", "\\\\")
                    .replace("%", "\\%")
                    .replace("_", "\\_");
            final String pattern = "%" + escapedQuery + "%";
            sql.append("AND (p.title ILIKE ? ESCAPE '\\' OR p.description ILIKE ? ESCAPE '\\' ");
            params.add(pattern);
            params.add(pattern);
            
            if (commerceId != null) {
                sql.append("OR (cl.name || ' ' || cl.last_name) ILIKE ? ESCAPE '\\' ");
                params.add(pattern);
            } else if (customerId != null) {
                sql.append("OR c.commercial_name ILIKE ? ESCAPE '\\' ");
                params.add(pattern);
            }
            sql.append(") ");
        }

        if (excludeAuctionPacks) {
            sql.append("AND NOT EXISTS (SELECT 1 FROM auctions a WHERE a.pack_id = r.pack_id) ");
        }
    }

    @Override
    public List<Reservation> filterReservations(Long commerceId, Long customerId, String query, Reservation.Status status, boolean excludeAuctionPacks, int page, int pageSize) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT r.id, r.customer_id, r.pack_id, r.reservation_date, r.final_price, r.status, r.pickup_code, r.pickup_confirmation_date, r.quantity, r.pickup_window ");
        
        List<Object> params = new ArrayList<>();
        appendReservationFilters(sql, params, commerceId, customerId, query, status, excludeAuctionPacks);

        sql.append("ORDER BY r.reservation_date DESC ");
        sql.append("LIMIT ? OFFSET ?");
        params.add(pageSize);
        params.add((page - 1) * pageSize);

        return jdbcTemplate.query(sql.toString(), RESERVATION_ROW_MAPPER, params.toArray());
    }

    @Override
    public int countFilteredReservations(Long commerceId, Long customerId, String query, Reservation.Status status, boolean excludeAuctionPacks) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT COUNT(r.id) ");
        
        List<Object> params = new ArrayList<>();
        appendReservationFilters(sql, params, commerceId, customerId, query, status, excludeAuctionPacks);

        Integer count = jdbcTemplate.queryForObject(sql.toString(), Integer.class, params.toArray());
        return count != null ? count : 0;
    }

    @Override
    public boolean hasActiveReservation(Long packId, Long customerId) {
        final Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(id) FROM reservations WHERE pack_id = ? AND customer_id = ? AND status = ?",
                Integer.class,
                packId, customerId, Reservation.Status.RESERVED.name()
        );
        return count != null && count > 0;
    }

    @Override
    public int countPaidReservationsInPeriod(final Long commerceId,
                                          final LocalDateTime periodStart,
                                          final LocalDateTime periodEnd) {
        final Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(r.id) FROM reservations r " +
                "JOIN packs p ON r.pack_id = p.id " +
                "WHERE p.commerce_id = ? AND r.status = ? " +
                "AND r.pickup_confirmation_date >= ? AND r.pickup_confirmation_date < ?",
                Integer.class,
                commerceId,
                Reservation.Status.PAID.name(),
                Timestamp.valueOf(periodStart),
                Timestamp.valueOf(periodEnd));
        return count != null ? count : 0;
    }

    @Override
    public List<Object[]> countPaidReservationsPerDay(final Long commerceId, final LocalDateTime from,
            final LocalDateTime to) {
        return jdbcTemplate.query(
                "SELECT DATE(reservation_date) as day, COUNT(*) as cnt "
                        + "FROM reservations r JOIN packs p ON r.pack_id = p.id "
                        + "WHERE p.commerce_id = ? AND r.status = 'PAID' "
                        + "AND r.reservation_date >= ? AND r.reservation_date < ? "
                        + "GROUP BY DATE(reservation_date) ORDER BY day ASC",
                (rs, rowNum) -> new Object[] { rs.getDate("day").toLocalDate(), rs.getLong("cnt") },
                commerceId, Timestamp.valueOf(from), Timestamp.valueOf(to));
    }

    @Override
    public BigDecimal sumRevenueInPeriod(final Long commerceId, final LocalDateTime from,
            final LocalDateTime to) {
        final BigDecimal sum = jdbcTemplate.queryForObject(
                "SELECT SUM(r.final_price) FROM reservations r JOIN packs p ON r.pack_id = p.id "
                        + "WHERE p.commerce_id = ? AND r.status = 'PAID' "
                        + "AND r.pickup_confirmation_date >= ? AND r.pickup_confirmation_date < ?",
                BigDecimal.class,
                commerceId,
                Timestamp.valueOf(from),
                Timestamp.valueOf(to));
        return sum == null ? BigDecimal.ZERO : sum;
    }

    @Override
    public Optional<Long> findBestSellingPackId(final Long commerceId, final LocalDateTime from,
            final LocalDateTime to) {
        try {
            final Long id = jdbcTemplate.queryForObject(
                    "SELECT r.pack_id FROM reservations r JOIN packs p ON r.pack_id = p.id "
                            + "WHERE p.commerce_id = ? AND r.status = 'PAID' "
                            + "AND r.pickup_confirmation_date >= ? AND r.pickup_confirmation_date < ? "
                            + "GROUP BY r.pack_id ORDER BY COUNT(*) DESC LIMIT 1",
                    Long.class,
                    commerceId,
                    Timestamp.valueOf(from),
                    Timestamp.valueOf(to));
            return Optional.ofNullable(id);
        } catch (final EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public long countByStatusInPeriod(final Long commerceId, final Reservation.Status status,
            final LocalDateTime from, final LocalDateTime to) {
        final Long count = jdbcTemplate.queryForObject(
                "SELECT COUNT(r.id) FROM reservations r JOIN packs p ON r.pack_id = p.id "
                        + "WHERE p.commerce_id = ? AND r.status = ? "
                        + "AND r.reservation_date >= ? AND r.reservation_date < ?",
                Long.class,
                commerceId,
                status == null ? null : status.name(),
                Timestamp.valueOf(from),
                Timestamp.valueOf(to));
        return count == null ? 0L : count.longValue();
    }
}
