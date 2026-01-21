package com.firas.saas.shipping.entity;

import com.firas.saas.common.base.TenantEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * Represents a shipping zone (e.g., "Domestic", "International", "Europe")
 */
@Entity
@Table(name = "shipping_zones")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShippingZone extends TenantEntity {

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String countries; // Comma-separated country codes (e.g., "US,CA,MX")

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;
}

