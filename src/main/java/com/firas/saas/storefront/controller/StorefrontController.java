package com.firas.saas.storefront.controller;

import com.firas.saas.common.exception.ResourceNotFoundException;
import com.firas.saas.storefront.dto.*;
import com.firas.saas.storefront.entity.PageType;
import com.firas.saas.storefront.schema.ComponentRegistry;
import com.firas.saas.storefront.schema.SectionSchema;
import com.firas.saas.storefront.service.PageLayoutService;
import com.firas.saas.storefront.service.StoreSettingsService;
import com.firas.saas.storefront.service.ThemeService;
import com.firas.saas.tenant.entity.Tenant;
import com.firas.saas.tenant.repository.TenantRepository;
import com.firas.saas.product.repository.ProductRepository;
import com.firas.saas.product.repository.CategoryRepository;
import com.firas.saas.product.entity.Product;
import com.firas.saas.product.entity.Category;
import com.firas.saas.product.entity.ProductVariant;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Public Storefront API - No authentication required.
 * All endpoints are identified by tenant slug in the URL.
 *
 * This API is consumed by the Next.js storefront renderer.
 */
@RestController
@RequestMapping("/api/v1/storefront/{slug}")
@RequiredArgsConstructor
public class StorefrontController {

    private final StoreSettingsService storeSettingsService;
    private final PageLayoutService pageLayoutService;
    private final ThemeService themeService;
    private final ComponentRegistry componentRegistry;
    private final TenantRepository tenantRepository;
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    /**
     * Get store settings (branding, checkout mode, theme)
     * Called once when the storefront loads.
     */
    @GetMapping("/settings")
    public ResponseEntity<StoreSettingsResponse> getStoreSettings(@PathVariable String slug) {
        StoreSettingsResponse settings = storeSettingsService.getStoreSettingsBySlug(slug);
        return ResponseEntity.ok(settings);
    }

    /**
     * Get page layout JSON for rendering.
     * The Next.js renderer fetches this and maps sections to React components.
     *
     * @param slug Store slug
     * @param page Page type (home, product, collection, cart, checkout)
     */
    @GetMapping("/layout")
    public ResponseEntity<Map<String, Object>> getPageLayout(
            @PathVariable String slug,
            @RequestParam(defaultValue = "home") String page) {

        Tenant tenant = tenantRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Store", slug));

        // Check if store is published
        if (!storeSettingsService.isStorePublished(tenant.getId())) {
            throw new ResourceNotFoundException("Store", slug);
        }

        PageType pageType;
        try {
            pageType = PageType.valueOf(page.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ResourceNotFoundException("Page type", page);
        }

        Map<String, Object> layout = pageLayoutService.getPublishedLayoutJson(tenant.getId(), pageType);
        return ResponseEntity.ok(layout);
    }

    /**
     * Get custom page layout by handle
     */
    @GetMapping("/pages/{handle}")
    public ResponseEntity<Map<String, Object>> getCustomPageLayout(
            @PathVariable String slug,
            @PathVariable String handle) {

        Tenant tenant = tenantRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Store", slug));

        if (!storeSettingsService.isStorePublished(tenant.getId())) {
            throw new ResourceNotFoundException("Store", slug);
        }

        Map<String, Object> layout = pageLayoutService.getPublishedCustomPageLayoutJson(tenant.getId(), handle);
        return ResponseEntity.ok(layout);
    }

    /**
     * Get all products for the store (paginated)
     * Uses proper database-level pagination for performance.
     */
    @GetMapping("/products")
    public ResponseEntity<Map<String, Object>> getProducts(
            @PathVariable String slug,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "24") int limit,
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        Tenant tenant = tenantRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Store", slug));

        if (!storeSettingsService.isStorePublished(tenant.getId())) {
            throw new ResourceNotFoundException("Store", slug);
        }

        // Build pageable with sorting
        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, Math.min(limit, 100), sort); // Cap at 100 per page

        // Use proper database pagination
        Page<Product> productPage;
        if (category != null && !category.isBlank()) {
            productPage = productRepository.findByCategorySlugAndTenantId(category, tenant.getId(), pageable);
        } else {
            productPage = productRepository.findByTenantId(tenant.getId(), pageable);
        }

        List<PublicProductResponse> products = productPage.getContent().stream()
                .map(this::mapToPublicProduct)
                .collect(Collectors.toList());

        // Return paginated response with metadata
        Map<String, Object> response = new HashMap<>();
        response.put("products", products);
        response.put("currentPage", productPage.getNumber());
        response.put("totalPages", productPage.getTotalPages());
        response.put("totalProducts", productPage.getTotalElements());
        response.put("hasNext", productPage.hasNext());
        response.put("hasPrevious", productPage.hasPrevious());

