package com.ordersync.service;

import com.ordersync.dto.CustomerDto;
import com.ordersync.dto.OrderDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Client for calling the platform's App API endpoints.
 * Uses the app's access token for authentication.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PlatformApiClient {

    private final RestTemplate restTemplate;

    @Value("${platform.api.base-url}")
    private String platformBaseUrl;

    /**
     * Get order by ID from the platform.
     */
    public Optional<OrderDto> getOrder(Long orderId, String accessToken) {
        String url = platformBaseUrl + "/app/orders/" + orderId;
        
        try {
            HttpHeaders headers = createHeaders(accessToken);
            HttpEntity<Void> entity = new HttpEntity<>(headers);
            
            ResponseEntity<OrderDto> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity, OrderDto.class);
            
            log.info("Fetched order {} from platform", orderId);
            return Optional.ofNullable(response.getBody());
        } catch (HttpClientErrorException.NotFound e) {
            log.warn("Order {} not found on platform", orderId);
            return Optional.empty();
        } catch (HttpClientErrorException.Forbidden e) {
            log.error("Access denied fetching order {} - missing scope", orderId);
            throw new RuntimeException("Missing READ_ORDERS scope", e);
        } catch (Exception e) {
            log.error("Error fetching order {}: {}", orderId, e.getMessage());
            throw new RuntimeException("Failed to fetch order from platform", e);
        }
    }

    /**
     * Get all orders from the platform.
     */
    public List<OrderDto> getAllOrders(String accessToken) {
        String url = platformBaseUrl + "/app/orders";
        
        try {
            HttpHeaders headers = createHeaders(accessToken);
            HttpEntity<Void> entity = new HttpEntity<>(headers);
            
            ResponseEntity<OrderDto[]> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity, OrderDto[].class);
            
            OrderDto[] orders = response.getBody();
            log.info("Fetched {} orders from platform", orders != null ? orders.length : 0);
            return orders != null ? Arrays.asList(orders) : List.of();
        } catch (HttpClientErrorException.Forbidden e) {
            log.error("Access denied fetching orders - missing scope");
            throw new RuntimeException("Missing READ_ORDERS scope", e);
        } catch (Exception e) {
            log.error("Error fetching orders: {}", e.getMessage());
            throw new RuntimeException("Failed to fetch orders from platform", e);
        }
    }

    /**
     * Get customer by ID from the platform.
     */
    public Optional<CustomerDto> getCustomer(Long customerId, String accessToken) {
        String url = platformBaseUrl + "/app/customers/" + customerId;
        
        try {
            HttpHeaders headers = createHeaders(accessToken);
            HttpEntity<Void> entity = new HttpEntity<>(headers);
            
            ResponseEntity<CustomerDto> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity, CustomerDto.class);
            
            log.info("Fetched customer {} from platform", customerId);
            return Optional.ofNullable(response.getBody());
        } catch (HttpClientErrorException.NotFound e) {
            log.warn("Customer {} not found on platform", customerId);
            return Optional.empty();
        } catch (HttpClientErrorException.Forbidden e) {
            log.error("Access denied fetching customer {} - missing scope", customerId);
            throw new RuntimeException("Missing READ_CUSTOMERS scope", e);
        } catch (Exception e) {
            log.error("Error fetching customer {}: {}", customerId, e.getMessage());
            throw new RuntimeException("Failed to fetch customer from platform", e);
        }
    }

    /**
     * Update order status on the platform (e.g., mark as FULFILLED).
     */
    public OrderDto updateOrderStatus(Long orderId, String status, String accessToken) {
        String url = platformBaseUrl + "/app/orders/" + orderId + "/status?status=" + status;
        
        try {
            HttpHeaders headers = createHeaders(accessToken);
            HttpEntity<Void> entity = new HttpEntity<>(headers);
            
            ResponseEntity<OrderDto> response = restTemplate.exchange(
                    url, HttpMethod.PATCH, entity, OrderDto.class);
            
            log.info("Updated order {} status to {} on platform", orderId, status);
            return response.getBody();
        } catch (HttpClientErrorException.Forbidden e) {
            log.error("Access denied updating order {} - missing WRITE_ORDERS scope", orderId);
            throw new RuntimeException("Missing WRITE_ORDERS scope", e);
        } catch (HttpClientErrorException.BadRequest e) {
            log.error("Invalid status transition for order {}: {}", orderId, e.getResponseBodyAsString());
            throw new RuntimeException("Invalid order status transition", e);
        } catch (Exception e) {
            log.error("Error updating order {} status: {}", orderId, e.getMessage());
            throw new RuntimeException("Failed to update order status on platform", e);
        }
    }

    /**
     * Get app info (/me endpoint) to verify token is valid.
     */
    public Optional<AppInfoDto> getAppInfo(String accessToken) {
        String url = platformBaseUrl + "/app/me";
        
        try {
            HttpHeaders headers = createHeaders(accessToken);
            HttpEntity<Void> entity = new HttpEntity<>(headers);
            
            ResponseEntity<AppInfoDto> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity, AppInfoDto.class);
            
            log.info("Verified app token - app info retrieved");
            return Optional.ofNullable(response.getBody());
        } catch (HttpClientErrorException.Unauthorized e) {
            log.warn("Invalid or expired access token");
            return Optional.empty();
        } catch (Exception e) {
            log.error("Error verifying app token: {}", e.getMessage());
            return Optional.empty();
        }
    }

    private HttpHeaders createHeaders(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(accessToken);
        return headers;
    }

    @lombok.Data
    public static class AppInfoDto {
        private Long appId;
        private String clientId;
        private Long installationId;
        private Long tenantId;
        private List<String> scopes;
    }
}
