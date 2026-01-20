package com.firas.saas.shipping.dto;

import com.firas.saas.shipping.entity.Shipment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShipmentResponse {

    private Long id;
    private Long orderId;
    private String carrier;
    private String trackingNumber;
    private String trackingUrl;
    private Shipment.ShipmentStatus status;
    private LocalDateTime shippedAt;
    private LocalDateTime deliveredAt;
    private String shippingAddress;
    private LocalDateTime createdAt;
}

