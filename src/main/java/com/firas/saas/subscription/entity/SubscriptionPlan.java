package com.firas.saas.subscription.entity;

import com.firas.saas.common.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "subscription_plans")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubscriptionPlan extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false, unique = true)
    private String slug;

    @Column(nullable = false)
    private BigDecimal price;

    @Column(nullable = false)
    private String billingInterval; // MONTHLY, YEARLY

    @Column(columnDefinition = "TEXT")
    private String features; // JSON or comma-separated list

    @Builder.Default
    private boolean active = true;
}
