package jawa.sinaukoding.sk.controller;

import jawa.sinaukoding.sk.model.Authentication;
import jawa.sinaukoding.sk.model.Response;
import jawa.sinaukoding.sk.service.AuctionService;
import jawa.sinaukoding.sk.service.UserService;
import jawa.sinaukoding.sk.util.SecurityContextHolder;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/secured/auction")
public class AuctionController {

    private final AuctionService auctionService;

    public AuctionController(AuctionService auctionService) {
        this.auctionService = auctionService;
    }

    // seller bisa createAuction
    @PostMapping("")
    public Response<Object> createAuction() {
        return Response.badRequest();
    }
 
    // admin, bisa approve
    @PostMapping("")
    public Response<Object> approveAuction() {
        return Response.badRequest();
    }

    // admin, bisa reject
    @PostMapping("/reject")
    public Response<Object> rejectAuction( @RequestParam (value = "id") Long id) {
        Authentication authentication = SecurityContextHolder.getAuthentication();
        return auctionService.rejectAuction(authentication,id);
    }
}

