package com.youbid.fyp.repository;

import com.youbid.fyp.model.LoyaltySettings;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LoyaltySettingsRepository extends JpaRepository<LoyaltySettings, Integer> {
    LoyaltySettings findBySettingKey(String settingKey);
}