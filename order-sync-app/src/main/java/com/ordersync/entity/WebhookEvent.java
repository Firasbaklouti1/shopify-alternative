package com.ordersync.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Stores all received webhook events for audit and debugging.
 */
@Entity
@Table(name = "webhook_events")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WebhookEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Event ID from platform (for idempotency).
     */
    @Column(nullable = false, unique = true)
    private String eventId;

    /**
     * Event type (e.g., ORDER_CREATED, ORDER_PAID).
     */
    @Column(nullable = false)
    private String eventType;

    /**
     * Tenant ID from the webhook.
     */
    private Long tenantId;

    /**
     * Raw payload JSON.
     */
    @Column(columnDefinition = "TEXT")
    private String payload;

    /**
     * Signature from the webhook header.
     */
    private String signature;

    /**
     * Whether signature was valid.
     */
    private boolean signatureValid;

    /**
     * Processing status.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ProcessingStatus status = ProcessingStatus.RECEIVED;

    /**
     * When the webhook was received.
     */
    @Column(nullable = false)
    private LocalDateTime receivedAt;

    /**
     * When processing completed.
     */
    private LocalDateTime processedAt;

    /**
     * Error message if processing failed.
     */
    @Column(length = 1000)
    private String errorMessage;

    @PrePersist
    protected void onCreate() {
        receivedAt = LocalDateTime.now();
    }

    public enum ProcessingStatus {
        RECEIVED,
        PROCESSING,
        PROCESSED,
        IGNORED,
        FAILED
    }
}
