package com.youbid.fyp.service;

import com.youbid.fyp.model.LoyaltyPoints;
import com.youbid.fyp.model.LoyaltySettings;
import com.youbid.fyp.model.Product;
import com.youbid.fyp.model.User;
import com.youbid.fyp.repository.LoyaltyPointsRepository;
import com.youbid.fyp.repository.LoyaltySettingsRepository;
import com.youbid.fyp.repository.ProductRepository;
import com.youbid.fyp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class LoyaltyServiceImplementation implements LoyaltyService {

    @Autowired
    private LoyaltyPointsRepository loyaltyPointsRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private LoyaltySettingsRepository loyaltySettingsRepository;

    // Default point values - used as fallback if settings aren't found
    private static final int POINTS_BID_PLACED = 5;
    private static final int POINTS_LISTING_CREATED = 10;
    private static final int POINTS_AUCTION_WON = 20;
    private static final int POINTS_REVIEW_SUBMITTED = 5;
    private static final int POINTS_REFERRAL = 30;

    // Loyalty tiers
    private static final int BRONZE_THRESHOLD = 0;
    private static final int SILVER_THRESHOLD = 100;
    private static final int GOLD_THRESHOLD = 500;
    private static final int PLATINUM_THRESHOLD = 1000;

    // Helper method to get integer setting with default value
    private int getSettingIntValue(String key, int defaultValue) {
        LoyaltySettings setting = loyaltySettingsRepository.findBySettingKey(key);
        if (setting != null && setting.getSettingValue() != null) {
            try {
                return Integer.parseInt(setting.getSettingValue());
            } catch (NumberFormatException e) {
                System.err.println("Error parsing setting value for key " + key + ": " + e.getMessage());
                return defaultValue;
            }
        }
        return defaultValue;
    }

    // Helper method to get boolean setting with default value
    private boolean getSettingBoolValue(String key, boolean defaultValue) {
        LoyaltySettings setting = loyaltySettingsRepository.findBySettingKey(key);
        if (setting != null && setting.getSettingValue() != null) {
            return "true".equalsIgnoreCase(setting.getSettingValue()) || "1".equals(setting.getSettingValue());
        }
        return defaultValue;
    }

    @Override
    public Integer getUserPointsBalance(Integer userId) throws Exception {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new Exception("User not found"));

        Integer earned = loyaltyPointsRepository.getTotalPointsEarned(user);
        Integer redeemed = loyaltyPointsRepository.getTotalPointsRedeemed(user);

        earned = earned == null ? 0 : earned;
        redeemed = redeemed == null ? 0 : redeemed;

        return earned - redeemed;
    }

    @Override
    @Transactional
    public LoyaltyPoints awardPoints(Integer userId, Integer points, String source, String description, Integer productId) throws Exception {
        if (points <= 0) {
            throw new Exception("Points to award must be positive");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new Exception("User not found"));

        Product product = null;
        if (productId != null) {
            product = productRepository.findById(productId)
                    .orElseThrow(() -> new Exception("Product not found"));
        }

        LoyaltyPoints loyaltyPoints = new LoyaltyPoints();
        loyaltyPoints.setUser(user);
        loyaltyPoints.setPoints(points);
        loyaltyPoints.setTransactionType("EARNED");
        loyaltyPoints.setSource(source);
        loyaltyPoints.setDescription(description);
        loyaltyPoints.setRelatedProduct(product);
        loyaltyPoints.setTimestamp(LocalDateTime.now());

        LoyaltyPoints savedPoints = loyaltyPointsRepository.save(loyaltyPoints);

        // Notify user about points earned
        notificationService.createNotification(
                "loyalty_points",
                "Points Earned",
                "You earned " + points + " loyalty points: " + description,
                user,
                productId,
                null
        );

        System.out.println("Awarded " + points + " points to user " + userId + " for " + source);

        return savedPoints;
    }

    @Override
    @Transactional
    public LoyaltyPoints deductPoints(Integer userId, Integer points, String source, String description) throws Exception {
        if (points <= 0) {
            throw new Exception("Points to deduct must be positive");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new Exception("User not found"));

        // Check if user has enough points
        Integer balance = getUserPointsBalance(userId);
        if (balance < points) {
            throw new Exception("Insufficient points balance");
        }

        LoyaltyPoints loyaltyPoints = new LoyaltyPoints();
        loyaltyPoints.setUser(user);
        loyaltyPoints.setPoints(points);
        loyaltyPoints.setTransactionType("REDEEMED");
        loyaltyPoints.setSource(source);
        loyaltyPoints.setDescription(description);
        loyaltyPoints.setTimestamp(LocalDateTime.now());

        return loyaltyPointsRepository.save(loyaltyPoints);
    }

    @Override
    public List<LoyaltyPoints> getUserPointsHistory(Integer userId) throws Exception {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new Exception("User not found"));

        return loyaltyPointsRepository.findByUserOrderByTimestampDesc(user);
    }

    @Override
    @Transactional
    public LoyaltyPoints awardPointsForBidPlaced(Integer userId, Integer productId) throws Exception {
        int points = getSettingIntValue("bidPlaced", POINTS_BID_PLACED);
        System.out.println("Points for bidPlaced from settings: " + points);
        String description = "Points for placing a bid";
        return awardPoints(userId, points, "BID_PLACED", description, productId);
    }

    @Override
    @Transactional
    public LoyaltyPoints awardPointsForListingCreated(Integer userId, Integer productId) throws Exception {
        int points = getSettingIntValue("listingCreated", POINTS_LISTING_CREATED);
        System.out.println("Points for listingCreated from settings: " + points);
        String description = "Points for creating a new listing";
        return awardPoints(userId, points, "LISTING_CREATED", description, productId);
    }

    @Override
    @Transactional
    public LoyaltyPoints awardPointsForAuctionWon(Integer userId, Integer productId) throws Exception {
        int points = getSettingIntValue("auctionWon", POINTS_AUCTION_WON);
        System.out.println("Points for auctionWon from settings: " + points);
        String description = "Points for winning an auction";
        return awardPoints(userId, points, "AUCTION_WON", description, productId);
    }

    @Override
    @Transactional
    public LoyaltyPoints awardPointsForReviewSubmitted(Integer userId, Integer productId) throws Exception {
        int points = getSettingIntValue("reviewSubmitted", POINTS_REVIEW_SUBMITTED);
        String description = "Points for submitting a review";
        return awardPoints(userId, points, "REVIEW_SUBMITTED", description, productId);
    }

    @Override
    @Transactional
    public LoyaltyPoints awardPointsForReferral(Integer userId, Integer referredUserId) throws Exception {
        int points = getSettingIntValue("referral", POINTS_REFERRAL);
        String description = "Points for referring a new user";
        return awardPoints(userId, points, "REFERRAL", description, null);
    }

    @Override
    public Map<String, Object> getUserLoyaltyStatus(Integer userId) throws Exception {
        Integer balance = getUserPointsBalance(userId);

        // Get thresholds from settings
        int silverThreshold = getSettingIntValue("silver_threshold", SILVER_THRESHOLD);
        int goldThreshold = getSettingIntValue("gold_threshold", GOLD_THRESHOLD);
        int platinumThreshold = getSettingIntValue("platinum_threshold", PLATINUM_THRESHOLD);

        // Determine tier
        String tier = "Bronze";
        int nextTierThreshold = silverThreshold;
        int pointsToNextTier = silverThreshold - balance;

        if (balance >= platinumThreshold) {
            tier = "Platinum";
            nextTierThreshold = 0;
            pointsToNextTier = 0;
        } else if (balance >= goldThreshold) {
            tier = "Gold";
            nextTierThreshold = platinumThreshold;
            pointsToNextTier = platinumThreshold - balance;
        } else if (balance >= silverThreshold) {
            tier = "Silver";
            nextTierThreshold = goldThreshold;
            pointsToNextTier = goldThreshold - balance;
        }

        // Get benefits from settings
        Map<String, Object> benefits = new HashMap<>();
        benefits.put("discountPercent", getSettingIntValue(tier.toLowerCase() + "_discount",
                tier.equals("Silver") ? 5 : tier.equals("Gold") ? 10 : tier.equals("Platinum") ? 15 : 0));

        benefits.put("featuredListings", getSettingIntValue(tier.toLowerCase() + "_featuredListings",
                tier.equals("Silver") ? 1 : tier.equals("Gold") ? 2 : tier.equals("Platinum") ? 3 : 0));

        benefits.put("earlyAccess", getSettingBoolValue(tier.toLowerCase() + "_earlyAccess",
                tier.equals("Gold") || tier.equals("Platinum")));

        Map<String, Object> result = new HashMap<>();
        result.put("currentPoints", balance);
        result.put("tier", tier);
        result.put("nextTierThreshold", nextTierThreshold);
        result.put("pointsToNextTier", pointsToNextTier);
        result.put("benefits", benefits);

        return result;
    }

    @Override
    public Map<String, Object> getLoyaltySettings() throws Exception {
        Map<String, Object> settings = new HashMap<>();

        // Get point rules
        Map<String, Object> pointRules = new HashMap<>();
        pointRules.put("bidPlaced", getSettingIntValue("bidPlaced", POINTS_BID_PLACED));
        pointRules.put("listingCreated", getSettingIntValue("listingCreated", POINTS_LISTING_CREATED));
        pointRules.put("auctionWon", getSettingIntValue("auctionWon", POINTS_AUCTION_WON));
        pointRules.put("reviewSubmitted", getSettingIntValue("reviewSubmitted", POINTS_REVIEW_SUBMITTED));
        pointRules.put("referral", getSettingIntValue("referral", POINTS_REFERRAL));

        settings.put("pointRules", pointRules);

        // Get tier settings
        Map<String, Object> tierSettings = new HashMap<>();
        String[] tiers = {"bronze", "silver", "gold", "platinum"};

        for (String tier : tiers) {
            Map<String, Object> tierConfig = new HashMap<>();

            tierConfig.put("threshold", getSettingIntValue(tier + "_threshold",
                    tier.equals("silver") ? SILVER_THRESHOLD :
                            tier.equals("gold") ? GOLD_THRESHOLD :
                                    tier.equals("platinum") ? PLATINUM_THRESHOLD : 0));

            tierConfig.put("discount", getSettingIntValue(tier + "_discount",
                    tier.equals("silver") ? 5 :
                            tier.equals("gold") ? 10 :
                                    tier.equals("platinum") ? 15 : 0));

            tierConfig.put("featuredListings", getSettingIntValue(tier + "_featuredListings",
                    tier.equals("silver") ? 1 :
                            tier.equals("gold") ? 2 :
                                    tier.equals("platinum") ? 3 : 0));

            tierConfig.put("earlyAccess", getSettingBoolValue(tier + "_earlyAccess",
                    tier.equals("gold") || tier.equals("platinum")));

            tierSettings.put(tier, tierConfig);
        }

        settings.put("tierSettings", tierSettings);

        return settings;
    }

    @Override
    @Transactional
    public Map<String, Object> updateLoyaltySettings(Map<String, Object> settings) throws Exception {
        Map<String, Object> result = new HashMap<>();

        try {
            System.out.println("Updating loyalty settings: " + settings);

            // Handle point rules
            if (settings.containsKey("pointRules")) {
                Map<String, Object> pointRules = (Map<String, Object>) settings.get("pointRules");

                for (Map.Entry<String, Object> entry : pointRules.entrySet()) {
                    String key = entry.getKey();
                    String value = String.valueOf(entry.getValue());

                    System.out.println("Saving point rule: " + key + " = " + value);

                    LoyaltySettings setting = loyaltySettingsRepository.findBySettingKey(key);
                    if (setting == null) {
                        setting = new LoyaltySettings(key, value);
                    } else {
                        setting.setSettingValue(value);
                    }
                    loyaltySettingsRepository.save(setting);
                }

                result.put("pointRulesUpdated", true);
            }

            // Handle tier settings
            if (settings.containsKey("tierSettings")) {
                Map<String, Object> tierSettings = (Map<String, Object>) settings.get("tierSettings");

                for (Map.Entry<String, Object> entry : tierSettings.entrySet()) {
                    String tierName = entry.getKey();
                    Map<String, Object> tierProps = (Map<String, Object>) entry.getValue();

                    for (Map.Entry<String, Object> prop : tierProps.entrySet()) {
                        String key = tierName + "_" + prop.getKey();
                        String value = String.valueOf(prop.getValue());

                        System.out.println("Saving tier setting: " + key + " = " + value);

                        LoyaltySettings setting = loyaltySettingsRepository.findBySettingKey(key);
                        if (setting == null) {
                            setting = new LoyaltySettings(key, value);
                        } else {
                            setting.setSettingValue(value);
                        }
                        loyaltySettingsRepository.save(setting);
                    }
                }

                result.put("tierSettingsUpdated", true);
            }

            result.put("success", true);
            result.put("message", "Settings updated successfully");

        } catch (Exception e) {
            System.err.println("Error updating loyalty settings: " + e.getMessage());
            e.printStackTrace();
            result.put("success", false);
            result.put("error", e.getMessage());
            throw new Exception("Error updating loyalty settings: " + e.getMessage());
        }

        return result;
    }
}