package com.firas.saas.product.service;

import com.firas.saas.product.dto.*;
import com.firas.saas.product.entity.Category;
import com.firas.saas.product.entity.Product;
import com.firas.saas.product.entity.ProductVariant;
import com.firas.saas.product.repository.CategoryRepository;
import com.firas.saas.product.repository.ProductRepository;
import com.firas.saas.product.repository.ProductVariantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;
    private final com.firas.saas.webhook.service.WebhookService webhookService;
    private final com.firas.saas.tenant.repository.TenantRepository tenantRepository;

    @Override
    @Transactional
    public CategoryResponse createCategory(CategoryRequest request, Long tenantId) {
        if (categoryRepository.existsByNameAndTenantId(request.getName(), tenantId)) {
            throw new RuntimeException("Category already exists for this tenant");
        }

        Category category = Category.builder()
                .name(request.getName())
                .slug(request.getSlug())
                .description(request.getDescription())
                .build();
        category.setTenantId(tenantId);

        Category saved = categoryRepository.save(category);
        return mapToCategoryResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> getAllCategories(Long tenantId) {
        return categoryRepository.findAllByTenantId(tenantId).stream()
                .map(this::mapToCategoryResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ProductResponse createProduct(ProductRequest request, Long tenantId) {
        if (productRepository.existsBySlugAndTenantId(request.getSlug(), tenantId)) {
            throw new RuntimeException("Product slug already exists for this tenant");
        }

        Category category = null;
        if (request.getCategoryId() != null) {
            category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Category not found"));
            if (!category.getTenantId().equals(tenantId)) {
                throw new RuntimeException("Category does not belong to this tenant");
            }
        }

        Product product = Product.builder()
                .name(request.getName())
                .slug(request.getSlug())
                .description(request.getDescription())
                .category(category)
                .active(true)
                .build();
        product.setTenantId(tenantId);

        for (ProductVariantRequest variantRequest : request.getVariants()) {
            if (productVariantRepository.existsBySkuAndTenantId(variantRequest.getSku(), tenantId)) {
                throw new RuntimeException("SKU " + variantRequest.getSku() + " already exists for this tenant");
            }
            ProductVariant variant = ProductVariant.builder()
                    .name(variantRequest.getName())
                    .sku(variantRequest.getSku())
                    .price(variantRequest.getPrice())
                    .stockLevel(variantRequest.getStockLevel())
                    .build();
            variant.setTenantId(tenantId);
            product.addVariant(variant);
        }

        Product saved = productRepository.save(product);

        // Trigger Webhook
        try {
            String tenantSlug = tenantRepository.findById(tenantId)
                    .map(com.firas.saas.tenant.entity.Tenant::getSlug)
                    .orElse("unknown");

            java.util.Map<String, Object> data = java.util.Map.of(
                    "id", saved.getId(),
                    "name", saved.getName(),
                    "slug", saved.getSlug()
            );

            webhookService.triggerEvent(com.firas.saas.webhook.entity.Webhook.WebhookEvent.PRODUCT_CREATED, 
                    data, tenantId, tenantSlug);
        } catch (Exception e) {
            System.err.println("Failed to trigger PRODUCT_CREATED webhook: " + e.getMessage());
        }

        return mapToProductResponse(saved);
    }

    @Override
    @Transactional
    public ProductResponse updateProduct(Long id, ProductRequest request, Long tenantId) {
        Product product = productRepository.findById(id)
                .filter(p -> p.getTenantId().equals(tenantId))
                .orElseThrow(() -> new RuntimeException("Product not found or access denied"));

        product.setName(request.getName());
        product.setSlug(request.getSlug());
        product.setDescription(request.getDescription());
        
        // Simulating simple attribute update for brevity
        // In a real scenario, you'd handle variants and category changes more carefully
        
        Product saved = productRepository.save(product);

        // Trigger Webhook
        try {
            String tenantSlug = tenantRepository.findById(tenantId)
                    .map(com.firas.saas.tenant.entity.Tenant::getSlug)
                    .orElse("unknown");

            java.util.Map<String, Object> data = java.util.Map.of(
                    "id", saved.getId(),
                    "name", saved.getName(),
                    "slug", saved.getSlug()
            );

            webhookService.triggerEvent(com.firas.saas.webhook.entity.Webhook.WebhookEvent.PRODUCT_UPDATED, 
                    data, tenantId, tenantSlug);
        } catch (Exception e) {
            System.err.println("Failed to trigger PRODUCT_UPDATED webhook: " + e.getMessage());
        }

        return mapToProductResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse getProductBySlug(String slug, Long tenantId) {
        return productRepository.findBySlugAndTenantId(slug, tenantId)
                .map(this::mapToProductResponse)
                .orElseThrow(() -> new RuntimeException("Product not found"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> getAllProducts(Long tenantId) {
        return productRepository.findAllByTenantId(tenantId).stream()
                .map(this::mapToProductResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteCategory(Long id, Long tenantId) {
        Category category = categoryRepository.findById(id)
                .filter(c -> c.getTenantId().equals(tenantId))
                .orElseThrow(() -> new RuntimeException("Category not found or access denied"));
        
        // Nullify category reference in all associated products
        List<Product> productsInCategory = productRepository.findByCategoryIdAndTenantId(id, tenantId);
        for (Product product : productsInCategory) {
            product.setCategory(null);
            productRepository.save(product);
        }
        
        categoryRepository.delete(category);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse getProductById(Long id, Long tenantId) {
        return productRepository.findById(id)
                .filter(p -> p.getTenantId().equals(tenantId))
                .map(this::mapToProductResponse)
                .orElseThrow(() -> new RuntimeException("Product not found or access denied"));
    }

    @Override
    @Transactional
    public void deleteProduct(Long id, Long tenantId) {
        Product product = productRepository.findById(id)
                .filter(p -> p.getTenantId().equals(tenantId))
                .orElseThrow(() -> new RuntimeException("Product not found or access denied"));
        productRepository.delete(product);

        // Trigger Webhook
        try {
            String tenantSlug = tenantRepository.findById(tenantId)
                    .map(com.firas.saas.tenant.entity.Tenant::getSlug)
                    .orElse("unknown");

            java.util.Map<String, Object> data = java.util.Map.of(
                    "id", id,
                    "name", product.getName(),
                    "slug", product.getSlug()
            );

            webhookService.triggerEvent(com.firas.saas.webhook.entity.Webhook.WebhookEvent.PRODUCT_DELETED, 
                    data, tenantId, tenantSlug);
        } catch (Exception e) {
            System.err.println("Failed to trigger PRODUCT_DELETED webhook: " + e.getMessage());
        }
    }

    private CategoryResponse mapToCategoryResponse(Category category) {
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .slug(category.getSlug())
                .description(category.getDescription())
                .build();
    }

    private ProductResponse mapToProductResponse(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .slug(product.getSlug())
                .description(product.getDescription())
                .active(product.isActive())
                .category(product.getCategory() != null ? mapToCategoryResponse(product.getCategory()) : null)
                .variants(product.getVariants().stream()
                        .map(v -> ProductVariantResponse.builder()
                                .id(v.getId())
                                .name(v.getName())
                                .sku(v.getSku())
                                .price(v.getPrice())
                                .stockLevel(v.getStockLevel())
                                .build())
                        .collect(Collectors.toList()))
                .build();
    }
}
