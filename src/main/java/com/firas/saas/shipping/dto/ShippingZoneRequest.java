package com.firas.saas.shipping.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShippingZoneRequest {

    @NotBlank(message = "Zone name is required")
    private String name;

    private String countries; // Comma-separated country codes

    @Builder.Default
    private boolean active = true;
}

