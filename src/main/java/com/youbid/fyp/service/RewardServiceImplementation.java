package com.youbid.fyp.service;

import com.youbid.fyp.model.Product;
import com.youbid.fyp.model.Reward;
import com.youbid.fyp.model.User;
import com.youbid.fyp.model.UserReward;
import com.youbid.fyp.repository.ProductRepository;
import com.youbid.fyp.repository.RewardRepository;
import com.youbid.fyp.repository.UserRepository;
import com.youbid.fyp.repository.UserRewardRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class RewardServiceImplementation implements RewardService {

    @Autowired
    private RewardRepository rewardRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserRewardRepository userRewardRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private LoyaltyService loyaltyService;

    // Default expiration time in days for redeemed rewards
    private static final int DEFAULT_REWARD_EXPIRY_DAYS = 30;

    @Override
    public Reward createReward(Reward reward) throws Exception {
        if (reward.getPointsCost() <= 0) {
            throw new Exception("Points cost must be greater than zero");
        }

        // Set created time if it's null
        if (reward.getCreatedAt() == null) {
            reward.setCreatedAt(LocalDateTime.now());
        }

        return rewardRepository.save(reward);
    }

    @Override
    public Reward updateReward(Reward reward, Integer rewardId) throws Exception {
        Reward existingReward = rewardRepository.findById(rewardId)
                .orElseThrow(() -> new Exception("Reward not found"));

        if (reward.getName() != null) {
            existingReward.setName(reward.getName());
        }

        if (reward.getDescription() != null) {
            existingReward.setDescription(reward.getDescription());
        }

        if (reward.getPointsCost() != null && reward.getPointsCost() > 0) {
            existingReward.setPointsCost(reward.getPointsCost());
        }

        if (reward.getType() != null) {
            existingReward.setType(reward.getType());
        }

        if (reward.getDiscountPercent() != null) {
            existingReward.setDiscountPercent(reward.getDiscountPercent());
        }

        if (reward.getIsActive() != null) {
            existingReward.setIsActive(reward.getIsActive());
        }

        return rewardRepository.save(existingReward);
    }

    @Override
    public Reward deactivateReward(Integer rewardId) throws Exception {
        Reward reward = rewardRepository.findById(rewardId)
                .orElseThrow(() -> new Exception("Reward not found"));

        reward.setIsActive(false);
        return rewardRepository.save(reward);
    }

    @Override
    public List<Reward> getActiveRewards() {
        return rewardRepository.findByIsActiveTrue();
    }

    @Override
    public List<Reward> getAllRewards() {
        return rewardRepository.findAll();
    }

    @Override
    public List<Reward> getAvailableRewardsForUser(Integer userId) throws Exception {
        Integer userPoints = loyaltyService.getUserPointsBalance(userId);
        return rewardRepository.findByPointsCostLessThanEqualAndIsActiveTrueOrderByPointsCostAsc(userPoints);
    }

    @Override
    @Transactional
    public UserReward redeemReward(Integer userId, Integer rewardId) throws Exception {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new Exception("User not found"));

        Reward reward = rewardRepository.findById(rewardId)
                .orElseThrow(() -> new Exception("Reward not found"));

        if (!reward.getIsActive()) {
            throw new Exception("This reward is no longer active");
        }

        // Check if user has enough points
        Integer userPoints = loyaltyService.getUserPointsBalance(userId);
        if (userPoints < reward.getPointsCost()) {
            throw new Exception("Insufficient points to redeem this reward");
        }

        // Deduct points
        loyaltyService.deductPoints(
                userId,
                reward.getPointsCost(),
                "REWARD_REDEMPTION",
                "Redeemed points for: " + reward.getName()
        );

        // Create user reward
        UserReward userReward = new UserReward();
        userReward.setUser(user);
        userReward.setReward(reward);
        userReward.setRedeemedAt(LocalDateTime.now());
        userReward.setExpiresAt(LocalDateTime.now().plusDays(DEFAULT_REWARD_EXPIRY_DAYS));
        userReward.setIsUsed(false);

        // Generate unique redemption code
        String redemptionCode = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        userReward.setRedemptionCode(redemptionCode);

        UserReward savedReward = userRewardRepository.save(userReward);

        // Notify user
        notificationService.createNotification(
                "reward_redeemed",
                "Reward Redeemed",
                "You have successfully redeemed: " + reward.getName() + ". Your redemption code is: " + redemptionCode,
                user,
                null,
                null
        );

        return savedReward;
    }

    @Override
    public List<UserReward> getUserRewards(Integer userId) throws Exception {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new Exception("User not found"));

        return userRewardRepository.findByUserOrderByRedeemedAtDesc(user);
    }

    @Override
    @Transactional
    public Map<String, Object> applyRewardToProduct(String redemptionCode, Integer productId) throws Exception {
        UserReward userReward = userRewardRepository.findByRedemptionCode(redemptionCode)
                .orElseThrow(() -> new Exception("Invalid redemption code"));

        if (userReward.getIsUsed()) {
            throw new Exception("This reward has already been used");
        }

        if (userReward.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new Exception("This reward has expired");
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new Exception("Product not found"));

        Reward reward = userReward.getReward();
        Map<String, Object> result = new HashMap<>();

        // Apply reward based on type
        switch (reward.getType()) {
            case "DISCOUNT":
                // Calculate discount amount
                Integer discountPercent = reward.getDiscountPercent();

                // Apply discount to product
                product.setDiscountPercent(new BigDecimal(discountPercent));
                product.setDiscountCode(redemptionCode);
                product.setDiscountUntil(LocalDateTime.now().plusDays(14)); // Discount valid for 14 days

                productRepository.save(product);

                BigDecimal originalPrice = BigDecimal.valueOf(product.getPrice());
                BigDecimal discountDecimal = new BigDecimal(discountPercent).divide(new BigDecimal(100));
                BigDecimal discountAmount = originalPrice.multiply(discountDecimal);
                BigDecimal discountedPrice = originalPrice.subtract(discountAmount);

                result.put("originalPrice", originalPrice);
                result.put("discountPercent", discountPercent);
                result.put("discountAmount", discountAmount);
                result.put("discountedPrice", discountedPrice);
                break;

            case "FEATURED_LISTING":
                // Apply featured status to listing
                product.setIsFeatured(true);
                product.setFeaturedUntil(LocalDateTime.now().plusDays(7)); // Featured for 7 days

                productRepository.save(product);

                result.put("featuredStatus", "applied");
                result.put("featuredDuration", "7 days");
                result.put("featuredUntil", product.getFeaturedUntil());
                break;

            case "EXCLUSIVE_ACCESS":
                // Get user's loyalty tier
                User user = userReward.getUser();
                Map<String, Object> loyaltyStatus = loyaltyService.getUserLoyaltyStatus(user.getId());
                String userTier = (String) loyaltyStatus.get("tier");

                // Apply exclusive access
                product.setIsExclusive(true);
                product.setExclusiveAccessTier(userTier);
                product.setExclusiveUntil(LocalDateTime.now().plusDays(3)); // Exclusive for 3 days

                productRepository.save(product);

                result.put("exclusiveAccess", "granted");
                result.put("exclusiveTier", userTier);
                result.put("exclusiveUntil", product.getExclusiveUntil());
                break;

            default:
                throw new Exception("Unsupported reward type");
        }

        // Mark reward as used
        userReward.setIsUsed(true);
        userRewardRepository.save(userReward);

        // Create notification for the product owner
        notificationService.createNotification(
                "reward_applied",
                "Reward Applied",
                "Your " + reward.getType().toLowerCase().replace("_", " ") + " reward has been applied to your listing: " + product.getName(),
                product.getUser(),
                productId,
                null
        );

        // Add reference to the applied reward
        result.put("redemptionCode", redemptionCode);
        result.put("rewardName", reward.getName());
        result.put("rewardType", reward.getType());
        result.put("productId", productId);
        result.put("productName", product.getName());
        result.put("status", "success");

        return result;
    }

    @Override
    public Map<String, Object> getRewardStatistics() throws Exception {
        Map<String, Object> statistics = new HashMap<>();

        // Get all rewards
        List<Reward> allRewards = rewardRepository.findAll();

        // Count active rewards
        long activeRewardsCount = allRewards.stream()
                .filter(Reward::getIsActive)
                .count();

        statistics.put("activeRewards", activeRewardsCount);

        // Get all user rewards (redemptions)
        List<UserReward> allUserRewards = userRewardRepository.findAll();

        // Total redemptions
        statistics.put("totalRedemptions", allUserRewards.size());

        // Calculate total points spent on rewards
        int totalPointsSpent = 0;
        for (UserReward userReward : allUserRewards) {
            totalPointsSpent += userReward.getReward().getPointsCost();
        }
        statistics.put("pointsSpent", totalPointsSpent);

        // Create a map to count redemptions per reward
        Map<Integer, Integer> redemptionCounts = new HashMap<>();

        for (UserReward userReward : allUserRewards) {
            Integer rewardId = userReward.getReward().getId();
            redemptionCounts.put(rewardId, redemptionCounts.getOrDefault(rewardId, 0) + 1);
        }

        statistics.put("redemptionCounts", redemptionCounts);

        // Find most popular reward (most redeemed)
        if (!redemptionCounts.isEmpty()) {
            Optional<Map.Entry<Integer, Integer>> mostPopularEntry = redemptionCounts.entrySet().stream()
                    .max(Map.Entry.comparingByValue());

            if (mostPopularEntry.isPresent()) {
                Integer mostPopularRewardId = mostPopularEntry.get().getKey();
                Optional<Reward> mostPopularReward = allRewards.stream()
                        .filter(r -> r.getId().equals(mostPopularRewardId))
                        .findFirst();

                if (mostPopularReward.isPresent()) {
                    statistics.put("mostPopularReward", mostPopularReward.get().getName());
                } else {
                    statistics.put("mostPopularReward", "Unknown");
                }
            } else {
                statistics.put("mostPopularReward", "None");
            }
        } else {
            statistics.put("mostPopularReward", "None");
        }

        return statistics;
    }

    @Override
    @Transactional
    public void deleteReward(Integer rewardId) throws Exception {
        Reward reward = rewardRepository.findById(rewardId)
                .orElseThrow(() -> new Exception("Reward not found"));

        // Only allow deletion of inactive rewards to prevent issues with active redemptions
        if (reward.getIsActive()) {
            throw new Exception("Cannot delete an active reward. Please deactivate it first.");
        }

        // Check if this reward has been redeemed by any users
        List<UserReward> userRewards = userRewardRepository.findByReward(reward);

        // If users have redeemed this reward, mark as invalid (used)
        for (UserReward userReward : userRewards) {
            userReward.setIsUsed(true);
            userRewardRepository.save(userReward);
        }

        // Finally, delete the reward
        rewardRepository.delete(reward);
    }
}