package com.firas.saas.discount.dto;

import com.firas.saas.discount.entity.Discount;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiscountResponse {

    private Long id;
    private String code;
    private String description;
    private Discount.DiscountType type;
    private BigDecimal value;
    private BigDecimal minOrderAmount;
    private BigDecimal maxDiscountAmount;
    private Integer usageLimit;
    private Integer usageLimitPerCustomer;
    private Integer timesUsed;
    private LocalDateTime startsAt;
    private LocalDateTime expiresAt;
    private boolean active;
    private boolean valid; // Computed field
    private LocalDateTime createdAt;
}

