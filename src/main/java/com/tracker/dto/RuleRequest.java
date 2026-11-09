package com.tracker.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RuleRequest(
        @NotBlank(message = "Keyword is required") @Size(max = 60) String keyword,
        @NotBlank(message = "Merchant is required") @Size(max = 80) String merchant,
        @NotBlank(message = "Category is required") @Size(max = 40) String category) {
}
