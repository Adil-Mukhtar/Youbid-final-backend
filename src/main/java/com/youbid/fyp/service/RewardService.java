package com.youbid.fyp.service;

import com.youbid.fyp.model.Reward;
import com.youbid.fyp.model.UserReward;

import java.util.List;
import java.util.Map;

public interface RewardService {

    // Create a new reward type
    Reward createReward(Reward reward) throws Exception;

    // Update an existing reward
    Reward updateReward(Reward reward, Integer rewardId) throws Exception;

    // Deactivate a reward
    Reward deactivateReward(Integer rewardId) throws Exception;

    // Get all active rewards
    List<Reward> getActiveRewards();

    // Get rewards available to a user based on their points
    List<Reward> getAvailableRewardsForUser(Integer userId) throws Exception;

    // Allow user to redeem points for a reward
    UserReward redeemReward(Integer userId, Integer rewardId) throws Exception;

    // Get a user's redeemed rewards
    List<UserReward> getUserRewards(Integer userId) throws Exception;

    // Apply a user's reward to a product
    Map<String, Object> applyRewardToProduct(String redemptionCode, Integer productId) throws Exception;
}