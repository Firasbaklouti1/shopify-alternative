package com.firas.saas.discount.entity;

import com.firas.saas.common.base.TenantEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a discount/coupon code for a store
 */
@Entity
@Table(name = "discounts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Discount extends TenantEntity {

    @Column(nullable = false, unique = true)
    private String code; // e.g., "SAVE20", "SUMMER2026"

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DiscountType type;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal value; // Percentage (0-100) or fixed amount

    @Column(precision = 19, scale = 2)
    private BigDecimal minOrderAmount; // Minimum order value to apply

    @Column(precision = 19, scale = 2)
    private BigDecimal maxDiscountAmount; // Cap for percentage discounts

    private Integer usageLimit; // Max total uses (null = unlimited)

    private Integer usageLimitPerCustomer; // Max uses per customer

    @Column(nullable = false)
    @Builder.Default
    private Integer timesUsed = 0;

    private LocalDateTime startsAt;

    private LocalDateTime expiresAt;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;

    // Cascade delete: when discount is deleted, all usages are deleted too
    @OneToMany(mappedBy = "discount", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<DiscountUsage> usages = new ArrayList<>();

    public enum DiscountType {
        PERCENTAGE,  // e.g., 20% off
        FIXED_AMOUNT // e.g., $10 off
    }

    public boolean isValid() {
        if (!active) return false;

        LocalDateTime now = LocalDateTime.now();
        if (startsAt != null && now.isBefore(startsAt)) return false;
        if (expiresAt != null && now.isAfter(expiresAt)) return false;
        if (usageLimit != null && timesUsed >= usageLimit) return false;

        return true;
    }

    public BigDecimal calculateDiscount(BigDecimal orderTotal) {
        if (!isValid()) return BigDecimal.ZERO;
        if (minOrderAmount != null && orderTotal.compareTo(minOrderAmount) < 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal discount;
        if (type == DiscountType.PERCENTAGE) {
            discount = orderTotal.multiply(value).divide(BigDecimal.valueOf(100));
            if (maxDiscountAmount != null && discount.compareTo(maxDiscountAmount) > 0) {
                discount = maxDiscountAmount;
            }
        } else {
            discount = value;
        }

        // Discount cannot exceed order total
        if (discount.compareTo(orderTotal) > 0) {
            discount = orderTotal;
        }

        return discount;
    }
}

