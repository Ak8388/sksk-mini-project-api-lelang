
package jawa.sinaukoding.sk.service;


import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import jawa.sinaukoding.sk.entity.Auction;
import jawa.sinaukoding.sk.entity.AuctionBid;
import jawa.sinaukoding.sk.entity.User;
import jawa.sinaukoding.sk.model.Authentication;
import jawa.sinaukoding.sk.model.request.LoginReq;
import jawa.sinaukoding.sk.model.request.RegisterBuyerReq;
import jawa.sinaukoding.sk.model.request.RegisterSellerReq;
import jawa.sinaukoding.sk.model.request.UpdateProfileReq;
import jawa.sinaukoding.sk.model.Response;
import jawa.sinaukoding.sk.model.response.UserDto;
import jawa.sinaukoding.sk.repository.AuctionRepository;
import jawa.sinaukoding.sk.repository.UserRepository;
import jawa.sinaukoding.sk.util.HexUtils;
import jawa.sinaukoding.sk.util.JwtUtils;

import org.apache.catalina.startup.ClassLoaderFactory.Repository;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import jawa.sinaukoding.sk.model.request.ResetPasswordReq;


@Service
public final class AuctionService extends AbstractService {

    private final AuctionRepository auctionRepository;
    //private final PasswordEncoder passwordEncoder;
    //private final byte[] jwtKey;
    // private final JwtUtils jwtUtils;

    public AuctionService(final Environment env, final AuctionRepository auctionRepository) {
        this.auctionRepository = auctionRepository;
    }

    public Response<Object> approveAuction(final Authentication authentication, Long id) {
        return precondition(authentication, User.Role.ADMIN).orElseGet(() -> {
            Optional<Auction> auctionOptional = auctionRepository.findById(id);
            if (auctionOptional.isEmpty()) {
                return Response.create("01", "00", "Auction not found", null);
            }

            Auction auction = auctionOptional.get();
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
                auctionRepository.approveAuction(Id)(updatedAuction);
                
                return Response.create("01", "01", "Auction approve successfully", null);
            } else {
                return Response.create("01", "02", "Auction is valid", null);
            }
        });
    }
}
