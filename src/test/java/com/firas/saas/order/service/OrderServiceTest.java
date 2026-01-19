package com.firas.saas.order.service;

import com.firas.saas.order.dto.CartItemRequest;
import com.firas.saas.order.dto.CartResponse;
import com.firas.saas.order.dto.OrderResponse;
import com.firas.saas.order.entity.Cart;
import com.firas.saas.order.entity.CartItem;
import com.firas.saas.order.entity.Order;
import com.firas.saas.order.repository.CartRepository;
import com.firas.saas.order.repository.OrderRepository;
import com.firas.saas.product.entity.Product;
import com.firas.saas.product.entity.ProductVariant;
import com.firas.saas.product.repository.ProductRepository;
import com.firas.saas.product.repository.ProductVariantRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderService Unit Tests")
class OrderServiceTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductVariantRepository productVariantRepository;

    @InjectMocks
    private OrderServiceImpl orderService;

    @Nested
    @DisplayName("addToCart method")
    class AddToCartTests {

        @Test
        @DisplayName("should add item to new cart")
        void addToCart_NewCart() {
            // Arrange
            String email = "customer@example.com";
            Long tenantId = 1L;
            CartItemRequest request = CartItemRequest.builder()
                    .variantId(20L)
                    .quantity(2)
                    .build();

            when(cartRepository.findByCustomerEmailAndTenantId(email, tenantId)).thenReturn(Optional.empty());
            
            Product product = Product.builder().name("Test Product").build();
            product.setId(10L);
            
            ProductVariant variant = ProductVariant.builder().build();
            variant.setId(20L);
            variant.setTenantId(tenantId);
            variant.setProduct(product);
            when(productVariantRepository.findById(20L)).thenReturn(Optional.of(variant));

            when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            CartResponse response = orderService.addToCart(request, email, tenantId);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.getItems()).hasSize(1);
            assertThat(response.getItems().get(0).getQuantity()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("placeOrder method")
    class PlaceOrderTests {

        @Test
        @DisplayName("should successfully place order and clear cart")
        void placeOrder_Success() {
            // Arrange
            String email = "customer@example.com";
            Long tenantId = 1L;
            
            Cart cart = Cart.builder().customerEmail(email).build();
            cart.setTenantId(tenantId);
            CartItem cartItem = CartItem.builder().productId(10L).variantId(20L).quantity(2).build();
            cart.addItem(cartItem);

            when(cartRepository.findByCustomerEmailAndTenantId(email, tenantId)).thenReturn(Optional.of(cart));

            Product product = Product.builder().name("T-Shirt").build();
            product.setId(10L);
            ProductVariant variant = ProductVariant.builder().name("Red/L").sku("SKU-RED-L").price(new BigDecimal("20.00")).stockLevel(10).build();
            variant.setId(20L);
            variant.setTenantId(tenantId);

            when(productVariantRepository.findById(20L)).thenReturn(Optional.of(variant));
            when(productRepository.findById(10L)).thenReturn(Optional.of(product));
            when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
                Order order = invocation.getArgument(0);
                order.setId(100L);
                return order;
            });

            // Act
            OrderResponse response = orderService.placeOrder(email, tenantId);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.getTotalPrice()).isEqualByComparingTo("40.00");
            assertThat(variant.getStockLevel()).isEqualTo(8); // 10 - 2
            
            verify(productVariantRepository).save(any(ProductVariant.class));
            verify(cartRepository).delete(cart);
            verify(orderRepository).save(any(Order.class));
        }

        @Test
        @DisplayName("should throw exception when stock is insufficient")
        void placeOrder_InsufficientStock() {
            // Arrange
            String email = "customer@example.com";
            Long tenantId = 1L;
            
            Cart cart = Cart.builder().customerEmail(email).build();
            CartItem cartItem = CartItem.builder().productId(10L).variantId(20L).quantity(5).build();
            cart.addItem(cartItem);

            when(cartRepository.findByCustomerEmailAndTenantId(email, tenantId)).thenReturn(Optional.of(cart));

            ProductVariant variant = ProductVariant.builder().name("Red/L").stockLevel(2).build();
            when(productVariantRepository.findById(20L)).thenReturn(Optional.of(variant));

            // Act & Assert
            RuntimeException exception = assertThrows(RuntimeException.class, () -> orderService.placeOrder(email, tenantId));
            assertThat(exception.getMessage()).contains("Insufficient stock");
            verify(orderRepository, never()).save(any());
        }
    }
}
