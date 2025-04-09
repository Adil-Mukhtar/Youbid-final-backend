package com.youbid.fyp.service;

import com.youbid.fyp.model.Bid;
import com.youbid.fyp.model.Product;
import com.youbid.fyp.model.User;
import com.youbid.fyp.repository.BidRepository;
import com.youbid.fyp.repository.ProductRepository;
import com.youbid.fyp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class BidServiceExtension {

    @Autowired
    private BidRepository bidRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LoyaltyService loyaltyService;

    /**
     * Checks if a user is eligible to bid on a product, especially for exclusive auctions
     * @param productId The product ID to check
     * @param userId The user ID to check eligibility for
     * @return Map containing eligibility status and details
     */
    public Map<String, Object> checkBidEligibility(Integer productId, Integer userId) throws Exception {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new Exception("Product not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new Exception("User not found"));

        Map<String, Object> result = new HashMap<>();
        result.put("productId", productId);
        result.put("userId", userId);
        result.put("isEligible", true);

        // If the product is not exclusive, user is automatically eligible
        if (!product.hasExclusiveAccess()) {
            result.put("message", "This auction is open to all users");
            return result;
        }

        // Get user's loyalty tier
        Map<String, Object> loyaltyStatus = loyaltyService.getUserLoyaltyStatus(userId);
        String userTier = (String) loyaltyStatus.get("tier");

        // Check if user's tier meets the product's exclusive tier requirement
        String requiredTier = product.getExclusiveAccessTier();
        boolean tierEligible = isUserTierEligible(userTier, requiredTier);

        if (!tierEligible) {
            result.put("isEligible", false);
            result.put("message", "This is an exclusive auction for " + requiredTier +
                    " tier users. Your current tier is " + userTier);
            result.put("userTier", userTier);
            result.put("requiredTier", requiredTier);
        } else {
            result.put("message", "You have exclusive access to this auction");
            result.put("userTier", userTier);
        }

        return result;
    }

    /**
     * Checks if a user's loyalty tier is eligible for a required tier
     * @param userTier The user's loyalty tier
     * @param requiredTier The required loyalty tier
     * @return True if eligible, false otherwise
     */
    private boolean isUserTierEligible(String userTier, String requiredTier) {
        // Define tier hierarchy (higher index = higher tier)
        String[] tiers = {"Bronze", "Silver", "Gold", "Platinum"};

        int userTierIndex = -1;
        int requiredTierIndex = -1;

        // Find indices for the tiers
        for (int i = 0; i < tiers.length; i++) {
            if (tiers[i].equalsIgnoreCase(userTier)) {
                userTierIndex = i;
            }
            if (tiers[i].equalsIgnoreCase(requiredTier)) {
                requiredTierIndex = i;
            }
        }

        // If either tier is not found, assume not eligible
        if (userTierIndex == -1 || requiredTierIndex == -1) {
            return false;
        }

        // User is eligible if their tier is equal to or higher than the required tier
        return userTierIndex >= requiredTierIndex;
    }

    /**
     * Calculate discounted bid amount if applicable
     * @param product The product to calculate for
     * @param bidAmount The original bid amount
     * @return The actual amount to be processed after discounts
     */
    public BigDecimal calculateDiscountedBidAmount(Product product, BigDecimal bidAmount) {
        // Check if product has an active discount
        if (!product.hasActiveDiscount()) {
            return bidAmount;
        }

        // Apply discount to bid amount
        BigDecimal discountMultiplier = BigDecimal.ONE.subtract(
                product.getDiscountPercent().divide(new BigDecimal(100))
        );

        return bidAmount.multiply(discountMultiplier)
                .setScale(2, java.math.RoundingMode.HALF_UP);
    }
}