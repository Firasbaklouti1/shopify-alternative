package com.firas.saas.product.service;

import com.firas.saas.product.dto.CategoryRequest;
import com.firas.saas.product.dto.CategoryResponse;
import com.firas.saas.product.dto.ProductRequest;
import com.firas.saas.product.dto.ProductResponse;

import java.util.List;

public interface ProductService {
    CategoryResponse createCategory(CategoryRequest request, Long tenantId);
    List<CategoryResponse> getAllCategories(Long tenantId);
    void deleteCategory(Long id, Long tenantId);

    ProductResponse createProduct(ProductRequest request, Long tenantId);
    ProductResponse updateProduct(Long id, ProductRequest request, Long tenantId);
    ProductResponse getProductById(Long id, Long tenantId);
    ProductResponse getProductBySlug(String slug, Long tenantId);
    List<ProductResponse> getAllProducts(Long tenantId);
    void deleteProduct(Long id, Long tenantId);
}
