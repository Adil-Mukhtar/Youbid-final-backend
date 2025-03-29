package com.youbid.fyp.service;

import com.youbid.fyp.model.*;
import com.youbid.fyp.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AnalyticsService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private BidRepository bidRepository;

    // Get user revenue statistics
    public Map<String, Object> getUserRevenueAnalytics(Integer userId) throws Exception {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new Exception("User not found"));

        Map<String, Object> analytics = new HashMap<>();

        // Get all sold products by user
        List<Product> soldProducts = productRepository.findProductByUserId(userId).stream()
                .filter(p -> "sold".equalsIgnoreCase(p.getStatus()))
                .collect(Collectors.toList());

        // Calculate total revenue
        BigDecimal totalRevenue = soldProducts.stream()
                .map(p -> p.getHighestBid() != null ? p.getHighestBid() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        analytics.put("totalRevenue", totalRevenue);
        analytics.put("totalSoldItems", soldProducts.size());

        // Monthly revenue breakdown (last 6 months)
        Map<String, BigDecimal> monthlyRevenue = getMonthlyRevenue(soldProducts);
        analytics.put("monthlyRevenue", monthlyRevenue);

        // Get review sentiment analysis
        Map<String, Object> reviewAnalytics = getReviewAnalytics(userId);
        analytics.put("reviewAnalytics", reviewAnalytics);

        // Revenue by category
        Map<String, BigDecimal> revenueByCategory = getRevenueByCategory(soldProducts);
        analytics.put("revenueByCategory", revenueByCategory);

        // Average sale price
        BigDecimal avgSalePrice = soldProducts.isEmpty() ? BigDecimal.ZERO :
                totalRevenue.divide(new BigDecimal(soldProducts.size()), 2, BigDecimal.ROUND_HALF_UP);
        analytics.put("averageSalePrice", avgSalePrice);

        return analytics;
    }

    // Get monthly revenue for the last 6 months
    private Map<String, BigDecimal> getMonthlyRevenue(List<Product> soldProducts) {
        Map<String, BigDecimal> monthlyRevenue = new LinkedHashMap<>();
        LocalDateTime now = LocalDateTime.now();

        // Initialize last 6 months with zero
        for (int i = 5; i >= 0; i--) {
            LocalDateTime month = now.minus(i, ChronoUnit.MONTHS);
            String monthKey = month.getYear() + "-" + String.format("%02d", month.getMonthValue());
            monthlyRevenue.put(monthKey, BigDecimal.ZERO);
        }

        // Populate with actual data
        soldProducts.forEach(product -> {
            if (product.getCreatedAt() != null && product.getHighestBid() != null) {
                LocalDateTime soldDate = product.getAuctionDeadline();
                if (soldDate != null && soldDate.isAfter(now.minus(6, ChronoUnit.MONTHS))) {
                    String monthKey = soldDate.getYear() + "-" + String.format("%02d", soldDate.getMonthValue());
                    BigDecimal currentAmount = monthlyRevenue.getOrDefault(monthKey, BigDecimal.ZERO);
                    monthlyRevenue.put(monthKey, currentAmount.add(product.getHighestBid()));
                }
            }
        });

        return monthlyRevenue;
    }

