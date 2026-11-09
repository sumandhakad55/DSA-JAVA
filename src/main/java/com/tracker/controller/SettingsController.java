package com.tracker.controller;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import com.tracker.dto.RuleRequest;
import com.tracker.dto.SettingsRequest;
import com.tracker.entity.MerchantRule;
import com.tracker.service.RuleService;
import com.tracker.service.SettingsService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class SettingsController {

    private final SettingsService settings;
    private final RuleService rules;

    @GetMapping("/settings")
    public Map<String, BigDecimal> get() {
        Map<String, BigDecimal> out = new LinkedHashMap<>();
        out.put("startingBalance", settings.getDecimal(SettingsService.STARTING_BALANCE, BigDecimal.ZERO));
        out.put("monthlyBudget", settings.getDecimal(SettingsService.MONTHLY_BUDGET, BigDecimal.ZERO));
        return out;
    }

    @PutMapping("/settings")
    public Map<String, BigDecimal> update(@Valid @RequestBody SettingsRequest request) {
        settings.put(SettingsService.STARTING_BALANCE, request.startingBalance());
        settings.put(SettingsService.MONTHLY_BUDGET, request.monthlyBudget());
        return get();
    }

    @GetMapping("/rules")
    public List<MerchantRule> listRules() {
        return rules.all();
    }

    @PostMapping("/rules")
    @ResponseStatus(HttpStatus.CREATED)
    public MerchantRule addRule(@Valid @RequestBody RuleRequest request) {
        return rules.add(request);
    }

    @DeleteMapping("/rules/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteRule(@PathVariable Long id) {
        rules.delete(id);
    }
}
