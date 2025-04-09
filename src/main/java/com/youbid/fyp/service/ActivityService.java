package com.youbid.fyp.service;

import com.youbid.fyp.model.Activity;
import com.youbid.fyp.model.User;
import com.youbid.fyp.model.Product;

import java.util.List;
import java.util.Map;

public interface ActivityService {

    // Track different types of activities
    Activity trackUserActivity(String title, String description, Integer userId);
    Activity trackProductActivity(String title, String description, Integer productId);
    Activity trackSupportActivity(String title, String description, Integer supportId);
    Activity trackGenericActivity(String type, String title, String description);

    // Get recent activities
    List<Activity> getRecentActivities(int limit);

    // Get all activities
    List<Activity> getAllActivities();

    // Get activities by type
    List<Activity> getActivitiesByType(String type);

    // Get activities with relative time (e.g., "2 hours ago")
    List<Map<String, Object>> getRecentActivitiesWithRelativeTime(int limit);

    // Format timestamp to relative time
    String getRelativeTimeString(java.time.LocalDateTime timestamp);
}