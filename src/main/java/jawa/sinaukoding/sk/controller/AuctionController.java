package jawa.sinaukoding.sk.controller;

import jawa.sinaukoding.sk.model.Response;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/secured/auction")
public class AuctionController {

    // seller bisa createAuction
    @PostMapping("create-auction")
    public Response<Object> createAuction() {
        return Response.badRequest();
    }

    // admin, bisa approve
    @PostMapping("approve-auction")
    public Response<Object> approveAuction() {
        return Response.badRequest();
    }

    // admin, bisa reject
    @PostMapping("reject-auction")
    public Response<Object> rejectAuction() {
        return Response.badRequest();
    }
}
