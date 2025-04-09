package com.youbid.fyp.controller;

import com.youbid.fyp.model.Activity;
import com.youbid.fyp.model.User;
import com.youbid.fyp.service.ActivityService;
import com.youbid.fyp.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/activities")
@PreAuthorize("hasRole('ADMIN')")
public class AdminActivityController {

    @Autowired
    private ActivityService activityService;

    @Autowired
    private UserService userService;

    @GetMapping("/recent")
    public ResponseEntity<?> getRecentActivities(
            @RequestParam(value = "limit", defaultValue = "5") int limit,
            @RequestHeader("Authorization") String jwt) {
        try {
            User admin = userService.findUserByJwt(jwt);
            if (!"ADMIN".equals(admin.getRole())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Access denied"));
            }

            List<Map<String, Object>> activities = activityService.getRecentActivitiesWithRelativeTime(limit);
            return ResponseEntity.ok(activities);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAllActivities(@RequestHeader("Authorization") String jwt) {
        try {
            User admin = userService.findUserByJwt(jwt);
            if (!"ADMIN".equals(admin.getRole())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Access denied"));
            }

            List<Activity> activities = activityService.getAllActivities();
            return ResponseEntity.ok(activities);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<?> getActivitiesByType(
            @PathVariable String type,
            @RequestHeader("Authorization") String jwt) {
        try {
            User admin = userService.findUserByJwt(jwt);
            if (!"ADMIN".equals(admin.getRole())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Access denied"));
            }

            List<Activity> activities = activityService.getActivitiesByType(type);
            return ResponseEntity.ok(activities);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // Optional: Endpoint to manually create an activity (for testing)
    @PostMapping("/create")
    public ResponseEntity<?> createActivity(
            @RequestBody Map<String, String> activityData,
            @RequestHeader("Authorization") String jwt) {
        try {
            User admin = userService.findUserByJwt(jwt);
            if (!"ADMIN".equals(admin.getRole())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Access denied"));
            }

            String type = activityData.get("type");
            String title = activityData.get("title");
            String description = activityData.get("description");

            Activity activity = activityService.trackGenericActivity(type, title, description);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(activity);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}