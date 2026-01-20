package com.firas.saas.shipping.entity;

import com.firas.saas.common.base.TenantEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * Represents a shipping rate within a zone (e.g., "Standard Shipping - $5.99")
 */
@Entity
@Table(name = "shipping_rates")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShippingRate extends TenantEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "zone_id", nullable = false)
    private ShippingZone zone;

    @Column(nullable = false)
    private String name; // e.g., "Standard", "Express", "Overnight"

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal price;

    @Column(nullable = false, precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal minOrderAmount = BigDecimal.ZERO; // Free shipping threshold

    @Column(nullable = false)
    @Builder.Default
    private Integer minDeliveryDays = 3;

    @Column(nullable = false)
    @Builder.Default
    private Integer maxDeliveryDays = 7;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;
}

