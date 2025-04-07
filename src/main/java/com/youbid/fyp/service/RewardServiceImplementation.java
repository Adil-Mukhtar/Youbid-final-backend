package com.youbid.fyp.service;

import com.youbid.fyp.model.Reward;
import com.youbid.fyp.model.User;
import com.youbid.fyp.model.UserReward;
import com.youbid.fyp.model.Product;
import com.youbid.fyp.repository.RewardRepository;
import com.youbid.fyp.repository.UserRepository;
import com.youbid.fyp.repository.UserRewardRepository;
import com.youbid.fyp.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.math.BigDecimal;

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
    private LoyaltyService loyaltyService;

    @Autowired
    private NotificationService notificationService;

    // Default expiration time in days for redeemed rewards
    private static final int DEFAULT_REWARD_EXPIRY_DAYS = 30;

    @Override
    public Reward createReward(Reward reward) throws Exception {
        if (reward.getPointsCost() <= 0) {
            throw new Exception("Points cost must be greater than zero");
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
                BigDecimal originalPrice = BigDecimal.valueOf(product.getPrice());
                BigDecimal discountAmount = originalPrice.multiply(BigDecimal.valueOf(discountPercent).divide(BigDecimal.valueOf(100)));
                BigDecimal discountedPrice = originalPrice.subtract(discountAmount);

                result.put("originalPrice", originalPrice);
                result.put("discountPercent", discountPercent);
                result.put("discountAmount", discountAmount);
                result.put("discountedPrice", discountedPrice);
                break;

            case "FEATURED_LISTING":
                // Logic for applying featured status to a listing
                // This would typically involve updating some field on the product
                // and possibly affecting how it appears in search results
                result.put("featuredStatus", "applied");
                result.put("featuredDuration", "7 days");
                break;

            case "EXCLUSIVE_ACCESS":
                // Logic for granting exclusive early access to an auction
                result.put("exclusiveAccess", "granted");
                break;

            default:
                throw new Exception("Unsupported reward type");
        }

        // Mark reward as used
        userReward.setIsUsed(true);
        userRewardRepository.save(userReward);

        // Add reference to the applied reward
        result.put("redemptionCode", redemptionCode);
        result.put("rewardName", reward.getName());
        result.put("productId", productId);
        result.put("productName", product.getName());

        return result;
    }
}