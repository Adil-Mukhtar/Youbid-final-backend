package com.youbid.fyp.controller;

import com.youbid.fyp.model.Reward;
import com.youbid.fyp.model.User;
import com.youbid.fyp.service.RewardService;
import com.youbid.fyp.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/reward-management")  // Changed this to avoid conflicts
@PreAuthorize("hasRole('ADMIN')")
public class AdminRewardController {

    @Autowired
    private RewardService rewardService;

    @Autowired
    private UserService userService;

    @GetMapping("/all")
    public ResponseEntity<?> getAllRewards(@RequestHeader("Authorization") String jwt) {
        try {
            List<Reward> allRewards = rewardService.getAllRewards();
            return ResponseEntity.ok(allRewards);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/stats")
    public ResponseEntity<?> getRewardStats(@RequestHeader("Authorization") String jwt) {
        try {
            Map<String, Object> stats = rewardService.getRewardStatistics();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/create")
    public ResponseEntity<?> createReward(@RequestBody Reward reward, @RequestHeader("Authorization") String jwt) {
        try {
            Reward newReward = rewardService.createReward(reward);
            return ResponseEntity.status(HttpStatus.CREATED).body(newReward);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/update/{rewardId}")
    public ResponseEntity<?> updateReward(
            @PathVariable Integer rewardId,
            @RequestBody Reward reward,
            @RequestHeader("Authorization") String jwt) {
        try {
            Reward updatedReward = rewardService.updateReward(reward, rewardId);
            return ResponseEntity.ok(updatedReward);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/deactivate/{rewardId}")
    public ResponseEntity<?> deactivateReward(
            @PathVariable Integer rewardId,
            @RequestHeader("Authorization") String jwt) {
        try {
            Reward deactivatedReward = rewardService.deactivateReward(rewardId);
            return ResponseEntity.ok(deactivatedReward);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/delete/{rewardId}")
    public ResponseEntity<?> deleteReward(
            @PathVariable Integer rewardId,
            @RequestHeader("Authorization") String jwt) {
        try {
            rewardService.deleteReward(rewardId);
            return ResponseEntity.ok(Map.of("message", "Reward deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}