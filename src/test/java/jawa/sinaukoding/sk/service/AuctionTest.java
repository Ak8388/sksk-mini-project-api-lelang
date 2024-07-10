package jawa.sinaukoding.sk.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.math.BigInteger;
import java.util.Optional;

import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;

import jawa.sinaukoding.sk.repository.AuctionRepo;
import jawa.sinaukoding.sk.entity.Auction;
import jawa.sinaukoding.sk.entity.User;
import jawa.sinaukoding.sk.model.Authentication;
import jawa.sinaukoding.sk.model.Response;
import jawa.sinaukoding.sk.model.request.SellerCreateAuctionReq;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class AuctionTest {
    @Mock
    private AuctionRepo auctionRepo;

    @InjectMocks
    private AuctionService auctionService;

    @Mock
    private SellerCreateAuctionReq req;

    @Mock
    private Authentication authentication;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void AuctionCreateFailed(){
        Mockito.when(req.maximumPrice()).thenReturn(BigInteger.valueOf(100));
        Mockito.when(req.minimumPrice()).thenReturn(BigInteger.valueOf(200));

        Response<Object> response = auctionService.auctionCreate(authentication, null);

        Assertions.assertEquals("4000", response.code());
        Assertions.assertEquals("maximum price lebih kecil dari minimum price", response.message());
    }

    @Test
    void AuctionCreateSuccess(){
        BigInteger minimumPrice = new BigInteger("1000000");
        BigInteger maximumPrice = new BigInteger("9000000");
        final SellerCreateAuctionReq req = new SellerCreateAuctionReq("ini bajuku dah","ini baju gufron legenda kita semuah",minimumPrice,maximumPrice,"2024-07-10T14:32:45.123+02:00","2024-07-10T14:32:45.123+02:00");
        Mockito.when(auctionRepo.saveAuction(ArgumentMatchers.any())).thenReturn(2L);
        
        final Authentication authentication = new Authentication(2L, User.Role.SELLER, true);
        
        Response<Object> response = auctionService.auctionCreate(authentication, req);

        Assertions.assertNotNull(response);
        Assertions.assertEquals("2001", response.code());
        Assertions.assertEquals("sukses membuat pengajuan lelang",response.message());
        Assertions.assertEquals(2L,response.data());
    }
    
    @Test 
    void  AuctionRejectedSucces(){
        Long auctionId = 1L;
        User user = Mockito.mock(User.class);
        Auction auction = Mockito.mock(Auction.class);

        Mockito.when(user.id()).thenReturn(1L);
        Mockito.when(user.role()).thenReturn(User.Role.SELLER);
        Mockito.when(authentication.id()).thenReturn(1L);
        Mockito.when(authentication.role()).thenReturn(User.Role.SELLER);
        Mockito.when(auctionRepo.findById(auctionId)).thenReturn(Optional.of(auction));
        Mockito.when(auction.id()).thenReturn(1L);

        Response <Object> response = auctionService.rejectAuction(authentication, auctionId);
        Assertions.assertNotNull(response,"not null");
        Assertions.assertEquals("0700", response.code());
        Assertions.assertEquals("Auction rejected successfully", response.message());

        verify(auctionRepo).findById(auctionId);
        verify(auctionRepo).RejectedAuction(auctionId);

    }

    @Test
    public void auctionRejectedInvalidAuctionId() {
        Long auctionId = 2L;
        Mockito.when(auctionRepo.findById(auctionId)).thenReturn(Optional.empty());

        Response<Object> response = auctionService.rejectAuction(authentication, auctionId);

        assertNotNull(response, "Response should not be null");
        assertEquals("0704", response.code());
        assertEquals("Auction not found", response.message());
    }



     


}
