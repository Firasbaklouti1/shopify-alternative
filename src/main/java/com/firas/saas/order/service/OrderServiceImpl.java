package com.firas.saas.order.service;

import com.firas.saas.order.dto.*;
import com.firas.saas.order.entity.*;
import com.firas.saas.order.repository.CartRepository;
import com.firas.saas.order.repository.OrderRepository;
import com.firas.saas.product.entity.Product;
import com.firas.saas.product.entity.ProductVariant;
import com.firas.saas.product.repository.ProductRepository;
import com.firas.saas.product.repository.ProductVariantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final CartRepository cartRepository;
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;

    @Override
    @Transactional
    public CartResponse addToCart(CartItemRequest request, String customerEmail, Long tenantId) {
        System.out.println("Adding to cart. SKU: " + request.getSku() + ", ID: " + request.getVariantId() + " for email: " + customerEmail);

        Cart cart = cartRepository.findByCustomerEmailAndTenantId(customerEmail, tenantId)
                .orElseGet(() -> {
                    Cart newCart = Cart.builder().customerEmail(customerEmail).build();
                    newCart.setTenantId(tenantId);
                    return newCart;
                });

        // Validate product/variant exists and belongs to tenant
        ProductVariant variant;
        if (request.getVariantId() != null) {
            System.out.println("Looking up by ID: " + request.getVariantId());
            variant = productVariantRepository.findById(request.getVariantId())
                    .orElseThrow(() -> new RuntimeException("Variant not found"));
        } else if (request.getSku() != null) {
            System.out.println("Looking up by SKU: " + request.getSku() + " and tenantId: " + tenantId);
            variant = productVariantRepository.findBySkuAndTenantId(request.getSku(), tenantId)
                    .orElseThrow(() -> new RuntimeException("Variant with SKU " + request.getSku() + " not found"));
        } else {
            throw new RuntimeException("Either variantId or sku must be provided");
        }
        
        System.out.println("Found variant: " + variant.getName() + " with ID: " + variant.getId());

        if (!variant.getTenantId().equals(tenantId)) {
            throw new RuntimeException("Variant does not belong to this tenant");
        }

        Long vId = variant.getId();
        Long pId = variant.getProduct().getId();

        // Check if item already in cart
        cart.getItems().stream()
                .filter(item -> item.getVariantId().equals(vId))
                .findFirst()
                .ifPresentOrElse(
                        item -> item.setQuantity(item.getQuantity() + request.getQuantity()),
                        () -> cart.addItem(CartItem.builder()
                                .productId(pId)
                                .variantId(vId)
                                .quantity(request.getQuantity())
                                .build())
                );

        Cart saved = cartRepository.save(cart);
        return mapToCartResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public CartResponse getCart(String customerEmail, Long tenantId) {
        return cartRepository.findByCustomerEmailAndTenantId(customerEmail, tenantId)
                .map(this::mapToCartResponse)
                .orElse(CartResponse.builder().customerEmail(customerEmail).build());
    }

    @Override
    @Transactional
    public void clearCart(String customerEmail, Long tenantId) {
        cartRepository.findByCustomerEmailAndTenantId(customerEmail, tenantId)
                .ifPresent(cartRepository::delete);
    }

    @Override
    @Transactional
    public OrderResponse placeOrder(String customerEmail, Long tenantId) {
        Cart cart = cartRepository.findByCustomerEmailAndTenantId(customerEmail, tenantId)
                .orElseThrow(() -> new RuntimeException("Cart is empty"));

        if (cart.getItems().isEmpty()) {
            throw new RuntimeException("Cart is empty");
        }

        Order order = Order.builder()
                .orderNumber("ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .customerEmail(customerEmail)
                .status(OrderStatus.PENDING)
                .totalPrice(BigDecimal.ZERO)
                .build();
        order.setTenantId(tenantId);

        BigDecimal grandTotal = BigDecimal.ZERO;

        for (CartItem cartItem : cart.getItems()) {
            ProductVariant variant = productVariantRepository.findById(cartItem.getVariantId())
                    .orElseThrow(() -> new RuntimeException("Variant not found: " + cartItem.getVariantId()));

            if (variant.getStockLevel() < cartItem.getQuantity()) {
                throw new RuntimeException("Insufficient stock for: " + variant.getName());
            }

            // Deduct stock
            variant.setStockLevel(variant.getStockLevel() - cartItem.getQuantity());
            productVariantRepository.save(variant);

            Product product = productRepository.findById(cartItem.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product not found"));

            OrderItem orderItem = OrderItem.builder()
                    .productId(product.getId())
                    .variantId(variant.getId())
                    .productName(product.getName())
                    .variantName(variant.getName())
                    .sku(variant.getSku())
                    .price(variant.getPrice())
                    .quantity(cartItem.getQuantity())
                    .build();
            
            order.addItem(orderItem);
            grandTotal = grandTotal.add(variant.getPrice().multiply(new BigDecimal(cartItem.getQuantity())));
        }

        order.setTotalPrice(grandTotal);
        Order savedOrder = orderRepository.save(order);
        
        // Clear cart after order placement
        cartRepository.delete(cart);

        return mapToOrderResponse(savedOrder);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getCustomerOrders(String customerEmail, Long tenantId) {
        return orderRepository.findAllByCustomerEmailAndTenantId(customerEmail, tenantId).stream()
                .map(this::mapToOrderResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderByNumber(String orderNumber, Long tenantId) {
        return orderRepository.findByOrderNumberAndTenantId(orderNumber, tenantId)
                .map(this::mapToOrderResponse)
                .orElseThrow(() -> new RuntimeException("Order not found"));
    }

    private CartResponse mapToCartResponse(Cart cart) {
        return CartResponse.builder()
                .customerEmail(cart.getCustomerEmail())
                .items(cart.getItems().stream()
                        .map(item -> CartItemResponse.builder()
                                .productId(item.getProductId())
                                .variantId(item.getVariantId())
                                .quantity(item.getQuantity())
                                .build())
                        .collect(Collectors.toList()))
                .build();
    }

    private OrderResponse mapToOrderResponse(Order order) {
        return OrderResponse.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .customerEmail(order.getCustomerEmail())
                .status(order.getStatus())
                .totalPrice(order.getTotalPrice())
                .createdAt(order.getCreatedAt())
                .items(order.getItems().stream()
                        .map(item -> OrderItemResponse.builder()
                                .id(item.getId())
                                .productId(item.getProductId())
                                .variantId(item.getVariantId())
                                .productName(item.getProductName())
                                .variantName(item.getVariantName())
                                .sku(item.getSku())
                                .price(item.getPrice())
                                .quantity(item.getQuantity())
                                .build())
                        .collect(Collectors.toList()))
                .build();
    }
}