        return ResponseEntity.ok(response);
    }

    /**
     * Get single product by slug/handle
     */
    @GetMapping("/products/{productSlug}")
    public ResponseEntity<PublicProductResponse> getProduct(
            @PathVariable String slug,
            @PathVariable String productSlug) {

        Tenant tenant = tenantRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Store", slug));

        if (!storeSettingsService.isStorePublished(tenant.getId())) {
            throw new ResourceNotFoundException("Store", slug);
        }

        Product product = productRepository.findBySlugAndTenantId(productSlug, tenant.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Product", productSlug));

        return ResponseEntity.ok(mapToPublicProduct(product));
    }

    /**
     * Get all categories/collections for the store.
     * Uses a single batch query for product counts to avoid N+1 select problem.
     */
    @GetMapping("/collections")
    public ResponseEntity<List<PublicCategoryResponse>> getCollections(@PathVariable String slug) {
        Tenant tenant = tenantRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Store", slug));

        if (!storeSettingsService.isStorePublished(tenant.getId())) {
            throw new ResourceNotFoundException("Store", slug);
        }

        List<Category> categories = categoryRepository.findAllByTenantId(tenant.getId());

        // Fetch all product counts in a single query (avoids N+1 selects)
        Map<Long, Long> productCounts = new HashMap<>();
        List<Object[]> countResults = productRepository.countProductsByCategory(tenant.getId());
        for (Object[] row : countResults) {
            Long categoryId = (Long) row[0];
            Long count = (Long) row[1];
            if (categoryId != null) {
                productCounts.put(categoryId, count);
            }
        }

        List<PublicCategoryResponse> response = categories.stream()
                .map(category -> mapToPublicCategory(category, productCounts.getOrDefault(category.getId(), 0L).intValue()))
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    /**
     * Get single collection by slug/handle
     */
    @GetMapping("/collections/{collectionSlug}")
    public ResponseEntity<PublicCategoryResponse> getCollection(
            @PathVariable String slug,
            @PathVariable String collectionSlug) {

        Tenant tenant = tenantRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Store", slug));

        if (!storeSettingsService.isStorePublished(tenant.getId())) {
            throw new ResourceNotFoundException("Store", slug);
        }

        Category category = categoryRepository.findBySlugAndTenantId(collectionSlug, tenant.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Collection", collectionSlug));

        // Single query for product count (acceptable for single-item fetch)
        int productCount = productRepository.countByCategoryIdAndTenantId(category.getId(), tenant.getId());

        return ResponseEntity.ok(mapToPublicCategory(category, productCount));
    }

    /**
     * Get available themes (for theme preview/selection)
     */
    @GetMapping("/themes")
    public ResponseEntity<List<ThemeResponse>> getThemes() {
        return ResponseEntity.ok(themeService.getActiveThemes());
    }

    /**
     * Get component schema registry (for editor and AI generation)
     */
    @GetMapping("/schema/components")
    public ResponseEntity<List<SectionSchema>> getComponentSchemas() {
        return ResponseEntity.ok(componentRegistry.getAllSections());
    }

    // =============== Mapping Methods ===============

    private PublicProductResponse mapToPublicProduct(Product product) {
        String categoryName = null;
        String categorySlug = null;
        if (product.getCategory() != null) {
            categoryName = product.getCategory().getName();
            categorySlug = product.getCategory().getSlug();
        }

        List<PublicProductResponse.VariantInfo> variants = new ArrayList<>();
        BigDecimal productPrice = null;

        if (product.getVariants() != null && !product.getVariants().isEmpty()) {
            // Get the first variant's price as the product price
            productPrice = product.getVariants().get(0).getPrice();

            for (ProductVariant v : product.getVariants()) {
                variants.add(PublicProductResponse.VariantInfo.builder()
                        .id(v.getId())
                        .name(v.getName())
                        .sku(v.getSku())
                        .price(v.getPrice())
                        .inStock(v.getStockLevel() > 0)
                        .quantity(v.getStockLevel())
                        .build());
            }
        }

        return PublicProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .slug(product.getSlug())
                .description(product.getDescription())
                .price(productPrice)
                .categoryName(categoryName)
                .categorySlug(categorySlug)
                .inStock(variants.stream().anyMatch(PublicProductResponse.VariantInfo::isInStock))
                .variants(variants)
                .build();
    }

    private PublicCategoryResponse mapToPublicCategory(Category category, int productCount) {
        return PublicCategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .slug(category.getSlug())
                .description(category.getDescription())
                .productCount(productCount)
                .build();
    }
}
