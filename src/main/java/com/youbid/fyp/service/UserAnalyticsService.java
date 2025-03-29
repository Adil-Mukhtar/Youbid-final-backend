package com.youbid.fyp.service;

import com.youbid.fyp.model.*;
import com.youbid.fyp.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for providing analytics data to sellers about their listings
 */
@Service
public class UserAnalyticsService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private BidRepository bidRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private ReviewService reviewService;

    /**
     * Get user's seller performance analytics
     *
     * @param userId The user ID to get analytics for
     * @return Map containing analytics data
     */
    public Map<String, Object> getSellerAnalytics(Integer userId) throws Exception {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new Exception("User not found"));

        Map<String, Object> analytics = new HashMap<>();

        // Get all user's products
        List<Product> userProducts = productRepository.findProductByUserId(userId);

        // Get sold and live products
        List<Product> soldProducts = userProducts.stream()
                .filter(product -> "sold".equalsIgnoreCase(product.getStatus()))
                .collect(Collectors.toList());

        List<Product> liveProducts = userProducts.stream()
                .filter(product -> "live".equalsIgnoreCase(product.getStatus()))
                .collect(Collectors.toList());

        // Calculate total revenue
        BigDecimal totalRevenue = soldProducts.stream()
                .map(p -> p.getHighestBid() != null ? p.getHighestBid() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        analytics.put("totalListings", userProducts.size());
        analytics.put("liveListings", liveProducts.size());
        analytics.put("soldItems", soldProducts.size());
        analytics.put("totalRevenue", totalRevenue);

        // Calculate success rate (sold items / total completed listings)
        int completedListings = userProducts.stream()
                .filter(p -> !"live".equalsIgnoreCase(p.getStatus()) && !"pending".equalsIgnoreCase(p.getStatus()))
                .collect(Collectors.toList()).size();

        double successRate = completedListings > 0
                ? (double) soldProducts.size() / completedListings * 100
                : 0;

        analytics.put("successRate", Math.round(successRate * 10) / 10.0);

        // Calculate average sale price
        BigDecimal avgSalePrice = soldProducts.isEmpty() ? BigDecimal.ZERO :
                totalRevenue.divide(new BigDecimal(soldProducts.size()), 2, RoundingMode.HALF_UP);
        analytics.put("averageSalePrice", avgSalePrice);

        // Monthly revenue trend (last 6 months)
        Map<String, BigDecimal> monthlyRevenue = getMonthlyRevenue(soldProducts);
        analytics.put("monthlyRevenue", monthlyRevenue);

        // Revenue by category
        Map<String, BigDecimal> revenueByCategory = getRevenueByCategory(soldProducts);
        analytics.put("revenueByCategory", revenueByCategory);

        // Calculate bid engagement metrics
        analytics.put("bidMetrics", getBidMetrics(userProducts, userId));

        // Get review analytics if there are reviews
        List<Review> sellerReviews = reviewRepository.getAllSellerReviewsById(userId);
        if (!sellerReviews.isEmpty()) {
            try {
                Map<String, Object> reviewAnalytics = reviewService.getSentimentAnalysisForReviews(userId);
                analytics.put("reviewAnalytics", reviewAnalytics);
            } catch (Exception e) {
                Map<String, Object> fallbackReviewAnalytics = getFallbackReviewAnalytics(sellerReviews);
                analytics.put("reviewAnalytics", fallbackReviewAnalytics);
            }
        } else {
            analytics.put("reviewAnalytics", Collections.singletonMap("totalReviews", 0));
        }

        return analytics;
    }

    /**
     * Get monthly revenue for the last 6 months
     */
    private Map<String, BigDecimal> getMonthlyRevenue(List<Product> soldProducts) {
        Map<String, BigDecimal> monthlyRevenue = new LinkedHashMap<>();
        LocalDateTime now = LocalDateTime.now();

        // Initialize last 6 months with zero
        for (int i = 5; i >= 0; i--) {
            LocalDateTime month = now.minus(i, ChronoUnit.MONTHS);
            String monthKey = month.getYear() + "-" + String.format("%02d", month.getMonthValue());
            monthlyRevenue.put(monthKey, BigDecimal.ZERO);
        }

        // Fill in actual revenue data
        soldProducts.forEach(product -> {
            if (product.getCreatedAt() != null && product.getHighestBid() != null) {
                LocalDateTime soldDate = product.getAuctionDeadline();
                if (soldDate != null && soldDate.isAfter(now.minus(6, ChronoUnit.MONTHS))) {
                    String monthKey = soldDate.getYear() + "-" + String.format("%02d", soldDate.getMonthValue());
                    if (monthlyRevenue.containsKey(monthKey)) {
                        BigDecimal currentAmount = monthlyRevenue.get(monthKey);
                        monthlyRevenue.put(monthKey, currentAmount.add(product.getHighestBid()));
                    }
                }
            }
        });

        return monthlyRevenue;
    }

    /**
     * Get revenue breakdown by category
     */
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

    /**
     * Get bidding activity metrics for all user's products
     */
    private Map<String, Object> getBidMetrics(List<Product> products, Integer userId) {
        Map<String, Object> metrics = new HashMap<>();

        // Collect all bids across all user's products
        List<Bid> allBids = new ArrayList<>();
        for (Product product : products) {
            allBids.addAll(bidRepository.findAllBidsByProductIdOrderByBidAmountAsc(product.getId()));
        }

        // Total bids received
        metrics.put("totalBidsReceived", allBids.size());

        // Average bids per listing
        double avgBidsPerListing = products.isEmpty() ? 0 :
                (double) allBids.size() / products.size();
        metrics.put("averageBidsPerListing", Math.round(avgBidsPerListing * 10) / 10.0);

        // Average bid increase percentage
        double totalBidIncrease = 0;
        int bidIncreaseCount = 0;

        for (Product product : products) {
            List<Bid> productBids = bidRepository.findAllBidsByProductIdOrderByBidAmountAsc(product.getId());
            if (productBids.size() >= 2) {
                // Calculate percentage increase from first to highest bid
                BigDecimal firstBid = productBids.get(0).getAmount();
                BigDecimal highestBid = productBids.get(productBids.size() - 1).getAmount();

                if (firstBid.compareTo(BigDecimal.ZERO) > 0) {
                    double increase = highestBid.subtract(firstBid)
                            .divide(firstBid, 4, RoundingMode.HALF_UP)
                            .doubleValue() * 100;
                    totalBidIncrease += increase;
                    bidIncreaseCount++;
                }
            }
        }

        double avgBidIncrease = bidIncreaseCount > 0 ? totalBidIncrease / bidIncreaseCount : 0;
        metrics.put("averageBidIncrease", Math.round(avgBidIncrease * 10) / 10.0);

        // Analysis of times when bids are most common
        Map<Integer, Integer> bidsPerHour = new HashMap<>();
        for (int i = 0; i < 24; i++) {
            bidsPerHour.put(i, 0);
        }

        for (Bid bid : allBids) {
            int hour = bid.getBidPlaceTime().getHour();
            bidsPerHour.put(hour, bidsPerHour.get(hour) + 1);
        }

        metrics.put("bidsPerHour", bidsPerHour);

        // Get most active hour
        int mostActiveHour = 0;
        int maxBids = 0;
        for (Map.Entry<Integer, Integer> entry : bidsPerHour.entrySet()) {
            if (entry.getValue() > maxBids) {
                maxBids = entry.getValue();
                mostActiveHour = entry.getKey();
            }
        }

        metrics.put("mostActiveHour", mostActiveHour);

        // Get unique bidders
        Set<Integer> uniqueBidders = allBids.stream()
                .map(bid -> bid.getBidder().getId())
                .collect(Collectors.toSet());

        metrics.put("uniqueBiddersCount", uniqueBidders.size());

        return metrics;
    }

    /**
     * Create fallback review analytics if sentiment service fails
     */
    private Map<String, Object> getFallbackReviewAnalytics(List<Review> reviews) {
        Map<String, Object> fallbackAnalytics = new HashMap<>();

        // Simple text-based sentiment analysis
        int positiveReviews = 0;
        int neutralReviews = 0;
        int negativeReviews = 0;

        for (Review review : reviews) {
            String content = review.getContent().toLowerCase();

            // Simple keyword matching
            if (content.contains("great") || content.contains("good") ||
                    content.contains("excellent") || content.contains("love") ||
                    content.contains("recommend")) {
                positiveReviews++;
            } else if (content.contains("bad") || content.contains("poor") ||
                    content.contains("terrible") || content.contains("awful") ||
                    content.contains("worst")) {
                negativeReviews++;
            } else {
                neutralReviews++;
            }
        }

        fallbackAnalytics.put("totalReviews", reviews.size());
        fallbackAnalytics.put("positiveReviews", positiveReviews);
        fallbackAnalytics.put("neutralReviews", neutralReviews);
        fallbackAnalytics.put("negativeReviews", negativeReviews);

        // Determine overall sentiment
        String overallSentiment = "neutral";
        if (positiveReviews > negativeReviews && positiveReviews > neutralReviews) {
            overallSentiment = "positive";
        } else if (negativeReviews > positiveReviews && negativeReviews > neutralReviews) {
            overallSentiment = "negative";
        }

        fallbackAnalytics.put("overallSentiment", overallSentiment);

        // Recent reviews (up to 5)
        List<Map<String, Object>> recentReviews = reviews.stream()
                .sorted(Comparator.comparing(Review::getCreatedAt).reversed())
                .limit(5)
                .map(review -> {
                    Map<String, Object> reviewMap = new HashMap<>();
                    reviewMap.put("content", review.getContent());
                    reviewMap.put("reviewerName", review.getReviewerName());
                    reviewMap.put("createdAt", review.getCreatedAt());

                    // Simple sentiment for each review
                    String content = review.getContent().toLowerCase();
                    String sentiment = "neutral";

                    if (content.contains("great") || content.contains("good") ||
                            content.contains("excellent") || content.contains("love") ||
                            content.contains("recommend")) {
                        sentiment = "positive";
                    } else if (content.contains("bad") || content.contains("poor") ||
                            content.contains("terrible") || content.contains("awful") ||
                            content.contains("worst")) {
                        sentiment = "negative";
                    }

                    reviewMap.put("sentiment", sentiment);
                    return reviewMap;
                })
                .collect(Collectors.toList());

        fallbackAnalytics.put("recentReviews", recentReviews);

        return fallbackAnalytics;
    }

    /**
     * Get bidding activity for a particular product
     */
    public Map<String, Object> getProductBidAnalytics(Integer productId, Integer userId) throws Exception {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new Exception("Product not found"));

        // Verify the user is the owner of this product
        if (!product.getUser().getId().equals(userId)) {
            throw new Exception("You don't have permission to view analytics for this product");
        }

        Map<String, Object> analytics = new HashMap<>();
        analytics.put("productName", product.getName());
        analytics.put("productId", product.getId());
        analytics.put("status", product.getStatus());

        // Get all bids for this product
        List<Bid> bids = bidRepository.findAllBidsByProductIdOrderByBidAmountAsc(productId);
        analytics.put("totalBids", bids.size());

        // Get highest bid
        analytics.put("highestBid", product.getHighestBid());

        // Calculate bid progression (percentage increase from start price to current highest bid)
        BigDecimal startPrice = new BigDecimal(product.getPrice());

        double bidProgression = 0;
        if (product.getHighestBid() != null && startPrice.compareTo(BigDecimal.ZERO) > 0) {
            bidProgression = product.getHighestBid().subtract(startPrice)
                    .divide(startPrice, 4, RoundingMode.HALF_UP)
                    .doubleValue() * 100;
        }

        analytics.put("bidProgression", Math.round(bidProgression * 10) / 10.0);

        // Get unique bidders
        Set<Integer> uniqueBidders = bids.stream()
                .map(bid -> bid.getBidder().getId())
                .collect(Collectors.toSet());

        analytics.put("uniqueBiddersCount", uniqueBidders.size());

        // Get bidding timeline (time between bids)
        if (bids.size() >= 2) {
            List<Map<String, Object>> bidTimeline = new ArrayList<>();

            for (int i = 0; i < bids.size(); i++) {
                Map<String, Object> bidInfo = new HashMap<>();
                Bid bid = bids.get(i);

                bidInfo.put("bidAmount", bid.getAmount());
                bidInfo.put("bidTime", bid.getBidPlaceTime());

                if (i > 0) {
                    // Calculate time since previous bid
                    LocalDateTime prevBidTime = bids.get(i-1).getBidPlaceTime();
                    LocalDateTime currentBidTime = bid.getBidPlaceTime();

                    long minutesBetween = ChronoUnit.MINUTES.between(prevBidTime, currentBidTime);
                    bidInfo.put("minutesSincePreviousBid", minutesBetween);

                    // Calculate percentage increase from previous bid
                    BigDecimal prevAmount = bids.get(i-1).getAmount();
                    BigDecimal currentAmount = bid.getAmount();

                    double increase = currentAmount.subtract(prevAmount)
                            .divide(prevAmount, 4, RoundingMode.HALF_UP)
                            .doubleValue() * 100;

                    bidInfo.put("percentageIncrease", Math.round(increase * 10) / 10.0);
                }

                bidTimeline.add(bidInfo);
            }

            analytics.put("bidTimeline", bidTimeline);
        }

        // Check interest over time (divide auction duration into segments and count bids per segment)
        if (product.getAuctionDeadline() != null && product.getCreatedAt() != null) {
            Map<String, Integer> interestOverTime = getBidDistribution(product, bids);
            analytics.put("interestOverTime", interestOverTime);
        }

        return analytics;
    }

    /**
     * Get bid distribution over the auction duration
     */
    private Map<String, Integer> getBidDistribution(Product product, List<Bid> bids) {
        LocalDateTime startTime = product.getCreatedAt();
        LocalDateTime endTime = product.getAuctionDeadline();

        // Number of segments to divide the auction duration into
        int segments = 5;

        Map<String, Integer> distribution = new LinkedHashMap<>();

        // Initialize all segments with 0 bids
        for (int i = 1; i <= segments; i++) {
            distribution.put("Segment " + i, 0);
        }

        // Calculate total duration
        long totalMinutes = ChronoUnit.MINUTES.between(startTime, endTime);
        long segmentMinutes = totalMinutes / segments;

        // Count bids in each segment
        for (Bid bid : bids) {
            LocalDateTime bidTime = bid.getBidPlaceTime();
            long minutesFromStart = ChronoUnit.MINUTES.between(startTime, bidTime);

            // Determine which segment this bid falls into
            int segmentIndex = (int) (minutesFromStart / segmentMinutes) + 1;

            // Handle edge case for bids placed exactly at the end time
            if (segmentIndex > segments) {
                segmentIndex = segments;
            }

            String segmentKey = "Segment " + segmentIndex;
            distribution.put(segmentKey, distribution.get(segmentKey) + 1);
        }

        return distribution;
    }

    /**
     * Get engagement metrics per location and category
     */
    public Map<String, Object> getEngagementMetrics(Integer userId) throws Exception {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new Exception("User not found"));

        Map<String, Object> metrics = new HashMap<>();

        // Get all user's products
        List<Product> userProducts = productRepository.findProductByUserId(userId);

        // Performance by category
        Map<String, Map<String, Object>> categoryMetrics = new HashMap<>();

        // Performance by location
        Map<String, Map<String, Object>> locationMetrics = new HashMap<>();

        // Process each product
        for (Product product : userProducts) {
            String category = product.getCategory();
            String location = product.getLocation();

            if (category != null) {
                processCategory(categoryMetrics, category, product);
            }

            if (location != null) {
                processLocation(locationMetrics, location, product);
            }
        }

        // Calculate averages and success rates for categories
        finalizeMetrics(categoryMetrics);
        finalizeMetrics(locationMetrics);

        metrics.put("categoryMetrics", categoryMetrics);
        metrics.put("locationMetrics", locationMetrics);

        return metrics;
    }

    /**
     * Process a product's contribution to category metrics
     */
    private void processCategory(Map<String, Map<String, Object>> categoryMetrics, String category, Product product) {
        // Get or create metrics for this category
        Map<String, Object> metrics = categoryMetrics.computeIfAbsent(category, k -> initializeMetrics());

        updateMetrics(metrics, product);
    }

    /**
     * Process a product's contribution to location metrics
     */
    private void processLocation(Map<String, Map<String, Object>> locationMetrics, String location, Product product) {
        // Get or create metrics for this location
        Map<String, Object> metrics = locationMetrics.computeIfAbsent(location, k -> initializeMetrics());

        updateMetrics(metrics, product);
    }

    /**
     * Initialize empty metrics
     */
    private Map<String, Object> initializeMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("listings", 0);
        metrics.put("soldItems", 0);
        metrics.put("totalRevenue", BigDecimal.ZERO);
        metrics.put("totalBids", 0);
        metrics.put("averageBidsPerListing", 0.0);
        metrics.put("successRate", 0.0);
        metrics.put("averageSalePrice", BigDecimal.ZERO);

        return metrics;
    }

    /**
     * Update metrics with a product's data
     */
    private void updateMetrics(Map<String, Object> metrics, Product product) {
        // Increment total listings
        metrics.put("listings", (Integer) metrics.get("listings") + 1);

        // Check if product is sold
        if ("sold".equalsIgnoreCase(product.getStatus())) {
            metrics.put("soldItems", (Integer) metrics.get("soldItems") + 1);

            // Add to total revenue if there's a highest bid
            if (product.getHighestBid() != null) {
                BigDecimal totalRevenue = (BigDecimal) metrics.get("totalRevenue");
                metrics.put("totalRevenue", totalRevenue.add(product.getHighestBid()));
            }
        }

        // Get bid count for this product
        List<Bid> productBids = bidRepository.findAllBidsByProductIdOrderByBidAmountAsc(product.getId());
        metrics.put("totalBids", (Integer) metrics.get("totalBids") + productBids.size());
    }

    /**
     * Calculate final metrics (averages, rates, etc.)
     */
    private void finalizeMetrics(Map<String, Map<String, Object>> allMetrics) {
        for (Map.Entry<String, Map<String, Object>> entry : allMetrics.entrySet()) {
            Map<String, Object> metrics = entry.getValue();

            int listings = (Integer) metrics.get("listings");
            int soldItems = (Integer) metrics.get("soldItems");
            int totalBids = (Integer) metrics.get("totalBids");
            BigDecimal totalRevenue = (BigDecimal) metrics.get("totalRevenue");

            // Calculate average bids per listing
            double avgBidsPerListing = listings > 0 ? (double) totalBids / listings : 0;
            metrics.put("averageBidsPerListing", Math.round(avgBidsPerListing * 10) / 10.0);

            // Calculate success rate
            double successRate = listings > 0 ? (double) soldItems / listings * 100 : 0;
            metrics.put("successRate", Math.round(successRate * 10) / 10.0);

            // Calculate average sale price
            BigDecimal avgSalePrice = soldItems > 0 ?
                    totalRevenue.divide(new BigDecimal(soldItems), 2, RoundingMode.HALF_UP) :
                    BigDecimal.ZERO;
            metrics.put("averageSalePrice", avgSalePrice);
        }
    }
}