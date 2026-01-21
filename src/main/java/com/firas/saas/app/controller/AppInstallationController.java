package com.firas.saas.app.controller;

import com.firas.saas.app.dto.*;
import com.firas.saas.app.service.AppInstallationService;
import com.firas.saas.security.service.UserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for app installations (per-tenant).
 * MERCHANT operations for installing/uninstalling apps.
 */
@RestController
@RequestMapping("/api/v1/app-installations")
@RequiredArgsConstructor
public class AppInstallationController {

    private final AppInstallationService installationService;

    /**
     * Install an app for the current tenant.
     * Validates credentials, creates installation, generates access token.
     */
    @PostMapping("/install")
    @PreAuthorize("hasRole('MERCHANT')")
    public ResponseEntity<InstallationWithTokenResponse> installApp(
            @Valid @RequestBody InstallAppRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        return new ResponseEntity<>(
                installationService.installApp(request, principal.getTenantId(), principal.getId()),
                HttpStatus.CREATED);
    }

    /**
     * Uninstall an app. Revokes all tokens and stops webhook delivery.
     */
    @DeleteMapping("/{installationId}")
    @PreAuthorize("hasRole('MERCHANT')")
    public ResponseEntity<Void> uninstallApp(
            @PathVariable Long installationId,
            @AuthenticationPrincipal UserPrincipal principal) {
        installationService.uninstallApp(installationId, principal.getTenantId());
        return ResponseEntity.noContent().build();
    }

    /**
     * Get all installed apps for the current tenant.
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('MERCHANT', 'STAFF')")
    public ResponseEntity<List<AppInstallationResponse>> getInstalledApps(
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(installationService.getInstalledApps(principal.getTenantId()));
    }

    /**
     * Get all active installed apps for the current tenant.
     */
    @GetMapping("/active")
    @PreAuthorize("hasAnyRole('MERCHANT', 'STAFF')")
    public ResponseEntity<List<AppInstallationResponse>> getActiveInstalledApps(
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(installationService.getActiveInstalledApps(principal.getTenantId()));
    }

    /**
     * Get installation by ID.
     */
    @GetMapping("/{installationId}")
    @PreAuthorize("hasAnyRole('MERCHANT', 'STAFF')")
    public ResponseEntity<AppInstallationResponse> getInstallationById(
            @PathVariable Long installationId,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(installationService.getInstallationById(installationId, principal.getTenantId()));
    }

    /**
     * Rotate access token for an installation.
     * Revokes old tokens and generates a new one.
     */
    @PostMapping("/{installationId}/rotate-token")
    @PreAuthorize("hasRole('MERCHANT')")
    public ResponseEntity<RotateTokenResponse> rotateToken(
            @PathVariable Long installationId,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(installationService.rotateToken(installationId, principal.getTenantId()));
    }

    /**
     * Get token info for an installation (without actual token values).
     */
    @GetMapping("/{installationId}/tokens")
    @PreAuthorize("hasRole('MERCHANT')")
    public ResponseEntity<List<AccessTokenResponse>> getTokensForInstallation(
            @PathVariable Long installationId,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(installationService.getTokensForInstallation(installationId, principal.getTenantId()));
    }
}
