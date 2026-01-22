package com.firas.saas.storefront.dto;

import com.firas.saas.storefront.entity.PageType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Response DTO for page layout (used by both public storefront and merchant API)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LayoutResponse {

    private Long id;
    private PageType pageType;
    private String handle;
    private String name;
    private Map<String, Object> layoutJson;
    private boolean published;
    private Integer version;
    private String seoTitle;
    private String seoDescription;
    private LocalDateTime updatedAt;

    /**
     * Whether there are unpublished draft changes
     */
    private boolean hasDraft;
}
