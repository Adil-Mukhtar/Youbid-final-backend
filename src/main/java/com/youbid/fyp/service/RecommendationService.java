package com.youbid.fyp.service;

import com.youbid.fyp.model.Product;
import com.youbid.fyp.model.User;

import java.util.List;

public interface RecommendationService {

    /**
     * Track a user's search history
     *
     * @param userId User ID
     * @param query Search query
     * @param category Category filter
     * @param location Location filter
     * @param minPrice Minimum price filter
     * @param maxPrice Maximum price filter
     */
    void trackSearch(Integer userId, String query, String category, String location, Double minPrice, Double maxPrice);

    /**
     * Track a user's product click
     *
     * @param userId User ID
     * @param productId Product ID
     */
    void trackProductClick(Integer userId, Integer productId);

    /**
     * Get recommended products for a user based on their search history
     *
     * @param userId User ID
     * @param limit Maximum number of recommendations to return
     * @return List of recommended products
     */
    List<Product> getRecommendedProducts(Integer userId, int limit);

    /**
     * Get trending products across all users
     *
     * @param limit Maximum number of trending products to return
     * @return List of trending products
     */
    List<Product> getTrendingProducts(int limit);

    /**
     * Get similar products to a specific product
     *
     * @param productId Product ID
     * @param limit Maximum number of similar products to return
     * @return List of similar products
     */
    List<Product> getSimilarProducts(Integer productId, int limit);
}