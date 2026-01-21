package com.ordersync.controller;

import com.ordersync.entity.SyncedOrder;
import com.ordersync.entity.WebhookEvent;
import com.ordersync.repository.SyncedOrderRepository;
import com.ordersync.repository.WebhookEventRepository;
import com.ordersync.service.AppTokenManager;
import com.ordersync.service.OrderSyncService;
import com.ordersync.service.PlatformApiClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Management endpoints for the Order Sync App.
 * Used for configuration, debugging, and manual operations.
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class ManagementController {

    private final AppTokenManager tokenManager;
    private final PlatformApiClient platformApiClient;
    private final OrderSyncService orderSyncService;
    private final SyncedOrderRepository syncedOrderRepository;
    private final WebhookEventRepository webhookEventRepository;

    // ==================== CONFIGURATION ====================

    /**
     * Configure access token for a tenant.
     * Call this after installing the app in a merchant's store.
     */
    @PostMapping("/config/token")
    public ResponseEntity<Map<String, Object>> configureToken(
            @RequestBody Map<String, Object> request) {
        
        Long tenantId = extractLong(request, "tenantId");
        String accessToken = (String) request.get("accessToken");

        if (tenantId == null || accessToken == null) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "tenantId and accessToken are required"
            ));
        }

        tokenManager.setAccessToken(tenantId, accessToken);
        log.info("Configured access token for tenant {}", tenantId);

        return ResponseEntity.ok(Map.of(
                "status", "configured",
                "tenantId", tenantId
        ));
    }

    /**
     * Verify the configured token by calling /me endpoint.
     */
    @GetMapping("/config/verify/{tenantId}")
    public ResponseEntity<Map<String, Object>> verifyToken(@PathVariable Long tenantId) {
        String accessToken = tokenManager.getAccessToken(tenantId);
        
        if (accessToken == null) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "No token configured for tenant " + tenantId
            ));
        }

        var appInfo = platformApiClient.getAppInfo(accessToken);
        
        if (appInfo.isPresent()) {
            return ResponseEntity.ok(Map.of(
                    "status", "valid",
                    "appInfo", appInfo.get()
            ));
        } else {
            return ResponseEntity.ok(Map.of(
                    "status", "invalid",
                    "message", "Token is invalid or expired"
            ));
        }
    }

    /**
     * List all configured tenants.
     */
    @GetMapping("/config/tenants")
    public ResponseEntity<Map<String, Object>> listConfiguredTenants() {
        return ResponseEntity.ok(Map.of(
                "tenants", tokenManager.getConfiguredTenants()
        ));
    }

    // ==================== SYNCED ORDERS ====================

    /**
     * Get all synced orders.
     */
    @GetMapping("/orders")
    public ResponseEntity<List<SyncedOrder>> getAllSyncedOrders() {
        return ResponseEntity.ok(syncedOrderRepository.findAll());
    }

    /**
     * Get synced order by platform order ID.
     */
    @GetMapping("/orders/platform/{platformOrderId}")
    public ResponseEntity<SyncedOrder> getSyncedOrderByPlatformId(@PathVariable Long platformOrderId) {
        return syncedOrderRepository.findByPlatformOrderId(platformOrderId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get orders by sync status.
     */
    @GetMapping("/orders/status/{status}")
    public ResponseEntity<List<SyncedOrder>> getOrdersByStatus(@PathVariable String status) {
        try {
            SyncedOrder.SyncStatus syncStatus = SyncedOrder.SyncStatus.valueOf(status.toUpperCase());
            return ResponseEntity.ok(syncedOrderRepository.findBySyncStatus(syncStatus));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Manually trigger fulfillment for an order.
     */
    @PostMapping("/orders/{platformOrderId}/fulfill")
    public ResponseEntity<Map<String, Object>> manualFulfill(
            @PathVariable Long platformOrderId,
            @RequestParam Long tenantId) {
        try {
            SyncedOrder order = orderSyncService.manuallyFulfillOrder(platformOrderId, tenantId);
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "order", order
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", e.getMessage()
            ));
        }
    }

    // ==================== WEBHOOK EVENTS ====================

    /**
     * Get all received webhook events.
     */
    @GetMapping("/webhooks")
    public ResponseEntity<List<WebhookEvent>> getAllWebhookEvents() {
        return ResponseEntity.ok(webhookEventRepository.findAllByOrderByReceivedAtDesc());
    }

    /**
     * Get webhook events by type.
     */
    @GetMapping("/webhooks/type/{eventType}")
    public ResponseEntity<List<WebhookEvent>> getWebhooksByType(@PathVariable String eventType) {
        return ResponseEntity.ok(webhookEventRepository.findByEventType(eventType));
    }

    /**
     * Get webhook events by status.
     */
    @GetMapping("/webhooks/status/{status}")
    public ResponseEntity<List<WebhookEvent>> getWebhooksByStatus(@PathVariable String status) {
        try {
            WebhookEvent.ProcessingStatus processingStatus = 
                    WebhookEvent.ProcessingStatus.valueOf(status.toUpperCase());
            return ResponseEntity.ok(webhookEventRepository.findByStatus(processingStatus));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // ==================== HEALTH & STATUS ====================

    /**
     * Health check endpoint.
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "app", "Order Sync App",
                "port", 8081,
                "configuredTenants", tokenManager.getConfiguredTenants().size(),
                "totalSyncedOrders", syncedOrderRepository.count(),
                "totalWebhookEvents", webhookEventRepository.count()
        ));
    }

    /**
     * Get app status summary.
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> status() {
        long totalOrders = syncedOrderRepository.count();
        long receivedOrders = syncedOrderRepository.findBySyncStatus(SyncedOrder.SyncStatus.RECEIVED).size();
        long syncedOrders = syncedOrderRepository.findBySyncStatus(SyncedOrder.SyncStatus.SYNCED_TO_FULFILLMENT).size();
        long confirmedOrders = syncedOrderRepository.findBySyncStatus(SyncedOrder.SyncStatus.FULFILLMENT_CONFIRMED).size();
        long updatedOrders = syncedOrderRepository.findBySyncStatus(SyncedOrder.SyncStatus.PLATFORM_UPDATED).size();
        long failedOrders = syncedOrderRepository.findBySyncStatus(SyncedOrder.SyncStatus.FAILED).size();

        return ResponseEntity.ok(Map.of(
                "orders", Map.of(
                        "total", totalOrders,
                        "received", receivedOrders,
                        "syncedToFulfillment", syncedOrders,
                        "fulfillmentConfirmed", confirmedOrders,
                        "platformUpdated", updatedOrders,
                        "failed", failedOrders
                ),
                "webhooks", Map.of(
                        "total", webhookEventRepository.count(),
                        "processed", webhookEventRepository.findByStatus(WebhookEvent.ProcessingStatus.PROCESSED).size(),
                        "failed", webhookEventRepository.findByStatus(WebhookEvent.ProcessingStatus.FAILED).size()
                ),
                "configuredTenants", tokenManager.getConfiguredTenants()
        ));
    }

    private Long extractLong(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        if (value instanceof String) {
            try {
                return Long.parseLong((String) value);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }
}
