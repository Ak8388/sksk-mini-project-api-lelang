package jawa.sinaukoding.sk.entity;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.OffsetDateTime;

public record Auction(Long id, //
                      String code, //
                      String name, //
                      String description, //
                      BigInteger offer, //
                      BigInteger highestBid, //
                      Long highestBidderId,
                      String highestBidderName, //
                      Status status, //
                      OffsetDateTime startedAt, //
                      OffsetDateTime endedAt, //
                      Long createdBy, //
                      Long updatedBy, //
                      Long deletedBy, //
                      OffsetDateTime createdAt, //
                      OffsetDateTime updatedAt, //
                      OffsetDateTime deletedAt //
) {

    public static final String TABLE_NAME = "sk_auction";

    public PreparedStatement insert(final Connection connection) {
        try {

            final String sql = "INSERT INTO " + TABLE_NAME + " (code, name, description, offer, started_at, ended_at, highest_bid, highest_bidder_id, hignest_bidder_name, status, created_by, created_at) VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)";

            final PreparedStatement ps = connection.prepareStatement(sql,Statement.RETURN_GENERATED_KEYS);
            BigDecimal valueOffer = new BigDecimal(offer);
            BigDecimal valueHighest = new BigDecimal(highestBid);
            ps.setString(1, code());
            ps.setString(2, name());
            ps.setString(3, description());
            ps.setBigDecimal(4, valueOffer);
            ps.setObject(5, startedAt);
            ps.setObject(6, endedAt);
            ps.setBigDecimal(7, valueHighest);
            ps.setLong(8, highestBidderId);
            ps.setString(9, highestBidderName);
            ps.setString(10, status().name());
            ps.setLong(11, createdBy);
            ps.setObject(12, createdAt);

            return ps;
        } catch (Exception e) {
            
            return null;
        }
    }

    public enum Status {
        WAITING_FOR_APPROVAL, APPROVED, REJECTED, CLOSED
    }
}
