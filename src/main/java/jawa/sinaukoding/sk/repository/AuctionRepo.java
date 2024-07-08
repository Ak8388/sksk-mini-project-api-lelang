package jawa.sinaukoding.sk.repository;

import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import jawa.sinaukoding.sk.entity.Auction;


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
            return 0L;
        }      
    }
}
