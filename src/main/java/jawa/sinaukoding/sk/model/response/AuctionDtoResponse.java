package jawa.sinaukoding.sk.model.response;

import java.util.List;

import jawa.sinaukoding.sk.entity.Auction;

public record AuctionDtoResponse(Long totalData, Long totalPage, Long page, Long offset, List<AuctionDto> auctionData) {
    
}
