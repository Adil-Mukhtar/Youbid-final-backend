package com.youbid.fyp.service;

import com.youbid.fyp.DTO.BidDTO;
import com.youbid.fyp.model.Bid;
import com.youbid.fyp.model.Product;
import com.youbid.fyp.model.User;
import com.youbid.fyp.repository.BidRepository;
import com.youbid.fyp.repository.ProductRepository;
import com.youbid.fyp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class BidServiceImplementation implements BidService {

    @Autowired
    private BidRepository bidRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductService productService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private LoyaltyService loyaltyService;

    @Autowired
    private BidServiceExtension bidServiceExtension;

    @Override
    public Bid placeBid(Integer productId, User bidder, BigDecimal amount) throws Exception {
        Product product = productRepository.findById(productId).orElseThrow(() -> new Exception("Product not found"));

        BigDecimal productPrice = BigDecimal.valueOf(product.getPrice());

        if (LocalDateTime.now().isAfter(product.getAuctionDeadline())) {
            throw new Exception("Auction has ended");
        }

        if(bidder.getId() == product.getUser().getId()) {
            throw new Exception("You cannot place a bid on your own product!");
        }

        if(bidder.getBalance().compareTo(productPrice) < 0) {
            throw new Exception("You don't have enough balance to place bid on this product!");
        }

        // Add check for exclusive access eligibility
        if (product.hasExclusiveAccess()) {
            Map<String, Object> eligibility = bidServiceExtension.checkBidEligibility(productId, bidder.getId());
            if (!(boolean)eligibility.get("isEligible")) {
                throw new Exception((String)eligibility.get("message"));
            }
        }

        // New validation: Check if bid amount is less than the product price
        if (amount.compareTo(productPrice) < 0) {
            throw new Exception("Bid amount must be at least the product's starting price!");
        }

        if (product.getHighestBid() != null && amount.compareTo(product.getHighestBid()) <= 0) {
            throw new Exception("Bid must be higher than the current highest bid");
        }

        Bid bid = new Bid();
        bid.setProduct(product);
        bid.setBidder(bidder);
        bid.setAmount(amount);
        bid.setBidPlaceTime(LocalDateTime.now());
        bidRepository.save(bid);

        product.setHighestBid(amount);
        product.setHighestBidder(bidder);
        productRepository.save(product);

        // Create notification for the new highest bidder
        notificationService.notifyHighestBidder(
                bidder,
                product.getName(),
                amount.doubleValue(),
                productId
        );

        // If there was a previous highest bidder, notify them they've been outbid
        if (product.getHighestBidder() != null &&
                !product.getHighestBidder().getId().equals(bidder.getId())) {
            notificationService.notifyOutbid(
                    product.getHighestBidder(),
                    product.getName(),
                    amount.doubleValue(),
                    productId
            );
        }

        try {
            // Award loyalty points for placing a bid
            loyaltyService.awardPointsForBidPlaced(bidder.getId(), productId);
        } catch (Exception e) {
            // Just log the error but don't stop the bid process
            System.err.println("Error awarding loyalty points: " + e.getMessage());
        }

        return bid;
    }

    @Override
    public List<BidDTO> getBidHistory(Integer productId) throws Exception {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new Exception("Product not found"));

        return bidRepository.findAllBidDetailsByProductIdOrderByBidAmountAscDTO(productId);
    }

    @Override
    public Optional<Bid> getHighestBidByProductId(Integer productId) {
        return bidRepository.findTopByProductOrderByAmountDesc(
                productRepository.findById(productId).orElseThrow(() -> new RuntimeException("Product not found"))
        );
    }

    @Override
    @Scheduled(fixedRate = 5000) // Runs every 5 seconds
    @Transactional
    public void processAllAuctionWinnersScheduled() {
        System.out.println("⏳ Scheduled Task: Processing all auction winners...");

        try {
            // Fetch all eligible products for auction processing
            List<Product> eligibleProducts = productRepository.findAll()
                    .stream()
                    .filter(product -> product.getAuctionDeadline() != null &&
                            LocalDateTime.now().isAfter(product.getAuctionDeadline()) &&
                            "live".equalsIgnoreCase(product.getStatus()))
                    .toList();

            if (eligibleProducts.isEmpty()) {
                System.out.println("✅ No eligible products found for auction processing.");
                return; // Exit if no eligible products are found
            }

            for (Product product : eligibleProducts) {
                Optional<Bid> highestBid = getHighestBidByProductId(product.getId());

                if (highestBid.isPresent()) {
                    Bid bid = highestBid.get();
                    User winner = bid.getBidder();

                    // Ensure the 'wonItems' list is initialized
                    winner = userRepository.findById(winner.getId())
                            .orElseThrow(() -> new RuntimeException("Winner not found"));

                    // Assign the product to the winner
                    product.setHighestBid(bid.getAmount());
                    product.setHighestBidder(winner);
                    product.setStatus("sold");

                    // Ensure 'wonItems' is fetched and updated
                    if (!winner.getWonItems().contains(product)) {
                        winner.getWonItems().add(product);
                    }

                    // Save changes to repositories
                    productRepository.save(product);
                    userRepository.save(winner);

                    // Notify winner
                    notificationService.notifyAuctionWon(
                            winner,
                            product.getName(),
                            bid.getAmount().doubleValue(),
                            product.getId()
                    );

                    System.out.println("🏆 Product ID " + product.getId() + " sold to " + winner.getFirstname());

                    // After a winner is assigned to a product, award points
                    try {
                        loyaltyService.awardPointsForAuctionWon(winner.getId(), product.getId());
                    } catch (Exception e) {
                        System.err.println("Error awarding loyalty points for auction win: " + e.getMessage());
                    }
                } else {
                    // Mark product as expired if no valid bids are found
                    product.setStatus("expired");
                    productRepository.save(product);

                    System.out.println("❌ Product ID " + product.getId() + " marked as expired due to no bids.");
                }
            }

            System.out.println("✅ Scheduled Task: Auction processing completed successfully.");

        } catch (Exception e) {
            System.err.println("❗ Error during scheduled auction processing: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Notification method for auctions ending soon
    @Scheduled(fixedRate = 3600000) // Run every hour
    public void notifyAuctionsEndingSoon() {
        try {
            // Find products ending in the next 6 hours
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime sixHoursLater = now.plusHours(6);

            List<Product> endingSoonProducts = productRepository.findAll().stream()
                    .filter(product ->
                            product.getAuctionDeadline() != null &&
                                    product.getAuctionDeadline().isAfter(now) &&
                                    product.getAuctionDeadline().isBefore(sixHoursLater) &&
                                    "live".equalsIgnoreCase(product.getStatus())
                    )
                    .collect(Collectors.toList());

            for (Product product : endingSoonProducts) {
                // Calculate hours remaining
                long hoursRemaining = ChronoUnit.HOURS.between(now, product.getAuctionDeadline()) + 1;

                // Get all bidders for this product
                List<Bid> bids = bidRepository.findAllBidsByProductIdOrderByBidAmountAsc(product.getId());
                List<User> bidders = bids.stream()
                        .map(Bid::getBidder)
                        .distinct()
                        .collect(Collectors.toList());

                // Notify each bidder
                for (User bidder : bidders) {
                    notificationService.notifyAuctionEnding(
                            bidder,
                            product.getName(),
                            (int) hoursRemaining,
                            product.getId()
                    );
                }

                // Also notify the product owner
                notificationService.notifyAuctionEnding(
                        product.getUser(),
                        product.getName(),
                        (int) hoursRemaining,
                        product.getId()
                );
            }
        } catch (Exception e) {
            System.err.println("Error notifying about ending auctions: " + e.getMessage());
        }
    }

    // Implementation of the new methods added to BidService interface

    @Override
    public List<Bid> getActiveBidsByUser(Integer userId) throws Exception {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new Exception("User not found"));

        List<Bid> allUserBids = bidRepository.findByBidder(user);

        // Filter bids where the auction is still active
        return allUserBids.stream()
                .filter(bid -> {
                    Product product = bid.getProduct();
                    return product.getAuctionDeadline() != null &&
                            product.getAuctionDeadline().isAfter(LocalDateTime.now()) &&
                            "live".equalsIgnoreCase(product.getStatus());
                })
                .collect(Collectors.toList());
    }

    // Add this method to your existing BidServiceImplementation
    @Override
    public List<Bid> getLostBidsByUser(Integer userId) throws Exception {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new Exception("User not found"));

        List<Bid> allUserBids = bidRepository.findByBidder(user);

        // Filter bids where the auction has ended and the user was not the highest bidder
        return allUserBids.stream()
                .filter(bid -> {
                    Product product = bid.getProduct();

                    // Check if auction has ended (either deadline passed or status is "sold" or "expired")
                    boolean auctionEnded = (product.getAuctionDeadline() != null &&
                            product.getAuctionDeadline().isBefore(LocalDateTime.now())) ||
                            "sold".equalsIgnoreCase(product.getStatus()) ||
                            "expired".equalsIgnoreCase(product.getStatus());

                    // Check if user is not the highest bidder
                    boolean notHighestBidder = product.getHighestBidder() == null ||
                            !product.getHighestBidder().getId().equals(userId);

                    return auctionEnded && notHighestBidder;
                })
                .collect(Collectors.toList());
    }
}