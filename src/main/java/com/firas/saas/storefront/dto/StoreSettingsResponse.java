package com.firas.saas.storefront.dto;

import com.firas.saas.storefront.entity.CheckoutMode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Response DTO for store settings (public storefront API)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StoreSettingsResponse {

    private String storeName;
    private String storeSlug;
    private CheckoutMode checkoutMode;
    private Map<String, Object> globalStyles;
    private Map<String, Object> seoDefaults;
    private Map<String, Object> socialLinks;
    private String contactEmail;
    private String announcementText;
    private boolean announcementEnabled;
    private ThemeSummary theme;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ThemeSummary {
        private Long id;
        private String name;
        private Map<String, Object> cssVariables;
    }
}
