package com.firas.saas.storefront.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * Public product response for storefront API.
 * Contains only customer-facing information.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PublicProductResponse {

    private Long id;
    private String name;
    private String slug;
    private String description;
    private BigDecimal price;
    private BigDecimal compareAtPrice;
    private String imageUrl;
    private List<String> images;
    private String categoryName;
    private String categorySlug;
    private boolean inStock;
    private List<VariantInfo> variants;
    private String vendor;
    private List<String> tags;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VariantInfo {
        private Long id;
        private String name;
        private String sku;
        private BigDecimal price;
        private BigDecimal compareAtPrice;
        private boolean inStock;
        private int quantity;
        private List<OptionValue> options;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OptionValue {
        private String name;
        private String value;
    }
}
