package com.tracker.notification;

/** One way of delivering the nightly message. Add WhatsApp/SMS later by implementing this. */
public interface NotificationChannel {

    String name();

    boolean isEnabled();

    void send(String message) throws Exception;
}
