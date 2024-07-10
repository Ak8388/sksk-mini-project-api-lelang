package jawa.sinaukoding.sk.service;

import jawa.sinaukoding.sk.entity.User;
import jawa.sinaukoding.sk.model.Authentication;
import jawa.sinaukoding.sk.model.request.LoginReq;
import jawa.sinaukoding.sk.model.request.RegisterBuyerReq;
import jawa.sinaukoding.sk.model.Response;
import jawa.sinaukoding.sk.model.request.RegisterSellerReq;
import jawa.sinaukoding.sk.model.request.ResetPasswordReq;
import jawa.sinaukoding.sk.model.response.UserDto;
import jawa.sinaukoding.sk.repository.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@SpringBootTest
class UserServiceTest {

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

    @MockBean
    private UserRepository userRepository;

    @Autowired
    private UserService userService;
    @MockBean
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void findAdmin() {
        Mockito.when(userRepository.findById(ArgumentMatchers.eq(1L))).thenReturn(Optional.of(ADMIN));
        Mockito.when(userRepository.findById(ArgumentMatchers.eq(2L))).thenReturn(Optional.of(new User( //
                2L, //
                "Charlie", //
                "charlie", //
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
    void registerSeller() {
        final RegisterSellerReq req = new RegisterSellerReq("Charlie", "charlie", "alice");
        Mockito.when(userRepository.saveSeller(ArgumentMatchers.any())).thenReturn(2L);
        final User admin = userRepository.findById(1L).orElseThrow();
        final Authentication authentication = new Authentication(admin.id(), admin.role(), true);
        final Response<Object> response = userService.registerSeller(authentication, req);
        Assertions.assertNotNull(response);
        Assertions.assertEquals("0500", response.code());
        Assertions.assertEquals("Sukses", response.message());
        Assertions.assertEquals(2L, response.data());
    }


    @Test
    void registerSellerBadRequest() {
        final User admin = userRepository.findById(1L).orElseThrow();
        final Authentication authentication = new Authentication(admin.id(), admin.role(), true);
        final Response<Object> response1 = userService.registerSeller(authentication, null);
        Assertions.assertNotNull(response1);
        Assertions.assertEquals("0301", response1.code());
        Assertions.assertEquals("bad request", response1.message());
        Assertions.assertNull(response1.data());
    }

    @Test
    void registerSellerFailed() {
        final RegisterSellerReq req = new RegisterSellerReq("Charlie", "charlie", "alice");
        Mockito.when(userRepository.saveSeller(ArgumentMatchers.any())).thenReturn(0L);
        final User admin = userRepository.findById(1L).orElseThrow();
        final Authentication authentication = new Authentication(admin.id(), admin.role(), true);
        final Response<Object> response = userService.registerSeller(authentication, req);
        Assertions.assertNotNull(response);
        Assertions.assertEquals("0501", response.code());
        Assertions.assertEquals("Gagal mendaftarkan seller", response.message());
        Assertions.assertNull(response.data());
    }

    @Test
    void registerBuyer() {
        final RegisterBuyerReq req = new RegisterBuyerReq("Charlie", "charlie", "alice");
        Mockito.when(userRepository.saveBuyer(ArgumentMatchers.any())).thenReturn(2L);
        final User admin = userRepository.findById(1L).orElseThrow();
        final Authentication authentication = new Authentication(admin.id(), admin.role(), true);
        final Response<Object> response = userService.registerBuyer(authentication, req);
        Assertions.assertNotNull(response);
        Assertions.assertEquals("0600", response.code());
        Assertions.assertEquals("Sukses", response.message());
        Assertions.assertEquals(2L, response.data());
    }

    @Test
    void registerBuyerBadRequest() {
        final User admin = userRepository.findById(1L).orElseThrow();
        final Authentication authentication = new Authentication(admin.id(), admin.role(), true);
        final Response<Object> response1 = userService.registerBuyer(authentication, null);
        Assertions.assertNotNull(response1);
        Assertions.assertEquals("0301", response1.code());
        Assertions.assertEquals("bad request", response1.message());
        Assertions.assertNull(response1.data());
    }

    @Test
    void registerBuyerFailed() {
        final RegisterBuyerReq req = new RegisterBuyerReq("Charlie", "charlie", "alice");
        Mockito.when(userRepository.saveSeller(ArgumentMatchers.any())).thenReturn(0L);
        final User admin = userRepository.findById(1L).orElseThrow();
        final Authentication authentication = new Authentication(admin.id(), admin.role(), true);
        final Response<Object> response = userService.registerBuyer(authentication, req);
        Assertions.assertNotNull(response);
        Assertions.assertEquals("0601", response.code());
        Assertions.assertEquals("Gagal mendaftarkan buyer", response.message());
        Assertions.assertNull(response.data());
    }

    @Test
    void loginBadRequest() {
        final Response<Object> response = userService.login(null);
        Assertions.assertEquals(Response.badRequest().code(), response.code());
        Assertions.assertEquals(Response.badRequest().message(), response.message());
    }

    @Test
    void loginUserNotFound() {
        Mockito.when(userRepository.findByEmail(ArgumentMatchers.anyString())).thenReturn(Optional.empty());
        final Response<Object> response = userService.login(new LoginReq("charlie@sksk.com", "12345678"));
        Assertions.assertEquals("0801", response.code());
        Assertions.assertEquals("Email atau password salah", response.message());
    }

    @Test
    void loginWrongPassword() {
        User user = Mockito.mock(User.class);
        Mockito.when(user.id()).thenReturn(1L);
        Mockito.when(user.role()).thenReturn(User.Role.ADMIN);
        Mockito.when(user.password()).thenReturn("234234234");

        Mockito.when(userRepository.findByEmail(ArgumentMatchers.anyString())).thenReturn(Optional.of(user));
        final Response<Object> response = userService.login(new LoginReq("charlie@sksk.com", "12345678"));
        Assertions.assertEquals("0802", response.code());
        Assertions.assertEquals("Email atau password salah", response.message());
    }
     

    @Test
    void listUserBadRequest() {
        final User admin = userRepository.findById(1L).orElseThrow();
        final Authentication authentication = new Authentication(admin.id(), admin.role(), true);

        Response<Object> response1 = userService.listUsers(authentication, 0, 10);
        Assertions.assertEquals(Response.badRequest().code(), response1.code());
        Assertions.assertEquals(Response.badRequest().message(), response1.message());

        Response<Object> response2 = userService.listUsers(authentication, -1, 10);
        Assertions.assertEquals(Response.badRequest().code(), response2.code());
        Assertions.assertEquals(Response.badRequest().message(), response2.message());

        Response<Object> response3 = userService.listUsers(authentication, 1, 0);
        Assertions.assertEquals(Response.badRequest().code(), response3.code());
        Assertions.assertEquals(Response.badRequest().message(), response3.message());

        Response<Object> response4 = userService.listUsers(authentication, 1, -1);
        Assertions.assertEquals(Response.badRequest().code(), response4.code());
        Assertions.assertEquals(Response.badRequest().message(), response4.message());
    }

    @Test
    void listUser() {
        final User admin = userRepository.findById(1L).orElseThrow();
        Mockito.when(userRepository.listUsers(1, 10)).thenReturn(List.of(admin));
        final Authentication authentication = new Authentication(admin.id(), admin.role(), true);

        Response<Object> response = userService.listUsers(authentication, 1, 10);
        Assertions.assertEquals("0900", response.code());
        Assertions.assertEquals("Sukses", response.message());
    }


    @Test
    void resetPassword_WrongOldPassword() {
        User user = new User(1L, "Krise", "krise@gmail.com", new BCryptPasswordEncoder().encode("oldPassword"), User.Role.BUYER, null, null, null, OffsetDateTime.now(), null, null);
        Mockito.when(userRepository.findById(user.id())).thenReturn(Optional.of(user));
        Mockito.when(passwordEncoder.matches("oldPassword", user.password())).thenReturn(false);

        Authentication authentication = new Authentication(user.id(), user.role(), true);

        ResetPasswordReq req = new ResetPasswordReq("oldPassword", "newPassword");

        Response<Object> response = userService.resetPassword(authentication, req, 1L);

        Assertions.assertEquals("0703", response.code());
        Assertions.assertEquals("Old password is incorrect", response.message());
        verify(userRepository, never()).updatePassword(anyLong(), anyString());
    }

    @Test
    void resetPassword_SameOldAndNewPassword() {
        User user = new User(1L, "Krise", "krise@gmail.com", new BCryptPasswordEncoder().encode("oldPassword"), User.Role.SELLER, null, null, null, OffsetDateTime.now(), null, null);
        Mockito.when(userRepository.findById(user.id())).thenReturn(Optional.of(user));
        Mockito.when(passwordEncoder.matches("oldPassword", user.password())).thenReturn(true);
        Mockito.when(passwordEncoder.matches("newPassword", user.password())).thenReturn(true);

        Authentication authentication = new Authentication(user.id(), user.role(), true);

        ResetPasswordReq req = new ResetPasswordReq("oldPassword", "newPassword");

        Response<Object> response = userService.resetPassword(authentication, req, null);

        Assertions.assertEquals("0704", response.code());
        Assertions.assertEquals("New password cannot be the same as the old password", response.message());
        verify(userRepository, never()).updatePassword(anyLong(), anyString());
    }

    @Test
    void resetPassword_UserNotFound() {
        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.empty());

        final Authentication authentication = new Authentication(1L, User.Role.BUYER, true);

        ResetPasswordReq req = new ResetPasswordReq("oldPassword", "newPassword");

        Response<Object> response = userService.resetPassword(authentication, req, null);

        Assertions.assertEquals("0701", response.code());
        Assertions.assertEquals("user not found", response.message());
        verify(userRepository, never()).updatePassword(anyLong(), anyString());
    }

    @Test
    void resetPassword_AccountDeleted() {
        User user = new User(1L, "krise", "krise@gmail.com", new BCryptPasswordEncoder().encode("oldPassword"), User.Role.SELLER, null, null, 1L, OffsetDateTime.now(), null, OffsetDateTime.now());
        Mockito.when(userRepository.findById(user.id())).thenReturn(Optional.of(user));

        final Authentication authentication = new Authentication(user.id(), user.role(), true);

        ResetPasswordReq req = new ResetPasswordReq("oldPassword", "newPassword");

        Response<Object> response = userService.resetPassword(authentication, req, null);

        Assertions.assertEquals("0706", response.code());
        Assertions.assertEquals("Account has been deleted", response.message());
        verify(userRepository, never()).updatePassword(anyLong(), anyString());
    }

    @Test
    void resetPassword_BadRequest() {
        final Authentication authentication = new Authentication(1L, User.Role.SELLER, true);

        Response<Object> response = userService.resetPassword(authentication, null, null);

        Assertions.assertEquals("0301", response.code());
        Assertions.assertEquals("bad request", response.message());
    }

    
    @Test
public void ResetPassword_Success() {

    ResetPasswordReq req = Mockito.mock(ResetPasswordReq.class);
    Mockito.when(req.newPassword()).thenReturn("newPassword123");
    Mockito.when(req.oldPassword()).thenReturn("oldPassword123");
    
    // final User user = Mockito.mock(User.class);
    // Mockito.when(user.id()).thenReturn(1L);
    // Mockito.when(user.password()).thenReturn("encodedOldPassword");
    // Mockito.when(user.name()).thenReturn("testUser");
    // Mockito.when(user.role()).thenReturn(User.Role.SELLER);
    // Mockito.when(user.deletedAt()).thenReturn(null);
    // Mockito.when(user.deletedBy()).thenReturn(null);

      User user = new User(1L,"nama","email@gmail","$2a$10$uIBEJjpFQcMlzSM40cBOBOKrmd6QdcHiPiwtBjm/WjMkrsHgarVTO",User.Role.SELLER, 2L,2L,null,OffsetDateTime.parse("2024-07-08T14:32:45.123+02:00"),OffsetDateTime.parse("2024-07-08T14:30:45.123+02:00") ,null);

        Optional <User> user2 = Optional.of(user);

    final Authentication authentication = new Authentication(user.id(), user.role(), true);
    Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(user));
    Mockito.when(passwordEncoder.matches("oldPassword123", "encodedOldPassword")).thenReturn(true);
    Mockito.when(passwordEncoder.matches("newPassword123", "encodedOldPassword")).thenReturn(false);
    Mockito.when(passwordEncoder.encode("newPassword123")).thenReturn("encodedNewPassword");
    Mockito.when(userRepository.updatePassword(1L, "encodedNewPassword")).thenReturn(1L);

    Response<Object> response = userService.resetPassword(authentication, req, 1L);
  
    Assertions.assertNotNull(response, "Response should not be null");
    Assertions.assertEquals("0700", response.code());
    Assertions.assertEquals("Success", response.message());
 
    Mockito.verify(userRepository, Mockito.times(2)).findById(1L);
    Mockito.verify(passwordEncoder).matches("oldPassword123", "encodedOldPassword");
    Mockito.verify(passwordEncoder).matches("newPassword123", "encodedOldPassword");
    Mockito.verify(passwordEncoder).encode("newPassword123");
    Mockito.verify(userRepository).updatePassword(1L, "encodedNewPassword");



        //  User user1 = new User(1L,"nama","email@gmail","$2a$10$uIBEJjpFQcMlzSM40cBOBOKrmd6QdcHiPiwtBjm/WjMkrsHgarVTO",User.Role.SELLER, 2L,2L,null,OffsetDateTime.parse("2024-07-08T14:32:45.123+02:00"),OffsetDateTime.parse("2024-07-08T14:30:45.123+02:00") ,null);

        //  Optional <User> user2 = Optional.of(user1);

        // Mockito.when(userRepository.findById(ArgumentMatchers.any())).thenReturn(user2);

        // ResetPasswordReq req = new ResetPasswordReq("12345678", "87654321");

        // final Authentication authentication = new Authentication(1L, User.Role.SELLER, true);
        // Mockito.when(passwordEncoder.encode("newPassword123")).thenReturn("encodedNewPassword");
        // Mockito.when(userRepository.updatePassword(1L, "encodedNewPassword")).thenReturn(1L);


        // Mockito.when(userRepository.updatePassword(ArgumentMatchers.any(), ArgumentMatchers.any())).thenReturn(1L);

        // Response <Object> response = userService.resetPassword(authentication, req, 1L);
        // Assertions.assertNotNull(response);
        // Assertions.assertEquals("0700", response.code());
        // Assertions.assertEquals("Success", response.message());



        

    }


}
