package jawa.sinaukoding.sk.service;

import jawa.sinaukoding.sk.entity.User;
import jawa.sinaukoding.sk.model.Authentication;
import jawa.sinaukoding.sk.model.Response;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.mockito.ArgumentMatchers.nullable;

import java.util.Optional;

public class AuthenticationTest {

    @Test
    void preconditionUnauthenticated() {
        AuthenticationServiceTest service = new AuthenticationServiceTest();

        Optional<Response<Object>> response1 = service.precondition(null, User.Role.ADMIN);
        Assertions.assertTrue(response1.isPresent());
        Assertions.assertEquals(Response.unauthenticated().code(), response1.get().code());
        Assertions.assertEquals(Response.unauthenticated().message(), response1.get().message());

        Optional<Response<Object>> response2 = service.precondition(new Authentication(1L, User.Role.ADMIN, false), User.Role.ADMIN);
        Assertions.assertTrue(response2.isPresent());
        Assertions.assertEquals(Response.unauthenticated().code(), response2.get().code());
        Assertions.assertEquals(Response.unauthenticated().message(), response2.get().message());
    }

    @Test
    void preconditionUnauthorized() {
        AuthenticationServiceTest service = new AuthenticationServiceTest();

        Optional<Response<Object>> response0 = service.precondition(new Authentication(null, User.Role.SELLER, true), User.Role.SELLER);
        Assertions.assertTrue(response0.isPresent());
        Assertions.assertEquals(Response.unauthorized().code(), response0.get().code());
        Assertions.assertEquals(Response.unauthorized().message(), response0.get().message());

        Optional<Response<Object>> response1 = service.precondition(new Authentication(-1L, User.Role.SELLER, true), User.Role.SELLER);
        Assertions.assertTrue(response1.isPresent());
        Assertions.assertEquals(Response.unauthorized().code(), response1.get().code());
        Assertions.assertEquals(Response.unauthorized().message(), response1.get().message());


        Optional<Response<Object>> response2 = service.precondition(new Authentication(1L, User.Role.SELLER, true), User.Role.ADMIN);
        Assertions.assertTrue(response2.isPresent());
        Assertions.assertEquals(Response.unauthorized().code(), response2.get().code());
        Assertions.assertEquals(Response.unauthorized().message(), response2.get().message());
    }


    public static final class AuthenticationServiceTest extends AbstractService {

    }
}
