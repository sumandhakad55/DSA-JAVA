package com.tracker.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record SettingsRequest(
        @NotNull(message = "Opening balance is required") BigDecimal startingBalance,
        @NotNull(message = "Monthly budget is required") @PositiveOrZero BigDecimal monthlyBudget) {
}
