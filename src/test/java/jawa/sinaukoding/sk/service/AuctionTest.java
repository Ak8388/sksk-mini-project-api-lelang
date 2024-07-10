package jawa.sinaukoding.sk.service;
import java.math.BigInteger;
import java.time.OffsetDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.MockBeans;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.junit.jupiter.api.Assertions;

import jawa.sinaukoding.sk.repository.AuctionRepo;
import jawa.sinaukoding.sk.repository.UserRepository;
import jawa.sinaukoding.sk.entity.User;
import jawa.sinaukoding.sk.model.Authentication;
import jawa.sinaukoding.sk.model.Response;
import jawa.sinaukoding.sk.model.request.SellerCreateAuctionReq;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class AuctionTest {
    private static final User ADMIN = new User(1L, //
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

    @BeforeEach
    void findAdmin() {
        Mockito.when(userRepository.findById(ArgumentMatchers.eq(1L))).thenReturn(Optional.of(ADMIN));
        Mockito.when(userRepository.findById(ArgumentMatchers.eq(2L))).thenReturn(Optional.of(new User( //
                2L, //
                "Charlie", //
                "charlie@example.com", //
                "alice", //
                User.Role.SELLER, //
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
        final User admin = userRepository.findById(1L).orElseThrow();
        final Authentication authentication = new Authentication(admin.id(), admin.role(), true);
        Response<Object> response = auctionService.auctionCreate(authentication, req);

        Assertions.assertEquals("4000", response.code());
        Assertions.assertEquals("maximum price lebih kecil dari minimum price", response.message());
    }

    @Test
    public void AuctionCreateFiledStartAtAfterEnd(){
        final SellerCreateAuctionReq req = new SellerCreateAuctionReq("jual barang orang", "saya menjual barang orang dengan harga start mulai dari 70000 saja", BigInteger.valueOf(10000), BigInteger.valueOf(200000), "2025-07-12T14:32:45.123+07:00", "2025-07-10T17:20:45.123+07:00");
        final User admin = userRepository.findById(1L).orElseThrow();
        final Authentication authentication = new Authentication(admin.id(), admin.role(), true);
        Response<Object> response = auctionService.auctionCreate(authentication, req);

        Assertions.assertEquals("4000", response.code());
        Assertions.assertEquals("set waktu lelang dengan benar", response.message());
    }

    public void AuctionCreateFiledStartAtBeforeTimeNow(){
        final SellerCreateAuctionReq req = new SellerCreateAuctionReq("jual barang orang", "saya menjual barang orang dengan harga start mulai dari 70000 saja", BigInteger.valueOf(10000), BigInteger.valueOf(20000), "2025-07-07T14:32:45.123+07:00", "2025-07-10T17:20:45.123+07:00");
        final User admin = userRepository.findById(1L).orElseThrow();
        final Authentication authentication = new Authentication(admin.id(), admin.role(), true);
        Response<Object> response = auctionService.auctionCreate(authentication, req);

        Assertions.assertEquals("4000", response.code());
        Assertions.assertEquals("waktu lelang tidak boleh kurang atau sama dengan hari ini", response.message());
    }

    @Test
    public void AuctionCreateFailedOfferPricetooLow() {
        final SellerCreateAuctionReq req = new SellerCreateAuctionReq("jual barang orang", "saya menjual barang orang dengan harga start mulai dari 70000 saja", BigInteger.valueOf(900), BigInteger.valueOf(1000), "2025-07-10T14:32:45.123+07:00", "2025-07-10T17:20:45.123+07:00");
        final User admin = userRepository.findById(1L).orElseThrow();
        final Authentication authentication = new Authentication(admin.id(), admin.role(), true);
        Response<Object> response = auctionService.auctionCreate(authentication, req);

        Assertions.assertEquals("0301", response.code());
        Assertions.assertEquals("bad request", response.message());
    }

    @Test
    public void AuctionCreateFailedSaveAuction() {
        final SellerCreateAuctionReq req = new SellerCreateAuctionReq("jual barang orang", "saya menjual barang orang dengan harga start mulai dari 70000 saja", BigInteger.valueOf(90000), BigInteger.valueOf(100000), "2025-07-10T14:32:45.123+07:00", "2025-07-10T17:20:45.123+07:00");
        final User admin = userRepository.findById(2L).orElseThrow();
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
        final User admin = userRepository.findById(1L).orElseThrow();
        final Authentication authentication = new Authentication(admin.id(), admin.role(), true);
        
        Response<Object> response = auctionService.auctionCreate(authentication, req);

        Assertions.assertNotNull(response);
        Assertions.assertEquals("2001", response.code());
        Assertions.assertEquals("sukses membuat pengajuan lelang",response.message());
        Assertions.assertEquals(2L,response.data());
    }
    
}