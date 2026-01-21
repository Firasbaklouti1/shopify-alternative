package com.firas.saas.shipping.service;

import com.firas.saas.shipping.dto.*;
import com.firas.saas.shipping.entity.Shipment;

import java.util.List;

public interface ShippingService {

    // Shipping Zones
    ShippingZoneResponse createZone(ShippingZoneRequest request, Long tenantId);
    List<ShippingZoneResponse> getAllZones(Long tenantId);
    ShippingZoneResponse getZoneById(Long id, Long tenantId);
    ShippingZoneResponse updateZone(Long id, ShippingZoneRequest request, Long tenantId);
    void deleteZone(Long id, Long tenantId);

    // Shipping Rates
    ShippingRateResponse createRate(ShippingRateRequest request, Long tenantId);
    List<ShippingRateResponse> getRatesByZone(Long zoneId, Long tenantId);
    List<ShippingRateResponse> getAllRates(Long tenantId);
    ShippingRateResponse getRateById(Long id, Long tenantId);
    ShippingRateResponse updateRate(Long id, ShippingRateRequest request, Long tenantId);
    void deleteRate(Long id, Long tenantId);

    // Shipments
    ShipmentResponse createShipment(ShipmentRequest request, Long tenantId);
    List<ShipmentResponse> getShipmentsByOrder(Long orderId, Long tenantId);
    ShipmentResponse getShipmentById(Long id, Long tenantId);
    ShipmentResponse updateShipmentStatus(Long id, Shipment.ShipmentStatus status, Long tenantId);
    ShipmentResponse getShipmentByTrackingNumber(String trackingNumber, Long tenantId);
}

