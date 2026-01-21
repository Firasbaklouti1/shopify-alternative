package com.firas.saas.discount.controller;

import com.firas.saas.discount.dto.*;
import com.firas.saas.discount.service.DiscountService;
import com.firas.saas.security.service.UserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/discounts")
@RequiredArgsConstructor
public class DiscountController {

    private final DiscountService discountService;

    @PostMapping
    @PreAuthorize("hasRole('MERCHANT')")
    public ResponseEntity<DiscountResponse> createDiscount(
            @Valid @RequestBody DiscountRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        return new ResponseEntity<>(discountService.createDiscount(request, principal.getTenantId()), HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('MERCHANT', 'STAFF')")
    public ResponseEntity<List<DiscountResponse>> getAllDiscounts(
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(discountService.getAllDiscounts(principal.getTenantId()));
    }

    @GetMapping("/active")
    @PreAuthorize("hasAnyRole('MERCHANT', 'STAFF', 'CUSTOMER')")
    public ResponseEntity<List<DiscountResponse>> getActiveDiscounts(
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(discountService.getActiveDiscounts(principal.getTenantId()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('MERCHANT', 'STAFF')")
    public ResponseEntity<DiscountResponse> getDiscountById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(discountService.getDiscountById(id, principal.getTenantId()));
    }

    @GetMapping("/code/{code}")
    @PreAuthorize("hasAnyRole('MERCHANT', 'STAFF', 'CUSTOMER')")
    public ResponseEntity<DiscountResponse> getDiscountByCode(
            @PathVariable String code,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(discountService.getDiscountByCode(code, principal.getTenantId()));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('MERCHANT')")
    public ResponseEntity<DiscountResponse> updateDiscount(
            @PathVariable Long id,
            @Valid @RequestBody DiscountRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(discountService.updateDiscount(id, request, principal.getTenantId()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('MERCHANT')")
    public ResponseEntity<Void> deleteDiscount(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        discountService.deleteDiscount(id, principal.getTenantId());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/validate")
    @PreAuthorize("hasAnyRole('MERCHANT', 'STAFF', 'CUSTOMER')")
    public ResponseEntity<ApplyDiscountResponse> validateDiscount(
            @Valid @RequestBody ApplyDiscountRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(discountService.validateDiscount(request, principal.getTenantId()));
    }

    @PostMapping("/apply/{orderId}")
    @PreAuthorize("hasAnyRole('MERCHANT', 'STAFF')")
    public ResponseEntity<ApplyDiscountResponse> applyDiscount(
            @PathVariable Long orderId,
            @Valid @RequestBody ApplyDiscountRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(discountService.applyDiscount(request, orderId, principal.getTenantId()));
    }

    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasRole('MERCHANT')")
    public ResponseEntity<DiscountResponse> activateDiscount(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(discountService.activateDiscount(id, principal.getTenantId()));
    }

    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('MERCHANT')")
    public ResponseEntity<DiscountResponse> deactivateDiscount(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(discountService.deactivateDiscount(id, principal.getTenantId()));
    }
}
