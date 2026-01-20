package com.firas.saas.shipping.dto;

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
public class ShippingRateResponse {

    private Long id;
    private Long zoneId;
    private String zoneName;
    private String name;
    private BigDecimal price;
    private BigDecimal minOrderAmount;
    private Integer minDeliveryDays;
    private Integer maxDeliveryDays;
    private boolean active;
    private LocalDateTime createdAt;
}

