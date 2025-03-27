package com.youbid.fyp.controller;

import com.youbid.fyp.model.Product;
import com.youbid.fyp.service.RecommendationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/public/recommendations")
public class PublicRecommendationController {

    @Autowired
    private RecommendationService recommendationService;

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
}