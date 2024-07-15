package jawa.sinaukoding.sk.repository;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.jdbc.core.RowMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import jawa.sinaukoding.sk.entity.Auction;
import jawa.sinaukoding.sk.entity.AuctionBid;
import jawa.sinaukoding.sk.exception.CustomeException1;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;



@Repository
public class AuctionRepo {
    private static final Logger log = LoggerFactory.getLogger(UserRepository.class);

    private final JdbcTemplate jdbcTemplate;
    public AuctionRepo(JdbcTemplate jdbcTemplate){
        this.jdbcTemplate = jdbcTemplate;
    }

    public Long saveAuction(Auction auction){
        KeyHolder keyHolder = new GeneratedKeyHolder();

        try{
            if(jdbcTemplate.update(con->Objects.requireNonNull(auction.insert(con)),keyHolder) != 1){
                return 0L;
            }else{
                return Objects.requireNonNull(keyHolder.getKey()).longValue();
            }
        } catch(Exception e){
            log.error("{}", e);
            throw new CustomeException1("failed to save");
        } 

    }

    public Long RejectedAuction(Long Id){
        try {
            if(jdbcTemplate.update(con -> {
                final PreparedStatement ps = con.prepareStatement("UPDATE " + Auction.TABLE_NAME + " SET status = ?, updated_at=? WHERE id=?");
                ps.setString(1, Auction.Status.REJECTED.toString());
                ps.setObject(2,OffsetDateTime.now(ZoneOffset.UTC));
                ps.setLong(3,Id);
                return ps;
            }) > 0){
                return Id;
            }else {
                return 0L;
            }
        } catch (Exception e) {
        System.err.println("error to updating status" + Id + ":" + e.getMessage());
        throw new CustomeException1("Failed rejected");

        }

    }

    public Long ApproveAuction(Long Id){
        try {
            if(jdbcTemplate.update(con -> {
                final PreparedStatement ps = con.prepareStatement("UPDATE " + Auction.TABLE_NAME + " SET status = ?, updated_at=? WHERE id=?");
                ps.setString(1, Auction.Status.APPROVED.toString());
                ps.setObject(2,OffsetDateTime.now(ZoneOffset.UTC));
                ps.setLong(3,Id);
                return ps;
            }) > 0){
                return Id;
            }else {
                return 0L;
            }
        } catch (Exception e) {
        System.err.println("error to updating status" + Id + ":" + e.getMessage());
        throw new CustomeException1("Failed approved");
        }

    }

