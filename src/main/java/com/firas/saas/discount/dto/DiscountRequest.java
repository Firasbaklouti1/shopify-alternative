package com.firas.saas.discount.dto;

import com.firas.saas.discount.entity.Discount;
import jakarta.validation.constraints.*;
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
public class DiscountRequest {

    @NotBlank(message = "Discount code is required")
    @Size(min = 3, max = 50, message = "Code must be between 3 and 50 characters")
    @Pattern(regexp = "^[A-Za-z0-9_-]+$", message = "Code must contain only letters, numbers, hyphens, and underscores")
    private String code;

    private String description;

    @NotNull(message = "Discount type is required")
    private Discount.DiscountType type;

    @NotNull(message = "Discount value is required")
    @Positive(message = "Value must be positive")
    private BigDecimal value;

    @PositiveOrZero(message = "Minimum order amount must be zero or positive")
    private BigDecimal minOrderAmount;

    @Positive(message = "Max discount amount must be positive")
    private BigDecimal maxDiscountAmount;

    @Positive(message = "Usage limit must be positive")
    private Integer usageLimit;

    @Positive(message = "Usage limit per customer must be positive")
    private Integer usageLimitPerCustomer;

    private LocalDateTime startsAt;

    private LocalDateTime expiresAt;

    @Builder.Default
    private boolean active = true;
}

