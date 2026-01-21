package com.firas.saas.webhook.entity;

import com.firas.saas.common.base.TenantEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * Represents a webhook endpoint registered by a merchant or app
 */
@Entity
@Table(name = "webhooks", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"tenant_id", "url", "event"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Webhook extends TenantEntity {

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String url; // HTTPS endpoint URL

    @Column(nullable = false)
    private String secret; // Secret for HMAC signature

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WebhookEvent event;

    @Column(nullable = false)
    @Builder.Default
    private String apiVersion = "v1"; // Event version

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;

    @Column(nullable = false)
    @Builder.Default
    private boolean paused = false;

    @Column(nullable = false)
    @Builder.Default
    private Integer maxRetries = 5;

    private String headers; // JSON string of additional headers

    /**
     * Events that can trigger webhooks - following the specification
     */
    public enum WebhookEvent {
        // Store lifecycle
        STORE_CREATED,
        STORE_UPDATED,
        STORE_DELETED,

        // Products
        PRODUCT_CREATED,
        PRODUCT_UPDATED,
        PRODUCT_DELETED,

        // Inventory
        INVENTORY_UPDATED,
        INVENTORY_LOW,

        // Orders
        ORDER_CREATED,
        ORDER_UPDATED,
        ORDER_PAID,
        ORDER_FULFILLED,
        ORDER_CANCELLED,

        // Customers
        CUSTOMER_CREATED,
        CUSTOMER_UPDATED,

        // Payments
        PAYMENT_SUCCEEDED,
        PAYMENT_FAILED,
        REFUND_CREATED,

        // App events
        APP_INSTALLED,
        APP_UNINSTALLED,

        // Subscription events
        SUBSCRIPTION_CREATED,
        SUBSCRIPTION_CANCELLED,
        SUBSCRIPTION_RENEWED
    }
}
