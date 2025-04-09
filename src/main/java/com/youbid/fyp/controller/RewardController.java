package com.youbid.fyp.controller;

import com.youbid.fyp.model.Reward;
import com.youbid.fyp.model.User;
import com.youbid.fyp.model.UserReward;
import com.youbid.fyp.service.RewardService;
import com.youbid.fyp.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/rewards")
public class RewardController {

    @Autowired
    private RewardService rewardService;

    @Autowired
    private UserService userService;

    @GetMapping
    public ResponseEntity<?> getAllRewards() {
        try {
            List<Reward> rewards = rewardService.getActiveRewards();
            return ResponseEntity.ok(rewards);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/available")
    public ResponseEntity<?> getAvailableRewards(@RequestHeader("Authorization") String jwt) {
        try {
            User user = userService.findUserByJwt(jwt);
            List<Reward> rewards = rewardService.getAvailableRewardsForUser(user.getId());
            return ResponseEntity.ok(rewards);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/my-rewards")
    public ResponseEntity<?> getUserRewards(@RequestHeader("Authorization") String jwt) {
        try {
            User user = userService.findUserByJwt(jwt);
            List<UserReward> rewards = rewardService.getUserRewards(user.getId());
            return ResponseEntity.ok(rewards);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/redeem/{rewardId}")
    public ResponseEntity<?> redeemReward(
            @PathVariable Integer rewardId,
            @RequestHeader("Authorization") String jwt) {
        try {
            User user = userService.findUserByJwt(jwt);
            UserReward reward = rewardService.redeemReward(user.getId(), rewardId);
            return ResponseEntity.ok(reward);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // Updated endpoint to apply reward to product with better logging
    @PostMapping("/apply")
    public ResponseEntity<?> applyReward(
            @RequestBody Map<String, Object> request,
            @RequestHeader("Authorization") String jwt) {
        try {
            User user = userService.findUserByJwt(jwt);

            // Extract parameters and validate
            String redemptionCode = (String) request.get("redemptionCode");
            Integer productId = (Integer) request.get("productId");

            if (redemptionCode == null || productId == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Redemption code and product ID are required"));
            }

            System.out.println("Applying reward with code " + redemptionCode + " to product " + productId);

            // Apply the reward
            Map<String, Object> result = rewardService.applyRewardToProduct(redemptionCode, productId);

            System.out.println("Reward application result: " + result);

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            System.err.println("Error applying reward: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // Add a new endpoint to fetch a reward by code
    @GetMapping("/by-code/{code}")
    public ResponseEntity<?> getRewardByCode(
            @PathVariable String code,
            @RequestHeader("Authorization") String jwt) {
        try {
            User user = userService.findUserByJwt(jwt);

            // Fetch user rewards
            List<UserReward> userRewards = rewardService.getUserRewards(user.getId());

            // Find the reward with the matching code
            Optional<UserReward> matchingReward = userRewards.stream()
                    .filter(r -> r.getRedemptionCode().equals(code) && !r.getIsUsed())
                    .findFirst();

            if (matchingReward.isPresent()) {
                return ResponseEntity.ok(matchingReward.get());
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Reward not found or already used"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // Admin endpoints for managing rewards

    @PostMapping("/admin/create")
    public ResponseEntity<?> createReward(
            @RequestBody Reward reward,
            @RequestHeader("Authorization") String jwt) {
        try {
            User admin = userService.findUserByJwt(jwt);
            if (!"ADMIN".equals(admin.getRole())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Only admins can create rewards"));
            }

            Reward createdReward = rewardService.createReward(reward);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdReward);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/admin/update/{rewardId}")
    public ResponseEntity<?> updateReward(
            @PathVariable Integer rewardId,
            @RequestBody Reward reward,
            @RequestHeader("Authorization") String jwt) {
        try {
            User admin = userService.findUserByJwt(jwt);
            if (!"ADMIN".equals(admin.getRole())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Only admins can update rewards"));
            }

            Reward updatedReward = rewardService.updateReward(reward, rewardId);
            return ResponseEntity.ok(updatedReward);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/admin/deactivate/{rewardId}")
    public ResponseEntity<?> deactivateReward(
            @PathVariable Integer rewardId,
            @RequestHeader("Authorization") String jwt) {
        try {
            User admin = userService.findUserByJwt(jwt);
            if (!"ADMIN".equals(admin.getRole())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Only admins can deactivate rewards"));
            }

            Reward deactivatedReward = rewardService.deactivateReward(rewardId);
            return ResponseEntity.ok(deactivatedReward);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/admin/delete/{rewardId}")
    public ResponseEntity<?> deleteReward(
            @PathVariable Integer rewardId,
            @RequestHeader("Authorization") String jwt) {
        try {
            User admin = userService.findUserByJwt(jwt);
            if (!"ADMIN".equals(admin.getRole())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Only admins can delete rewards"));
            }

            rewardService.deleteReward(rewardId);
            return ResponseEntity.ok(Map.of("message", "Reward deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/admin/statistics")
    public ResponseEntity<?> getRewardStatistics(@RequestHeader("Authorization") String jwt) {
        try {
            User admin = userService.findUserByJwt(jwt);
            if (!"ADMIN".equals(admin.getRole())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Only admins can view reward statistics"));
            }

            Map<String, Object> statistics = rewardService.getRewardStatistics();
            return ResponseEntity.ok(statistics);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // Specialized endpoints for specific reward types
    @PostMapping("/features/apply-featured")
    public ResponseEntity<?> applyFeaturedListing(
            @RequestBody Map<String, Object> request,
            @RequestHeader("Authorization") String jwt) {
        try {
            // Mark the reward type specifically for featured listings
            request.put("rewardType", "FEATURED_LISTING");
            System.out.println("Applying Featured Listing reward");

            // Use the same application logic
            return applyReward(request, jwt);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/features/apply-exclusive")
    public ResponseEntity<?> applyExclusiveAccess(
            @RequestBody Map<String, Object> request,
            @RequestHeader("Authorization") String jwt) {
        try {
            // Mark the reward type specifically for exclusive access
            request.put("rewardType", "EXCLUSIVE_ACCESS");
            System.out.println("Applying Exclusive Access reward");

            // Use the same application logic
            return applyReward(request, jwt);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/features/apply-discount")
    public ResponseEntity<?> applyDiscount(
            @RequestBody Map<String, Object> request,
            @RequestHeader("Authorization") String jwt) {
        try {
            // Mark the reward type specifically for discount
            request.put("rewardType", "DISCOUNT");
            System.out.println("Applying Discount reward");

            // Use the same application logic
            return applyReward(request, jwt);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}