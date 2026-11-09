package com.tracker;

import java.util.TimeZone;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ExpenseTrackerApplication {

    public static void main(String[] args) {
        // "Today" and the nightly report should follow Indian time
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Kolkata"));
        SpringApplication.run(ExpenseTrackerApplication.class, args);
    }
}
