package com.tracker.notification;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
public class EmailChannel implements NotificationChannel {

    private final ObjectProvider<JavaMailSender> mailSender;
    private final String to;
    private final String from;

    public EmailChannel(ObjectProvider<JavaMailSender> mailSender,
                        @Value("${tracker.email.to:}") String to,
                        @Value("${tracker.email.from:}") String from) {
        this.mailSender = mailSender;
        this.to = to;
        this.from = from;
    }

    @Override
    public String name() {
        return "Email";
    }

    @Override
    public boolean isEnabled() {
        return !to.isBlank() && mailSender.getIfAvailable() != null;
    }

    @Override
    public void send(String message) {
        SimpleMailMessage mail = new SimpleMailMessage();
        mail.setTo(to);
        if (!from.isBlank()) {
            mail.setFrom(from);
        }
        mail.setSubject("Your daily spending summary");
        mail.setText(message);
        mailSender.getObject().send(mail);
    }
}
