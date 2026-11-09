package com.tracker.notification;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.tracker.service.SummaryService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DailyReportScheduler {

    private final SummaryService summary;
    private final NotificationService notifications;

    /** Runs every night. Change the time with tracker.notification.cron in application.properties. */
    @Scheduled(cron = "${tracker.notification.cron:0 0 21 * * *}",
               zone = "${tracker.notification.zone:Asia/Kolkata}")
    public void sendDailyReport() {
        notifications.broadcast(summary.nightlyMessage());
    }
}
