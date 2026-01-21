package com.ordersync.service;

import com.ordersync.dto.CustomerDto;
import com.ordersync.dto.OrderDto;
import com.ordersync.entity.SyncedOrder;
import com.ordersync.repository.SyncedOrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Service that handles order synchronization and auto-fulfillment.
 * 
 * Flow:
 * 1. Receive ORDER_CREATED/ORDER_PAID webhook
 * 2. Fetch full order details from platform API
 * 3. Sync to external fulfillment system (simulated)
 * 4. When fulfillment confirms, update order status on platform
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OrderSyncService {

    private final SyncedOrderRepository syncedOrderRepository;
    private final PlatformApiClient platformApiClient;
    private final AppTokenManager tokenManager;

    /**
     * Process an ORDER_CREATED or ORDER_PAID event.
     */
    @Async
    @Transactional
    public void processOrderEvent(String eventType, Map<String, Object> data, Long tenantId) {
        Long orderId = extractOrderId(data);
        if (orderId == null) {
            log.error("No order ID in webhook data");
            return;
        }

        log.info("Processing {} event for order {} in tenant {}", eventType, orderId, tenantId);

        // Check if already synced
        if (syncedOrderRepository.existsByPlatformOrderId(orderId)) {
            log.info("Order {} already synced, skipping", orderId);
            return;
        }

        try {
            // Get access token for this tenant
            String accessToken = tokenManager.getAccessToken(tenantId);
            if (accessToken == null) {
                log.error("No access token configured for tenant {}", tenantId);
                return;
            }

            // Fetch full order details from platform
            Optional<OrderDto> orderOpt = platformApiClient.getOrder(orderId, accessToken);
            if (orderOpt.isEmpty()) {
                log.error("Could not fetch order {} from platform", orderId);
                return;
            }

            OrderDto order = orderOpt.get();

            // Create synced order record
            SyncedOrder syncedOrder = SyncedOrder.builder()
                    .platformOrderId(orderId)
                    .tenantId(tenantId)
                    .customerEmail(order.getCustomerEmail())
                    .totalAmount(order.getTotal())
                    .platformStatus(order.getStatus())
                    .syncStatus(SyncedOrder.SyncStatus.RECEIVED)
                    .build();

            // Fetch customer details if we have a customer ID
            if (order.getCustomerId() != null) {
                try {
                    Optional<CustomerDto> customerOpt = platformApiClient.getCustomer(order.getCustomerId(), accessToken);
                    customerOpt.ifPresent(customer -> 
                        syncedOrder.setCustomerName(customer.getFirstName() + " " + customer.getLastName())
                    );
                } catch (Exception e) {
                    log.warn("Could not fetch customer details: {}", e.getMessage());
                }
            }

            syncedOrderRepository.save(syncedOrder);
            log.info("Created synced order record for platform order {}", orderId);

            // Sync to external fulfillment (simulated)
            syncToExternalFulfillment(syncedOrder, accessToken);

        } catch (Exception e) {
            log.error("Error processing order event for order {}: {}", orderId, e.getMessage(), e);
        }
    }

    /**
     * Simulate syncing to an external fulfillment system.
     * In a real app, this would call a third-party API.
     */
    @Transactional
    public void syncToExternalFulfillment(SyncedOrder syncedOrder, String accessToken) {
        log.info("Syncing order {} to external fulfillment system", syncedOrder.getPlatformOrderId());

        try {
            // Simulate external API call delay
            Thread.sleep(500);

            // Generate fake fulfillment reference
            String fulfillmentRef = "FF-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            syncedOrder.setFulfillmentReference(fulfillmentRef);
            syncedOrder.setSyncStatus(SyncedOrder.SyncStatus.SYNCED_TO_FULFILLMENT);
            syncedOrderRepository.save(syncedOrder);

            log.info("Order {} synced to fulfillment with ref: {}", 
                    syncedOrder.getPlatformOrderId(), fulfillmentRef);

            // Simulate immediate fulfillment confirmation (in reality, this would be async)
            simulateFulfillmentConfirmation(syncedOrder, accessToken);

        } catch (Exception e) {
            syncedOrder.setSyncStatus(SyncedOrder.SyncStatus.FAILED);
            syncedOrder.setErrorMessage("Failed to sync to fulfillment: " + e.getMessage());
            syncedOrderRepository.save(syncedOrder);
            log.error("Failed to sync order {} to fulfillment: {}", 
                    syncedOrder.getPlatformOrderId(), e.getMessage());
        }
    }

    /**
     * Simulate receiving fulfillment confirmation from external system.
     */
    @Transactional
    public void simulateFulfillmentConfirmation(SyncedOrder syncedOrder, String accessToken) {
        log.info("Simulating fulfillment confirmation for order {}", syncedOrder.getPlatformOrderId());

        try {
            // Simulate delay
            Thread.sleep(300);

            // Generate tracking number
            String trackingNumber = "TRK-" + UUID.randomUUID().toString().substring(0, 12).toUpperCase();
            syncedOrder.setTrackingNumber(trackingNumber);
            syncedOrder.setFulfilledAt(LocalDateTime.now());
            syncedOrder.setSyncStatus(SyncedOrder.SyncStatus.FULFILLMENT_CONFIRMED);
            syncedOrderRepository.save(syncedOrder);

            log.info("Fulfillment confirmed for order {} with tracking: {}", 
                    syncedOrder.getPlatformOrderId(), trackingNumber);

            // Update platform with fulfillment status
            updatePlatformWithFulfillment(syncedOrder, accessToken);

        } catch (Exception e) {
            syncedOrder.setSyncStatus(SyncedOrder.SyncStatus.FAILED);
            syncedOrder.setErrorMessage("Failed to process fulfillment confirmation: " + e.getMessage());
            syncedOrderRepository.save(syncedOrder);
            log.error("Failed to process fulfillment for order {}: {}", 
                    syncedOrder.getPlatformOrderId(), e.getMessage());
        }
    }

    /**
     * Update the platform with fulfillment status.
     * This calls the platform API to mark the order as FULFILLED.
     */
    @Transactional
    public void updatePlatformWithFulfillment(SyncedOrder syncedOrder, String accessToken) {
        log.info("Updating platform for order {} - marking as FULFILLED", syncedOrder.getPlatformOrderId());

        try {
            // First update to SHIPPED, then to DELIVERED (if order state machine allows)
            // For now, let's try SHIPPED
            OrderDto updatedOrder = platformApiClient.updateOrderStatus(
                    syncedOrder.getPlatformOrderId(), 
                    "SHIPPED", 
                    accessToken
            );

            syncedOrder.setPlatformStatus(updatedOrder.getStatus());
            syncedOrder.setPlatformUpdatedAt(LocalDateTime.now());
            syncedOrder.setSyncStatus(SyncedOrder.SyncStatus.PLATFORM_UPDATED);
            syncedOrderRepository.save(syncedOrder);

            log.info("Successfully updated platform - order {} is now {}", 
                    syncedOrder.getPlatformOrderId(), updatedOrder.getStatus());

        } catch (Exception e) {
            syncedOrder.setSyncStatus(SyncedOrder.SyncStatus.FAILED);
            syncedOrder.setErrorMessage("Failed to update platform: " + e.getMessage());
            syncedOrderRepository.save(syncedOrder);
            log.error("Failed to update platform for order {}: {}", 
                    syncedOrder.getPlatformOrderId(), e.getMessage());
        }
    }

    /**
     * Get all synced orders.
     */
    public List<SyncedOrder> getAllSyncedOrders() {
        return syncedOrderRepository.findAll();
    }

    /**
     * Get synced orders by status.
     */
    public List<SyncedOrder> getSyncedOrdersByStatus(SyncedOrder.SyncStatus status) {
        return syncedOrderRepository.findBySyncStatus(status);
    }

    /**
     * Get synced order by platform order ID.
     */
    public Optional<SyncedOrder> getSyncedOrderByPlatformId(Long platformOrderId) {
        return syncedOrderRepository.findByPlatformOrderId(platformOrderId);
    }

    /**
     * Manually trigger fulfillment for a pending order.
     */
    @Transactional
    public SyncedOrder manuallyFulfillOrder(Long platformOrderId, Long tenantId) {
        SyncedOrder syncedOrder = syncedOrderRepository.findByPlatformOrderId(platformOrderId)
                .orElseThrow(() -> new RuntimeException("Synced order not found: " + platformOrderId));

        String accessToken = tokenManager.getAccessToken(tenantId);
        if (accessToken == null) {
            throw new RuntimeException("No access token for tenant: " + tenantId);
        }

        simulateFulfillmentConfirmation(syncedOrder, accessToken);
        return syncedOrder;
    }

    private Long extractOrderId(Map<String, Object> data) {
        Object orderIdObj = data.get("orderId");
        if (orderIdObj == null) {
            orderIdObj = data.get("id");
        }
        if (orderIdObj instanceof Number) {
            return ((Number) orderIdObj).longValue();
        }
        if (orderIdObj instanceof String) {
            try {
                return Long.parseLong((String) orderIdObj);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }
}
