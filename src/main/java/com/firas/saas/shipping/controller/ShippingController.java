package com.firas.saas.shipping.controller;

import com.firas.saas.security.service.UserPrincipal;
import com.firas.saas.shipping.dto.*;
import com.firas.saas.shipping.entity.Shipment;
import com.firas.saas.shipping.service.ShippingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/shipping")
@RequiredArgsConstructor
public class ShippingController {

    private final ShippingService shippingService;

    // ==================== SHIPPING ZONES ====================

    @PostMapping("/zones")
    @PreAuthorize("hasRole('MERCHANT')")
    public ResponseEntity<ShippingZoneResponse> createZone(
            @Valid @RequestBody ShippingZoneRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        return new ResponseEntity<>(shippingService.createZone(request, principal.getTenantId()), HttpStatus.CREATED);
    }

    @GetMapping("/zones")
    @PreAuthorize("hasAnyRole('MERCHANT', 'STAFF')")
    public ResponseEntity<List<ShippingZoneResponse>> getAllZones(
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(shippingService.getAllZones(principal.getTenantId()));
    }

    @GetMapping("/zones/{id}")
    @PreAuthorize("hasAnyRole('MERCHANT', 'STAFF')")
    public ResponseEntity<ShippingZoneResponse> getZoneById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(shippingService.getZoneById(id, principal.getTenantId()));
    }

    @PutMapping("/zones/{id}")
    @PreAuthorize("hasRole('MERCHANT')")
    public ResponseEntity<ShippingZoneResponse> updateZone(
            @PathVariable Long id,
            @Valid @RequestBody ShippingZoneRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(shippingService.updateZone(id, request, principal.getTenantId()));
    }

    @DeleteMapping("/zones/{id}")
    @PreAuthorize("hasRole('MERCHANT')")
    public ResponseEntity<Void> deleteZone(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        shippingService.deleteZone(id, principal.getTenantId());
        return ResponseEntity.noContent().build();
    }

    // ==================== SHIPPING RATES ====================

    @PostMapping("/rates")
    @PreAuthorize("hasRole('MERCHANT')")
    public ResponseEntity<ShippingRateResponse> createRate(
            @Valid @RequestBody ShippingRateRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        return new ResponseEntity<>(shippingService.createRate(request, principal.getTenantId()), HttpStatus.CREATED);
    }

    @GetMapping("/rates")
    @PreAuthorize("hasAnyRole('MERCHANT', 'STAFF')")
    public ResponseEntity<List<ShippingRateResponse>> getAllRates(
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(shippingService.getAllRates(principal.getTenantId()));
    }

    @GetMapping("/zones/{zoneId}/rates")
    @PreAuthorize("hasAnyRole('MERCHANT', 'STAFF')")
    public ResponseEntity<List<ShippingRateResponse>> getRatesByZone(
            @PathVariable Long zoneId,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(shippingService.getRatesByZone(zoneId, principal.getTenantId()));
    }

    @GetMapping("/rates/{id}")
    @PreAuthorize("hasAnyRole('MERCHANT', 'STAFF')")
    public ResponseEntity<ShippingRateResponse> getRateById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(shippingService.getRateById(id, principal.getTenantId()));
    }

    @PutMapping("/rates/{id}")
    @PreAuthorize("hasRole('MERCHANT')")
    public ResponseEntity<ShippingRateResponse> updateRate(
            @PathVariable Long id,
            @Valid @RequestBody ShippingRateRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(shippingService.updateRate(id, request, principal.getTenantId()));
    }

    @DeleteMapping("/rates/{id}")
    @PreAuthorize("hasRole('MERCHANT')")
    public ResponseEntity<Void> deleteRate(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        shippingService.deleteRate(id, principal.getTenantId());
        return ResponseEntity.noContent().build();
    }

    // ==================== SHIPMENTS ====================

    @PostMapping("/shipments")
    @PreAuthorize("hasAnyRole('MERCHANT', 'STAFF')")
    public ResponseEntity<ShipmentResponse> createShipment(
            @Valid @RequestBody ShipmentRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        return new ResponseEntity<>(shippingService.createShipment(request, principal.getTenantId()), HttpStatus.CREATED);
    }

    @GetMapping("/orders/{orderId}/shipments")
    @PreAuthorize("hasAnyRole('MERCHANT', 'STAFF')")
    public ResponseEntity<List<ShipmentResponse>> getShipmentsByOrder(
            @PathVariable Long orderId,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(shippingService.getShipmentsByOrder(orderId, principal.getTenantId()));
    }

    @GetMapping("/shipments/{id}")
    @PreAuthorize("hasAnyRole('MERCHANT', 'STAFF')")
    public ResponseEntity<ShipmentResponse> getShipmentById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(shippingService.getShipmentById(id, principal.getTenantId()));
    }

    @PatchMapping("/shipments/{id}/status")
    @PreAuthorize("hasAnyRole('MERCHANT', 'STAFF')")
    public ResponseEntity<ShipmentResponse> updateShipmentStatus(
            @PathVariable Long id,
            @RequestParam Shipment.ShipmentStatus status,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(shippingService.updateShipmentStatus(id, status, principal.getTenantId()));
    }

    @GetMapping("/shipments/track/{trackingNumber}")
    @PreAuthorize("hasAnyRole('MERCHANT', 'STAFF', 'CUSTOMER')")
    public ResponseEntity<ShipmentResponse> trackShipment(
            @PathVariable String trackingNumber,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(shippingService.getShipmentByTrackingNumber(trackingNumber, principal.getTenantId()));
    }
}
