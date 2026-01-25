package com.firas.saas.order.service;

import com.firas.saas.order.dto.CartItemRequest;
import com.firas.saas.order.dto.CartResponse;
import com.firas.saas.order.dto.GuestCheckoutRequest;
import com.firas.saas.order.dto.OrderResponse;
import com.firas.saas.order.entity.OrderStatus;

import java.util.List;

public interface OrderService {
    CartResponse addToCart(CartItemRequest request, String customerEmail, Long tenantId);
    CartResponse getCart(String customerEmail, Long tenantId);
    void clearCart(String customerEmail, Long tenantId);

    OrderResponse placeOrder(String customerEmail, Long tenantId);

    /**
     * Guest checkout - creates an order directly from cart items without authentication.
     * Used by the public storefront API.
     */
    OrderResponse placeGuestOrder(GuestCheckoutRequest request, Long tenantId);

    List<OrderResponse> getCustomerOrders(String customerEmail, Long tenantId);
    OrderResponse getOrderByNumber(String orderNumber, Long tenantId);
    OrderResponse getOrderById(Long orderId, Long tenantId);
    OrderResponse updateOrderStatus(Long orderId, OrderStatus status, Long tenantId);
    List<OrderResponse> getAllOrders(Long tenantId);
}
