 package jawa.sinaukoding.sk.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import jawa.sinaukoding.sk.entity.Auction;


public interface AuctionRepository extends JpaRepository<Auction, Long> {

    
}