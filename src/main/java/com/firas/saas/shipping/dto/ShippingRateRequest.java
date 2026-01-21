package com.firas.saas.shipping.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShippingRateRequest {

    @NotNull(message = "Zone ID is required")
    private Long zoneId;

    @NotBlank(message = "Rate name is required")
    private String name;

    @NotNull(message = "Price is required")
    @Positive(message = "Price must be positive")
    private BigDecimal price;

    @Builder.Default
    private BigDecimal minOrderAmount = BigDecimal.ZERO;

    @Builder.Default
    private Integer minDeliveryDays = 3;

    @Builder.Default
    private Integer maxDeliveryDays = 7;

    @Builder.Default
    private boolean active = true;
}

