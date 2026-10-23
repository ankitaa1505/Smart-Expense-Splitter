package com.expensetracker;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.TimeZone;

@SpringBootApplication
@EnableCaching
@EnableScheduling
public class SmartExpenseTrackerApplication {

    public static void main(String[] args) {
        SpringApplication.run(SmartExpenseTrackerApplication.class, args);
    }

    @PostConstruct
    public void init() {
        // Enforce UTC timezone across all application date/time operations
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    }
}
