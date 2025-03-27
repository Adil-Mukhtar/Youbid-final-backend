package com.youbid.fyp.controller;

import com.youbid.fyp.model.Product;
import com.youbid.fyp.model.User;
import com.youbid.fyp.service.RecommendationService;
import com.youbid.fyp.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/recommendations")
public class RecommendationController {

    @Autowired
    private RecommendationService recommendationService;

    @Autowired
    private UserService userService;

    @GetMapping("/user")
    public ResponseEntity<List<Product>> getRecommendedProducts(
            @RequestParam(defaultValue = "4") int limit,
            @RequestHeader("Authorization") String jwt) {
        try {
            User user = userService.findUserByJwt(jwt);
            List<Product> recommendations = recommendationService.getRecommendedProducts(user.getId(), limit);
            return new ResponseEntity<>(recommendations, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/trending")
    public ResponseEntity<List<Product>> getTrendingProducts(
            @RequestParam(defaultValue = "4") int limit) {
        try {
            List<Product> trendingProducts = recommendationService.getTrendingProducts(limit);
            return new ResponseEntity<>(trendingProducts, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/similar/{productId}")
    public ResponseEntity<List<Product>> getSimilarProducts(
            @PathVariable Integer productId,
            @RequestParam(defaultValue = "4") int limit) {
        try {
            List<Product> similarProducts = recommendationService.getSimilarProducts(productId, limit);
            return new ResponseEntity<>(similarProducts, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/track/search")
    public ResponseEntity<?> trackSearch(
            @RequestBody Map<String, Object> searchData,
            @RequestHeader("Authorization") String jwt) {
        try {
            User user = userService.findUserByJwt(jwt);

            String query = (String) searchData.get("query");
            String category = (String) searchData.get("category");
            String location = (String) searchData.get("location");

            Double minPrice = null;
            if (searchData.get("minPrice") != null) {
                minPrice = Double.valueOf(searchData.get("minPrice").toString());
            }

            Double maxPrice = null;
            if (searchData.get("maxPrice") != null) {
                maxPrice = Double.valueOf(searchData.get("maxPrice").toString());
            }

            recommendationService.trackSearch(user.getId(), query, category, location, minPrice, maxPrice);

            return new ResponseEntity<>(Map.of("message", "Search tracked successfully"), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(Map.of("error", e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Public endpoint for trending products (no authentication required)
    @GetMapping("/public/trending")
    public ResponseEntity<List<Product>> getPublicTrendingProducts(
            @RequestParam(defaultValue = "4") int limit) {
        try {
            List<Product> trendingProducts = recommendationService.getTrendingProducts(limit);
            return new ResponseEntity<>(trendingProducts, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Public endpoint for similar products (no authentication required)
    @GetMapping("/public/similar/{productId}")
    public ResponseEntity<List<Product>> getPublicSimilarProducts(
            @PathVariable Integer productId,
            @RequestParam(defaultValue = "4") int limit) {
        try {
            List<Product> similarProducts = recommendationService.getSimilarProducts(productId, limit);
            return new ResponseEntity<>(similarProducts, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}