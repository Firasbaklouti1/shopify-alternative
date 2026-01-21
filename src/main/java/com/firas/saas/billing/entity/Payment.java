package com.firas.saas.billing.entity;

import com.firas.saas.common.base.TenantEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Represents a payment transaction for an invoice.
 * Used for tracking payment gateway responses and transaction history.
 */
@Entity
@Table(name = "payments")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Payment extends TenantEntity {

    // tenantId is inherited from TenantEntity

    @Column(nullable = false)
    private Long invoiceId;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;

    @Column(nullable = false)
    private String paymentMethod; // e.g., "CARD", "PAYPAL", "BANK_TRANSFER"

    @Column(unique = true)
    private String transactionId; // External payment gateway transaction ID

    @Column(unique = true)
    private String paymentIntentId; // Stripe PaymentIntent ID or equivalent

    @Column(columnDefinition = "TEXT")
    private String gatewayResponse; // JSON response from payment gateway

    private String failureReason; // If payment failed

    @Column(nullable = false)
    private LocalDateTime processedAt;

    /**
     * Payment status enum
     */
    public enum PaymentStatus {
        PENDING,
        PROCESSING,
        SUCCEEDED,
        FAILED,
        CANCELLED,
        REFUNDED,
        PARTIALLY_REFUNDED
    }
}

