package com.firas.saas.storefront.dto;

import com.firas.saas.storefront.entity.PageType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Request DTO for creating a new custom page (merchant API)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatePageRequest {

    /**
     * Page type - for custom pages, use CUSTOM
     */
    @NotNull(message = "Page type is required")
    private PageType pageType;

    /**
     * URL handle for the page (required for CUSTOM pages)
     * Example: "about-us", "contact", "faq"
     */
    @Pattern(regexp = "^[a-z0-9]+(-[a-z0-9]+)*$",
             message = "Handle must be lowercase alphanumeric with hyphens")
    @Size(min = 2, max = 50, message = "Handle must be between 2 and 50 characters")
    private String handle;

    /**
     * Display name for the page
     */
    @NotBlank(message = "Page name is required")
    @Size(max = 100, message = "Page name must be less than 100 characters")
    private String name;

    /**
     * Initial layout configuration (optional - uses default if not provided)
     */
    private Map<String, Object> layoutJson;

    /**
     * SEO title
     */
    @Size(max = 70, message = "SEO title should be less than 70 characters")
    private String seoTitle;

    /**
     * SEO description
     */
    @Size(max = 160, message = "SEO description should be less than 160 characters")
    private String seoDescription;
}
