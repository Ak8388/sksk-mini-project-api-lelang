package jawa.sinaukoding.sk.service;

import jawa.sinaukoding.sk.entity.User;
import jawa.sinaukoding.sk.model.Authentication;
import jawa.sinaukoding.sk.model.request.LoginReq;
import jawa.sinaukoding.sk.model.request.RegisterBuyerReq;
import jawa.sinaukoding.sk.model.Response;
import jawa.sinaukoding.sk.model.request.RegisterSellerReq;
import jawa.sinaukoding.sk.model.request.ResetPasswordReq;
import jawa.sinaukoding.sk.model.request.UpdateProfileReq;
import jawa.sinaukoding.sk.model.request.deleteReq;
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

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
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
    void deleteUser_Succes(){
        //TODO
        deleteReq Delete = new deleteReq(1L);
        final User admin = userRepository.findById(1L).orElseThrow();
        final Authentication authentication = new Authentication(admin.id(), admin.role(), true);
        Mockito.when(userRepository.deleteUser(ArgumentMatchers.any(),ArgumentMatchers.any())).thenReturn(1L);

        Response<Object> response = userService.deletedResponse(authentication, Delete, 2L);
        Assertions.assertEquals("0600", response.code());
        Assertions.assertEquals("Berhasil Menghapus", response.message());
        //Assertions.assertEquals(true, response.data());
    }

    @Test
    void updateProfileUserFailedIdTest(){
        User user = new User(
            2L, 
            "Joko",
            "Jokowidodo@gmail.com",
            null, 
            User.Role.BUYER, 
            1L, 
            2L, 
            null, 
            OffsetDateTime.now(), 
            OffsetDateTime.now(), 
            null
        );

        UpdateProfileReq updateProfileReq = new UpdateProfileReq("jaya jaya jaya", "jayaKusuma@gmail.com");
        Authentication authentication = new Authentication(user.id(), user.role(), true);
        
        Response<Object> updateProf = userService.updateProfile(authentication, updateProfileReq, 0L);

        Assertions.assertNotNull(updateProf);
        Assertions.assertEquals("0301",updateProf.code());
        Assertions.assertEquals("bad request",updateProf.message());
    }

    @Test
    void updateProfileUserFailedDeleteAtNotNullTest(){
        User user = new User(
            2L, 
            "Joko",
            "Jokowidodo@gmail.com",
            null, 
            User.Role.BUYER, 
            1L, 
            2L, 
            null, 
            OffsetDateTime.now(), 
            OffsetDateTime.now(), 
            OffsetDateTime.now()
        );

        UpdateProfileReq updateProfileReq = new UpdateProfileReq("jaya jaya jaya", "jayaKusuma@gmail.com");
        Authentication authentication = new Authentication(user.id(), user.role(), true);

        Optional<User> useOpt = Optional.of(user);
        Mockito.when(userRepository.findById(ArgumentMatchers.any())).thenReturn(useOpt);
        Response<Object> updateProf = userService.updateProfile(authentication, updateProfileReq, 1L);

        Assertions.assertNotNull(updateProf);
        Assertions.assertEquals("0301",updateProf.code());
        Assertions.assertEquals("bad request",updateProf.message());
    }
    
    @Test
    void updateProfileUserFailed(){
        User user = new User(
            2L, 
            "Joko",
            "Jokowidodo@gmail.com",
            null, 
            User.Role.BUYER, 
            1L, 
            2L, 
            null, 
            OffsetDateTime.now(), 
            OffsetDateTime.now(), 
            null
        );

        UpdateProfileReq updateProfileReq = new UpdateProfileReq("jaya jaya jaya", "jayaKusuma@gmail.com");
        Authentication authentication = new Authentication(user.id(), user.role(), true);

        Optional<User> useOpt = Optional.of(user);
        Mockito.when(userRepository.findById(ArgumentMatchers.any())).thenReturn(useOpt);
        Mockito.when(userRepository.updateProfile(ArgumentMatchers.any())).thenReturn(0L);
        Response<Object> updateProf = userService.updateProfile(authentication, updateProfileReq, 1L);

        Assertions.assertNotNull(updateProf);
        Assertions.assertEquals("0601",updateProf.code());
        Assertions.assertEquals("gagal update profile",updateProf.message());
    }

    void updateProfileUserFailedName(){
        User user = new User(
            2L, 
            "Joko",
            "Jokowidodo@gmail.com",
            null, 
            User.Role.BUYER, 
            1L, 
            2L, 
            null, 
            OffsetDateTime.now(), 
            OffsetDateTime.now(), 
            null
        );

        UpdateProfileReq updateProfileReq = new UpdateProfileReq("Joko", "jayaKusuma@gmail.com");
        Authentication authentication = new Authentication(user.id(), user.role(), true);

        Optional<User> useOpt = Optional.of(user);
        Mockito.when(userRepository.findById(ArgumentMatchers.any())).thenReturn(useOpt);
        Response<Object> updateProf = userService.updateProfile(authentication, updateProfileReq, 1L);

        Assertions.assertNotNull(updateProf);
        Assertions.assertEquals("4000",updateProf.code());
        Assertions.assertEquals("gagal update user karena nama yang baru sama dengan nama lama",updateProf.message());
    }

    void updateProfileUserFailedEmail(){
        User user = new User(
            2L, 
            "Joko",
            "Jokowidodo@gmail.com",
            null, 
            User.Role.BUYER, 
            1L, 
            2L, 
            null, 
            OffsetDateTime.now(), 
            OffsetDateTime.now(), 
            null
        );

        UpdateProfileReq updateProfileReq = new UpdateProfileReq("jaya jaya jaya", "Jokowidodo@gmail.com");
        Authentication authentication = new Authentication(user.id(), user.role(), true);

        Optional<User> useOpt = Optional.of(user);
        Mockito.when(userRepository.findById(ArgumentMatchers.any())).thenReturn(useOpt);
        Response<Object> updateProf = userService.updateProfile(authentication, updateProfileReq, 1L);

        Assertions.assertNotNull(updateProf);
        Assertions.assertEquals("4000",updateProf.code());
        Assertions.assertEquals("gagal update user karena nama yang baru sama dengan nama lama",updateProf.message());
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
    void resetPasswordBadRequest() {
        User user = new User(1L, "Krise", "krise@gmail.com", new BCryptPasswordEncoder().encode("oldPassword"), User.Role.BUYER, null, null, null, OffsetDateTime.now(), null, null);
        Authentication authentication = new Authentication(user.id(), user.role(), true);
        final Response<Object> response = userService.resetPassword(authentication, null, 1L);
        Assertions.assertEquals(Response.badRequest().code(), response.code());
    }
    

}
