package com.firas.saas.app.controller;

import com.firas.saas.app.entity.AppScope;
import com.firas.saas.app.security.AppPrincipal;
import com.firas.saas.app.security.RequiresScope;
import com.firas.saas.customer.dto.CustomerResponse;
import com.firas.saas.customer.service.CustomerService;
import com.firas.saas.order.dto.OrderResponse;
import com.firas.saas.order.entity.OrderStatus;
import com.firas.saas.order.service.OrderService;
import com.firas.saas.product.dto.ProductResponse;
import com.firas.saas.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * API endpoints that apps call using their access tokens.
 * All endpoints are under /api/v1/app/ and use AppTokenAuthFilter for authentication.
 * Scope enforcement is done via @RequiresScope annotation.
 */
@RestController
@RequestMapping("/api/v1/app")
@RequiredArgsConstructor
@Slf4j
public class AppApiController {

    private final OrderService orderService;
    private final ProductService productService;
    private final CustomerService customerService;

    // ==================== ORDERS ====================

    /**
     * Get all orders for the tenant.
     */
    @GetMapping("/orders")
    @RequiresScope(AppScope.READ_ORDERS)
    public ResponseEntity<List<OrderResponse>> getOrders(@AuthenticationPrincipal AppPrincipal principal) {
        log.info("App {} fetching orders for tenant {}", principal.getClientId(), principal.getTenantId());
        return ResponseEntity.ok(orderService.getAllOrders(principal.getTenantId()));
    }

    /**
     * Get order by ID.
     */
    @GetMapping("/orders/{orderId}")
    @RequiresScope(AppScope.READ_ORDERS)
    public ResponseEntity<OrderResponse> getOrderById(
            @PathVariable Long orderId,
            @AuthenticationPrincipal AppPrincipal principal) {
        log.info("App {} fetching order {} for tenant {}", principal.getClientId(), orderId, principal.getTenantId());
        return ResponseEntity.ok(orderService.getOrderById(orderId, principal.getTenantId()));
    }

    /**
     * Update order status (e.g., mark as fulfilled).
     */
    @PatchMapping("/orders/{orderId}/status")
    @RequiresScope(AppScope.WRITE_ORDERS)
    public ResponseEntity<OrderResponse> updateOrderStatus(
            @PathVariable Long orderId,
            @RequestParam OrderStatus status,
            @AuthenticationPrincipal AppPrincipal principal) {
        log.info("App {} updating order {} status to {} for tenant {}",
                principal.getClientId(), orderId, status, principal.getTenantId());
        return ResponseEntity.ok(orderService.updateOrderStatus(orderId, status, principal.getTenantId()));
    }

    // ==================== PRODUCTS ====================

    /**
     * Get all products for the tenant.
     */
    @GetMapping("/products")
    @RequiresScope(AppScope.READ_PRODUCTS)
    public ResponseEntity<List<ProductResponse>> getProducts(@AuthenticationPrincipal AppPrincipal principal) {
        log.info("App {} fetching products for tenant {}", principal.getClientId(), principal.getTenantId());
        return ResponseEntity.ok(productService.getAllProducts(principal.getTenantId()));
    }

    /**
     * Get product by ID.
     */
    @GetMapping("/products/{productId}")
    @RequiresScope(AppScope.READ_PRODUCTS)
    public ResponseEntity<ProductResponse> getProductById(
            @PathVariable Long productId,
            @AuthenticationPrincipal AppPrincipal principal) {
        log.info("App {} fetching product {} for tenant {}", principal.getClientId(), productId, principal.getTenantId());
        return ResponseEntity.ok(productService.getProductById(productId, principal.getTenantId()));
    }

    // ==================== CUSTOMERS ====================

    /**
     * Get all customers for the tenant.
     */
    @GetMapping("/customers")
    @RequiresScope(AppScope.READ_CUSTOMERS)
    public ResponseEntity<List<CustomerResponse>> getCustomers(@AuthenticationPrincipal AppPrincipal principal) {
        log.info("App {} fetching customers for tenant {}", principal.getClientId(), principal.getTenantId());
        return ResponseEntity.ok(customerService.getAllCustomers(principal.getTenantId()));
    }

    /**
     * Get customer by ID.
     */
    @GetMapping("/customers/{customerId}")
    @RequiresScope(AppScope.READ_CUSTOMERS)
    public ResponseEntity<CustomerResponse> getCustomerById(
            @PathVariable Long customerId,
            @AuthenticationPrincipal AppPrincipal principal) {
        log.info("App {} fetching customer {} for tenant {}", principal.getClientId(), customerId, principal.getTenantId());
        return ResponseEntity.ok(customerService.getCustomerById(customerId, principal.getTenantId()));
    }

    // ==================== APP INFO ====================

    /**
     * Get current app info (for debugging/verification).
     */
    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getAppInfo(@AuthenticationPrincipal AppPrincipal principal) {
        return ResponseEntity.ok(Map.of(
                "appId", principal.getAppId(),
                "clientId", principal.getClientId(),
                "installationId", principal.getInstallationId(),
                "tenantId", principal.getTenantId(),
                "scopes", principal.getScopes()
        ));
    }
}
