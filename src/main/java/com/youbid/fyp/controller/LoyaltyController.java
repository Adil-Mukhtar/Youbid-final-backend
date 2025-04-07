package com.youbid.fyp.controller;

import com.youbid.fyp.model.LoyaltyPoints;
import com.youbid.fyp.model.User;
import com.youbid.fyp.service.LoyaltyService;
import com.youbid.fyp.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/loyalty")
public class LoyaltyController {

    @Autowired
    private LoyaltyService loyaltyService;

    @Autowired
    private UserService userService;

    @GetMapping("/points")
    public ResponseEntity<?> getUserPoints(@RequestHeader("Authorization") String jwt) {
        try {
            User user = userService.findUserByJwt(jwt);
            Integer points = loyaltyService.getUserPointsBalance(user.getId());

            Map<String, Object> response = new HashMap<>();
            response.put("userId", user.getId());
            response.put("pointsBalance", points);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/history")
    public ResponseEntity<?> getPointsHistory(@RequestHeader("Authorization") String jwt) {
        try {
            User user = userService.findUserByJwt(jwt);
            List<LoyaltyPoints> history = loyaltyService.getUserPointsHistory(user.getId());

            return ResponseEntity.ok(history);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/status")
    public ResponseEntity<?> getLoyaltyStatus(@RequestHeader("Authorization") String jwt) {
        try {
            User user = userService.findUserByJwt(jwt);
            Map<String, Object> status = loyaltyService.getUserLoyaltyStatus(user.getId());

            return ResponseEntity.ok(status);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // Admin endpoints for managing points

    @PostMapping("/admin/award")
    public ResponseEntity<?> awardPointsToUser(
            @RequestBody Map<String, Object> request,
            @RequestHeader("Authorization") String jwt) {
        try {
            User admin = userService.findUserByJwt(jwt);
            if (!"ADMIN".equals(admin.getRole())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Access denied"));
            }

            Integer userId = (Integer) request.get("userId");
            Integer points = (Integer) request.get("points");
            String source = (String) request.get("source");
            String description = (String) request.get("description");
            Integer productId = (Integer) request.get("productId");

            LoyaltyPoints result = loyaltyService.awardPoints(
                    userId, points, source, description, productId);

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/admin/deduct")
    public ResponseEntity<?> deductPointsFromUser(
            @RequestBody Map<String, Object> request,
            @RequestHeader("Authorization") String jwt) {
        try {
            User admin = userService.findUserByJwt(jwt);
            if (!"ADMIN".equals(admin.getRole())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Access denied"));
            }

            Integer userId = (Integer) request.get("userId");
            Integer points = (Integer) request.get("points");
            String source = (String) request.get("source");
            String description = (String) request.get("description");

            LoyaltyPoints result = loyaltyService.deductPoints(
                    userId, points, source, description);

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}