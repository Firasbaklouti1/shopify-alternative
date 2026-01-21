package com.firas.saas.storefront.dto;

import com.firas.saas.storefront.entity.PageType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Request DTO for updating page layout (merchant API)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateLayoutRequest {

    /**
     * The full layout configuration JSON.
     * Must contain "sections" and "order" keys.
     */
    @NotNull(message = "Layout JSON is required")
    private Map<String, Object> layoutJson;

    /**
     * Optional page name for display in editor
     */
    @Size(max = 100, message = "Page name must be less than 100 characters")
    private String name;

    /**
     * SEO title override
     */
    @Size(max = 70, message = "SEO title should be less than 70 characters")
    private String seoTitle;

    /**
     * SEO description override
     */
    @Size(max = 160, message = "SEO description should be less than 160 characters")
    private String seoDescription;
}
