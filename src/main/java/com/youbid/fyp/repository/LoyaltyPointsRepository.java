package com.youbid.fyp.repository;

import com.youbid.fyp.model.LoyaltyPoints;
import com.youbid.fyp.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface LoyaltyPointsRepository extends JpaRepository<LoyaltyPoints, Integer> {

    // Find all points transactions for a user
    List<LoyaltyPoints> findByUserOrderByTimestampDesc(User user);

    // Find all earnings for a user
    List<LoyaltyPoints> findByUserAndTransactionTypeOrderByTimestampDesc(User user, String transactionType);

    // Calculate total points a user has earned
    @Query("SELECT SUM(lp.points) FROM LoyaltyPoints lp WHERE lp.user = :user AND lp.transactionType = 'EARNED'")
    Integer getTotalPointsEarned(@Param("user") User user);

    // Calculate total points a user has redeemed
    @Query("SELECT SUM(lp.points) FROM LoyaltyPoints lp WHERE lp.user = :user AND lp.transactionType = 'REDEEMED'")
    Integer getTotalPointsRedeemed(@Param("user") User user);

    // Get recent points transactions
    List<LoyaltyPoints> findByUserAndTimestampAfterOrderByTimestampDesc(User user, LocalDateTime since);
}