package jawa.sinaukoding.sk.repository;

import jawa.sinaukoding.sk.entity.User;
import jawa.sinaukoding.sk.exception.CustomeException1;
import jawa.sinaukoding.sk.model.request.deleteReq;

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
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Repository
public class UserRepository {

    private static final Logger log = LoggerFactory.getLogger(UserRepository.class);

    private final JdbcTemplate jdbcTemplate;

    public UserRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<User> listUsers(int page, int size) {
        try{
            final String sql = "SELECT id, name, role FROM %s".formatted(User.TABLE_NAME)+" Where deleted_at IS NULL Limit "+size+" OFFSET "+(page*size-size); 

            final List<User> users = jdbcTemplate.query(sql, new RowMapper<User>() {
                @Override
                public User mapRow(ResultSet rs, int rowNum) throws SQLException {
                    final User.Role role = User.Role.fromString(rs.getString("role"));
                    return new User(
                            rs.getLong("id"), //
                            rs.getString("name"), //
                            null, //
                            null, //
                            role, //
                            null, //
                            null, //
                            null, //
                            null, //
                            null, //
                            null); //
                }

                
            });
            return users;
        }catch(Exception e){
            throw new CustomeException1("gagal ambil data");
        }
    }

    public Long listCountData(){
        try{
            return jdbcTemplate.queryForObject("select count(id) from "+User.TABLE_NAME, Long.class);
        }catch(Exception e){
            throw new CustomeException1("gagal count data");
        }
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
            throw new CustomeException1("Failed save seller");

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
            throw new CustomeException1("Failed save");

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
           throw new CustomeException1("Failed to update ");
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

    public Long deleteUser(final deleteReq req, Long idUser) {
        try {
            if (jdbcTemplate.update(con -> {
                final PreparedStatement ps = con.prepareStatement("UPDATE " + User.TABLE_NAME + " SET deleted_by=?, deleted_at=CURRENT_TIMESTAMP WHERE id=?");
                ps.setLong(1, idUser);
                ps.setLong(2, req.id());
                return ps;
            }) > 0) {
                return req.id();
            } else {
                return 0L;
    
            }
            
        } catch (Exception e) {
            log.error("ERROR", e);
           throw new CustomeException1("failed to delete");
        }
       
    }
}
