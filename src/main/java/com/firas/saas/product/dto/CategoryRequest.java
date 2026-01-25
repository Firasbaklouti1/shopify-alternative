package com.firas.saas.product.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryRequest {
    @NotBlank(message = "Category name is required")
    private String name;

    @NotBlank(message = "Slug is required")
    private String slug;

    private String description;

    private String imageUrl;
}