// in AnalyticsService.java, update the getReviewAnalytics method

    private Map<String, Object> getReviewAnalytics(Integer userId) {
        Map<String, Object> reviewAnalytics = new HashMap<>();
        List<Review> reviews = reviewRepository.getAllSellerReviewsById(userId);

        reviewAnalytics.put("totalReviews", reviews.size());

        // Use the existing sentiment analysis service
        try {
            // Prepare review content list
            List<String> reviewContents = reviews.stream()
                    .map(Review::getContent)
                    .collect(Collectors.toList());

            if (!reviewContents.isEmpty()) {
                // Call sentiment analysis service
                Map<String, Object> sentimentResults = reviewService.getSentimentAnalysisForReviews(userId);

                // Extract sentiment counts
                List<Map<String, Object>> reviewsWithSentiment =
                        (List<Map<String, Object>>) sentimentResults.get("reviews_with_sentiment");

                int positiveReviews = 0;
                int neutralReviews = 0;
                int negativeReviews = 0;

                for (Map<String, Object> review : reviewsWithSentiment) {
                    String sentiment = (String) review.get("sentiment");
                    if ("positive".equalsIgnoreCase(sentiment)) {
                        positiveReviews++;
                    } else if ("negative".equalsIgnoreCase(sentiment)) {
                        negativeReviews++;
                    } else {
                        neutralReviews++;
                    }
                }

                reviewAnalytics.put("positiveReviews", positiveReviews);
                reviewAnalytics.put("neutralReviews", neutralReviews);
                reviewAnalytics.put("negativeReviews", negativeReviews);
                reviewAnalytics.put("overallSentiment", sentimentResults.get("overall_classification"));
            } else {
                reviewAnalytics.put("positiveReviews", 0);
                reviewAnalytics.put("neutralReviews", 0);
                reviewAnalytics.put("negativeReviews", 0);
                reviewAnalytics.put("overallSentiment", "neutral");
            }
        } catch (Exception e) {
            // Fallback to simple keyword-based sentiment analysis if service fails
            int positiveReviews = 0;
            int neutralReviews = 0;
            int negativeReviews = 0;

            for (Review review : reviews) {
                String content = review.getContent().toLowerCase();
                if (content.contains("great") || content.contains("excellent") || content.contains("good")) {
                    positiveReviews++;
                } else if (content.contains("bad") || content.contains("poor") || content.contains("terrible")) {
                    negativeReviews++;
                } else {
                    neutralReviews++;
                }
            }

            reviewAnalytics.put("positiveReviews", positiveReviews);
            reviewAnalytics.put("neutralReviews", neutralReviews);
            reviewAnalytics.put("negativeReviews", negativeReviews);
            reviewAnalytics.put("overallSentiment", "neutral");
        }

        // Sales correlation with review sentiment
        Map<String, Object> salesCorrelation = new HashMap<>();
        int totalSentimentReviews = (int) reviewAnalytics.get("positiveReviews") +
                (int) reviewAnalytics.get("neutralReviews") +
                (int) reviewAnalytics.get("negativeReviews");

        if (totalSentimentReviews > 0) {
            double positivePercentage = (double) (int) reviewAnalytics.get("positiveReviews") / totalSentimentReviews;
            salesCorrelation.put("positive", positivePercentage > 0.6 ? "high" : (positivePercentage > 0.4 ? "medium" : "low"));
        } else {
            salesCorrelation.put("positive", "N/A");
        }

        reviewAnalytics.put("salesCorrelation", salesCorrelation);

        return reviewAnalytics;
    }

    // Get revenue breakdown by category
    private Map<String, BigDecimal> getRevenueByCategory(List<Product> soldProducts) {
        Map<String, BigDecimal> revenueByCategory = new HashMap<>();

        soldProducts.forEach(product -> {
            if (product.getCategory() != null && product.getHighestBid() != null) {
                BigDecimal currentAmount = revenueByCategory.getOrDefault(product.getCategory(), BigDecimal.ZERO);
                revenueByCategory.put(product.getCategory(), currentAmount.add(product.getHighestBid()));
            }
        });

        return revenueByCategory;
    }

    // Get overall platform analytics for admins
    public Map<String, Object> getOverallAnalytics() {
        Map<String, Object> analytics = new HashMap<>();

        // Total users
        analytics.put("totalUsers", userRepository.count());

        // Total products
        analytics.put("totalProducts", productRepository.count());

        // Active listings
        long activeListings = productRepository.findAll().stream()
                .filter(p -> "live".equalsIgnoreCase(p.getStatus()))
                .count();
        analytics.put("activeListings", activeListings);

        // Total sold items
        long soldItems = productRepository.findAll().stream()
                .filter(p -> "sold".equalsIgnoreCase(p.getStatus()))
                .count();
        analytics.put("soldItems", soldItems);

        // Platform revenue (could be calculated if you have platform fees)
        // This is just a placeholder
        analytics.put("platformRevenue", BigDecimal.ZERO);

        // Top categories by sales
        Map<String, Long> topCategories = getTopCategoriesBySales();
        analytics.put("topCategories", topCategories);

        // User growth over time
        Map<String, Long> userGrowth = getUserGrowthByMonth();
        analytics.put("userGrowth", userGrowth);

        return analytics;
    }

    // Get top categories by number of sales
    private Map<String, Long> getTopCategoriesBySales() {
        Map<String, Long> categoryCounts = productRepository.findAll().stream()
                .filter(p -> "sold".equalsIgnoreCase(p.getStatus()) && p.getCategory() != null)
                .collect(Collectors.groupingBy(Product::getCategory, Collectors.counting()));

        // Sort by count descending and take top 5
        return categoryCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(5)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));
    }

    // Get user growth by month (last 6 months)
    private Map<String, Long> getUserGrowthByMonth() {
        Map<String, Long> userGrowth = new LinkedHashMap<>();
        LocalDateTime now = LocalDateTime.now();

        // Create a map for the last 6 months
        for (int i = 5; i >= 0; i--) {
            LocalDateTime month = now.minus(i, ChronoUnit.MONTHS);
            String monthKey = month.getYear() + "-" + String.format("%02d", month.getMonthValue());
            userGrowth.put(monthKey, 0L);
        }

        // TODO: This would require tracking user registration dates
        // For now, we'll return the placeholder map

        return userGrowth;
    }
}