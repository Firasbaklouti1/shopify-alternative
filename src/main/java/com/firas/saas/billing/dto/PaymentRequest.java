package com.firas.saas.billing.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequest {
    private BigDecimal amount;
    private String currency;
    private String paymentMethod; // e.g., "MOCK", "STRIPE", "PAYPAL"
    private String description;
    
    // Additional fields for real gateways (tokens, customerId, etc.) can be added here
    private String paymentToken; 
}
