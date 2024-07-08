package jawa.sinaukoding.sk.repository;

import jawa.sinaukoding.sk.entity.Auction;
import jawa.sinaukoding.sk.entity.AuctionBid;
import jawa.sinaukoding.sk.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

@Repository
public class AuctionRepository {

    private static final Logger log = LoggerFactory.getLogger(AuctionRepository.class);

    private final JdbcTemplate jdbcTemplate;

    public AuctionRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public long approveAuction(long Id) {
        try {
            if (jdbcTemplate.update(con -> {
                final prepareStatement ps = con.prepareStatement("UPDATE" + Auction.TABLE_NAME + "SET status=?, update")
            }, null))
        }
    }

    

    public Optional<Auction> findById(final Long id) {
        if (id == null || id < 0) {
            return Optional.empty();
        }
        return Optional.ofNullable(jdbcTemplate.query(con -> {
            final PreparedStatement ps = con.prepareStatement("SELECT * FROM " + Auction.TABLE_NAME + " WHERE id=?");
            ps.setLong(1, id);
            return ps;
        }, rs -> {
            if (rs.getLong("id") <= 0) {
                return null;
            }
            Long id = rs.getLong("id")
            final String code  = rs.getString("code");
            final String name = rs.getString("name");
            final String description = rs.getString("description");
            final Long offer = rs.getBlob("offer")
            final Long highhest = rs.getBlob("offer")
            final Long highestBidderId = rs.getLong("highestBidderId");
            final String highestBidderName  = rs.getString("highestBidderName");
            //final Status status = rs.ge
            final OffsetDateTime startedAt = rs.getTimestamp("started_at") == null ? null : rs.getTimestamp("started_at").toInstant().atOffset(ZoneOffset.UTC);
            final OffsetDateTime endedAt = rs.getTimestamp("ended_at") == null ? null : rs.getTimestamp("ended_at").toInstant().atOffset(ZoneOffset.UTC);
            //final User.Role role = User.Role.valueOf(rs.getString("role"));
            final Long createdBy = rs.getLong("created_by");
            final Long updatedBy = rs.getLong("updated_by");
            final Long deletedBy = rs.getLong("deleted_by");
            final OffsetDateTime createdAt = rs.getTimestamp("created_at") == null ? null : rs.getTimestamp("created_at").toInstant().atOffset(ZoneOffset.UTC);
            final OffsetDateTime updatedAt = rs.getTimestamp("updated_at") == null ? null : rs.getTimestamp("updated_at").toInstant().atOffset(ZoneOffset.UTC);
            final OffsetDateTime deletedAt = rs.getTimestamp("deleted_at") == null ? null : rs.getTimestamp("deleted_at").toInstant().atOffset(ZoneOffset.UTC);
            return new Auction(id, code, name, description, offer, highestBid, highestBidderId, highestBidderName, startedAt, endedAt, createdBy, updatedBy, deletedBy, createdAt, updatedAt, deletedAt);
        }));
    }
}
