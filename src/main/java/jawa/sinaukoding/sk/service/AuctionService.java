package jawa.sinaukoding.sk.service;

import jawa.sinaukoding.sk.model.Response;
import jawa.sinaukoding.sk.model.Authentication;
import jawa.sinaukoding.sk.entity.Auction;
import jawa.sinaukoding.sk.entity.User;
import jawa.sinaukoding.sk.repository.AuctionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;


import java.util.Optional;

@Service
public class AuctionService extends AbstractService {

    @Autowired
    private final AuctionRepository auctionRepository;

    @Autowired
    public AuctionService(final Environment env, final AuctionRepository auctionRepository) {
        this.auctionRepository = auctionRepository;
    }

    public Response<Object> rejectAuction(final Authentication authentication, Long id) {
        return precondition(authentication, User.Role.ADMIN).orElseGet(() -> {
            Optional<Auction> auctionOptional = auctionRepository.findById(id);
            if (auctionOptional.isEmpty()) {
                return Response.create("01", "00", "Auction not found", null);
            }

            Auction auction = auctionOptional.get();
            if (isInvalid(auction)) {
                Auction updatedAuction = new Auction(
                    auction.id(),
                    auction.code(),
                    auction.name(),
                    auction.description(),
                    auction.offer(),
                    auction.highestBid(),
                    auction.highestBidderId(),
                    auction.highestBidderName(),
                    Auction.Status.REJECTED,
                    auction.startedAt(),
                    auction.endedAt(),
                    auction.createdBy(),
                    auction.updatedBy(),
                    auction.deletedBy(),
                    auction.createdAt(),
                    auction.updatedAt(),
                    auction.deletedAt()
                );
                auctionRepository.save(updatedAuction);
                
                return Response.create("01", "01", "Auction rejected successfully", null);
            } else {
                return Response.create("01", "02", "Auction is valid", null);
            }
        });
    }

    private boolean isInvalid(Auction auction) {
        return auction.id() == null ||
               isNullOrEmpty(auction.code()) ||
               isNullOrEmpty(auction.name()) ||
               isNullOrEmpty(auction.description()) ||
               auction.offer() == null ||
               auction.highestBid() == null ||
               auction.highestBidderId() == null ||
               isNullOrEmpty(auction.highestBidderName()) ||
               auction.status() == null ||
               auction.startedAt() == null ||
               auction.endedAt() == null ||
               auction.createdBy() == null ||
               auction.updatedBy() == null ||
               auction.deletedBy() == null ||
               auction.createdAt() == null ||
               auction.updatedAt() == null ||
               auction.deletedAt() == null;
    }

    private boolean isNullOrEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }
}
