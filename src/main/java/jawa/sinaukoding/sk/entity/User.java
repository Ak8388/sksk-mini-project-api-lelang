package jawa.sinaukoding.sk.entity;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.OffsetDateTime;

public record User(Long id, //
                   String name, //
                   String email, //
                   String password, //
                   Role role, //
                   Long createdBy, //
                   Long updatedBy, //
                   Long deletedBy, // 
                   OffsetDateTime createdAt, //
                   OffsetDateTime updatedAt, //
                   OffsetDateTime deletedAt) { //

    public static final String TABLE_NAME = "sys_user";

    public PreparedStatement insert(final Connection connection) {
        try {
            final String sql = "INSERT INTO " + TABLE_NAME + " (name, email, password, role, created_by, created_at) VALUES (?, ?, ?, ?, ?, CURRENT_TIMESTAMP)";
            final PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, name);
            ps.setString(2, email);
            ps.setString(3, password);
            ps.setString(4, role.name());
            ps.setLong(5, createdBy);
            return ps;
        } catch (Exception e) {
            return null;
        }
    }

    public enum Role {
        ADMIN, BUYER, SELLER, UNKNOWN;

        public static Role fromString(String str) {
            if (ADMIN.name().equals(str)) {
                return ADMIN;
            } else if (BUYER.name().equals(str)) {
                return BUYER;
            } else if (SELLER.name().equals(str)) {
                return SELLER;
            } else {
                return UNKNOWN;
            }
        }
    }
}
