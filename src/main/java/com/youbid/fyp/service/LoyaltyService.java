package com.youbid.fyp.service;

import com.youbid.fyp.model.LoyaltyPoints;
import com.youbid.fyp.model.Product;
import com.youbid.fyp.model.User;

import java.util.List;
import java.util.Map;

public interface LoyaltyService {

    // Calculate a user's current points balance
    Integer getUserPointsBalance(Integer userId) throws Exception;

    // Add points to a user's account
    LoyaltyPoints awardPoints(Integer userId, Integer points, String source, String description, Integer productId) throws Exception;

    // Deduct points from a user's account
    LoyaltyPoints deductPoints(Integer userId, Integer points, String source, String description) throws Exception;

    // Get a user's points history
    List<LoyaltyPoints> getUserPointsHistory(Integer userId) throws Exception;

    // Award points for specific activities
    LoyaltyPoints awardPointsForBidPlaced(Integer userId, Integer productId) throws Exception;
    LoyaltyPoints awardPointsForListingCreated(Integer userId, Integer productId) throws Exception;
    LoyaltyPoints awardPointsForAuctionWon(Integer userId, Integer productId) throws Exception;
    LoyaltyPoints awardPointsForReviewSubmitted(Integer userId, Integer productId) throws Exception;
    LoyaltyPoints awardPointsForReferral(Integer userId, Integer referredUserId) throws Exception;

    // Get user's loyalty tier and benefits
    Map<String, Object> getUserLoyaltyStatus(Integer userId) throws Exception;

    // New methods for settings management
    Map<String, Object> getLoyaltySettings() throws Exception;
    Map<String, Object> updateLoyaltySettings(Map<String, Object> settings) throws Exception;
}