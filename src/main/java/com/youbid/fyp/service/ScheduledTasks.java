package com.youbid.fyp.service;

import com.youbid.fyp.model.UserReward;
import com.youbid.fyp.repository.UserRewardRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class ScheduledTasks {

    @Autowired
    private UserRewardRepository userRewardRepository;

    // Run daily at midnight
    @Scheduled(cron = "0 0 0 * * ?")
    public void cleanupExpiredRewards() {
        LocalDateTime now = LocalDateTime.now();
        List<UserReward> expiredRewards = userRewardRepository.findByIsUsedFalseAndExpiresAtBefore(now);

        for (UserReward reward : expiredRewards) {
            reward.setIsUsed(true); // Mark as used so it can't be applied
            userRewardRepository.save(reward);
        }

        System.out.println("Cleaned up " + expiredRewards.size() + " expired rewards");
    }
}