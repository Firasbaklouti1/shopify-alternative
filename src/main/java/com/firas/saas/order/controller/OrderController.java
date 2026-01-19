package com.firas.saas.order.controller;

import com.firas.saas.order.dto.CartItemRequest;
import com.firas.saas.order.dto.CartResponse;
import com.firas.saas.order.dto.OrderResponse;
import com.firas.saas.order.service.OrderService;
import com.firas.saas.security.service.UserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/cart")
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

    @PostMapping("/checkout")
    public ResponseEntity<OrderResponse> placeOrder(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(orderService.placeOrder(principal.getEmail(), principal.getTenantId()));
    }

    @GetMapping
    public ResponseEntity<List<OrderResponse>> getMyOrders(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(orderService.getCustomerOrders(principal.getEmail(), principal.getTenantId()));
    }

    @GetMapping("/{orderNumber}")
    public ResponseEntity<OrderResponse> getOrder(
            @PathVariable String orderNumber,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(orderService.getOrderByNumber(orderNumber, principal.getTenantId()));
    }
}
