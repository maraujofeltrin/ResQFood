package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.models.auction.Bid;
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
public class BidJdbcDao implements BidDao {

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert simpleJdbcInsert;

    private static final RowMapper<Bid> BID_ROW_MAPPER = (rs, rowNum) -> {
        final Timestamp ts = rs.getTimestamp("timestamp");
        return new Bid(
                rs.getLong("id"),
                rs.getLong("auction_id"),
                rs.getLong("client_id"),
                rs.getDouble("amount"),
                ts != null ? ts.toLocalDateTime() : null
        );
    };

    @Autowired
    public BidJdbcDao(final DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.simpleJdbcInsert = new SimpleJdbcInsert(dataSource)
                .withTableName("bids")
                .usingGeneratedKeyColumns("id");
    }

    @Override
    public Bid createBid(final long auctionId, final long clientId, final double amount) {
        final LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        final Map<String, Object> params = new HashMap<>();
        params.put("auction_id", auctionId);
        params.put("client_id", clientId);
        params.put("amount", amount);
        params.put("timestamp", Timestamp.valueOf(now));

        final Number id = simpleJdbcInsert.executeAndReturnKey(params);

        return new Bid(id.longValue(), auctionId, clientId, amount, now);
    }

    @Override
    public List<Bid> findByAuctionId(final long auctionId) {
        return jdbcTemplate.query(
                "SELECT id, auction_id, client_id, amount, timestamp FROM bids WHERE auction_id = ? ORDER BY amount DESC, timestamp ASC",
                BID_ROW_MAPPER, auctionId
        );
    }

    @Override
    public Optional<Bid> findHighestBid(final long auctionId) {
        return jdbcTemplate.query(
                "SELECT id, auction_id, client_id, amount, timestamp FROM bids WHERE auction_id = ? ORDER BY amount DESC, timestamp ASC LIMIT 1",
                BID_ROW_MAPPER, auctionId
        ).stream().findFirst();
    }

    @Override
    public List<Bid> findByClientId(final long clientId) {
        return jdbcTemplate.query(
                "SELECT id, auction_id, client_id, amount, timestamp FROM bids WHERE client_id = ? ORDER BY timestamp DESC",
                BID_ROW_MAPPER, clientId
        );
    }

    @Override
    public int countByAuctionId(final long auctionId) {
        final Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM bids WHERE auction_id = ?",
                Integer.class, auctionId
        );
        return count != null ? count : 0;
    }
}
