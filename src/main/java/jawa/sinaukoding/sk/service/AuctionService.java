package jawa.sinaukoding.sk.service;

import jawa.sinaukoding.sk.entity.User;
import jawa.sinaukoding.sk.model.Authentication;
import jawa.sinaukoding.sk.model.Response;
import jawa.sinaukoding.sk.model.request.SellerCreateAuctionReq;
import jawa.sinaukoding.sk.repository.AuctionRepo;

import java.math.BigInteger;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import org.springframework.stereotype.Service;

import jawa.sinaukoding.sk.entity.Auction;

@Service
public class AuctionService extends AbstractService{
    private final AuctionRepo auctionRepository;

    public AuctionService(final AuctionRepo userRepository) {
        this.auctionRepository = userRepository;
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
                OffsetDateTime.now(ZoneOffset.UTC), 
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
