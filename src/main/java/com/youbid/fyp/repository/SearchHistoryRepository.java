package com.youbid.fyp.repository;

import com.youbid.fyp.model.SearchHistory;
import com.youbid.fyp.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SearchHistoryRepository extends JpaRepository<SearchHistory, Integer> {

    // Find search history by user with most recent first
    List<SearchHistory> findByUserOrderBySearchTimeDesc(User user);

    // Find by user, query, category, and location - used to update existing entries rather than create duplicates
    Optional<SearchHistory> findByUserAndQueryAndCategoryAndLocation(
            User user, String query, String category, String location);

    // Find top search terms for a user
    @Query("SELECT sh.query FROM SearchHistory sh WHERE sh.user = :user AND sh.query IS NOT NULL AND sh.query != '' " +
            "GROUP BY sh.query ORDER BY SUM(sh.clickCount) DESC")
    List<String> findTopQueriesByUser(@Param("user") User user);

    // Find top categories for a user
    @Query("SELECT sh.category FROM SearchHistory sh WHERE sh.user = :user AND sh.category IS NOT NULL AND sh.category != '' " +
            "GROUP BY sh.category ORDER BY SUM(sh.clickCount) DESC")
    List<String> findTopCategoriesByUser(@Param("user") User user);

    // Find top locations for a user
    @Query("SELECT sh.location FROM SearchHistory sh WHERE sh.user = :user AND sh.location IS NOT NULL AND sh.location != '' " +
            "GROUP BY sh.location ORDER BY SUM(sh.clickCount) DESC")
    List<String> findTopLocationsByUser(@Param("user") User user);

    // Find recent searches limited by count
    @Query("SELECT sh FROM SearchHistory sh WHERE sh.user = :user ORDER BY sh.searchTime DESC")
    List<SearchHistory> findRecentSearchesByUser(@Param("user") User user, org.springframework.data.domain.Pageable pageable);
}