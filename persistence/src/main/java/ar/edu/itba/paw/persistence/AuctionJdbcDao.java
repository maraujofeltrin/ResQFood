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
            "p.commerce_id, p.title, p.description, p.original_price, p.final_price, p.stock, p.active, p.deleted, p.image_id " +
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
                rs.getObject("image_id") != null ? rs.getLong("image_id") : null
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

    private void appendFilterJoinsAndConditions(final StringBuilder sql, final List<Object> params,
                                                final String query, final List<PackTag> tags,
                                                final String city, final List<String> timeRanges,
                                                final boolean requirePositiveStock) {
        final boolean hasQuery = query != null && !query.isBlank();
        final boolean hasTags = tags != null && !tags.isEmpty();
        final boolean hasCity = city != null && !city.isBlank();
        final boolean hasTime = timeRanges != null && !timeRanges.isEmpty();

        sql.append("FROM auctions a JOIN packs p ON a.pack_id = p.id ")
           .append("JOIN commerces c ON p.commerce_id = c.user_id ");

        if (hasTags) {
            sql.append("JOIN pack_tags pt ON p.id = pt.pack_id ");
        }

        sql.append("WHERE a.status = 'ACTIVE' AND a.end_time > ? ")
           .append("AND p.active = true AND p.deleted = false ");

        if (requirePositiveStock) {
            sql.append("AND p.stock > 0 ");
        }

        params.add(Timestamp.valueOf(LocalDateTime.now(ZoneOffset.UTC)));

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
    public List<Auction> filterAuctions(final String query, final List<PackTag> tags,
                                        final String city, final List<String> timeRanges,
                                        final AuctionSortOption sort,
                                        final int page, final int pageSize,
                                        final boolean requirePositiveStock) {
        final StringBuilder sql = new StringBuilder();
        sql.append("SELECT a.id AS auction_id, a.pack_id, a.initial_price, a.min_bid_increment, a.current_bid, a.current_bidder_id, ")
           .append("a.end_time, a.status, a.created_at, ")
           .append("p.commerce_id, p.title, p.description, p.original_price, p.final_price, p.stock, p.active, p.deleted, p.image_id ");

        final List<Object> params = new ArrayList<>();
        appendFilterJoinsAndConditions(sql, params, query, tags, city, timeRanges, requirePositiveStock);

        if (tags != null && !tags.isEmpty()) {
            sql.append("GROUP BY a.id, a.pack_id, a.initial_price, a.min_bid_increment, a.current_bid, a.current_bidder_id, ")
               .append("a.end_time, a.status, a.created_at, ")
               .append("p.commerce_id, p.title, p.description, p.original_price, p.final_price, p.stock, p.active, p.deleted, p.image_id, c.commercial_name ")
               .append("HAVING COUNT(DISTINCT pt.tag) = ? ");
            params.add(tags.size());
        }

        final AuctionSortOption safeSortOption = sort != null ? sort : AuctionSortOption.TIME_REMAINING_ASC;
        sql.append("ORDER BY ").append(safeSortOption.getOrderByClause()).append(" ");

        sql.append("LIMIT ? OFFSET ?");
        params.add(pageSize);
        params.add((page - 1) * pageSize);

        return jdbcTemplate.query(sql.toString(), auctionRowMapper, params.toArray());
    }

    @Override
    public int countFilteredAuctions(final String query, final List<PackTag> tags,
                                     final String city, final List<String> timeRanges,
                                     final boolean requirePositiveStock) {
        final StringBuilder sqlJoinsAndConditions = new StringBuilder();
        final List<Object> params = new ArrayList<>();
        appendFilterJoinsAndConditions(sqlJoinsAndConditions, params, query, tags, city, timeRanges,
                requirePositiveStock);

        if (tags != null && !tags.isEmpty()) {
            final StringBuilder wrapperSql = new StringBuilder();
            wrapperSql.append("SELECT COUNT(*) FROM (SELECT a.id ");
            wrapperSql.append(sqlJoinsAndConditions);
            wrapperSql.append("GROUP BY a.id HAVING COUNT(DISTINCT pt.tag) = ?) AS subquery");
            params.add(tags.size());
            Integer count = jdbcTemplate.queryForObject(wrapperSql.toString(), Integer.class, params.toArray());
            return count != null ? count : 0;
        } else {
            final StringBuilder sql = new StringBuilder();
            sql.append("SELECT COUNT(DISTINCT a.id) ");
            sql.append(sqlJoinsAndConditions);
            Integer count = jdbcTemplate.queryForObject(sql.toString(), Integer.class, params.toArray());
            return count != null ? count : 0;
        }
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

    @Override
    public List<Auction> filterParticipatedAuctions(final long clientId, final Auction.Status status,
                                                     final String query, final int page, final int pageSize) {
        final StringBuilder sql = new StringBuilder();
        sql.append("SELECT a.id AS auction_id, a.pack_id, a.initial_price, a.min_bid_increment, a.current_bid, a.current_bidder_id, ")
           .append("a.end_time, a.status, a.created_at, ")
           .append("p.commerce_id, p.title, p.description, p.original_price, p.final_price, p.stock, p.active, p.deleted, p.image_id ");

        final List<Object> params = new ArrayList<>();
        appendParticipatedConditions(sql, params, clientId, status, query);

        sql.append("ORDER BY b_latest.latest_bid DESC ");
        sql.append("LIMIT ? OFFSET ?");
        params.add(pageSize);
        params.add((page - 1) * pageSize);

        return jdbcTemplate.query(sql.toString(), auctionRowMapper, params.toArray());
    }

    @Override
    public int countParticipatedAuctions(final long clientId, final Auction.Status status, final String query) {
        final StringBuilder sql = new StringBuilder("SELECT COUNT(DISTINCT a.id) ");
        final List<Object> params = new ArrayList<>();
        appendParticipatedConditions(sql, params, clientId, status, query);
        final Integer count = jdbcTemplate.queryForObject(sql.toString(), Integer.class, params.toArray());
        return count != null ? count : 0;
    }

    private void appendParticipatedConditions(final StringBuilder sql, final List<Object> params,
                                               final long clientId, final Auction.Status status, final String query) {
        sql.append("FROM auctions a ")
           .append("JOIN packs p ON a.pack_id = p.id ")
           .append("JOIN commerces c ON p.commerce_id = c.user_id ")
           .append("JOIN (SELECT auction_id, MAX(timestamp) AS latest_bid FROM bids WHERE client_id = ? GROUP BY auction_id) b_latest ")
           .append("ON a.id = b_latest.auction_id ")
           .append("WHERE p.deleted = false ");
        params.add(clientId);

        if (status != null) {
            sql.append("AND a.status = ? ");
            params.add(status.name());
        }

        if (query != null && !query.isBlank()) {
            final String escaped = query.trim()
                    .replace("\\", "\\\\")
                    .replace("%", "\\%")
                    .replace("_", "\\_");
            final String pattern = "%" + escaped + "%";
            sql.append("AND (p.title ILIKE ? ESCAPE '\\' OR p.description ILIKE ? ESCAPE '\\' OR c.commercial_name ILIKE ? ESCAPE '\\') ");
            params.add(pattern);
            params.add(pattern);
            params.add(pattern);
        }
    }
}
