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

    @PostMapping("/apply")
    public ResponseEntity<?> applyReward(
            @RequestBody Map<String, Object> request,
            @RequestHeader("Authorization") String jwt) {
        try {
            User user = userService.findUserByJwt(jwt);
            String redemptionCode = (String) request.get("redemptionCode");
            Integer productId = (Integer) request.get("productId");

            Map<String, Object> result = rewardService.applyRewardToProduct(redemptionCode, productId);
            return ResponseEntity.ok(result);
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
                        .body(Map.of("error", "Access denied"));
            }

            Reward newReward = rewardService.createReward(reward);
            return ResponseEntity.status(HttpStatus.CREATED).body(newReward);
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
                        .body(Map.of("error", "Access denied"));
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
                        .body(Map.of("error", "Access denied"));
            }

            Reward deactivatedReward = rewardService.deactivateReward(rewardId);
            return ResponseEntity.ok(deactivatedReward);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}