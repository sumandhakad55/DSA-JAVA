package com.tracker.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tracker.entity.MerchantRule;

public interface RuleRepository extends JpaRepository<MerchantRule, Long> {

    boolean existsByKeywordIgnoreCase(String keyword);
}
