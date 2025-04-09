package com.youbid.fyp.repository;

import com.youbid.fyp.model.Activity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ActivityRepository extends JpaRepository<Activity, Integer> {

    // Find recent activities ordered by timestamp
    List<Activity> findByOrderByTimestampDesc();

    // Find limited number of recent activities
    @Query("SELECT a FROM Activity a ORDER BY a.timestamp DESC")
    List<Activity> findRecentActivities(org.springframework.data.domain.Pageable pageable);

    // Find activities by type
    List<Activity> findByTypeOrderByTimestampDesc(String type);

    // Find activities related to a specific user
    List<Activity> findByUserIdOrderByTimestampDesc(Integer userId);

    // Find activities related to a specific product
    List<Activity> findByProductIdOrderByTimestampDesc(Integer productId);

    // Find activities in a date range
    List<Activity> findByTimestampBetweenOrderByTimestampDesc(LocalDateTime start, LocalDateTime end);

    // Search activities
    @Query("SELECT a FROM Activity a WHERE LOWER(a.title) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(a.description) LIKE LOWER(CONCAT('%', :query, '%')) ORDER BY a.timestamp DESC")
    List<Activity> searchActivities(@Param("query") String query);
}