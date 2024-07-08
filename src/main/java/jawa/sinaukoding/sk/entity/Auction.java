package jawa.sinaukoding.sk.entity;

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

    public static final String TABLE_NAME = "auction";

    public PreparedStatement insert(final Connection connection) {
        try {
            // TODO: INSERT
            // final String sql = " INSERT INTO " + TABLE_NAME + "(code, name, description, offer, started_at, ended_at, highest_bid, highest_bidder_id, hignest_bidder_name, status, created_by, created_at)" + "VALUES (?, ?, ?, ?, ?, ?, ?, ?,?, ?, ?, ?)" ;
            // PreparedStatement ps = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
            // ps.setString(1, code());
            // ps.setString(2, name());
            // ps.setString(3, description());
            // ps.setInt(4, offer());
            // // ps.set
            // // ps.startedAt
            // ps.setInt(7, highestBid());
            // ps.setLong(8, highestBidderId());
            // ps.setString(9, highestBidderName());
            // ps.setString(10, status());
            // ps.setLong(11, createdBy());

            return null;
        } catch (Exception e) {
            return null;
        }
    }

    public enum Status {
        WAITING_FOR_APPROVAL, APPROVED, REJECTED, CLOSED
    }
}
