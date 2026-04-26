package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.models.auction.Auction;
import ar.edu.itba.paw.models.auction.AuctionSortOption;
import ar.edu.itba.paw.models.pack.Pack;
import ar.edu.itba.paw.models.pack.PackTag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class AuctionJdbcDao implements AuctionDao {

    private static final String AUCTION_JOIN_PACK =
            "SELECT a.id AS auction_id, a.pack_id, a.initial_price, a.min_bid_increment, a.current_bid, a.current_bidder_id, " +
            "a.end_time, a.status, a.created_at, " +
            "p.commerce_id, p.title, p.description, p.original_price, p.final_price, p.stock, p.active, p.deleted " +
            "FROM auctions a JOIN packs p ON a.pack_id = p.id";

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert simpleJdbcInsert;
    private final RowMapper<Auction> auctionRowMapper;

    @Autowired
    public AuctionJdbcDao(final DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.simpleJdbcInsert = new SimpleJdbcInsert(dataSource)
                .withTableName("auctions")
                .usingGeneratedKeyColumns("id");

        this.auctionRowMapper = (rs, rowNum) -> {
            final Pack pack = mapPack(rs);
            return mapAuction(rs, pack);
        };
    }

    private static Pack mapPack(final ResultSet rs) throws SQLException {
        return new Pack(
                rs.getLong("pack_id"),
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

    private static Auction mapAuction(final ResultSet rs, final Pack pack) throws SQLException {
        final Double currentBid = rs.getObject("current_bid") != null ? rs.getDouble("current_bid") : null;
        final Long currentBidderId = rs.getObject("current_bidder_id") != null ? rs.getLong("current_bidder_id") : null;
        final Timestamp endTimeTs = rs.getTimestamp("end_time");
        final Timestamp createdAtTs = rs.getTimestamp("created_at");

        return new Auction(
                rs.getLong("auction_id"),
                pack,
                rs.getDouble("initial_price"),
                rs.getDouble("min_bid_increment"),
                currentBid,
                currentBidderId,
                endTimeTs != null ? endTimeTs.toLocalDateTime() : null,
                Auction.Status.valueOf(rs.getString("status")),
                createdAtTs != null ? createdAtTs.toLocalDateTime() : null
        );
    }

    @Override
    public Auction createAuction(final long packId, final double initialPrice, final double minBidIncrement, final LocalDateTime endTime) {
        final Map<String, Object> params = new HashMap<>();
        params.put("pack_id", packId);
        params.put("initial_price", initialPrice);
        params.put("min_bid_increment", minBidIncrement);
        params.put("end_time", Timestamp.valueOf(endTime));
        params.put("status", Auction.Status.ACTIVE.name());
        params.put("created_at", Timestamp.valueOf(LocalDateTime.now(ZoneOffset.UTC)));

        final Number id = simpleJdbcInsert.executeAndReturnKey(params);

        return findById(id.longValue())
                .orElseThrow(() -> new IllegalStateException("Failed to retrieve auction after insert, id=" + id));
    }

    @Override
    public Optional<Auction> findById(final long id) {
        return jdbcTemplate.query(
                AUCTION_JOIN_PACK + " WHERE a.id = ?",
                auctionRowMapper, id
        ).stream().findFirst();
    }

    @Override
    public Optional<Auction> findByPackId(final long packId) {
        return jdbcTemplate.query(
                AUCTION_JOIN_PACK + " WHERE a.pack_id = ?",
                auctionRowMapper, packId
        ).stream().findFirst();
    }

    @Override
    public List<Auction> findActive() {
        return findActive(AuctionSortOption.TIME_REMAINING_ASC);
    }

    @Override
    public List<Auction> findActive(final AuctionSortOption sort) {
        return jdbcTemplate.query(
                AUCTION_JOIN_PACK + " WHERE a.status = 'ACTIVE' AND a.end_time > ? AND p.active = true "
                        + "AND p.deleted = false ORDER BY " + sort.getOrderByClause(),
                auctionRowMapper,
                Timestamp.valueOf(LocalDateTime.now(ZoneOffset.UTC))
        );
    }

    @Override
    public List<Auction> searchActive(final String query, final AuctionSortOption sort) {
        final String pattern = "%" + query + "%";
        return jdbcTemplate.query(
                AUCTION_JOIN_PACK +
                        " JOIN commerces c ON p.commerce_id = c.user_id" +
                        " WHERE a.status = 'ACTIVE' AND a.end_time > ? AND p.active = true" +
                        " AND (p.title ILIKE ? OR c.commercial_name ILIKE ?)" +
                        " ORDER BY " + sort.getOrderByClause(),
                auctionRowMapper,
                Timestamp.valueOf(LocalDateTime.now(ZoneOffset.UTC)),
                pattern,
                pattern
        );
    }

    @Override
    public List<Auction> findActiveByTags(final List<PackTag> tags, final AuctionSortOption sort) {
        if (tags == null || tags.isEmpty()) {
            return findActive(sort);
        }
        final String inClause = String.join(", ", Collections.nCopies(tags.size(), "?"));
        final List<Object> params = new ArrayList<>();
        params.add(Timestamp.valueOf(LocalDateTime.now(ZoneOffset.UTC)));
        for (final PackTag tag : tags) {
            params.add(tag.name());
        }
        params.add(tags.size());

        return jdbcTemplate.query(
                AUCTION_JOIN_PACK +
                        " WHERE a.status = 'ACTIVE' AND a.end_time > ? AND p.active = true" +
                        " AND p.id IN (" +
                        "     SELECT pt.pack_id" +
                        "     FROM pack_tags pt" +
                        "     WHERE pt.tag IN (" + inClause + ")" +
                        "     GROUP BY pt.pack_id" +
                        "     HAVING COUNT(DISTINCT pt.tag) = ?" +
                        " )" +
                        " ORDER BY " + sort.getOrderByClause(),
                auctionRowMapper,
                params.toArray()
        );
    }

    @Override
    public List<Auction> searchActiveWithTags(final String query, final List<PackTag> tags,
            final AuctionSortOption sort) {
        if (tags == null || tags.isEmpty()) {
            return searchActive(query, sort);
        }
        final String pattern = "%" + query + "%";
        final String inClause = String.join(", ", Collections.nCopies(tags.size(), "?"));
        final List<Object> params = new ArrayList<>();
        params.add(Timestamp.valueOf(LocalDateTime.now(ZoneOffset.UTC)));
        params.add(pattern);
        params.add(pattern);
        for (final PackTag tag : tags) {
            params.add(tag.name());
        }
        params.add(tags.size());

        return jdbcTemplate.query(
                AUCTION_JOIN_PACK +
                        " JOIN commerces c ON p.commerce_id = c.user_id" +
                        " WHERE a.status = 'ACTIVE' AND a.end_time > ? AND p.active = true" +
                        " AND (p.title ILIKE ? OR c.commercial_name ILIKE ?)" +
                        " AND p.id IN (" +
                        "     SELECT pt.pack_id" +
                        "     FROM pack_tags pt" +
                        "     WHERE pt.tag IN (" + inClause + ")" +
                        "     GROUP BY pt.pack_id" +
                        "     HAVING COUNT(DISTINCT pt.tag) = ?" +
                        " )" +
                        " ORDER BY " + sort.getOrderByClause(),
                auctionRowMapper,
                params.toArray()
        );
    }

    @Override
    public List<Auction> findByCommerceId(final long commerceId) {
        return jdbcTemplate.query(
                AUCTION_JOIN_PACK + " WHERE p.commerce_id = ? AND p.deleted = false ORDER BY a.created_at DESC",
                auctionRowMapper, commerceId
        );
    }

    @Override
    public List<Auction> findByStatus(final Auction.Status status) {
        return jdbcTemplate.query(
                AUCTION_JOIN_PACK + " WHERE a.status = ? AND p.deleted = false ORDER BY a.end_time ASC",
                auctionRowMapper, status.name()
        );
    }

    @Override
    public void updateStatus(final long auctionId, final Auction.Status status) {
        jdbcTemplate.update("UPDATE auctions SET status = ? WHERE id = ?", status.name(), auctionId);
    }

    @Override
    public void updateCurrentBid(final long auctionId, final double amount, final long bidderId) {
        jdbcTemplate.update(
                "UPDATE auctions SET current_bid = ?, current_bidder_id = ? WHERE id = ?",
                amount, bidderId, auctionId
        );
    }

    @Override
    public List<Auction> findExpiredActive() {
        return jdbcTemplate.query(
                AUCTION_JOIN_PACK + " WHERE a.status = 'ACTIVE' AND a.end_time <= ? AND p.deleted = false",
                auctionRowMapper,
                Timestamp.valueOf(LocalDateTime.now(ZoneOffset.UTC))
        );
    }
}
