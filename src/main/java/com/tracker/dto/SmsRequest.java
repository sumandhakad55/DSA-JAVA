package com.tracker.dto;

import jakarta.validation.constraints.NotBlank;

public record SmsRequest(@NotBlank(message = "Message text is required") String text) {
}
