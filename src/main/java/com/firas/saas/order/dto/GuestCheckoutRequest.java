package com.firas.saas.order.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Request DTO for guest checkout (no authentication required).
 * Contains customer info and cart items.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GuestCheckoutRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    private String phone;

    // Shipping address
    @NotBlank(message = "Address is required")
    private String address;

    @NotBlank(message = "City is required")
    private String city;

    @NotBlank(message = "State is required")
    private String state;

    @NotBlank(message = "ZIP code is required")
    private String zipCode;

    @NotBlank(message = "Country is required")
    private String country;

    // Cart items
    @NotEmpty(message = "Cart cannot be empty")
    @Valid
    private List<GuestCartItem> items;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class GuestCartItem {
        private Long productId;

        @NotNull(message = "Variant ID is required")
        private Long variantId;

        @NotNull(message = "Quantity is required")
        @Min(value = 1, message = "Quantity must be at least 1")
        private Integer quantity;
    }
}
