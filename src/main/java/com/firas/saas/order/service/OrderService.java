package com.firas.saas.order.service;

import com.firas.saas.order.dto.CartItemRequest;
import com.firas.saas.order.dto.CartResponse;
import com.firas.saas.order.dto.OrderResponse;

import java.util.List;

public interface OrderService {
    CartResponse addToCart(CartItemRequest request, String customerEmail, Long tenantId);
    CartResponse getCart(String customerEmail, Long tenantId);
    void clearCart(String customerEmail, Long tenantId);

    OrderResponse placeOrder(String customerEmail, Long tenantId);
    List<OrderResponse> getCustomerOrders(String customerEmail, Long tenantId);
    OrderResponse getOrderByNumber(String orderNumber, Long tenantId);
}
