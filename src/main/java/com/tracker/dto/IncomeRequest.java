package com.tracker.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.*;

public record IncomeRequest(
        @NotNull(message = "Amount is required") @Positive(message = "Amount must be more than 0")
        @Digits(integer = 10, fraction = 2) BigDecimal amount,
        LocalDate receivedOn,
        @NotBlank(message = "Source is required") @Size(max = 80) String source) {
}
