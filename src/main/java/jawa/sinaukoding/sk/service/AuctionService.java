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
import jawa.sinaukoding.sk.model.request.SellerCreateAuctionReq;
import jawa.sinaukoding.sk.repository.AuctionRepo;

import java.math.BigInteger;
import java.time.OffsetDateTime;
import java.util.UUID;

import jawa.sinaukoding.sk.entity.Auction;

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

    public Response<Object> auctionCreate(Authentication authentication, SellerCreateAuctionReq req){
        return precondition(authentication, User.Role.SELLER).orElseGet(()->{
            if(req.maximumPrice().compareTo(req.minimumPrice()) <= 0){
                return Response.create("40","00","maximum price lebih kecil dari minimum price",null);
            }

            if(req.maximumPrice().compareTo(BigInteger.ZERO) <= 0 || req.minimumPrice().compareTo(BigInteger.ZERO) <= 0){
                return Response.create("40","00","maximum price atau minimum price kurang dari 1",null);
            }

            OffsetDateTime startAt = OffsetDateTime.parse(req.startedAt());
            OffsetDateTime endAt = OffsetDateTime.parse(req.endedAt());
            BigInteger offerPrice = req.maximumPrice().subtract(req.minimumPrice()).divide(BigInteger.TWO);

            Auction auction = new Auction(
                null, 
                UUID.randomUUID().toString().substring(0,8).toUpperCase(),
                req.name(), 
                req.description(),
                offerPrice, 
                offerPrice, 
                0L, 
                "", 
                Auction.Status.WAITING_FOR_APPROVAL, 
                startAt, 
                endAt, 
                authentication.id(), 
                null, 
                null, 
                null, 
                null, 
                null);

            if(auction.startedAt().isAfter(auction.endedAt()) || auction.startedAt().equals(auction.endedAt())){
                return Response.create("40","00","set waktu lelang dengan benar",null);
            }

            if(auction.startedAt().toLocalDate().equals(OffsetDateTime.now().toLocalDate()) || auction.startedAt().isBefore(OffsetDateTime.now())){
                return Response.create("40","00","waktu lelang tidak boleh kurang atau sama dengan hari ini",null);
            }

            if(auction.offer().compareTo(BigInteger.valueOf(1000)) <= 0){
                return Response.badRequest();
            }

            if(auction.name().length() < 5){
                return Response.create("40","00","masukan nama lelang dengan benar",null);
            }

            if(auction.description().length() < 20){
                return Response.create("40","00","masukan deskripsi lelang dengan benar",null);
            }

           Long auctionRepo = auctionRepository.saveAuction(auction);

           if(auctionRepo == 0L){
                return Response.create("40", "00", "gagal save auction", null);
           }

           return Response.create("20", "01", "sukses membuat pengajuan lelang", auctionRepo);
        });

    }
}
