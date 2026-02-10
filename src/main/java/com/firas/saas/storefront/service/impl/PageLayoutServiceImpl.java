package com.firas.saas.storefront.service.impl;

import com.firas.saas.common.exception.ResourceNotFoundException;
import com.firas.saas.storefront.dto.CreatePageRequest;
import com.firas.saas.storefront.dto.LayoutResponse;
import com.firas.saas.storefront.dto.UpdateLayoutRequest;
import com.firas.saas.storefront.entity.PageLayout;
import com.firas.saas.storefront.entity.PageLayoutVersion;
import com.firas.saas.storefront.entity.PageType;
import com.firas.saas.storefront.entity.Theme;
import com.firas.saas.storefront.repository.PageLayoutRepository;
import com.firas.saas.storefront.repository.PageLayoutVersionRepository;
import com.firas.saas.storefront.repository.ThemeRepository;
import com.firas.saas.storefront.service.PageLayoutService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PageLayoutServiceImpl implements PageLayoutService {

    private final PageLayoutRepository pageLayoutRepository;
    private final PageLayoutVersionRepository versionRepository;
    private final ThemeRepository themeRepository;

    private static final int MAX_VERSIONS_TO_KEEP = 10;

    @Override
    @Transactional(readOnly = true)
    public List<LayoutResponse> getAllLayouts(Long tenantId) {
        return pageLayoutRepository.findByTenantId(tenantId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public LayoutResponse getLayout(Long tenantId, PageType pageType) {
        PageLayout layout = pageLayoutRepository.findFirstByTenantIdAndPageType(tenantId, pageType)
                .orElseThrow(() -> new ResourceNotFoundException("Page layout for " + pageType.name()));
        return mapToResponse(layout);
    }

    @Override
    @Transactional(readOnly = true)
    public LayoutResponse getCustomPageLayout(Long tenantId, String handle) {
        PageLayout layout = pageLayoutRepository.findByTenantIdAndPageTypeAndHandle(tenantId, PageType.CUSTOM, handle)
                .orElseThrow(() -> new ResourceNotFoundException("Custom page", handle));
        return mapToResponse(layout);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getPublishedLayoutJson(Long tenantId, PageType pageType) {
        PageLayout layout = pageLayoutRepository.findFirstByTenantIdAndPageType(tenantId, pageType)
                .orElseThrow(() -> new ResourceNotFoundException("Page layout for " + pageType.name()));

        if (!layout.isPublished()) {
            throw new ResourceNotFoundException("Page layout for " + pageType.name() + " is not published");
        }

        return layout.getLayoutJson();
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getPublishedCustomPageLayoutJson(Long tenantId, String handle) {
        PageLayout layout = pageLayoutRepository.findByTenantIdAndPageTypeAndHandle(tenantId, PageType.CUSTOM, handle)
                .orElseThrow(() -> new ResourceNotFoundException("Custom page", handle));

        if (!layout.isPublished()) {
            throw new ResourceNotFoundException("Custom page " + handle + " is not published");
        }

        return layout.getLayoutJson();
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getDraftLayoutJson(Long tenantId, PageType pageType) {
        PageLayout layout = pageLayoutRepository.findFirstByTenantIdAndPageType(tenantId, pageType)
                .orElseThrow(() -> new ResourceNotFoundException("Page layout for " + pageType.name()));

        // Return draft if exists, otherwise return published layout
        return layout.getDraftJson() != null ? layout.getDraftJson() : layout.getLayoutJson();
    }

    @Override
    @Transactional
    public LayoutResponse createCustomPage(Long tenantId, CreatePageRequest request) {
        if (request.getPageType() != PageType.CUSTOM) {
            throw new IllegalArgumentException("This endpoint is for creating custom pages only");
        }

        if (request.getHandle() == null || request.getHandle().isBlank()) {
            throw new IllegalArgumentException("Handle is required for custom pages");
        }

        // Check if handle already exists
        if (pageLayoutRepository.existsByTenantIdAndPageTypeAndHandle(tenantId, PageType.CUSTOM, request.getHandle())) {
            throw new IllegalArgumentException("Custom page with handle '" + request.getHandle() + "' already exists");
        }

        Map<String, Object> layoutJson = request.getLayoutJson();
        if (layoutJson == null) {
            layoutJson = createDefaultCustomPageLayout();
        }

        PageLayout layout = new PageLayout();
        layout.setTenantId(tenantId);
        layout.setPageType(PageType.CUSTOM);
        layout.setHandle(request.getHandle());
        layout.setName(request.getName());
        layout.setLayoutJson(layoutJson);
        layout.setDraftJson(layoutJson);
        layout.setPublished(false);
        layout.setSeoTitle(request.getSeoTitle());
        layout.setSeoDescription(request.getSeoDescription());

        return mapToResponse(pageLayoutRepository.save(layout));
    }

    @Override
    @Transactional
    public LayoutResponse updateLayout(Long tenantId, PageType pageType, UpdateLayoutRequest request, String userEmail) {
        PageLayout layout = pageLayoutRepository.findFirstByTenantIdAndPageType(tenantId, pageType)
                .orElseThrow(() -> new ResourceNotFoundException("Page layout for " + pageType.name()));

        return updateLayoutInternal(layout, request);
    }

    @Override
    @Transactional
    public LayoutResponse updateCustomPageLayout(Long tenantId, String handle, UpdateLayoutRequest request, String userEmail) {
        PageLayout layout = pageLayoutRepository.findByTenantIdAndPageTypeAndHandle(tenantId, PageType.CUSTOM, handle)
                .orElseThrow(() -> new ResourceNotFoundException("Custom page", handle));

        return updateLayoutInternal(layout, request);
    }

    private LayoutResponse updateLayoutInternal(PageLayout layout, UpdateLayoutRequest request) {
        // Update draft (not live layout yet)
        layout.setDraftJson(request.getLayoutJson());

        if (request.getName() != null) {
            layout.setName(request.getName());
        }
        if (request.getSeoTitle() != null) {
            layout.setSeoTitle(request.getSeoTitle());
        }
        if (request.getSeoDescription() != null) {
            layout.setSeoDescription(request.getSeoDescription());
        }

        return mapToResponse(pageLayoutRepository.save(layout));
    }

    @Override
    @Transactional
    public LayoutResponse publishLayout(Long tenantId, PageType pageType, String userEmail) {
        PageLayout layout = pageLayoutRepository.findFirstByTenantIdAndPageType(tenantId, pageType)
                .orElseThrow(() -> new ResourceNotFoundException("Page layout for " + pageType.name()));

        return publishLayoutInternal(layout, userEmail);
    }

    @Override
    @Transactional
    public LayoutResponse publishCustomPageLayout(Long tenantId, String handle, String userEmail) {
        PageLayout layout = pageLayoutRepository.findByTenantIdAndPageTypeAndHandle(tenantId, PageType.CUSTOM, handle)
                .orElseThrow(() -> new ResourceNotFoundException("Custom page", handle));

        return publishLayoutInternal(layout, userEmail);
    }

    private LayoutResponse publishLayoutInternal(PageLayout layout, String userEmail) {
        // Publish draft to live
        Map<String, Object> draftToPublish = layout.getDraftJson();
        if (draftToPublish == null) {
            throw new IllegalStateException("No draft to publish");
        }

        layout.setLayoutJson(draftToPublish);
        layout.setDraftJson(null); // Clear draft after publishing
        layout.setPublished(true);

        PageLayout savedLayout = pageLayoutRepository.save(layout);

        // Create version snapshot AFTER publishing (so version 1 = first published layout)
        createVersionSnapshot(savedLayout, userEmail);

        return mapToResponse(savedLayout);
    }

    @Override
    @Transactional
    public LayoutResponse discardDraft(Long tenantId, PageType pageType) {
        PageLayout layout = pageLayoutRepository.findFirstByTenantIdAndPageType(tenantId, pageType)
                .orElseThrow(() -> new ResourceNotFoundException("Page layout for " + pageType.name()));

        layout.setDraftJson(null);
        return mapToResponse(pageLayoutRepository.save(layout));
    }

    @Override
    @Transactional
    public void deleteCustomPage(Long tenantId, String handle) {
        PageLayout layout = pageLayoutRepository.findByTenantIdAndPageTypeAndHandle(tenantId, PageType.CUSTOM, handle)
                .orElseThrow(() -> new ResourceNotFoundException("Custom page", handle));

        // Delete all versions first
        List<PageLayoutVersion> versions = versionRepository.findByPageLayoutOrderByVersionNumberDesc(layout);
        versionRepository.deleteAll(versions);

        pageLayoutRepository.delete(layout);
    }

    @Override
    @Transactional
    public void initializeDefaultLayouts(Long tenantId, Long themeId) {
        Map<String, Object> defaultLayouts = null;

        if (themeId != null) {
            Theme theme = themeRepository.findById(themeId).orElse(null);
            if (theme != null) {
                defaultLayouts = theme.getDefaultLayoutsJson();
            }
        }

        // Create default layouts for standard page types
        for (PageType pageType : List.of(PageType.HOME, PageType.PRODUCT, PageType.COLLECTION, PageType.CART, PageType.CHECKOUT)) {
            if (!pageLayoutRepository.existsByTenantIdAndPageType(tenantId, pageType)) {
                Map<String, Object> layoutJson = getDefaultLayoutForPageType(pageType, defaultLayouts);

                PageLayout layout = new PageLayout();
                layout.setTenantId(tenantId);
                layout.setPageType(pageType);
                layout.setName(pageType.name().charAt(0) + pageType.name().substring(1).toLowerCase() + " Page");
                layout.setLayoutJson(layoutJson);
                layout.setDraftJson(layoutJson);
                layout.setPublished(false);

                pageLayoutRepository.save(layout);
            }
        }

        log.info("Initialized default layouts for tenant {}", tenantId);
    }

    @Override
    @Transactional
    public LayoutResponse rollbackToVersion(Long tenantId, PageType pageType, Integer versionNumber, String userEmail) {
        PageLayout layout = pageLayoutRepository.findFirstByTenantIdAndPageType(tenantId, pageType)
                .orElseThrow(() -> new ResourceNotFoundException("Page layout for " + pageType.name()));

        PageLayoutVersion version = versionRepository.findByPageLayoutAndVersionNumber(layout, versionNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Version " + versionNumber + " not found"));

        // Set the old version as the new draft
        layout.setDraftJson(version.getLayoutSnapshot());

        return mapToResponse(pageLayoutRepository.save(layout));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getVersionHistory(Long tenantId, PageType pageType) {
        PageLayout layout = pageLayoutRepository.findFirstByTenantIdAndPageType(tenantId, pageType)
                .orElseThrow(() -> new ResourceNotFoundException("Page layout for " + pageType.name()));

        List<PageLayoutVersion> versions = versionRepository.findByPageLayoutOrderByVersionNumberDesc(layout);

        return versions.stream()
                .map(v -> {
                    Map<String, Object> versionInfo = new LinkedHashMap<>();
                    versionInfo.put("versionNumber", v.getVersionNumber());
                    versionInfo.put("changedBy", v.getChangedBy());
                    versionInfo.put("changeDescription", v.getChangeDescription());
                    versionInfo.put("createdAt", v.getCreatedAt());
                    return versionInfo;
                })
                .toList();
    }

    private void createVersionSnapshot(PageLayout layout, String userEmail) {
        Integer nextVersion = versionRepository.findMaxVersionNumber(layout).orElse(0) + 1;

        PageLayoutVersion version = new PageLayoutVersion();
        version.setTenantId(layout.getTenantId());
        version.setPageLayout(layout);
        version.setVersionNumber(nextVersion);
        version.setLayoutSnapshot(layout.getLayoutJson());
        version.setChangedBy(userEmail);
        version.setChangeDescription("Published version " + nextVersion);

        versionRepository.save(version);

        // Cleanup old versions (keep only MAX_VERSIONS_TO_KEEP)
        long count = versionRepository.countByPageLayout(layout);
        if (count > MAX_VERSIONS_TO_KEEP) {
            List<PageLayoutVersion> allVersions = versionRepository.findByPageLayoutOrderByVersionNumberDesc(layout);
            List<PageLayoutVersion> toDelete = allVersions.subList(MAX_VERSIONS_TO_KEEP, allVersions.size());
            versionRepository.deleteAll(toDelete);
        }
    }

    private Map<String, Object> createDefaultCustomPageLayout() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("content", List.of(
                Map.of(
                        "type", "RichText",
                        "props", Map.of(
                                "id", "RichText-1",
                                "heading", "Page Title",
                                "content", "Add your content here...",
                                "text_alignment", "center",
                                "max_width", "medium",
                                "padding", "medium"
                        )
                )
        ));
        result.put("root", Map.of("props", Map.of("title", "Custom Page")));
        result.put("zones", Map.of());
        return result;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> getDefaultLayoutForPageType(PageType pageType, Map<String, Object> themeDefaults) {
        // Try to get from theme defaults first
        if (themeDefaults != null && themeDefaults.containsKey(pageType.name())) {
            return (Map<String, Object>) themeDefaults.get(pageType.name());
        }

        // Fallback to hardcoded defaults
        return switch (pageType) {
            case HOME -> createDefaultHomeLayout();
            case PRODUCT -> createDefaultProductLayout();
            case COLLECTION -> createDefaultCollectionLayout();
            case CART -> createDefaultCartLayout();
            case CHECKOUT -> createDefaultCheckoutLayout();
            default -> createDefaultCustomPageLayout();
        };
    }

    private Map<String, Object> createDefaultHomeLayout() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("content", List.of(
                Map.of(
                        "type", "HeroBanner",
                        "props", Map.of(
                                "id", "HeroBanner-1",
                                "title", "Welcome to Our Store",
                                "subtitle", "Discover amazing products",
                                "bg_image", "",
                                "cta_text", "Shop Now",
                                "cta_link", "/collections/all",
                                "overlay_opacity", 0.4,
                                "text_color", "light",
                                "text_alignment", "center",
                                "height", "large"
                        )
                ),
                Map.of(
                        "type", "ProductGrid",
                        "props", Map.of(
                                "id", "ProductGrid-1",
                                "title", "Featured Products",
                                "collection_handle", "",
                                "limit", 8,
                                "columns", 4,
                                "show_price", true,
                                "show_vendor", false,
                                "show_sale_badge", true,
                                "image_ratio", "square"
                        )
                ),
                Map.of(
                        "type", "Newsletter",
                        "props", Map.of(
                                "id", "Newsletter-1",
                                "title", "Subscribe to our newsletter",
                                "subtitle", "Get the latest updates and offers",
                                "button_text", "Subscribe",
                                "text_alignment", "center",
                                "layout", "inline"
                        )
                )
        ));
        result.put("root", Map.of("props", Map.of("title", "Home Page")));
        result.put("zones", Map.of());
        return result;
    }

    private Map<String, Object> createDefaultProductLayout() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("content", List.of(
                Map.of(
                        "type", "ProductMain",
                        "props", Map.of(
                                "id", "ProductMain-1",
                                "gallery_position", "left",
                                "show_vendor", true,
                                "show_sku", false,
                                "show_quantity_selector", true
                        )
                ),
                Map.of(
                        "type", "ProductGrid",
                        "props", Map.of(
                                "id", "ProductGrid-related",
                                "title", "You May Also Like",
                                "limit", 4,
                                "columns", 4,
                                "show_price", true,
                                "show_sale_badge", true,
                                "image_ratio", "square"
                        )
                )
        ));
        result.put("root", Map.of("props", Map.of("title", "Product Page")));
        result.put("zones", Map.of());
        return result;
    }

    private Map<String, Object> createDefaultCollectionLayout() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("content", List.of(
                Map.of(
                        "type", "CollectionFilters",
                        "props", Map.of(
                                "id", "CollectionFilters-1",
                                "show_sort", true,
                                "show_filter", false,
                                "filter_type", "dropdown"
                        )
                ),
                Map.of(
                        "type", "ProductGrid",
                        "props", Map.of(
                                "id", "ProductGrid-collection",
                                "limit", 24,
                                "columns", 4,
                                "show_price", true,
                                "show_sale_badge", true,
                                "image_ratio", "square"
                        )
                )
        ));
        result.put("root", Map.of("props", Map.of("title", "Collection Page")));
        result.put("zones", Map.of());
        return result;
    }

    private Map<String, Object> createDefaultCartLayout() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("content", List.of(
                Map.of(
                        "type", "RichText",
                        "props", Map.of(
                                "id", "RichText-cart",
                                "heading", "Your Cart",
                                "text_alignment", "center",
                                "padding", "medium"
                        )
                )
        ));
        result.put("root", Map.of("props", Map.of("title", "Cart")));
        result.put("zones", Map.of());
        return result;
    }

    private Map<String, Object> createDefaultCheckoutLayout() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("content", List.of(
                Map.of(
                        "type", "RichText",
                        "props", Map.of(
                                "id", "RichText-checkout",
                                "heading", "Checkout",
                                "text_alignment", "center",
                                "padding", "medium"
                        )
                )
        ));
        result.put("root", Map.of("props", Map.of("title", "Checkout")));
        result.put("zones", Map.of());
        return result;
    }

    private LayoutResponse mapToResponse(PageLayout layout) {
        return LayoutResponse.builder()
                .id(layout.getId())
                .pageType(layout.getPageType())
                .handle(layout.getHandle())
                .name(layout.getName())
                .layoutJson(layout.getDraftJson() != null ? layout.getDraftJson() : layout.getLayoutJson())
                .published(layout.isPublished())
                .version(layout.getVersion())
                .seoTitle(layout.getSeoTitle())
                .seoDescription(layout.getSeoDescription())
                .updatedAt(layout.getUpdatedAt())
                .hasDraft(layout.getDraftJson() != null)
                .build();
    }
}
