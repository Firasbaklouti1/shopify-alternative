package com.firas.saas.storefront.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Public category/collection response for storefront API.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PublicCategoryResponse {

    private Long id;
    private String name;
    private String slug;
    private String description;
    private String imageUrl;
    private int productCount;
    private List<PublicCategoryResponse> subcategories;
}
