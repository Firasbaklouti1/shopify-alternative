package com.firas.saas.storefront.controller;

import com.firas.saas.security.service.UserPrincipal;
import com.firas.saas.storefront.dto.*;
import com.firas.saas.storefront.entity.PageType;
import com.firas.saas.storefront.schema.ComponentRegistry;
import com.firas.saas.storefront.schema.SectionSchema;
import com.firas.saas.storefront.service.PageLayoutService;
import com.firas.saas.storefront.service.StoreSettingsService;
import com.firas.saas.storefront.service.ThemeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Layout Editor API - Authenticated, MERCHANT role required.
 * Used by the visual drag-and-drop editor in the admin dashboard.
 */
@RestController
@RequestMapping("/api/v1/stores")
@RequiredArgsConstructor
public class LayoutEditorController {

    private final PageLayoutService pageLayoutService;
    private final StoreSettingsService storeSettingsService;
    private final ThemeService themeService;
    private final ComponentRegistry componentRegistry;

    // =============== Store Settings ===============

    /**
     * Get current store settings
     */
    @GetMapping("/settings")
    @PreAuthorize("hasAnyRole('MERCHANT', 'STAFF')")
    public ResponseEntity<StoreSettingsResponse> getStoreSettings(
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(storeSettingsService.getStoreSettings(principal.getTenantId()));
    }

    /**
     * Update store settings
     */
    @PutMapping("/settings")
    @PreAuthorize("hasRole('MERCHANT')")
    public ResponseEntity<StoreSettingsResponse> updateStoreSettings(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody UpdateStoreSettingsRequest request) {
        return ResponseEntity.ok(storeSettingsService.updateStoreSettings(principal.getTenantId(), request));
    }

    /**
     * Publish store (make publicly accessible)
     */
    @PostMapping("/publish")
    @PreAuthorize("hasRole('MERCHANT')")
    public ResponseEntity<StoreSettingsResponse> publishStore(
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(storeSettingsService.publishStore(principal.getTenantId()));
    }

    /**
     * Unpublish store (make private)
     */
    @PostMapping("/unpublish")
    @PreAuthorize("hasRole('MERCHANT')")
    public ResponseEntity<StoreSettingsResponse> unpublishStore(
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(storeSettingsService.unpublishStore(principal.getTenantId()));
    }

    // =============== Page Layouts ===============

    /**
     * Get all page layouts for the store
     */
    @GetMapping("/layouts")
    @PreAuthorize("hasAnyRole('MERCHANT', 'STAFF')")
    public ResponseEntity<List<LayoutResponse>> getAllLayouts(
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(pageLayoutService.getAllLayouts(principal.getTenantId()));
    }

    /**
     * Get layout for a specific page type
     */
    @GetMapping("/layouts/{pageType}")
    @PreAuthorize("hasAnyRole('MERCHANT', 'STAFF')")
    public ResponseEntity<LayoutResponse> getLayout(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable String pageType) {
        PageType type = PageType.valueOf(pageType.toUpperCase());
        return ResponseEntity.ok(pageLayoutService.getLayout(principal.getTenantId(), type));
    }

    /**
     * Get draft layout JSON for editor preview
     */
    @GetMapping("/layouts/{pageType}/draft")
    @PreAuthorize("hasAnyRole('MERCHANT', 'STAFF')")
    public ResponseEntity<Map<String, Object>> getDraftLayout(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable String pageType) {
        PageType type = PageType.valueOf(pageType.toUpperCase());
        return ResponseEntity.ok(pageLayoutService.getDraftLayoutJson(principal.getTenantId(), type));
    }

    /**
     * Update layout (saves to draft)
     */
    @PutMapping("/layouts/{pageType}")
    @PreAuthorize("hasRole('MERCHANT')")
    public ResponseEntity<LayoutResponse> updateLayout(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable String pageType,
            @Valid @RequestBody UpdateLayoutRequest request) {
        PageType type = PageType.valueOf(pageType.toUpperCase());
        return ResponseEntity.ok(pageLayoutService.updateLayout(
                principal.getTenantId(), type, request, principal.getEmail()));
    }

    /**
     * Publish layout (draft → live)
     */
    @PostMapping("/layouts/{pageType}/publish")
    @PreAuthorize("hasRole('MERCHANT')")
    public ResponseEntity<LayoutResponse> publishLayout(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable String pageType) {
        PageType type = PageType.valueOf(pageType.toUpperCase());
        return ResponseEntity.ok(pageLayoutService.publishLayout(
                principal.getTenantId(), type, principal.getEmail()));
    }

    /**
     * Discard draft changes
     */
    @DeleteMapping("/layouts/{pageType}/draft")
    @PreAuthorize("hasRole('MERCHANT')")
    public ResponseEntity<LayoutResponse> discardDraft(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable String pageType) {
        PageType type = PageType.valueOf(pageType.toUpperCase());
        return ResponseEntity.ok(pageLayoutService.discardDraft(principal.getTenantId(), type));
    }

    /**
     * Rollback to a previous version
     */
    @PostMapping("/layouts/{pageType}/rollback/{version}")
    @PreAuthorize("hasRole('MERCHANT')")
    public ResponseEntity<LayoutResponse> rollbackLayout(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable String pageType,
            @PathVariable Integer version) {
        PageType type = PageType.valueOf(pageType.toUpperCase());
        return ResponseEntity.ok(pageLayoutService.rollbackToVersion(
                principal.getTenantId(), type, version, principal.getEmail()));
    }

