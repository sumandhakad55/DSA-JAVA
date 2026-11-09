package com.tracker.controller;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tracker.notification.NotificationService;
import com.tracker.service.SummaryService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notifications;
    private final SummaryService summary;

    /** Sends tonight's message right now so you can check that Telegram / email works. */
    @PostMapping("/test")
    public Map<String, Object> test() {
        String message = "(Test) " + summary.nightlyMessage();
        Map<String, Object> result = new LinkedHashMap<>(notifications.broadcast(message));
        result.put("message", message);
        return result;
    }
}
