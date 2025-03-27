package com.youbid.fyp.service;

import com.youbid.fyp.model.Product;
import com.youbid.fyp.model.SearchHistory;
import com.youbid.fyp.model.User;
import com.youbid.fyp.repository.ProductRepository;
import com.youbid.fyp.repository.SearchHistoryRepository;
import com.youbid.fyp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class RecommendationServiceImplementation implements RecommendationService {

    @Autowired
    private SearchHistoryRepository searchHistoryRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    private static final int MAX_RECENT_SEARCHES = 10;
    private static final double QUERY_WEIGHT = 0.5;
    private static final double CATEGORY_WEIGHT = 0.3;
    private static final double LOCATION_WEIGHT = 0.2;
    private static final int MAX_SEARCH_AGE_DAYS = 30;

    @Override
    @Transactional
    public void trackSearch(Integer userId, String query, String category, String location, Double minPrice, Double maxPrice) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Try to find an existing search with the same parameters
        Optional<SearchHistory> existingSearch = searchHistoryRepository
                .findByUserAndQueryAndCategoryAndLocation(user, query, category, location);

        if (existingSearch.isPresent()) {
            // Update existing search
            SearchHistory searchHistory = existingSearch.get();
            searchHistory.incrementClickCount();
            searchHistory.setSearchTime(LocalDateTime.now());
            searchHistory.setMinPrice(minPrice);
            searchHistory.setMaxPrice(maxPrice);
            searchHistoryRepository.save(searchHistory);
        } else {
            // Create new search history entry
            SearchHistory searchHistory = new SearchHistory();
            searchHistory.setUser(user);
            searchHistory.setQuery(query);
            searchHistory.setCategory(category);
            searchHistory.setLocation(location);
            searchHistory.setMinPrice(minPrice);
            searchHistory.setMaxPrice(maxPrice);
            searchHistory.setSearchTime(LocalDateTime.now());
            searchHistoryRepository.save(searchHistory);
        }
    }

    @Override
    @Transactional
    public void trackProductClick(Integer userId, Integer productId) {
        // This method could be expanded to track product clicks specifically
        // For now, we'll focus on search tracking
    }

    @Override
    public List<Product> getRecommendedProducts(Integer userId, int limit) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Get user's top search parameters
        List<String> topQueries = searchHistoryRepository.findTopQueriesByUser(user);
        List<String> topCategories = searchHistoryRepository.findTopCategoriesByUser(user);
        List<String> topLocations = searchHistoryRepository.findTopLocationsByUser(user);

        // If user has no search history, return trending products
        if (topQueries.isEmpty() && topCategories.isEmpty() && topLocations.isEmpty()) {
            return getTrendingProducts(limit);
        }

        // Build a scoring map for all products
        Map<Product, Double> productScores = new HashMap<>();

        // Score based on queries
        if (!topQueries.isEmpty()) {
            for (String query : topQueries.subList(0, Math.min(3, topQueries.size()))) {
                if (query != null && !query.trim().isEmpty()) {
                    List<Product> matchingProducts = productRepository.searchProducts(query, null, null, null, null);

                    for (Product product : matchingProducts) {
                        // Only consider live products
                        if ("live".equalsIgnoreCase(product.getStatus())) {
                            Double score = productScores.getOrDefault(product, 0.0);
                            productScores.put(product, score + QUERY_WEIGHT);
                        }
                    }
                }
            }
        }

        // Score based on categories
        if (!topCategories.isEmpty()) {
            for (String category : topCategories.subList(0, Math.min(3, topCategories.size()))) {
                if (category != null && !category.trim().isEmpty()) {
                    List<Product> matchingProducts = productRepository.searchProducts(null, null, category, null, null);

                    for (Product product : matchingProducts) {
                        if ("live".equalsIgnoreCase(product.getStatus())) {
                            Double score = productScores.getOrDefault(product, 0.0);
                            productScores.put(product, score + CATEGORY_WEIGHT);
                        }
                    }
                }
            }
        }

        // Score based on locations
        if (!topLocations.isEmpty()) {
            for (String location : topLocations.subList(0, Math.min(3, topLocations.size()))) {
                if (location != null && !location.trim().isEmpty()) {
                    List<Product> matchingProducts = productRepository.searchProducts(null, location, null, null, null);

                    for (Product product : matchingProducts) {
                        if ("live".equalsIgnoreCase(product.getStatus())) {
                            Double score = productScores.getOrDefault(product, 0.0);
                            productScores.put(product, score + LOCATION_WEIGHT);
                        }
                    }
                }
            }
        }

        // Sort products by score and return top matches
        return productScores.entrySet().stream()
                .sorted(Map.Entry.<Product, Double>comparingByValue().reversed())
                .limit(limit)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    @Override
    public List<Product> getTrendingProducts(int limit) {
        // For now, simply return the most recently added live products
        return productRepository.findAll().stream()
                .filter(p -> "live".equalsIgnoreCase(p.getStatus()))
                .sorted(Comparator.comparing(Product::getCreatedAt).reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }

    @Override
    public List<Product> getSimilarProducts(Integer productId, int limit) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        // Find products with the same category
        List<Product> similarProducts = productRepository.findAll().stream()
                .filter(p -> !p.getId().equals(productId)) // Exclude the current product
                .filter(p -> "live".equalsIgnoreCase(p.getStatus())) // Only live products
                .filter(p -> p.getCategory() != null && p.getCategory().equals(product.getCategory()))
                .sorted(Comparator.comparing(Product::getCreatedAt).reversed())
                .limit(limit)
                .collect(Collectors.toList());

        // If we don't have enough, add some from the same location
        if (similarProducts.size() < limit && product.getLocation() != null) {
            List<Product> locationProducts = productRepository.findAll().stream()
                    .filter(p -> !p.getId().equals(productId))
                    .filter(p -> "live".equalsIgnoreCase(p.getStatus()))
                    .filter(p -> !similarProducts.contains(p))
                    .filter(p -> p.getLocation() != null && p.getLocation().equals(product.getLocation()))
                    .sorted(Comparator.comparing(Product::getCreatedAt).reversed())
                    .limit(limit - similarProducts.size())
                    .collect(Collectors.toList());

            similarProducts.addAll(locationProducts);
        }

        // If we still don't have enough, add recent products
        if (similarProducts.size() < limit) {
            List<Product> recentProducts = productRepository.findAll().stream()
                    .filter(p -> !p.getId().equals(productId))
                    .filter(p -> "live".equalsIgnoreCase(p.getStatus()))
                    .filter(p -> !similarProducts.contains(p))
                    .sorted(Comparator.comparing(Product::getCreatedAt).reversed())
                    .limit(limit - similarProducts.size())
                    .collect(Collectors.toList());

            similarProducts.addAll(recentProducts);
        }

        return similarProducts;
    }
}