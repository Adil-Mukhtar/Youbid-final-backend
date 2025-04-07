package com.youbid.fyp.repository;

import com.youbid.fyp.model.Reward;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RewardRepository extends JpaRepository<Reward, Integer> {

    // Find active rewards
    List<Reward> findByIsActiveTrue();

    // Find rewards by type
    List<Reward> findByTypeAndIsActiveTrue(String type);

    // Find rewards by point cost range
    List<Reward> findByPointsCostLessThanEqualAndIsActiveTrueOrderByPointsCostAsc(Integer maxPoints);
}