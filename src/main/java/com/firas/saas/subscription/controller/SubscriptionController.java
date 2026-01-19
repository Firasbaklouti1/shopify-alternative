package com.firas.saas.subscription.controller;

import com.firas.saas.security.service.UserPrincipal;
import com.firas.saas.subscription.dto.SubscribeRequest;
import com.firas.saas.subscription.dto.SubscriptionPlanResponse;
import com.firas.saas.subscription.dto.SubscriptionResponse;
import com.firas.saas.subscription.service.SubscriptionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/subscriptions")
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    @GetMapping("/plans")
    public ResponseEntity<List<SubscriptionPlanResponse>> getAllPlans() {
        return ResponseEntity.ok(subscriptionService.getAllPlans());
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('MERCHANT')")
    public ResponseEntity<SubscriptionResponse> getCurrentSubscription(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(subscriptionService.getCurrentSubscription(principal.getTenantId()));
    }

    @PostMapping("/subscribe")
    @PreAuthorize("hasRole('MERCHANT')")
    public ResponseEntity<SubscriptionResponse> subscribe(
            @Valid @RequestBody SubscribeRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(subscriptionService.subscribe(principal.getTenantId(), request));
    }

    @PostMapping("/cancel")
    @PreAuthorize("hasRole('MERCHANT')")
    public ResponseEntity<Void> cancelSubscription(@AuthenticationPrincipal UserPrincipal principal) {
        subscriptionService.cancelSubscription(principal.getTenantId());
        return ResponseEntity.noContent().build();
    }

    // Admin Endpoints

    @PostMapping("/plans")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SubscriptionPlanResponse> createPlan(@Valid @RequestBody com.firas.saas.subscription.dto.SubscriptionPlanRequest request) {
        return ResponseEntity.ok(subscriptionService.createPlan(request));
    }

    @PutMapping("/plans/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SubscriptionPlanResponse> updatePlan(
            @PathVariable Long id,
            @Valid @RequestBody com.firas.saas.subscription.dto.SubscriptionPlanRequest request) {
        return ResponseEntity.ok(subscriptionService.updatePlan(id, request));
    }

    @DeleteMapping("/plans/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deletePlan(@PathVariable Long id) {
        subscriptionService.deletePlan(id);
        return ResponseEntity.noContent().build();
    }
}