    /**
     * Get version history for a page layout
     */
    @GetMapping("/layouts/{pageType}/versions")
    @PreAuthorize("hasAnyRole('MERCHANT', 'STAFF')")
    public ResponseEntity<List<Map<String, Object>>> getVersionHistory(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable String pageType) {
        PageType type = PageType.valueOf(pageType.toUpperCase());
        return ResponseEntity.ok(pageLayoutService.getVersionHistory(principal.getTenantId(), type));
    }

    // =============== Custom Pages ===============

    /**
     * Get custom page layout by handle
     */
    @GetMapping("/pages/{handle}")
    @PreAuthorize("hasAnyRole('MERCHANT', 'STAFF')")
    public ResponseEntity<LayoutResponse> getCustomPage(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable String handle) {
        return ResponseEntity.ok(pageLayoutService.getCustomPageLayout(principal.getTenantId(), handle));
    }

    /**
     * Create a new custom page
     */
    @PostMapping("/pages")
    @PreAuthorize("hasRole('MERCHANT')")
    public ResponseEntity<LayoutResponse> createCustomPage(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody CreatePageRequest request) {
        return ResponseEntity.ok(pageLayoutService.createCustomPage(principal.getTenantId(), request));
    }

    /**
     * Update custom page layout
     */
    @PutMapping("/pages/{handle}")
    @PreAuthorize("hasRole('MERCHANT')")
    public ResponseEntity<LayoutResponse> updateCustomPage(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable String handle,
            @Valid @RequestBody UpdateLayoutRequest request) {
        return ResponseEntity.ok(pageLayoutService.updateCustomPageLayout(
                principal.getTenantId(), handle, request, principal.getEmail()));
    }

    /**
     * Publish custom page
     */
    @PostMapping("/pages/{handle}/publish")
    @PreAuthorize("hasRole('MERCHANT')")
    public ResponseEntity<LayoutResponse> publishCustomPage(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable String handle) {
        return ResponseEntity.ok(pageLayoutService.publishCustomPageLayout(
                principal.getTenantId(), handle, principal.getEmail()));
    }

    /**
     * Delete custom page
     */
    @DeleteMapping("/pages/{handle}")
    @PreAuthorize("hasRole('MERCHANT')")
    public ResponseEntity<Void> deleteCustomPage(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable String handle) {
        pageLayoutService.deleteCustomPage(principal.getTenantId(), handle);
        return ResponseEntity.noContent().build();
    }

    // =============== Themes ===============

    /**
     * Get all available themes
     */
    @GetMapping("/themes")
    @PreAuthorize("hasAnyRole('MERCHANT', 'STAFF')")
    public ResponseEntity<List<ThemeResponse>> getThemes() {
        return ResponseEntity.ok(themeService.getActiveThemes());
    }

    /**
     * Get theme details
     */
    @GetMapping("/themes/{themeId}")
    @PreAuthorize("hasAnyRole('MERCHANT', 'STAFF')")
    public ResponseEntity<ThemeResponse> getTheme(@PathVariable Long themeId) {
        return ResponseEntity.ok(themeService.getThemeById(themeId));
    }

    /**
     * Initialize store with a theme (creates default layouts)
     */
    @PostMapping("/themes/{themeId}/apply")
    @PreAuthorize("hasRole('MERCHANT')")
    public ResponseEntity<StoreSettingsResponse> applyTheme(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long themeId) {
        // Initialize layouts with theme defaults
        pageLayoutService.initializeDefaultLayouts(principal.getTenantId(), themeId);

        // Update store settings with new theme
        UpdateStoreSettingsRequest request = UpdateStoreSettingsRequest.builder()
                .themeId(themeId)
                .build();
        return ResponseEntity.ok(storeSettingsService.updateStoreSettings(principal.getTenantId(), request));
    }

    // =============== Component Schema ===============

    /**
     * Get all available section components
     */
    @GetMapping("/schema/sections")
    @PreAuthorize("hasAnyRole('MERCHANT', 'STAFF')")
    public ResponseEntity<List<SectionSchema>> getSectionSchemas() {
        return ResponseEntity.ok(componentRegistry.getAllSections());
    }

    /**
     * Get sections available for a specific page type
     */
    @GetMapping("/schema/sections/{pageType}")
    @PreAuthorize("hasAnyRole('MERCHANT', 'STAFF')")
    public ResponseEntity<List<SectionSchema>> getSectionsForPageType(@PathVariable String pageType) {
        return ResponseEntity.ok(componentRegistry.getSectionsForPageType(pageType.toUpperCase()));
    }

    // =============== AI Generation Placeholder ===============

    /**
     * AI-powered layout generation (placeholder for future implementation)
     */
    @PostMapping("/layouts/generate")
    @PreAuthorize("hasRole('MERCHANT')")
    public ResponseEntity<Map<String, Object>> generateLayout(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody Map<String, Object> request) {
        // Placeholder - returns 501 Not Implemented
        // Future: Feed component schema + prompt to LLM, return generated layout JSON
        return ResponseEntity.status(501).body(Map.of(
                "error", "AI generation not yet implemented",
                "message", "This endpoint will accept { prompt: string, pageType: string } and return generated layout JSON"
        ));
    }
}
