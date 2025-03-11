package com.youbid.fyp.controller;

import com.youbid.fyp.DTO.BidDTO;
import com.youbid.fyp.model.Bid;
import com.youbid.fyp.model.Product;
import com.youbid.fyp.model.User;
import com.youbid.fyp.repository.ProductRepository;
import com.youbid.fyp.repository.UserRepository;
import com.youbid.fyp.service.BidService;
import com.youbid.fyp.service.ProductService;
import com.youbid.fyp.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/bids")
public class BidController {

    @Autowired
    private BidService bidService;

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @PostMapping("/place/{productId}")
    public ResponseEntity<Bid> placeBid( @PathVariable Integer productId,  @RequestHeader("Authorization") String jwt,
            @RequestBody Bid bid
    ) throws Exception {

        BigDecimal amount = bid.getAmount();
        User bidder = userService.findUserByJwt(jwt);
        Bid placedBid = bidService.placeBid(productId, bidder, amount);
        return new ResponseEntity<>(placedBid, HttpStatus.CREATED);
    }

    @GetMapping("/getBidHistory/{productId}")
    public ResponseEntity<List<BidDTO>> getBidHistory(@PathVariable Integer productId) throws Exception {
        List<BidDTO> bids = bidService.getBidHistory(productId);
        return new ResponseEntity<>(bids, HttpStatus.OK);
    }


    //for testing purposes

    //for testing purposes
    @GetMapping("/winner/{productId}")
    public ResponseEntity<?> AuctionWinner(@PathVariable Integer productId) {
        try {
            // Fetch the highest bid for the given product
            Optional<Bid> highestBid = bidService.getHighestBidByProductId(productId);
            Product product = productService.findProductById(productId);

            // Check if the auction has ended
            if (highestBid.isPresent() && LocalDateTime.now().isAfter(product.getAuctionDeadline())) {
                Bid bid = highestBid.get();
                User winner = bid.getBidder();

                // Assign the product to the winner
                product.setHighestBid(bid.getAmount());
                product.setHighestBidder(winner);
                product.setStatus("sold");

                // Add product to winner's 'wonItems'
                if (!winner.getWonItems().contains(product)) {
                    winner.getWonItems().add(product);
                }

                // Save changes to repositories
                productRepository.save(product);
                userRepository.save(winner);

                return ResponseEntity.ok()
                        .body(Map.of(
                                "winnerId", winner.getId(),
                                "winnerName", winner.getFirstname() + " " + winner.getLastname(),
                                "bidAmount", bid.getAmount(),
                                "auctionEndedAt", product.getAuctionDeadline(),
                                "status", product.getStatus()
                        ));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("No winner found. Either no bids were placed or the auction hasn't ended yet.");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to retrieve and assign auction winner: " + e.getMessage());
        }
    }

    //for testing purposes
    @GetMapping("/winners/process-all")
    public ResponseEntity<?> processAllAuctionWinners() {
        try {
            List<Product> eligibleProducts = productRepository.findAll()
                    .stream()
                    .filter(product -> product.getAuctionDeadline() != null &&
                            LocalDateTime.now().isAfter(product.getAuctionDeadline()) &&
                            "live".equalsIgnoreCase(product.getStatus()))
                    .toList();

            if (eligibleProducts.isEmpty()) {
                return ResponseEntity.ok("No eligible products found for auction processing.");
            }

            List<Map<String, Object>> winnerDetails = new ArrayList<>();

            for (Product product : eligibleProducts) {
                Optional<Bid> highestBid = bidService.getHighestBidByProductId(product.getId());

                if (highestBid.isPresent()) {
                    Bid bid = highestBid.get();
                    User winner = bid.getBidder();

                    // Assign the product to the winner
                    product.setHighestBid(bid.getAmount());
                    product.setHighestBidder(winner);
                    product.setStatus("sold");

                    // Add product to winner's 'wonItems'
                    if (!winner.getWonItems().contains(product)) {
                        winner.getWonItems().add(product);
                    }

                    // Save changes to repositories
                    productRepository.save(product);
                    userRepository.save(winner);

                    // Add winner details to the response list
                    Map<String, Object> winnerInfo = Map.of(
                            "productId", product.getId(),
                            "winnerId", winner.getId(),
                            "winnerName", winner.getFirstname() + " " + winner.getLastname(),
                            "bidAmount", bid.getAmount(),
                            "auctionEndedAt", product.getAuctionDeadline(),
                            "status", product.getStatus()
                    );

                    winnerDetails.add(winnerInfo);
                } else {
                    // Mark product as expired if no valid bids are found
                    product.setStatus("expired");
                    productRepository.save(product);
                }
            }

            return ResponseEntity.ok(winnerDetails);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to process auction winners: " + e.getMessage());
        }
    }

    // Add these methods to your existing BidController.java

    @GetMapping("/api/bids/active")
    public ResponseEntity<List<Bid>> getActiveBids(@RequestHeader("Authorization") String jwt) {
        try {
            User user = userService.findUserByJwt(jwt);
            List<Bid> activeBids = bidService.getActiveBidsByUser(user.getId());
            return new ResponseEntity<>(activeBids, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/api/bids/lost")
    public ResponseEntity<List<Bid>> getLostBids(@RequestHeader("Authorization") String jwt) {
        try {
            User user = userService.findUserByJwt(jwt);
            List<Bid> lostBids = bidService.getLostBidsByUser(user.getId());
            return new ResponseEntity<>(lostBids, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


}
