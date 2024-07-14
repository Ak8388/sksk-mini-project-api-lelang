package jawa.sinaukoding.sk.model.response;

import java.math.BigInteger;
import java.time.OffsetDateTime;
import jawa.sinaukoding.sk.entity.Auction;

public record AuctionDto(Long id, String name, String description, BigInteger offer, BigInteger highestBid, Long highestBidderId, String highestBidderName, Auction.Status status, OffsetDateTime startedAt, OffsetDateTime endedAt) {
   
}
