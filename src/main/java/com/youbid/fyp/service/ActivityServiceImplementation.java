package com.youbid.fyp.service;

import com.youbid.fyp.model.Activity;
import com.youbid.fyp.repository.ActivityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
public class ActivityServiceImplementation implements ActivityService {

    @Autowired
    private ActivityRepository activityRepository;

    @Override
    public Activity trackUserActivity(String title, String description, Integer userId) {
        Activity activity = new Activity("user", title, description);
        activity.setUserId(userId);
        return activityRepository.save(activity);
    }

    @Override
    public Activity trackProductActivity(String title, String description, Integer productId) {
        Activity activity = new Activity("listing", title, description);
        activity.setProductId(productId);
        return activityRepository.save(activity);
    }

    @Override
    public Activity trackSupportActivity(String title, String description, Integer supportId) {
        Activity activity = new Activity("support", title, description);
        activity.setSupportId(supportId);
        return activityRepository.save(activity);
    }

    @Override
    public Activity trackGenericActivity(String type, String title, String description) {
        Activity activity = new Activity(type, title, description);
        return activityRepository.save(activity);
    }

    @Override
    public List<Activity> getRecentActivities(int limit) {
        return activityRepository.findRecentActivities(PageRequest.of(0, limit));
    }

    @Override
    public List<Activity> getAllActivities() {
        return activityRepository.findByOrderByTimestampDesc();
    }

    @Override
    public List<Activity> getActivitiesByType(String type) {
        return activityRepository.findByTypeOrderByTimestampDesc(type);
    }

    @Override
    public List<Map<String, Object>> getRecentActivitiesWithRelativeTime(int limit) {
        List<Activity> activities = getRecentActivities(limit);
        List<Map<String, Object>> result = new ArrayList<>();

        for (Activity activity : activities) {
            Map<String, Object> activityMap = new HashMap<>();
            activityMap.put("id", activity.getId());
            activityMap.put("type", activity.getType());
            activityMap.put("title", activity.getTitle());
            activityMap.put("description", activity.getDescription());
            activityMap.put("time", getRelativeTimeString(activity.getTimestamp()));
            activityMap.put("userId", activity.getUserId());
            activityMap.put("productId", activity.getProductId());
            activityMap.put("supportId", activity.getSupportId());
            activityMap.put("timestamp", activity.getTimestamp());

            result.add(activityMap);
        }

        return result;
    }

    @Override
    public String getRelativeTimeString(LocalDateTime timestamp) {
        if (timestamp == null) {
            return "Unknown time";
        }

        LocalDateTime now = LocalDateTime.now();
        long minutesAgo = ChronoUnit.MINUTES.between(timestamp, now);
        long hoursAgo = ChronoUnit.HOURS.between(timestamp, now);
        long daysAgo = ChronoUnit.DAYS.between(timestamp, now);
        long weeksAgo = daysAgo / 7;
        long monthsAgo = ChronoUnit.MONTHS.between(timestamp, now);
        long yearsAgo = ChronoUnit.YEARS.between(timestamp, now);

        if (minutesAgo < 1) {
            return "Just now";
        } else if (minutesAgo < 60) {
            return minutesAgo + (minutesAgo == 1 ? " minute ago" : " minutes ago");
        } else if (hoursAgo < 24) {
            return hoursAgo + (hoursAgo == 1 ? " hour ago" : " hours ago");
        } else if (daysAgo < 7) {
            return daysAgo + (daysAgo == 1 ? " day ago" : " days ago");
        } else if (weeksAgo < 4) {
            return weeksAgo + (weeksAgo == 1 ? " week ago" : " weeks ago");
        } else if (monthsAgo < 12) {
            return monthsAgo + (monthsAgo == 1 ? " month ago" : " months ago");
        } else {
            return yearsAgo + (yearsAgo == 1 ? " year ago" : " years ago");
        }
    }
}