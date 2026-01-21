package com.firas.saas.webhook.entity;

import com.firas.saas.common.base.TenantEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Tracks webhook delivery attempts with full observability
 */
@Entity
@Table(name = "webhook_deliveries", indexes = {
    @Index(name = "idx_webhook_delivery_event_id", columnList = "event_id"),
    @Index(name = "idx_webhook_delivery_status", columnList = "status"),
    @Index(name = "idx_webhook_delivery_webhook", columnList = "webhook_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WebhookDelivery extends TenantEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "webhook_id", nullable = false)
    private Webhook webhook;

    @Column(name = "event_id", nullable = false, unique = true)
    private String eventId; // Globally unique event ID (e.g., "evt_abc123")

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Webhook.WebhookEvent eventType;

    @Column(nullable = false)
    private String apiVersion; // Event version (v1, v2)

    @Column(columnDefinition = "TEXT", nullable = false)
    private String payload; // JSON payload sent

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DeliveryStatus status;

    private Integer responseCode;

    @Column(columnDefinition = "TEXT")
    private String responseBody; // Truncated response

    @Column(nullable = false)
    private Integer attemptNumber;

    @Column(nullable = false)
    @Builder.Default
    private Integer maxAttempts = 5;

    private LocalDateTime nextRetryAt;

    @Column(columnDefinition = "TEXT")
    private String errorMessage;

    @Column(nullable = false)
    private LocalDateTime triggeredAt; // When event was created

    private LocalDateTime deliveredAt; // When successfully delivered

    private Long durationMs; // Time taken for the request

    private String idempotencyKey; // For consumer idempotency

    public enum DeliveryStatus {
        PENDING,    // Queued for delivery
        SENDING,    // Currently being sent
        SUCCESS,    // Delivered successfully (2xx)
        FAILED,     // Permanently failed (4xx except 429)
        RETRYING,   // Will retry
        EXHAUSTED   // All retries exhausted
    }

    public boolean shouldRetry() {
        return status == DeliveryStatus.RETRYING && attemptNumber < maxAttempts;
    }
}
