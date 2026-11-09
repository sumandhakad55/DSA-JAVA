package com.tracker.service;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;

import com.tracker.entity.AppSetting;
import com.tracker.repository.SettingRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SettingsService {

    public static final String STARTING_BALANCE = "starting_balance";
    public static final String MONTHLY_BUDGET = "monthly_budget";

    private final SettingRepository repo;

    public BigDecimal getDecimal(String key, BigDecimal fallback) {
        return repo.findById(key)
                .map(s -> {
                    try {
                        return new BigDecimal(s.getSettingValue());
                    } catch (NumberFormatException e) {
                        return fallback;
                    }
                })
                .orElse(fallback);
    }

    public void put(String key, BigDecimal value) {
        repo.save(new AppSetting(key, value.toPlainString()));
    }
}
