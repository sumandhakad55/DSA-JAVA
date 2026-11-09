package com.tracker.notification;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final List<NotificationChannel> channels;

    public NotificationService(List<NotificationChannel> channels) {
        this.channels = channels;
    }

    /** Sends to every channel that is set up. Returns which ones worked and which failed. */
    public Map<String, Object> broadcast(String message) {
        List<String> sent = new ArrayList<>();
        List<String> failed = new ArrayList<>();
        for (NotificationChannel ch : channels) {
            if (!ch.isEnabled()) {
                continue;
            }
            try {
                ch.send(message);
                sent.add(ch.name());
            } catch (Exception e) {
                log.error("Could not send via {}", ch.name(), e);
                failed.add(ch.name() + ": " + e.getMessage());
            }
        }
        if (sent.isEmpty() && failed.isEmpty()) {
            log.warn("No notification channel is set up. Add Telegram or email details in application.properties.");
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("sent", sent);
        result.put("failed", failed);
        return result;
    }
}
