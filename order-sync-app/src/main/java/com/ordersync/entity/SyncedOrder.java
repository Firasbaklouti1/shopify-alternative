package com.ordersync.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Represents an order synced from the platform.
 * This is stored in the app's own database (H2).
 */
@Entity
@Table(name = "synced_orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SyncedOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The order ID from the platform.
     */
    @Column(nullable = false, unique = true)
    private Long platformOrderId;

    /**
     * Tenant ID from the platform.
     */
    @Column(nullable = false)
    private Long tenantId;

    /**
     * Customer email from the platform.
     */
    private String customerEmail;

    /**
     * Customer name.
     */
    private String customerName;

    /**
     * Order total.
     */
    private BigDecimal totalAmount;

    /**
     * Order status from platform.
     */
    private String platformStatus;

    /**
     * Status of sync/fulfillment process.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private SyncStatus syncStatus = SyncStatus.RECEIVED;

    /**
     * External fulfillment reference (simulated).
     */
    private String fulfillmentReference;

    /**
     * Tracking number from external system.
     */
    private String trackingNumber;

    /**
     * When the order was synced.
     */
    @Column(nullable = false)
    private LocalDateTime syncedAt;

    /**
     * When fulfillment was completed.
     */
    private LocalDateTime fulfilledAt;

    /**
     * When the platform was updated with fulfillment status.
     */
    private LocalDateTime platformUpdatedAt;

    /**
     * Error message if any step failed.
     */
    @Column(length = 1000)
    private String errorMessage;

    @PrePersist
    protected void onCreate() {
        syncedAt = LocalDateTime.now();
    }

    public enum SyncStatus {
        RECEIVED,              // Webhook received
        SYNCED_TO_FULFILLMENT, // Sent to external fulfillment
        FULFILLMENT_CONFIRMED, // External system confirmed
        PLATFORM_UPDATED,      // Platform updated with tracking
        FAILED                 // Error occurred
    }
}
