package com.firas.saas.app.controller;

import com.firas.saas.app.dto.*;
import com.firas.saas.app.entity.AppScope;
import com.firas.saas.app.service.AppService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for App Store management.
 * ADMIN-only operations for app CRUD.
 * Merchants can view published apps.
 */
@RestController
@RequestMapping("/api/v1/apps")
@RequiredArgsConstructor
public class AppController {

    private final AppService appService;

    // ==================== ADMIN OPERATIONS ====================

    /**
     * Create a new app. Returns clientId and plaintext clientSecret (shown once).
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AppCreatedResponse> createApp(@Valid @RequestBody CreateAppRequest request) {
        return new ResponseEntity<>(appService.createApp(request), HttpStatus.CREATED);
    }

    /**
     * Get all apps. ADMIN sees all, others see only published.
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MERCHANT')")
    public ResponseEntity<List<AppResponse>> getAllApps(
            @RequestParam(defaultValue = "false") boolean includeNonPublished) {
        // Only ADMIN can see non-published apps
        return ResponseEntity.ok(appService.getAllApps(includeNonPublished));
    }

    /**
     * Get app by ID.
     */
    @GetMapping("/{appId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MERCHANT')")
    public ResponseEntity<AppResponse> getAppById(@PathVariable Long appId) {
        return ResponseEntity.ok(appService.getAppById(appId));
    }

    /**
     * Get app by client ID.
     */
    @GetMapping("/by-client-id/{clientId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MERCHANT')")
    public ResponseEntity<AppResponse> getAppByClientId(@PathVariable String clientId) {
        return ResponseEntity.ok(appService.getAppByClientId(clientId));
    }

    /**
     * Update an app.
     */
    @PutMapping("/{appId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AppResponse> updateApp(
            @PathVariable Long appId,
            @Valid @RequestBody UpdateAppRequest request) {
        return ResponseEntity.ok(appService.updateApp(appId, request));
    }

    /**
     * Publish an app (make available for installation).
     */
    @PatchMapping("/{appId}/publish")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AppResponse> publishApp(@PathVariable Long appId) {
        return ResponseEntity.ok(appService.publishApp(appId));
    }

    /**
     * Suspend an app.
     */
    @PatchMapping("/{appId}/suspend")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AppResponse> suspendApp(@PathVariable Long appId) {
        return ResponseEntity.ok(appService.suspendApp(appId));
    }

    /**
     * Unpublish an app (set to DRAFT).
     */
    @PatchMapping("/{appId}/unpublish")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AppResponse> unpublishApp(@PathVariable Long appId) {
        return ResponseEntity.ok(appService.unpublishApp(appId));
    }

    /**
     * Regenerate client secret. Returns new plaintext secret (shown once).
     */
    @PostMapping("/{appId}/regenerate-secret")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AppCreatedResponse> regenerateClientSecret(@PathVariable Long appId) {
        return ResponseEntity.ok(appService.regenerateClientSecret(appId));
    }

    /**
     * Delete an app. Only allowed if no active installations.
     */
    @DeleteMapping("/{appId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteApp(@PathVariable Long appId) {
        appService.deleteApp(appId);
        return ResponseEntity.noContent().build();
    }

    // ==================== PUBLIC INFO ====================

    /**
     * Get all available scopes.
     */
    @GetMapping("/scopes")
    public ResponseEntity<List<AppScope>> getAvailableScopes() {
        return ResponseEntity.ok(appService.getAvailableScopes());
    }
}
