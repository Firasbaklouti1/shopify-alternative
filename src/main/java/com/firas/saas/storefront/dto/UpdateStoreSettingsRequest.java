package com.firas.saas.storefront.dto;

import com.firas.saas.storefront.entity.CheckoutMode;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Request DTO for updating store settings (merchant API)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateStoreSettingsRequest {

    private Long themeId;

    private CheckoutMode checkoutMode;

    private Map<String, Object> globalStyles;

    private Map<String, Object> seoDefaults;

    private Map<String, Object> socialLinks;

    @Email(message = "Contact email must be valid")
    private String contactEmail;

    @Pattern(regexp = "^[a-z0-9]([a-z0-9-]*[a-z0-9])?(\\.[a-z0-9]([a-z0-9-]*[a-z0-9])?)*$",
             message = "Custom domain must be a valid domain name")
    private String customDomain;

    private String announcementText;

    private Boolean announcementEnabled;

    private Boolean published;
}
