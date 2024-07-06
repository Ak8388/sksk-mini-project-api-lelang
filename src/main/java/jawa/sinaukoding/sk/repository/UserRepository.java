package jawa.sinaukoding.sk.repository;

import jawa.sinaukoding.sk.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

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
public class UserRepository {

    private static final Logger log = LoggerFactory.getLogger(UserRepository.class);

    private final JdbcTemplate jdbcTemplate;

    public UserRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<User> listUsers(int page, int size) {
        final String sql = "SELECT * FROM %s".formatted(User.TABLE_NAME)+" Limit "+size+" OFFSET "+(page*size-size); 
        final List<User> users = jdbcTemplate.query(sql, new RowMapper<User>() {
            @Override
            public User mapRow(ResultSet rs, int rowNum) throws SQLException {
                final User.Role role = User.Role.fromString(rs.getString("role"));
                final Timestamp createdAt = rs.getTimestamp("created_at");
                final Timestamp updatedAt = rs.getTimestamp("updated_at");
                final Timestamp deletedAt = rs.getTimestamp("deleted_at");
                return new User(rs.getLong("id"), //
                        rs.getString("name"), //
                        rs.getString("email"), //
                        rs.getString("password"), //
                        role, //
                        rs.getLong("created_by"), //
                        rs.getLong("updated_by"), //
                        rs.getLong("deleted_by"), //
                        createdAt == null ? null : createdAt.toInstant().atOffset(ZoneOffset.UTC), //
                        updatedAt == null ? null : updatedAt.toInstant().atOffset(ZoneOffset.UTC), //
                        deletedAt == null ? null : deletedAt.toInstant().atOffset(ZoneOffset.UTC)); //
            }
        });
        return users;
    }

    public long saveSeller(final User user) {
        final KeyHolder keyHolder = new GeneratedKeyHolder();
        try {
            if (jdbcTemplate.update(con -> Objects.requireNonNull(user.insert(con)), keyHolder) != 1) {
                return 0L;
            } else {
                return Objects.requireNonNull(keyHolder.getKey()).longValue();
            }
        } catch (Exception e) {
            log.error("{}", e.getMessage());
            return 0L;
        }
    }

    public long saveBuyer(final User user) {
        final KeyHolder keyHolder = new GeneratedKeyHolder();
        try {
            if (jdbcTemplate.update(con -> Objects.requireNonNull(user.insert(con)), keyHolder) != 1) {
                return 0L;
            } else {
                return Objects.requireNonNull(keyHolder.getKey()).longValue();
            }
        } catch (Exception e) {
            log.error("{}", e.getMessage());
            return 0L;
        }
    }

    public long updatePassword(Long userId, String newPassword) {
        try {
            if (jdbcTemplate.update(con -> {
                final PreparedStatement ps = con.prepareStatement("UPDATE " + User.TABLE_NAME + " SET password=?, updated_by=?, updated_at=CURRENT_TIMESTAMP WHERE id=?");
                ps.setString(1, newPassword);
                ps.setLong(2, userId);
                ps.setLong(3, userId);
                return ps;
            }) > 0) {
                return userId;
            } else {
                return 0L;
            }
            
        } catch (Exception e) {
           System.err.println("Error reset password for user id" + userId + ": " + e.getMessage());
           return 0L;
        }
        

        
    }

