package com.firas.saas.shipping.entity;

import com.firas.saas.common.base.TenantEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Represents shipment tracking for an order
 */
@Entity
@Table(name = "shipments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Shipment extends TenantEntity {

    @Column(nullable = false)
    private Long orderId;

    @Column(nullable = false)
    private String carrier; // e.g., "UPS", "FedEx", "USPS", "DHL"

    @Column(unique = true)
    private String trackingNumber;

    private String trackingUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ShipmentStatus status = ShipmentStatus.PENDING;

    private LocalDateTime shippedAt;

    private LocalDateTime deliveredAt;

    private String shippingAddress;

    public enum ShipmentStatus {
        PENDING,
        LABEL_CREATED,
        PICKED_UP,
        IN_TRANSIT,
        OUT_FOR_DELIVERY,
        DELIVERED,
        FAILED,
        RETURNED
    }
}