    public Optional<Auction> findById(Long id){
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
            Long Id = rs.getLong("id");
            String code = rs.getString("code");
            String name = rs.getString("name");
            String description = rs.getString("description");
            BigDecimal offer = rs.getBigDecimal("offer");

            Object startedAt = rs.getObject("started_at");
            String str = startedAt.toString();
            OffsetDateTime startAtAsli = OffsetDateTime.parse(str) == null ? null : OffsetDateTime.parse(str);

            Object endedAt = rs.getObject("ended_at");
            String end = endedAt.toString();
            OffsetDateTime endAtAsli = OffsetDateTime.parse(end) == null ? null : OffsetDateTime.parse(end);

            BigDecimal highestBid = rs.getBigDecimal("highest_bid");
            Long highestBidderId = rs.getLong("highest_bidder_id");
            String highestBidderName = rs.getString("hignest_bidder_name");
            Auction.Status status = Auction.Status.valueOf(rs.getString("status"));

            Long createdBy = rs.getLong("created_by");
            Long updatedBy = rs.getLong("updated_by");
            Long deletedBy = rs.getLong("deleted_by");

            Object createdAt  = rs.getObject("created_at");
            String created = createdAt.toString();
            OffsetDateTime creatAtAsli = OffsetDateTime.parse(created) == null ? null : OffsetDateTime.parse(created);

            Object updatedAt  = rs.getObject("updated_at");
            String update = updatedAt == null ? null : updatedAt.toString();
            OffsetDateTime updateAtAsli = updatedAt == null ? null : OffsetDateTime.parse(update);

            Object deletedAt  = rs.getObject("deleted_at");
            String deleted = deletedAt == null ? null : deletedAt.toString();
            OffsetDateTime deletedAsli = deletedAt == null ? null : OffsetDateTime.parse(deleted);
            
            BigInteger offerBigInt = offer.toBigInteger();
            BigInteger highestBidBigInteger = highestBid.toBigInteger();

            return new Auction(Id,code,name,description,offerBigInt,highestBidBigInteger, highestBidderId,highestBidderName,status,startAtAsli,endAtAsli,createdBy,updatedBy,deletedBy,creatAtAsli,updateAtAsli,deletedAsli);

        }));

    }

    public Long updateHigestBidAndInsertBidTable(final Auction auction,final AuctionBid auctionBid){
        try{
            if(jdbcTemplate.update(con ->{
                PreparedStatement ps = con.prepareStatement("Update "+Auction.TABLE_NAME+" Set highest_bid=?, highest_bidder_id=?, hignest_bidder_name=? Where id=?");
                
                ps.setObject(1, auction.highestBid());
                ps.setLong(2, auction.highestBidderId());
                ps.setString(3, auction.highestBidderName());
                ps.setLong(4, auction.id());

                return ps;
            }) > 0){  
                if(jdbcTemplate.update(con ->{
                    PreparedStatement ps2 = con.prepareStatement("Insert Into "+"sk_auction_bit"+" (auction_id, bid, bidder, created_at) Values(?,?,?,?)");
                    ps2.setLong(1,auctionBid.auctionId());
                    ps2.setObject(2, auctionBid.bid());
                    ps2.setLong(3, auctionBid.bidder());
                    ps2.setObject(4, auctionBid.createdAt());
                    return ps2;
                }) > 0) {
                    return 1L;
                }else{
                    throw new RuntimeException("Gagal");
                }
            }else{
                throw new RuntimeException("Gagal");
            }
  
        }catch(Exception e){
            log.error("{}", e);
            throw new CustomeException1("failed update");
        }
    }

    public List<Auction> listAuction(int page,int size,String stts){
        try{
            final String sql = "SELECT id, name, description, offer, highest_bid, highest_bidder_id, hignest_bidder_name, status, started_at, ended_at " +
            "FROM %s WHERE status = ? AND deleted_at IS NULL LIMIT ? OFFSET ?".formatted(Auction.TABLE_NAME); 
            final List<Auction> auctions = jdbcTemplate.query(sql,new RowMapper<Auction>() {
                @Override
                public Auction mapRow(ResultSet rs, int rowNum) throws SQLException {
                    final Auction.Status status = Auction.Status.fromString(rs.getString("status"));
                    return new Auction(
                    rs.getLong("id"),
                    null,
                    rs.getString("name"),
                    rs.getString("description"),
                    BigInteger.valueOf(rs.getLong("offer")),
                    BigInteger.valueOf(rs.getLong("highest_bid")),
                    rs.getLong("highest_bidder_id"),
                    rs.getString("hignest_bidder_name"),
                    status,
                    OffsetDateTime.parse(rs.getString("started_at")),
                    OffsetDateTime.parse(rs.getString("ended_at")),
                    null,
                    null,
                    null,
                    null,
                    null,
                    null);
                }
            }, new Object[]{stts, size, (page-1)*size});
            return auctions;
        }catch(Exception e){
            log.error("{}", e);
            throw new CustomeException1("gagal select data");
        }

    }

    public Long countData(String stts){
        try{ 
            return jdbcTemplate.query(con -> {
                PreparedStatement ps = con.prepareStatement("Select Count(id) as total_data from %s".formatted(Auction.TABLE_NAME)+" Where status=?");
                ps.setString(1,stts);
    
                return ps;
                }, rs->{
                   Long totalData = rs.getLong("total_data");
    
                    return totalData;
                });
        }catch(Exception e){
            log.error("{}", e);
            throw new CustomeException1("gagal count data");
        }
    }

}
