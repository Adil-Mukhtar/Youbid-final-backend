package com.youbid.fyp.controller;

import com.youbid.fyp.model.User;
import com.youbid.fyp.service.UserAnalyticsService;
import com.youbid.fyp.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controller for user-focused analytics
 */
@RestController
@RequestMapping("/api/user-analytics")
public class UserAnalyticsController {

    @Autowired
    private UserAnalyticsService userAnalyticsService;

    @Autowired
    private UserService userService;

    /**
     * Get seller analytics for the current user
     */
    @GetMapping("/seller")
    public ResponseEntity<?> getSellerAnalytics(@RequestHeader("Authorization") String jwt) {
        try {
            User user = userService.findUserByJwt(jwt);
            Map<String, Object> analytics = userAnalyticsService.getSellerAnalytics(user.getId());
            return new ResponseEntity<>(analytics, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(Map.of("error", e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Get bidding analytics for a specific product
     */
    @GetMapping("/product/{productId}")
    public ResponseEntity<?> getProductAnalytics(
            @PathVariable Integer productId,
            @RequestHeader("Authorization") String jwt) {
        try {
            User user = userService.findUserByJwt(jwt);
            Map<String, Object> analytics = userAnalyticsService.getProductBidAnalytics(productId, user.getId());
            return new ResponseEntity<>(analytics, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(Map.of("error", e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Get engagement metrics by category and location
     */
    @GetMapping("/engagement")
    public ResponseEntity<?> getEngagementMetrics(@RequestHeader("Authorization") String jwt) {
        try {
            User user = userService.findUserByJwt(jwt);
            Map<String, Object> metrics = userAnalyticsService.getEngagementMetrics(user.getId());
            return new ResponseEntity<>(metrics, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(Map.of("error", e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}