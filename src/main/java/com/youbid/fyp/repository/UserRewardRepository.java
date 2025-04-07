package com.youbid.fyp.repository;

import com.youbid.fyp.model.UserReward;
import com.youbid.fyp.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserRewardRepository extends JpaRepository<UserReward, Integer> {

    // Find rewards redeemed by a user
    List<UserReward> findByUserOrderByRedeemedAtDesc(User user);

    // Find active (not used) rewards for a user
    List<UserReward> findByUserAndIsUsedFalseAndExpiresAtAfterOrderByExpiresAtAsc(User user, LocalDateTime now);

    // Find reward by redemption code
    Optional<UserReward> findByRedemptionCode(String redemptionCode);

    // Find expired but unused rewards
    List<UserReward> findByIsUsedFalseAndExpiresAtBefore(LocalDateTime now);
}