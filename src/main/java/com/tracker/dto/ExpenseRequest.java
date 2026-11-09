package com.tracker.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.validation.constraints.*;

public record ExpenseRequest(
        @NotNull(message = "Amount is required") @Positive(message = "Amount must be more than 0")
        @Digits(integer = 10, fraction = 2, message = "Amount can have at most 2 decimal places")
        BigDecimal amount,
        LocalDateTime spentAt,
        @NotBlank(message = "App or shop name is required") @Size(max = 80) String merchant,
        @NotBlank(message = "Category is required") @Size(max = 40) String category,
        @NotBlank(message = "Payment method is required") @Size(max = 30) String paymentMethod,
        @Size(max = 255) String note) {
}
