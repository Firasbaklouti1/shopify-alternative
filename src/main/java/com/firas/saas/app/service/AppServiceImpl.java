package com.firas.saas.app.service;

import com.firas.saas.app.dto.*;
import com.firas.saas.app.entity.*;
import com.firas.saas.app.repository.AppInstallationRepository;
import com.firas.saas.app.repository.AppRepository;
import com.firas.saas.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AppServiceImpl implements AppService {

    private final AppRepository appRepository;
    private final AppInstallationRepository installationRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public AppCreatedResponse createApp(CreateAppRequest request) {
        // Validate unique name
        if (appRepository.existsByName(request.getName())) {
            throw new IllegalArgumentException("An app with this name already exists");
        }

        // Generate client ID and secret
        String clientId = "app_" + UUID.randomUUID().toString().replace("-", "");
        String clientSecret = "secret_" + UUID.randomUUID().toString().replace("-", "");
        String clientSecretHash = passwordEncoder.encode(clientSecret);

        App app = App.builder()
                .name(request.getName())
                .description(request.getDescription())
                .developerName(request.getDeveloperName())
                .clientId(clientId)
                .clientSecretHash(clientSecretHash)
                .webhookUrl(request.getWebhookUrl())
                .declaredScopes(request.getDeclaredScopes())
                .status(AppStatus.DRAFT)
                .build();

        App savedApp = appRepository.save(app);
        log.info("Created new app: {} (clientId: {})", savedApp.getName(), clientId);

        return AppCreatedResponse.builder()
                .app(AppResponse.fromEntity(savedApp))
                .clientSecret(clientSecret)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppResponse> getAllApps(boolean includeNonPublished) {
        List<App> apps;
        if (includeNonPublished) {
            apps = appRepository.findAll();
        } else {
            apps = appRepository.findAllByStatus(AppStatus.PUBLISHED);
        }
        return apps.stream()
                .map(AppResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public AppResponse getAppById(Long appId) {
        return appRepository.findById(appId)
                .map(AppResponse::fromEntity)
                .orElseThrow(() -> new ResourceNotFoundException("App", appId));
    }

    @Override
    @Transactional(readOnly = true)
    public AppResponse getAppByClientId(String clientId) {
        return appRepository.findByClientId(clientId)
                .map(AppResponse::fromEntity)
                .orElseThrow(() -> new ResourceNotFoundException("App", clientId));
    }

    @Override
    @Transactional
    public AppResponse updateApp(Long appId, UpdateAppRequest request) {
        App app = appRepository.findById(appId)
                .orElseThrow(() -> new ResourceNotFoundException("App", appId));

        if (request.getName() != null) {
            // Check uniqueness if name is changing
            if (!request.getName().equals(app.getName()) && appRepository.existsByName(request.getName())) {
                throw new IllegalArgumentException("An app with this name already exists");
            }
            app.setName(request.getName());
        }
        if (request.getDescription() != null) {
            app.setDescription(request.getDescription());
        }
        if (request.getDeveloperName() != null) {
            app.setDeveloperName(request.getDeveloperName());
        }
        if (request.getWebhookUrl() != null) {
            app.setWebhookUrl(request.getWebhookUrl());
        }
        if (request.getDeclaredScopes() != null && !request.getDeclaredScopes().isEmpty()) {
            app.setDeclaredScopes(request.getDeclaredScopes());
        }

        App savedApp = appRepository.save(app);
        log.info("Updated app: {} (ID: {})", savedApp.getName(), appId);

        return AppResponse.fromEntity(savedApp);
    }

    @Override
    @Transactional
    public AppResponse publishApp(Long appId) {
        App app = appRepository.findById(appId)
                .orElseThrow(() -> new ResourceNotFoundException("App", appId));

        if (app.getStatus() == AppStatus.PUBLISHED) {
            throw new IllegalStateException("App is already published");
        }

        app.setStatus(AppStatus.PUBLISHED);
        App savedApp = appRepository.save(app);
        log.info("Published app: {} (ID: {})", savedApp.getName(), appId);

        return AppResponse.fromEntity(savedApp);
    }

    @Override
    @Transactional
    public AppResponse suspendApp(Long appId) {
        App app = appRepository.findById(appId)
                .orElseThrow(() -> new ResourceNotFoundException("App", appId));

        app.setStatus(AppStatus.SUSPENDED);
        App savedApp = appRepository.save(app);
        log.info("Suspended app: {} (ID: {})", savedApp.getName(), appId);

        return AppResponse.fromEntity(savedApp);
    }

    @Override
    @Transactional
    public AppResponse unpublishApp(Long appId) {
        App app = appRepository.findById(appId)
                .orElseThrow(() -> new ResourceNotFoundException("App", appId));

        app.setStatus(AppStatus.DRAFT);
        App savedApp = appRepository.save(app);
        log.info("Unpublished app: {} (ID: {})", savedApp.getName(), appId);

        return AppResponse.fromEntity(savedApp);
    }

    @Override
    @Transactional
    public AppCreatedResponse regenerateClientSecret(Long appId) {
        App app = appRepository.findById(appId)
                .orElseThrow(() -> new ResourceNotFoundException("App", appId));

        String newSecret = "secret_" + UUID.randomUUID().toString().replace("-", "");
        app.setClientSecretHash(passwordEncoder.encode(newSecret));

        App savedApp = appRepository.save(app);
        log.info("Regenerated client secret for app: {} (ID: {})", savedApp.getName(), appId);

        return AppCreatedResponse.builder()
                .app(AppResponse.fromEntity(savedApp))
                .clientSecret(newSecret)
                .warning("Save the new clientSecret now. The old secret is no longer valid.")
                .build();
    }

    @Override
    @Transactional
    public void deleteApp(Long appId) {
        App app = appRepository.findById(appId)
                .orElseThrow(() -> new ResourceNotFoundException("App", appId));

        // Check for active installations
        List<AppInstallation> activeInstallations = installationRepository.findAllByAppId(appId)
                .stream()
                .filter(i -> i.getStatus() == InstallationStatus.ACTIVE)
                .toList();

        if (!activeInstallations.isEmpty()) {
            throw new IllegalStateException(
                    "Cannot delete app with active installations. Uninstall from all tenants first.");
        }

        appRepository.delete(app);
        log.info("Deleted app: {} (ID: {})", app.getName(), appId);
    }

    @Override
    public List<AppScope> getAvailableScopes() {
        return Arrays.asList(AppScope.values());
    }
}
