package com.firas.saas.shipping.service;

import com.firas.saas.common.exception.ResourceNotFoundException;
import com.firas.saas.shipping.dto.*;
import com.firas.saas.shipping.entity.Shipment;
import com.firas.saas.shipping.entity.ShippingRate;
import com.firas.saas.shipping.entity.ShippingZone;
import com.firas.saas.shipping.repository.ShipmentRepository;
import com.firas.saas.shipping.repository.ShippingRateRepository;
import com.firas.saas.shipping.repository.ShippingZoneRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ShippingServiceImpl implements ShippingService {

    private final ShippingZoneRepository zoneRepository;
    private final ShippingRateRepository rateRepository;
    private final ShipmentRepository shipmentRepository;

    // ==================== SHIPPING ZONES ====================

    @Override
    @Transactional
    public ShippingZoneResponse createZone(ShippingZoneRequest request, Long tenantId) {
        if (zoneRepository.existsByNameAndTenantId(request.getName(), tenantId)) {
            throw new RuntimeException("Shipping zone with this name already exists");
        }

        ShippingZone zone = ShippingZone.builder()
                .name(request.getName())
                .countries(request.getCountries())
                .active(request.isActive())
                .build();
        zone.setTenantId(tenantId);

        return mapToZoneResponse(zoneRepository.save(zone));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ShippingZoneResponse> getAllZones(Long tenantId) {
        return zoneRepository.findAllByTenantId(tenantId).stream()
                .map(this::mapToZoneResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ShippingZoneResponse getZoneById(Long id, Long tenantId) {
        return zoneRepository.findByIdAndTenantId(id, tenantId)
                .map(this::mapToZoneResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Shipping zone", id));
    }

    @Override
    @Transactional
    public ShippingZoneResponse updateZone(Long id, ShippingZoneRequest request, Long tenantId) {
        ShippingZone zone = zoneRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Shipping zone", id));

        zone.setName(request.getName());
        zone.setCountries(request.getCountries());
        zone.setActive(request.isActive());

        return mapToZoneResponse(zoneRepository.save(zone));
    }

    @Override
    @Transactional
    public void deleteZone(Long id, Long tenantId) {
        ShippingZone zone = zoneRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Shipping zone", id));
        zoneRepository.delete(zone);
    }

    // ==================== SHIPPING RATES ====================

    @Override
    @Transactional
    public ShippingRateResponse createRate(ShippingRateRequest request, Long tenantId) {
        ShippingZone zone = zoneRepository.findByIdAndTenantId(request.getZoneId(), tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Shipping zone", request.getZoneId()));

        if (rateRepository.existsByNameAndZoneIdAndTenantId(request.getName(), request.getZoneId(), tenantId)) {
            throw new RuntimeException("Rate with this name already exists in this zone");
        }

        ShippingRate rate = ShippingRate.builder()
                .zone(zone)
                .name(request.getName())
                .price(request.getPrice())
                .minOrderAmount(request.getMinOrderAmount())
                .minDeliveryDays(request.getMinDeliveryDays())
                .maxDeliveryDays(request.getMaxDeliveryDays())
                .active(request.isActive())
                .build();
        rate.setTenantId(tenantId);

        return mapToRateResponse(rateRepository.save(rate));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ShippingRateResponse> getRatesByZone(Long zoneId, Long tenantId) {
        return rateRepository.findAllByZoneIdAndTenantId(zoneId, tenantId).stream()
                .map(this::mapToRateResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ShippingRateResponse> getAllRates(Long tenantId) {
        return rateRepository.findAllByTenantId(tenantId).stream()
                .map(this::mapToRateResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ShippingRateResponse getRateById(Long id, Long tenantId) {
        return rateRepository.findByIdAndTenantId(id, tenantId)
                .map(this::mapToRateResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Shipping rate", id));
    }

    @Override
    @Transactional
    public ShippingRateResponse updateRate(Long id, ShippingRateRequest request, Long tenantId) {
        ShippingRate rate = rateRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Shipping rate", id));

        ShippingZone zone = zoneRepository.findByIdAndTenantId(request.getZoneId(), tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Shipping zone", id));

        rate.setZone(zone);
        rate.setName(request.getName());
        rate.setPrice(request.getPrice());
        rate.setMinOrderAmount(request.getMinOrderAmount());
        rate.setMinDeliveryDays(request.getMinDeliveryDays());
        rate.setMaxDeliveryDays(request.getMaxDeliveryDays());
        rate.setActive(request.isActive());

        return mapToRateResponse(rateRepository.save(rate));
    }

    @Override
    @Transactional
    public void deleteRate(Long id, Long tenantId) {
        ShippingRate rate = rateRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Shipping rate", id));
        rateRepository.delete(rate);
    }

    // ==================== SHIPMENTS ====================

    @Override
    @Transactional
    public ShipmentResponse createShipment(ShipmentRequest request, Long tenantId) {
        Shipment shipment = Shipment.builder()
                .orderId(request.getOrderId())
                .carrier(request.getCarrier())
                .trackingNumber(request.getTrackingNumber())
                .trackingUrl(request.getTrackingUrl())
                .shippingAddress(request.getShippingAddress())
                .status(Shipment.ShipmentStatus.PENDING)
                .build();
        shipment.setTenantId(tenantId);

        return mapToShipmentResponse(shipmentRepository.save(shipment));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ShipmentResponse> getShipmentsByOrder(Long orderId, Long tenantId) {
        return shipmentRepository.findAllByOrderIdAndTenantId(orderId, tenantId).stream()
                .map(this::mapToShipmentResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ShipmentResponse getShipmentById(Long id, Long tenantId) {
        return shipmentRepository.findByIdAndTenantId(id, tenantId)
                .map(this::mapToShipmentResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Shipment", id));
    }

    @Override
    @Transactional
    public ShipmentResponse updateShipmentStatus(Long id, Shipment.ShipmentStatus status, Long tenantId) {
        Shipment shipment = shipmentRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Shipment", id));

        shipment.setStatus(status);

        if (status == Shipment.ShipmentStatus.PICKED_UP || status == Shipment.ShipmentStatus.IN_TRANSIT) {
            if (shipment.getShippedAt() == null) {
                shipment.setShippedAt(LocalDateTime.now());
            }
        }

        if (status == Shipment.ShipmentStatus.DELIVERED) {
            shipment.setDeliveredAt(LocalDateTime.now());
        }

        return mapToShipmentResponse(shipmentRepository.save(shipment));
    }

    @Override
    @Transactional(readOnly = true)
    public ShipmentResponse getShipmentByTrackingNumber(String trackingNumber, Long tenantId) {
        return shipmentRepository.findByTrackingNumberAndTenantId(trackingNumber, tenantId)
                .map(this::mapToShipmentResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Shipment", trackingNumber));
    }

    // ==================== MAPPERS ====================

    private ShippingZoneResponse mapToZoneResponse(ShippingZone zone) {
        return ShippingZoneResponse.builder()
                .id(zone.getId())
                .name(zone.getName())
                .countries(zone.getCountries())
                .active(zone.isActive())
                .createdAt(zone.getCreatedAt())
                .updatedAt(zone.getUpdatedAt())
                .build();
    }

    private ShippingRateResponse mapToRateResponse(ShippingRate rate) {
        return ShippingRateResponse.builder()
                .id(rate.getId())
                .zoneId(rate.getZone().getId())
                .zoneName(rate.getZone().getName())
                .name(rate.getName())
                .price(rate.getPrice())
                .minOrderAmount(rate.getMinOrderAmount())
                .minDeliveryDays(rate.getMinDeliveryDays())
                .maxDeliveryDays(rate.getMaxDeliveryDays())
                .active(rate.isActive())
                .createdAt(rate.getCreatedAt())
                .build();
    }

    private ShipmentResponse mapToShipmentResponse(Shipment shipment) {
        return ShipmentResponse.builder()
                .id(shipment.getId())
                .orderId(shipment.getOrderId())
                .carrier(shipment.getCarrier())
                .trackingNumber(shipment.getTrackingNumber())
                .trackingUrl(shipment.getTrackingUrl())
                .status(shipment.getStatus())
                .shippedAt(shipment.getShippedAt())
                .deliveredAt(shipment.getDeliveredAt())
                .shippingAddress(shipment.getShippingAddress())
                .createdAt(shipment.getCreatedAt())
                .build();
    }
}