    public Optional<User> findById(final Long id) {
        if (id == null || id < 0) {
            return Optional.empty();
        }
        return Optional.ofNullable(jdbcTemplate.query(con -> {
            final PreparedStatement ps = con.prepareStatement("SELECT * FROM " + User.TABLE_NAME + " WHERE id=?");
            ps.setLong(1, id);
            return ps;
        }, rs -> {
            if (rs.getLong("id") <= 0) {
                return null;
            }
            final String name = rs.getString("name");
            final String email = rs.getString("email");
            final String password = rs.getString("password");
            final User.Role role = User.Role.valueOf(rs.getString("role"));
            final Long createdBy = rs.getLong("created_by");
            final Long updatedBy = rs.getLong("updated_by");
            final Long deletedBy = rs.getLong("deleted_by");
            final OffsetDateTime createdAt = rs.getTimestamp("created_at") == null ? null : rs.getTimestamp("created_at").toInstant().atOffset(ZoneOffset.UTC);
            final OffsetDateTime updatedAt = rs.getTimestamp("updated_at") == null ? null : rs.getTimestamp("updated_at").toInstant().atOffset(ZoneOffset.UTC);
            final OffsetDateTime deletedAt = rs.getTimestamp("deleted_at") == null ? null : rs.getTimestamp("deleted_at").toInstant().atOffset(ZoneOffset.UTC);
            return new User(id, name, email, password, role, createdBy, updatedBy, deletedBy, createdAt, updatedAt, deletedAt);
        }));
    }

    public Optional<User> findByEmail(final String email) {
        if (email == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(jdbcTemplate.query(con -> {
            final PreparedStatement ps = con.prepareStatement("SELECT * FROM " + User.TABLE_NAME + " WHERE email=?");
            ps.setString(1, email);
            return ps;
        }, rs -> {
            final Long id = rs.getLong("id");
            if (id <= 0) {
                return null;
            }
            final String name = rs.getString("name");
            final String password = rs.getString("password");
            final User.Role role = User.Role.valueOf(rs.getString("role"));
            final Long createdBy = rs.getLong("created_by");
            final Long updatedBy = rs.getLong("updated_by");
            final Long deletedBy = rs.getLong("deleted_by");
            final OffsetDateTime createdAt = rs.getTimestamp("created_at") == null ? null : rs.getTimestamp("created_at").toInstant().atOffset(ZoneOffset.UTC);
            final OffsetDateTime updatedAt = rs.getTimestamp("updated_at") == null ? null : rs.getTimestamp("updated_at").toInstant().atOffset(ZoneOffset.UTC);
            final OffsetDateTime deletedAt = rs.getTimestamp("deleted_at") == null ? null : rs.getTimestamp("deleted_at").toInstant().atOffset(ZoneOffset.UTC);
            return new User(id, name, email, password, role, createdBy, updatedBy, deletedBy, createdAt, updatedAt, deletedAt);
        }));
    }

    public long updateProfile(User user){
        String idStr = Long.toString(user.id());
        ArrayList<String> listValue = new ArrayList<>();

        if(user.id() == 0){
            return 0L; 
        }

        StringBuilder qry = new StringBuilder();
        qry.append("UPDATE "+ User.TABLE_NAME +" SET ");
        
        if(user.name() != ""){
            qry.append("name=?");
            listValue.add(user.name());
        }

        if(user.email() != ""){
            if(listValue.size() >= 1){
                qry.append(", email=?");
            }else{
                qry.append("email=?");
            }
            
            listValue.add(user.email());
        }

        if(listValue.size() == 0){
            return 0L;
        }

        qry.append(",updated_by=?, updated_at=CURRENT_TIMESTAMP WHERE id=?");
        listValue.add(idStr);
        listValue.add(idStr);
        
       if(jdbcTemplate.update(con -> {
            final PreparedStatement ps = con.prepareStatement(qry.toString());

            for(int x=0; x<listValue.size(); x++){
                ps.setString(x+1,listValue.get(x));
            }
            
            return ps;
        }) > 0){
            return user.id();
        }else{
           return 0L; 
        }
    }

    public Long deleteUser(final Long id, Long idUser) {
        if (id == null) {
            return 0L;
        }
        if (jdbcTemplate.update(con -> {
            final PreparedStatement ps = con.prepareStatement("UPDATE " + User.TABLE_NAME + " SET deleted_by=?, deleted_at=CURRENT_TIMESTAMP WHERE id=?");
            ps.setLong(1, id);
            ps.setLong(2, idUser);
            return ps;
        }) > 0) {
            return id;
        } else {
            return 0L;

        }
    }
}
