package com.firas.saas.order.controller;

import com.firas.saas.order.dto.CartItemRequest;
import com.firas.saas.order.dto.CartResponse;
import com.firas.saas.order.dto.OrderResponse;
import com.firas.saas.order.entity.OrderStatus;
import com.firas.saas.order.service.OrderService;
import com.firas.saas.security.service.UserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    // ==================== CART ENDPOINTS ====================

    @PostMapping("/cart/add")
    public ResponseEntity<CartResponse> addToCart(
            @Valid @RequestBody CartItemRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(orderService.addToCart(request, principal.getEmail(), principal.getTenantId()));
    }

    @GetMapping("/cart")
    public ResponseEntity<CartResponse> getCart(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(orderService.getCart(principal.getEmail(), principal.getTenantId()));
    }

    @DeleteMapping("/cart")
    public ResponseEntity<Void> clearCart(@AuthenticationPrincipal UserPrincipal principal) {
        orderService.clearCart(principal.getEmail(), principal.getTenantId());
        return ResponseEntity.noContent().build();
    }

    // ==================== ORDER ENDPOINTS ====================

    @PostMapping("/checkout")
    public ResponseEntity<OrderResponse> placeOrder(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(orderService.placeOrder(principal.getEmail(), principal.getTenantId()));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('MERCHANT', 'STAFF')")
    public ResponseEntity<List<OrderResponse>> getAllOrders(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(orderService.getAllOrders(principal.getTenantId()));
    }

    @GetMapping("/my")
    public ResponseEntity<List<OrderResponse>> getMyOrders(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(orderService.getCustomerOrders(principal.getEmail(), principal.getTenantId()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrderById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(orderService.getOrderById(id, principal.getTenantId()));
    }

    @GetMapping("/number/{orderNumber}")
    public ResponseEntity<OrderResponse> getOrderByNumber(
            @PathVariable String orderNumber,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(orderService.getOrderByNumber(orderNumber, principal.getTenantId()));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('MERCHANT', 'STAFF')")
    public ResponseEntity<OrderResponse> updateOrderStatus(
            @PathVariable Long id,
            @RequestParam OrderStatus status,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(orderService.updateOrderStatus(id, status, principal.getTenantId()));
    }
}
