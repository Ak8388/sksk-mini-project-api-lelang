package jawa.sinaukoding.sk.service;

import jawa.sinaukoding.sk.model.Response;
import jawa.sinaukoding.sk.model.Authentication;
import jawa.sinaukoding.sk.entity.Auction;
import jawa.sinaukoding.sk.entity.AuctionBid;
import jawa.sinaukoding.sk.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import jawa.sinaukoding.sk.model.request.SellerCreateAuctionReq;
import jawa.sinaukoding.sk.model.request.UpdateHightBidReq;
import jawa.sinaukoding.sk.model.response.AuctionDto;
import jawa.sinaukoding.sk.model.response.AuctionDtoResponse;
import jawa.sinaukoding.sk.repository.AuctionRepo;
import jawa.sinaukoding.sk.repository.UserRepository;

import java.math.BigInteger;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;


@Service
public class AuctionService extends AbstractService {

    @Autowired
    private final AuctionRepo  auctionRepo;

    @Autowired
    private final UserRepository userRepository;

    @Autowired
    public AuctionService(final Environment env, final AuctionRepo auctionRepo,UserRepository userRepository) {
        this.auctionRepo = auctionRepo;
        this.userRepository = userRepository;
    }

    public Response<Object> rejectAuction(final Authentication authentication, Long id) {
        return precondition(authentication, User.Role.ADMIN).orElseGet(() -> {
            Optional<Auction> auctionOptional = auctionRepo.findById(id);
            System.out.println("TERSERAHH");
            if (auctionOptional.isEmpty()) {
                return Response.create("07", "04", "Auction not found", null);
            }
            Auction auction = auctionOptional.get();
            if (auction.status().equals(auction.status().WAITING_FOR_APPROVAL)){
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
                    Long x = auctionRepo.RejectedAuction(id);
                    return Response.create("07", "00", "Auction rejected",x);
              } 
              return Response.create("07", "03", "auction is not invalid", null);
            }
            return Response.badRequest();
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
     

    public Response<Object> listAuction(final Authentication authentication,int page,int size,String stts){
        return precondition(authentication, User.Role.ADMIN).orElseGet(()->{
            if(page <= 0 || size <= 0){
                return Response.create("02", "06", "size atau page tidak boleh kosong", null);
            }

            if(stts.isBlank()){
                return Response.create("02", "07","status tidak boleh kosong", null);
            }

            List<AuctionDto> auctions = auctionRepo.listAuction(page, size, stts) //
            .stream().map(auction -> new AuctionDto(auction.id(),auction.name(),auction.description(),auction.offer(),auction.highestBid(),auction.highestBidderId(),auction.highestBidderName(),auction.status(),auction.startedAt(),auction.endedAt())).toList();

            Long totalData = auctionRepo.countData(stts);

            Long totalPage = totalData/size;
            if(totalPage < 1){
                totalPage+=1;
            }

            AuctionDtoResponse auctionDtoResponse = new AuctionDtoResponse(totalData, totalPage, Long.valueOf(page), Long.valueOf(size), auctions);

            return Response.create("20", "01", "success get data", auctionDtoResponse);
        });
    }

    
    @Transactional
    public Response<Object> updateHigestBidAndInsertBidTable(final Authentication authentication,final UpdateHightBidReq updateHightBidReq){
        return precondition(authentication, User.Role.BUYER).orElseGet(()->{
            Optional<Auction> auction = auctionRepo.findById(updateHightBidReq.auctionID());

            if(auction.isEmpty()){
                return Response.create("02","06","auction is empty",null);
            }
            
            Auction aucGet = auction.get();

            if(aucGet.deletedAt() != null){
                return Response.badRequest();
            }
            
            if(!aucGet.status().equals(Auction.Status.APPROVED)){
                return Response.create("01", "03", "tidak bisa bid kepada barang yang belum atau tidak di approve", null);
            }

            if(OffsetDateTime.now().toLocalDate().isBefore(aucGet.startedAt().toLocalDate()) || OffsetDateTime.now().toLocalDate().isAfter(aucGet.endedAt().toLocalDate())){
                return Response.create("02","06","lelang belum di mulai atau sudah selesai",null);
            }

            if(updateHightBidReq.highestBid().compareTo(aucGet.highestBid()) <= 0 ){
                return Response.create("03","06","highest bid request must be higher",null);            }

            Optional<User> user = userRepository.findById(authentication.id());

            if(user.isEmpty()){
                return Response.create("01","07","user is empty",null);
            }
            
            User useGet = user.get();
            
            if(useGet.deletedAt() != null){
                return Response.create("04","05","user sudah di hapus",null);            
            }

            Auction auction2 = new Auction(aucGet.id(), null, null, null, null, updateHightBidReq.highestBid(), useGet.id(), useGet.name(), null, null, null, null, null, null, null, null, null);
            AuctionBid auctionBid = new AuctionBid(null, aucGet.id(), updateHightBidReq.highestBid(), useGet.id(), OffsetDateTime.now(ZoneOffset.UTC));

            Long auctionRepository = auctionRepo.updateHigestBidAndInsertBidTable(auction2, auctionBid);

            if(auctionRepository == 0L){
                return Response.create("03", "02", "gagal update auction", null);
            }

            return Response.create("01", "07", "sukses bid lelang", updateHightBidReq);
        });
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

            Long auctionRepository = auctionRepo.saveAuction(auction);

            if(auctionRepository == 0L){
                return Response.create("40", "00", "gagal save auction", null);
            }

           return Response.create("20", "01", "sukses membuat pengajuan lelang", auctionRepository);
        });

    }

    public Response<Object> ApproveAuction(final Authentication authentication, Long id) {
        return precondition(authentication, User.Role.ADMIN).orElseGet(() -> {
            Optional<Auction> auctionOptional = auctionRepo.findById(id); 
            Auction auction = auctionOptional.get();
            if (auction.status().equals(auction.status().WAITING_FOR_APPROVAL)){
                if (ifPresent(auction)) {
                    Auction updatedAuction = new Auction(
                        auction.id(),
                        auction.code(),
                        auction.name(),
                        auction.description(),
                        auction.offer(),
                        auction.highestBid(),
                        auction.highestBidderId(),
                        auction.highestBidderName(),
                        Auction.Status.APPROVED,
                        auction.startedAt(),
                        auction.endedAt(),
                        auction.createdBy(),
                        auction.updatedBy(),
                        auction.deletedBy(),
                        auction.createdAt(),
                        auction.updatedAt(),
                        auction.deletedAt()
                    );
                    Long x = auctionRepo.ApproveAuction(id);
                    return Response.create("05", "00", "Auction Approved successfully", x);
              } else {
                return Response.create("05", "01", "cannot Approved", null);
            }
        }
        return Response.badRequest();
         
        });
    }

    private boolean ifPresent(Auction auction) {
        return auction.id() != null ||
               isNotNullOrEmpty(auction.code()) ||
               isNotNullOrEmpty(auction.name()) ||
               isNotNullOrEmpty(auction.description()) ||
               auction.offer() != null ||
               auction.highestBid() != null ||
               auction.highestBidderId() != null ||
               isNotNullOrEmpty(auction.highestBidderName()) ||
               auction.status() != null ||
               auction.startedAt() != null ||
               auction.endedAt() != null ||
               auction.createdBy() != null ||
               auction.updatedBy() != null ||
               auction.deletedBy() != null ||
               auction.createdAt() != null ||
               auction.updatedAt() != null ||
               auction.deletedAt() != null;
    }

    private boolean isNotNullOrEmpty(String str) {
        return str != null && !str.trim().isEmpty();
      }
}
