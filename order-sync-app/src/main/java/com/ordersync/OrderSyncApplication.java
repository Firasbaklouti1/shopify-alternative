package com.ordersync;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Order Sync & Auto-Fulfillment App
 * 
 * This is a SEPARATE Spring Boot application that validates the App Platform.
 * It runs on port 8081 and interacts with the main platform (port 8080) via:
 * - HTTP APIs (using app-scoped access tokens)
 * - Webhooks (receiving ORDER_CREATED, ORDER_PAID events)
 * 
 * This app demonstrates:
 * 1. Receiving webhooks from the platform
 * 2. Calling platform APIs with scoped tokens
 * 3. Auto-fulfilling orders based on external confirmation
 */
@SpringBootApplication
@EnableAsync
@EnableScheduling
public class OrderSyncApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrderSyncApplication.class, args);
    }
}
