package com.firas.saas.subscription.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubscribeRequest {
    @NotNull(message = "Plan ID is required")
    private Long planId;
    
    private String paymentMethod; // Optional, defaults to MOCK if null
}
