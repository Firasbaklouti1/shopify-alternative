package com.firas.saas.app.service;

import com.firas.saas.app.dto.*;
import com.firas.saas.app.entity.*;
import com.firas.saas.app.repository.AppAccessTokenRepository;
import com.firas.saas.app.repository.AppInstallationRepository;
import com.firas.saas.app.repository.AppRepository;
import com.firas.saas.common.event.DomainEventPublisher;
import com.firas.saas.common.exception.ResourceNotFoundException;
import com.firas.saas.tenant.entity.Tenant;
import com.firas.saas.tenant.repository.TenantRepository;
import com.firas.saas.webhook.entity.Webhook;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AppInstallationServiceImpl implements AppInstallationService {

    private final AppRepository appRepository;
    private final AppInstallationRepository installationRepository;
    private final AppAccessTokenRepository tokenRepository;
    private final TenantRepository tenantRepository;
    private final DomainEventPublisher eventPublisher; // Observer pattern
    private final PasswordEncoder passwordEncoder;

    private static final int TOKEN_EXPIRY_DAYS = 30;

    @Override
    @Transactional
    public InstallationWithTokenResponse installApp(InstallAppRequest request, Long tenantId, Long userId) {
        // Find the app by clientId
        App app = appRepository.findByClientId(request.getClientId())
                .orElseThrow(() -> new ResourceNotFoundException("App", request.getClientId()));

        // Validate app status
        if (!app.isPublished()) {
            throw new IllegalStateException("App is not available for installation (status: " + app.getStatus() + ")");
        }

        // Validate client secret
        if (!passwordEncoder.matches(request.getClientSecret(), app.getClientSecretHash())) {
            throw new IllegalArgumentException("Invalid client credentials");
        }

        // Check if already installed
        if (installationRepository.existsByAppIdAndTenantIdAndStatus(app.getId(), tenantId, InstallationStatus.ACTIVE)) {
            throw new IllegalStateException("App is already installed for this store");
        }

        // Validate requested scopes are subset of declared scopes
        Set<AppScope> grantedScopes = request.getGrantedScopes();
        if (!app.getDeclaredScopes().containsAll(grantedScopes)) {
            throw new IllegalArgumentException("Requested scopes exceed app's declared scopes");
        }

        // Create installation
        AppInstallation installation = AppInstallation.builder()
                .app(app)
                .grantedScopes(grantedScopes)
                .status(InstallationStatus.ACTIVE)
                .installedByUserId(userId)
                .build();
        installation.setTenantId(tenantId);

        AppInstallation savedInstallation = installationRepository.save(installation);
        log.info("Installed app {} for tenant {} by user {}", app.getName(), tenantId, userId);

        // Generate access token
        AppAccessToken token = createAccessToken(savedInstallation);
        AppAccessToken savedToken = tokenRepository.save(token);

        // Trigger APP_INSTALLED webhook
        triggerAppInstalledWebhook(app, savedInstallation, tenantId);

        return InstallationWithTokenResponse.builder()
                .installation(AppInstallationResponse.fromEntity(savedInstallation))
                .accessToken(savedToken.getTokenValue())
                .tokenExpiresAt(savedToken.getExpiresAt())
                .build();
    }

    @Override
    @Transactional
    public void uninstallApp(Long installationId, Long tenantId) {
        AppInstallation installation = installationRepository.findByIdAndTenantId(installationId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("AppInstallation", installationId));

        if (installation.getStatus() == InstallationStatus.REVOKED) {
            throw new IllegalStateException("App is already uninstalled");
        }

        App app = installation.getApp();

        // Revoke all tokens
        tokenRepository.revokeAllByInstallationId(installationId);
        log.info("Revoked all tokens for installation {}", installationId);

        // Update installation status
        installation.setStatus(InstallationStatus.REVOKED);
        installationRepository.save(installation);
        log.info("Uninstalled app {} from tenant {}", app.getName(), tenantId);

        // Trigger APP_UNINSTALLED webhook
        triggerAppUninstalledWebhook(app, installation, tenantId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppInstallationResponse> getInstalledApps(Long tenantId) {
        return installationRepository.findAllByTenantId(tenantId).stream()
                .map(AppInstallationResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppInstallationResponse> getActiveInstalledApps(Long tenantId) {
        return installationRepository.findAllByTenantIdAndStatus(tenantId, InstallationStatus.ACTIVE).stream()
                .map(AppInstallationResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public AppInstallationResponse getInstallationById(Long installationId, Long tenantId) {
        return installationRepository.findByIdAndTenantId(installationId, tenantId)
                .map(AppInstallationResponse::fromEntity)
                .orElseThrow(() -> new ResourceNotFoundException("AppInstallation", installationId));
    }

    @Override
    @Transactional
    public RotateTokenResponse rotateToken(Long installationId, Long tenantId) {
        AppInstallation installation = installationRepository.findByIdAndTenantId(installationId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("AppInstallation", installationId));

        if (!installation.isActive()) {
            throw new IllegalStateException("Cannot rotate token for inactive installation");
        }

        // Revoke old tokens
        tokenRepository.revokeAllByInstallationId(installationId);

        // Generate new token
        AppAccessToken newToken = createAccessToken(installation);
        AppAccessToken savedToken = tokenRepository.save(newToken);

        log.info("Rotated token for installation {} in tenant {}", installationId, tenantId);

        return RotateTokenResponse.builder()
                .accessToken(savedToken.getTokenValue())
                .expiresAt(savedToken.getExpiresAt())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AccessTokenResponse> getTokensForInstallation(Long installationId, Long tenantId) {
        AppInstallation installation = installationRepository.findByIdAndTenantId(installationId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("AppInstallation", installationId));

        return tokenRepository.findAllByInstallationId(installationId).stream()
                .map(AccessTokenResponse::fromEntity)
                .collect(Collectors.toList());
    }

    // ==================== PRIVATE HELPERS ====================

    private AppAccessToken createAccessToken(AppInstallation installation) {
        String tokenValue = UUID.randomUUID().toString();

        AppAccessToken token = AppAccessToken.builder()
                .tokenValue(tokenValue)
                .installation(installation)
                .scopes(new HashSet<>(installation.getGrantedScopes()))
                .expiresAt(LocalDateTime.now().plusDays(TOKEN_EXPIRY_DAYS))
                .revoked(false)
                .build();
        token.setTenantId(installation.getTenantId());

        return token;
    }

    private void triggerAppInstalledWebhook(App app, AppInstallation installation, Long tenantId) {
        try {
            Tenant tenant = tenantRepository.findById(tenantId).orElse(null);
            String tenantSlug = tenant != null ? tenant.getSlug() : "unknown";

            Map<String, Object> payload = new HashMap<>();
            payload.put("appId", app.getId());
            payload.put("appName", app.getName());
            payload.put("clientId", app.getClientId());
            payload.put("installationId", installation.getId());
            payload.put("grantedScopes", installation.getGrantedScopes().stream()
                    .map(Enum::name)
                    .collect(Collectors.toList()));
            payload.put("installedAt", installation.getCreatedAt());
            payload.put("tenantId", tenantId);

            eventPublisher.publish(Webhook.WebhookEvent.APP_INSTALLED, payload, tenantId, tenantSlug);
            log.debug("Published APP_INSTALLED event for app {} in tenant {}", app.getName(), tenantId);
        } catch (Exception e) {
            log.error("Failed to publish APP_INSTALLED event: {}", e.getMessage());
            // Don't fail the installation if event publishing fails
        }
    }

    private void triggerAppUninstalledWebhook(App app, AppInstallation installation, Long tenantId) {
        try {
            Tenant tenant = tenantRepository.findById(tenantId).orElse(null);
            String tenantSlug = tenant != null ? tenant.getSlug() : "unknown";

            Map<String, Object> payload = new HashMap<>();
            payload.put("appId", app.getId());
            payload.put("appName", app.getName());
            payload.put("clientId", app.getClientId());
            payload.put("installationId", installation.getId());
            payload.put("uninstalledAt", LocalDateTime.now());
            payload.put("tenantId", tenantId);

            eventPublisher.publish(Webhook.WebhookEvent.APP_UNINSTALLED, payload, tenantId, tenantSlug);
            log.debug("Published APP_UNINSTALLED event for app {} in tenant {}", app.getName(), tenantId);
        } catch (Exception e) {
            log.error("Failed to publish APP_UNINSTALLED event: {}", e.getMessage());
            // Don't fail the uninstallation if event publishing fails
        }
    }
}
