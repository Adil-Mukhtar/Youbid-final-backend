package com.youbid.fyp.controller;


import com.youbid.fyp.model.Product;
import com.youbid.fyp.model.Review;
import com.youbid.fyp.service.ProductService;
import com.youbid.fyp.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/public")
public class PublicController {

    @Autowired
    private ProductService productService;

    @Autowired
    private ReviewService reviewService;

    @GetMapping("/products")
    public ResponseEntity<List<Product>> getAllProducts() {
        List<Product> products = productService.findAllProducts();
        return new ResponseEntity<>(products, HttpStatus.OK);
    }

    @GetMapping("/products/{productId}")
    public ResponseEntity<Product> getProductById(@PathVariable Integer productId) {
        try {
            Product product = productService.findProductById(productId);
            return new ResponseEntity<>(product, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/products/search")
    public ResponseEntity<List<Product>> searchProducts(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String category) {
        try {
            List<Product> products = productService.searchProducts(query, location, category);
            return new ResponseEntity<>(products, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/reviews/SellerReviewsWithSentiment/{productId}")
    public ResponseEntity<Map<String, Object>> getSentimentAnalysisForProduct(@PathVariable Integer productId) {
        try {
            // Call the service to get sentiment analysis
            Map<String, Object> sentimentAnalysis = reviewService.getSentimentAnalysisForReviews(productId);
            return new ResponseEntity<>(sentimentAnalysis, HttpStatus.OK);
        } catch (Exception e) {
            // Return error response if something goes wrong
            return new ResponseEntity<>(Map.of("error", e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }




}
