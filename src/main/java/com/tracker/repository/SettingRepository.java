package com.tracker.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tracker.entity.AppSetting;

public interface SettingRepository extends JpaRepository<AppSetting, String> {
}
