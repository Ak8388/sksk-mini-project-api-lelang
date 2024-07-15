package jawa.sinaukoding.sk.service;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;

import java.math.BigInteger;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.assertj.core.data.Offset;
import org.junit.jupiter.api.Assertions;

import jawa.sinaukoding.sk.repository.AuctionRepo;
import jawa.sinaukoding.sk.repository.UserRepository;
import jawa.sinaukoding.sk.entity.Auction;
import jawa.sinaukoding.sk.entity.User;
import jawa.sinaukoding.sk.model.Authentication;
import jawa.sinaukoding.sk.model.Response;
import jawa.sinaukoding.sk.model.request.SellerCreateAuctionReq;
import jawa.sinaukoding.sk.model.request.UpdateHightBidReq;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class AuctionTest {
    private static final User ADMIN = new User(1L, //
            "ADMIN", //
            "ADMIN@EXAMPLE.com", //
            "PASSWORD", //
            User.Role.ADMIN, //
            0L, //
            null, //
            null, //
            OffsetDateTime.now(), //
            null, //
            null); //

            private static final User SALLER = new User(3L, //
            "ADMIN", //
            "ADMIN@EXAMPLE.com", //
            "PASSWORD", //
            User.Role.SELLER, //
            0L, //
            null, //
            null, //
            OffsetDateTime.now(), //
            null, //
            null); //

    @MockBean
    private AuctionRepo auctionRepo;

    @MockBean
    private UserRepository userRepository;

    @Autowired
    private AuctionService auctionService;

    @MockBean
    private Authentication authentication;
    
    @BeforeEach
    void findAdmin() {
        Mockito.when(userRepository.findById(ArgumentMatchers.eq(1L))).thenReturn(Optional.of(ADMIN));
        Mockito.when(userRepository.findById(ArgumentMatchers.eq(3L))).thenReturn(Optional.of(SALLER));
        Mockito.when(userRepository.findById(ArgumentMatchers.eq(2L))).thenReturn(Optional.of(new User( //
                2L, //
                "Charlie", //
                "charlie@example.com", //
                "alice", //
                User.Role.BUYER, //
                ADMIN.id(), //
                null, //
                null, //
                OffsetDateTime.now(), //
                null, //
                null //
        )));
    }

    @Test
    public void AuctionCreateFailed() {
        final SellerCreateAuctionReq req = new SellerCreateAuctionReq("jual barang orang", "saya menjual barang orang dengan harga start mulai dari 70000 saja", BigInteger.valueOf(10000), BigInteger.valueOf(200), "2025-07-10T14:32:45.123+07:00", "2025-07-10T17:20:45.123+07:00");
        final User admin = userRepository.findById(3L).orElseThrow();
        final Authentication authentication = new Authentication(admin.id(), admin.role(), true);
        Response<Object> response = auctionService.auctionCreate(authentication, req);

        Assertions.assertEquals("4000", response.code());
        Assertions.assertEquals("maximum price lebih kecil dari minimum price", response.message());
    }

    @Test
    public void AuctionCreateFiledStartAtAfterEnd(){
        final SellerCreateAuctionReq req = new SellerCreateAuctionReq("jual barang orang", "saya menjual barang orang dengan harga start mulai dari 70000 saja", BigInteger.valueOf(10000), BigInteger.valueOf(200000), "2025-07-12T14:32:45.123+07:00", "2025-07-10T17:20:45.123+07:00");
        final User admin = userRepository.findById(3L).orElseThrow();
        final Authentication authentication = new Authentication(admin.id(), admin.role(), true);
        Response<Object> response = auctionService.auctionCreate(authentication, req);

        Assertions.assertEquals("4000", response.code());
        Assertions.assertEquals("set waktu lelang dengan benar", response.message());
    }

    public void AuctionCreateFiledStartAtBeforeTimeNow(){
        final SellerCreateAuctionReq req = new SellerCreateAuctionReq("jual barang orang", "saya menjual barang orang dengan harga start mulai dari 70000 saja", BigInteger.valueOf(10000), BigInteger.valueOf(20000), "2025-07-07T14:32:45.123+07:00", "2025-07-10T17:20:45.123+07:00");
        final User admin = userRepository.findById(3L).orElseThrow();
        final Authentication authentication = new Authentication(admin.id(), admin.role(), true);
        Response<Object> response = auctionService.auctionCreate(authentication, req);

        Assertions.assertEquals("4000", response.code());
        Assertions.assertEquals("waktu lelang tidak boleh kurang atau sama dengan hari ini", response.message());
    }

    @Test
    public void AuctionCreateFailedOfferPricetooLow() {
        final SellerCreateAuctionReq req = new SellerCreateAuctionReq("jual barang orang", "saya menjual barang orang dengan harga start mulai dari 70000 saja", BigInteger.valueOf(900), BigInteger.valueOf(1000), "2025-07-10T14:32:45.123+07:00", "2025-07-10T17:20:45.123+07:00");
        final User admin = userRepository.findById(3L).orElseThrow();
        final Authentication authentication = new Authentication(admin.id(), admin.role(), true);
        Response<Object> response = auctionService.auctionCreate(authentication, req);

        Assertions.assertEquals("0301", response.code());
        Assertions.assertEquals("bad request", response.message());
    }

    @Test
    public void AuctionCreateFailedSaveAuction() {
        final SellerCreateAuctionReq req = new SellerCreateAuctionReq("jual barang orang", "saya menjual barang orang dengan harga start mulai dari 70000 saja", BigInteger.valueOf(90000), BigInteger.valueOf(100000), "2025-07-10T14:32:45.123+07:00", "2025-07-10T17:20:45.123+07:00");
        final User admin = userRepository.findById(3L).orElseThrow();
        final Authentication authentication = new Authentication(admin.id(), admin.role(), true);
        Mockito.when(auctionRepo.saveAuction(ArgumentMatchers.any())).thenReturn(0L);
        Response<Object> response = auctionService.auctionCreate(authentication, req);

        Assertions.assertEquals("4000", response.code());
        Assertions.assertEquals("gagal save auction", response.message());
        Assertions.assertEquals(null, response.data());
    }

    @Test
    public void AuctionCreateSuccess() {
        BigInteger minimumPrice = new BigInteger("1000000");
        BigInteger maximumPrice = new BigInteger("9000000");
        final SellerCreateAuctionReq req = new SellerCreateAuctionReq("ini bajuku dah","ini baju gufron legenda kita semuah",minimumPrice,maximumPrice,"2025-07-10T14:32:45.123+02:00","2025-07-11T14:32:45.123+02:00");
        Mockito.when(auctionRepo.saveAuction(ArgumentMatchers.any())).thenReturn(2L);
        final User admin = userRepository.findById(3L).orElseThrow();
        final Authentication authentication = new Authentication(admin.id(), admin.role(), true);
        
        Response<Object> response = auctionService.auctionCreate(authentication, req);

        Assertions.assertNotNull(response);
        Assertions.assertEquals("2001", response.code());
        Assertions.assertEquals("sukses membuat pengajuan lelang",response.message());
        Assertions.assertEquals(2L,response.data());
    }

     @Test 
    void  AuctionRejectedSucces(){

        Auction auction = new Auction(
            1L,
            "code",
            "name",
            "description",
            new BigInteger("1000"),
            new BigInteger("1200"),
            2L,
            "highestBidderName",
            Auction.Status.WAITING_FOR_APPROVAL,
            OffsetDateTime.now(),
            OffsetDateTime.now().plusDays(1),
            1L,
            1L,
            null,
            OffsetDateTime.now(),
            OffsetDateTime.now(),
            null
    );
            Mockito.when(auctionRepo.findById(anyLong())).thenReturn(Optional.of(auction));
            Mockito.when(auctionRepo.ApproveAuction(anyLong())).thenReturn(1L);
            final User admin = userRepository.findById(1L).orElseThrow();
            final Authentication authentication = new Authentication(admin.id(), admin.role(), true);
    
            Response<Object> response = auctionService.rejectAuction(authentication, 1L);
            Assertions.assertEquals("0700", response.code());
            Assertions.assertEquals("Auction rejected", response.message());
    }



    @Test
    public void rejectAuctionBadRequest() {
        Auction auction = new Auction(
            1L,
            "code",
            "name",
            "description",
            new BigInteger("1000"),
            new BigInteger("1200"),
            2L,
            "highestBidderName",
            Auction.Status.REJECTED, 
            OffsetDateTime.now(),
            OffsetDateTime.now().plusDays(1),
            1L,
            1L,
            null,
            OffsetDateTime.now(),
            OffsetDateTime.now(),
            null
        );

        Mockito.when(auctionRepo.findById(anyLong())).thenReturn(Optional.of(auction));
        final User admin = userRepository.findById(1L).orElseThrow();
        final Authentication authentication = new Authentication(admin.id(), admin.role(), true);
        Response<Object> response = auctionService.rejectAuction(authentication, 1L);
        Assertions.assertEquals("0301", response.code());
        Assertions.assertEquals("bad request", response.message()); 

    }

    
    @Test
    public void updateHigestBidAndInsertBidTableSuccessTest(){
        Auction auction = new Auction(
            2L, 
            UUID.randomUUID().toString().substring(0,8).toUpperCase(),
            "Dukron", 
            "Dukron di adol gah cah ikih ceg ana",
            BigInteger.valueOf(30000), 
            BigInteger.valueOf(300000), 
            1L, 
            "Dukron", 
            Auction.Status.APPROVED, 
            OffsetDateTime.parse("2024-07-09T14:32:45.123+07:00"), 
            OffsetDateTime.parse("2025-07-12T14:32:45.123+07:00"), 
            2L, 
            null, 
            null, 
            OffsetDateTime.now(ZoneOffset.UTC), 
            null, 
            null);
    
        Optional<Auction> optAuct = Optional.of(auction);

        final UpdateHightBidReq req = new UpdateHightBidReq(auction.id(),BigInteger.valueOf(100000000));
        Mockito.when(auctionRepo.findById(ArgumentMatchers.any())).thenReturn(optAuct);

        final User admin = userRepository.findById(2L).orElseThrow();

        final Authentication authentication = new Authentication(admin.id(), admin.role(), true);
        Mockito.when(auctionRepo.updateHigestBidAndInsertBidTable(ArgumentMatchers.any(), ArgumentMatchers.any())).thenReturn(2L);

        Response<Object> response = auctionService.updateHigestBidAndInsertBidTable(authentication, req);

        Assertions.assertEquals("0107", response.code());
        Assertions.assertEquals("sukses bid lelang", response.message());        
    }

    @Test
    public void updateHigestBidAndInsertBidTableFailedTest(){
        Mockito.when(auctionRepo.findById(ArgumentMatchers.any())).thenReturn(Optional.empty());
        final UpdateHightBidReq req = new UpdateHightBidReq(1L,BigInteger.valueOf(100000000));

        final User admin = userRepository.findById(2L).orElseThrow();

        final Authentication authentication = new Authentication(admin.id(), admin.role(), true);
        Mockito.when(auctionRepo.updateHigestBidAndInsertBidTable(ArgumentMatchers.any(), ArgumentMatchers.any())).thenReturn(2L);

        Response<Object> response = auctionService.updateHigestBidAndInsertBidTable(authentication, req);

        Assertions.assertEquals("0206", response.code());
        Assertions.assertEquals("auction is empty", response.message());        
    }

    @Test
    public void updateHigestBidAndInsertBidTableFailedNotApproveSttsTest(){
        Auction auction = new Auction(
            2L, 
            UUID.randomUUID().toString().substring(0,8).toUpperCase(),
            "Dukron", 
            "Dukron di adol gah cah ikih ceg ana",
            BigInteger.valueOf(30000), 
            BigInteger.valueOf(300000), 
            1L, 
            "Dukron", 
            Auction.Status.WAITING_FOR_APPROVAL, 
            OffsetDateTime.parse("2024-07-09T14:32:45.123+07:00"), 
            OffsetDateTime.parse("2025-07-12T14:32:45.123+07:00"), 
            2L, 
            null, 
            null, 
            OffsetDateTime.now(ZoneOffset.UTC), 
            null, 
            null);

        Optional<Auction> optAuc = Optional.of(auction);

        Mockito.when(auctionRepo.findById(ArgumentMatchers.any())).thenReturn(optAuc);
        final UpdateHightBidReq req = new UpdateHightBidReq(1L,BigInteger.valueOf(100000000));

        final User admin = userRepository.findById(2L).orElseThrow();

        final Authentication authentication = new Authentication(admin.id(), admin.role(), true);

        Response<Object> response = auctionService.updateHigestBidAndInsertBidTable(authentication, req);

        Assertions.assertEquals("0103", response.code());
        Assertions.assertEquals("tidak bisa bid kepada barang yang belum atau tidak di approve", response.message());        
    }

    @Test
    public void updateHigestBidAndInsertBidTableFailedNotYetStartedTest(){
        Auction auction = new Auction(
            2L, 
            UUID.randomUUID().toString().substring(0,8).toUpperCase(),
            "Dukron", 
            "Dukron di adol gah cah ikih ceg ana",
            BigInteger.valueOf(30000), 
            BigInteger.valueOf(300000), 
            1L, 
            "Dukron", 
            Auction.Status.APPROVED, 
            OffsetDateTime.parse("2024-07-01T14:32:45.123+07:00"), 
            OffsetDateTime.parse("2024-07-11T14:32:45.123+07:00"), 
            2L, 
            null, 
            null, 
            OffsetDateTime.now(ZoneOffset.UTC), 
            null, 
            null);

        Optional<Auction> optAuc = Optional.of(auction);

        Mockito.when(auctionRepo.findById(ArgumentMatchers.any())).thenReturn(optAuc);
        final UpdateHightBidReq req = new UpdateHightBidReq(1L,BigInteger.valueOf(100000000));

        final User admin = userRepository.findById(2L).orElseThrow();

        final Authentication authentication = new Authentication(admin.id(), admin.role(), true);

        Response<Object> response = auctionService.updateHigestBidAndInsertBidTable(authentication, req);

        Assertions.assertEquals("0206", response.code());
        Assertions.assertEquals("lelang belum di mulai atau sudah selesai", response.message());        
    }

    @Test
    public void updateHigestBidAndInsertBidTableFailedHigstBidStartedTest(){
        Auction auction = new Auction(
            2L, 
            UUID.randomUUID().toString().substring(0,8).toUpperCase(),
            "Dukron", 
            "Dukron di adol gah cah ikih ceg ana",
            BigInteger.valueOf(30000), 
            BigInteger.valueOf(300000), 
            1L, 
            "Dukron", 
            Auction.Status.APPROVED, 
            OffsetDateTime.parse("2024-07-09T14:32:45.123+07:00"), 
            OffsetDateTime.parse("2025-07-12T14:32:45.123+07:00"), 
            2L, 
            null, 
            null, 
            OffsetDateTime.now(ZoneOffset.UTC), 
            null, 
            null);

        Optional<Auction> optAuc = Optional.of(auction);

        Mockito.when(auctionRepo.findById(ArgumentMatchers.any())).thenReturn(optAuc);
        final UpdateHightBidReq req = new UpdateHightBidReq(1L,BigInteger.valueOf(1000));

        final User admin = userRepository.findById(2L).orElseThrow();

        final Authentication authentication = new Authentication(admin.id(), admin.role(), true);

        Response<Object> response = auctionService.updateHigestBidAndInsertBidTable(authentication, req);

        Assertions.assertEquals("0306", response.code());
        Assertions.assertEquals("highest bid request must be her", response.message());        
    }

    @Test
    public void updateHigestBidAndInsertBidTableFailedUserEmptyTest(){
        Auction auction = new Auction(
            2L, 
            UUID.randomUUID().toString().substring(0,8).toUpperCase(),
            "Dukron", 
            "Dukron di adol gah cah ikih ceg ana",
            BigInteger.valueOf(30000), 
            BigInteger.valueOf(300000), 
            1L, 
            "Dukron", 
            Auction.Status.APPROVED, 
            OffsetDateTime.parse("2024-07-09T14:32:45.123+07:00"), 
            OffsetDateTime.parse("2025-07-12T14:32:45.123+07:00"), 
            2L, 
            null, 
            null, 
            OffsetDateTime.now(ZoneOffset.UTC), 
            null, 
            null);

        Optional<Auction> optAuc = Optional.of(auction);

        Mockito.when(auctionRepo.findById(ArgumentMatchers.any())).thenReturn(optAuc);
        final UpdateHightBidReq req = new UpdateHightBidReq(1L,BigInteger.valueOf(10000000));

        Mockito.when(userRepository.findById(ArgumentMatchers.any())).thenReturn(Optional.empty());

        final Authentication authentication = new Authentication(2L, User.Role.BUYER, true);

        Response<Object> response = auctionService.updateHigestBidAndInsertBidTable(authentication, req);

        Assertions.assertEquals("0107", response.code());
        Assertions.assertEquals("user is empty", response.message());        
    }

    @Test
    public void updateHigestBidAndInsertBidTableFailedUserHasBenDeletedTest(){
        Auction auction = new Auction(
            2L, 
            UUID.randomUUID().toString().substring(0,8).toUpperCase(),
            "Dukron", 
            "Dukron di adol gah cah ikih ceg ana",
            BigInteger.valueOf(30000), 
            BigInteger.valueOf(300000), 
            1L, 
            "Dukron", 
            Auction.Status.APPROVED, 
            OffsetDateTime.parse("2024-07-09T14:32:45.123+07:00"), 
            OffsetDateTime.parse("2025-07-12T14:32:45.123+07:00"), 
            2L, 
            null, 
            null, 
            OffsetDateTime.now(ZoneOffset.UTC), 
            null, 
            null
            );

        User user = new User(null, null, null, null, null, null, null, null, null, null, OffsetDateTime.now(ZoneOffset.UTC));

        Optional<Auction> optAuc = Optional.of(auction);
        Optional<User> optUser = Optional.of(user);

        Mockito.when(auctionRepo.findById(ArgumentMatchers.any())).thenReturn(optAuc);
        final UpdateHightBidReq req = new UpdateHightBidReq(1L,BigInteger.valueOf(10000000));

        Mockito.when(userRepository.findById(ArgumentMatchers.any())).thenReturn(optUser);

        Mockito.when(auctionRepo.updateHigestBidAndInsertBidTable(ArgumentMatchers.any(),ArgumentMatchers.any())).thenReturn(1L);

        final Authentication authentication = new Authentication(2L, User.Role.BUYER, true);

        Response<Object> response = auctionService.updateHigestBidAndInsertBidTable(authentication, req);

        Assertions.assertEquals("0405", response.code());
        Assertions.assertEquals("user sudah di hapus", response.message());        
    }

    @Test
    public void updateHigestBidAndInsertBidTableFailedRepo(){
        Auction auction = new Auction(
            2L, 
            UUID.randomUUID().toString().substring(0,8).toUpperCase(),
            "Dukron", 
            "Dukron di adol gah cah ikih ceg ana",
            BigInteger.valueOf(30000), 
            BigInteger.valueOf(300000), 
            1L, 
            "Dukron", 
            Auction.Status.APPROVED, 
            OffsetDateTime.parse("2024-07-09T14:32:45.123+07:00"), 
            OffsetDateTime.parse("2025-07-12T14:32:45.123+07:00"), 
            2L, 
            null, 
            null, 
            OffsetDateTime.now(ZoneOffset.UTC), 
            null, 
            null
            );

        Optional<Auction> optAuc = Optional.of(auction);

        Mockito.when(auctionRepo.findById(ArgumentMatchers.any())).thenReturn(optAuc);
        final UpdateHightBidReq req = new UpdateHightBidReq(1L,BigInteger.valueOf(10000000));

        final User saller = userRepository.findById(2L).orElseThrow();

        Mockito.when(auctionRepo.updateHigestBidAndInsertBidTable(ArgumentMatchers.any(),ArgumentMatchers.any())).thenReturn(0L);

        final Authentication authentication = new Authentication(saller.id(), saller.role(), true);

        Response<Object> response = auctionService.updateHigestBidAndInsertBidTable(authentication, req);

        Assertions.assertEquals("0302", response.code());
        Assertions.assertEquals("gagal update auction", response.message());        
    }

    @Test
    public void updateHigestBidAndInsertBidTableFailedAuctionHasBenDeletedTest(){
        Auction auction = new Auction(
            2L, 
            UUID.randomUUID().toString().substring(0,8).toUpperCase(),
            "Dukron", 
            "Dukron di adol gah cah ikih ceg ana",
            BigInteger.valueOf(30000), 
            BigInteger.valueOf(300000), 
            1L, 
            "Dukron", 
            Auction.Status.APPROVED, 
            OffsetDateTime.parse("2024-07-09T14:32:45.123+07:00"), 
            OffsetDateTime.parse("2025-07-12T14:32:45.123+07:00"), 
            2L, 
            null, 
            null, 
            OffsetDateTime.now(ZoneOffset.UTC), 
            null, 
            OffsetDateTime.now(ZoneOffset.UTC));

        Optional<Auction> optAuc = Optional.of(auction);

        Mockito.when(auctionRepo.findById(ArgumentMatchers.any())).thenReturn(optAuc);
        final UpdateHightBidReq req = new UpdateHightBidReq(1L,BigInteger.valueOf(1000000));

        final User admin = userRepository.findById(2L).orElseThrow();

        final Authentication authentication = new Authentication(admin.id(), admin.role(), true);

        Response<Object> response = auctionService.updateHigestBidAndInsertBidTable(authentication, req);

        Assertions.assertEquals("0301", response.code());
        Assertions.assertEquals("bad request", response.message());        
    }


    @Test
    public void approveAuctionSucces() {
        Auction auction = new Auction(
            1L,
            "code",
            "name",
            "description",
            new BigInteger("1000"),
            new BigInteger("1200"),
            2L,
            "highestBidderName",
            Auction.Status.WAITING_FOR_APPROVAL,
            OffsetDateTime.now(),
            OffsetDateTime.now().plusDays(1),
            1L,
            1L,
            null,
            OffsetDateTime.now(),
            OffsetDateTime.now(),
            null
    );
            Mockito.when(auctionRepo.findById(anyLong())).thenReturn(Optional.of(auction));
            Mockito.when(auctionRepo.ApproveAuction(anyLong())).thenReturn(1L);
            final User admin = userRepository.findById(1L).orElseThrow();
            final Authentication authentication = new Authentication(admin.id(), admin.role(), true);
    
            Response<Object> response = auctionService.ApproveAuction(authentication, 1L);
            Assertions.assertEquals("0101", response.code());
            Assertions.assertEquals("Auction Approved successfully", response.message());
            Assertions.assertEquals(1L, response.data());
        }

    @Test
    public void approveAuctionBadRequest() {
        Auction auction = new Auction(
            1L,
            "code",
            "name",
            "description",
            new BigInteger("1000"),
            new BigInteger("1200"),
            2L,
            "highestBidderName",
            Auction.Status.APPROVED, // Auction already approved
            OffsetDateTime.now(),
            OffsetDateTime.now().plusDays(1),
            1L,
            1L,
            null,
            OffsetDateTime.now(),
            OffsetDateTime.now(),
            null
        );

        Mockito.when(auctionRepo.findById(anyLong())).thenReturn(Optional.of(auction));
        final User admin = userRepository.findById(1L).orElseThrow();
        final Authentication authentication = new Authentication(admin.id(), admin.role(), true);
        Response<Object> response = auctionService.ApproveAuction(authentication, 1L);
        Assertions.assertEquals("0301", response.code());
        Assertions.assertEquals("bad request", response.message()); 
    }

    @Test
    public void SuccessListAuctionTest(){
        Auction auction = new Auction(
            1L, 
            null,
            "Jsutin", 
            "Justin Bibeh", 
            BigInteger.valueOf(1000000), 
            BigInteger.valueOf(12000000), 
            2L, 
            "Joko Anwar", 
            Auction.Status.APPROVED, 
            OffsetDateTime.now(), 
            OffsetDateTime.now(), 
            null, 
            null, 
            null, 
            null, 
            null, 
            null);

        List<Auction> listAuct = List.of(auction);

        Authentication auth = new Authentication(1L,User.Role.ADMIN, true);

        Mockito.when(auctionRepo.listAuction(1, 10, auction.status().toString())).thenReturn(listAuct);

        Mockito.when(auctionRepo.countData(ArgumentMatchers.any())).thenReturn(3L);

        Response<Object> res = auctionService.listAuction(auth, 1, 10, "APPROVED");

        Assertions.assertNotNull(res);
        Assertions.assertEquals("2001", res.code());
        Assertions.assertEquals("success get data", res.message());
    }

    @Test
    public void ListAuctionFailed(){
        Authentication authentication = new Authentication(1L, User.Role.ADMIN, true);
        Response<Object> res1 = auctionService.listAuction(authentication, 0, 0, "APPROVED");

        Assertions.assertNotNull(res1);
        Assertions.assertEquals("0206",res1.code());
        Assertions.assertEquals("size atau page tidak boleh kosong",res1.message());

        Response<Object> res2 = auctionService.listAuction(authentication, 1, 10, "         ");
        
        Assertions.assertEquals("0207",res2.code());
        Assertions.assertEquals("status tidak boleh kosong",res2.message());
    }
}