package com.youbid.fyp.controller;

import com.youbid.fyp.model.Product;
import com.youbid.fyp.model.User;
import com.youbid.fyp.service.BidServiceExtension;
import com.youbid.fyp.service.ProductServiceExtension;
import com.youbid.fyp.service.LoyaltyService;
import com.youbid.fyp.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/products")
public class EnhancedProductController {

    @Autowired
    private ProductServiceExtension productServiceExtension;

    @Autowired
    private BidServiceExtension bidServiceExtension;

    @Autowired
    private UserService userService;

    @Autowired
    private LoyaltyService loyaltyService;

    /**
     * Get all products with featured products shown first
     */
    @GetMapping("/featured")
    public ResponseEntity<List<Product>> getFeaturedProducts() {
        List<Product> products = productServiceExtension.findAllProductsWithFeaturedFirst();
        return new ResponseEntity<>(products, HttpStatus.OK);
    }

    /**
     * Get exclusive products for the user's loyalty tier
     */
    @GetMapping("/exclusive")
    public ResponseEntity<?> getExclusiveProducts(@RequestHeader("Authorization") String jwt) {
        try {
            User user = userService.findUserByJwt(jwt);
            Map<String, Object> loyaltyStatus = loyaltyService.getUserLoyaltyStatus(user.getId());
            String userTier = (String) loyaltyStatus.get("tier");

            List<Product> exclusiveProducts = productServiceExtension.findExclusiveProducts(userTier);

            Map<String, Object> response = new HashMap<>();
            response.put("userTier", userTier);
            response.put("exclusiveProducts", exclusiveProducts);

            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(Map.of("error", e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Get products with active discounts
     */
    @GetMapping("/discounted")
    public ResponseEntity<List<Product>> getDiscountedProducts() {
        List<Product> products = productServiceExtension.findDiscountedProducts();
        return new ResponseEntity<>(products, HttpStatus.OK);
    }

    /**
     * Check if a user is eligible to bid on a product (for exclusive auctions)
     */
    @GetMapping("/{productId}/bid-eligibility")
    public ResponseEntity<?> checkBidEligibility(
            @PathVariable Integer productId,
            @RequestHeader("Authorization") String jwt) {
        try {
            User user = userService.findUserByJwt(jwt);
            Map<String, Object> eligibility = bidServiceExtension.checkBidEligibility(productId, user.getId());
            return new ResponseEntity<>(eligibility, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(Map.of("error", e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Get public information about featured products
     */
    @GetMapping("/public/featured")
    public ResponseEntity<List<Product>> getPublicFeaturedProducts() {
        List<Product> products = productServiceExtension.findAllProductsWithFeaturedFirst()
                .stream()
                .filter(Product::hasFeaturedStatus)
                .collect(Collectors.toList()); // Using collect(Collectors.toList()) instead of toList() for better compatibility
        return new ResponseEntity<>(products, HttpStatus.OK);
    }
}