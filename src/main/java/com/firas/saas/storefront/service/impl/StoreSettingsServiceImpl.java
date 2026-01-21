package com.firas.saas.storefront.service.impl;

import com.firas.saas.common.exception.ResourceNotFoundException;
import com.firas.saas.storefront.dto.StoreSettingsResponse;
import com.firas.saas.storefront.dto.UpdateStoreSettingsRequest;
import com.firas.saas.storefront.entity.CheckoutMode;
import com.firas.saas.storefront.entity.StoreSettings;
import com.firas.saas.storefront.entity.Theme;
import com.firas.saas.storefront.repository.StoreSettingsRepository;
import com.firas.saas.storefront.repository.ThemeRepository;
import com.firas.saas.storefront.service.StoreSettingsService;
import com.firas.saas.tenant.entity.Tenant;
import com.firas.saas.tenant.repository.TenantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StoreSettingsServiceImpl implements StoreSettingsService {

    private final StoreSettingsRepository storeSettingsRepository;
    private final TenantRepository tenantRepository;
    private final ThemeRepository themeRepository;

    @Override
    @Transactional(readOnly = true)
    public StoreSettingsResponse getStoreSettings(Long tenantId) {
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant", tenantId));

        StoreSettings settings = storeSettingsRepository.findByTenantId(tenantId)
                .orElseGet(() -> createDefaultSettings(tenantId));

        return mapToResponse(settings, tenant);
    }

    @Override
    @Transactional(readOnly = true)
    public StoreSettingsResponse getStoreSettingsBySlug(String slug) {
        Tenant tenant = tenantRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Store", slug));

        StoreSettings settings = storeSettingsRepository.findByTenantId(tenant.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Store settings not found for: " + slug));

        // Only return if store is published (for public API)
        if (!settings.isPublished()) {
            throw new ResourceNotFoundException("Store", slug);
        }

        return mapToResponse(settings, tenant);
    }

    @Override
    @Transactional
    public StoreSettings getOrCreateStoreSettings(Long tenantId) {
        return storeSettingsRepository.findByTenantId(tenantId)
                .orElseGet(() -> createAndSaveDefaultSettings(tenantId));
    }

    @Override
    @Transactional
    public StoreSettingsResponse updateStoreSettings(Long tenantId, UpdateStoreSettingsRequest request) {
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant", tenantId));

        StoreSettings settings = getOrCreateStoreSettings(tenantId);

        // Update theme if provided
        if (request.getThemeId() != null) {
            Theme theme = themeRepository.findById(request.getThemeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Theme", request.getThemeId()));
            settings.setTheme(theme);
        }

        // Update other fields
        if (request.getCheckoutMode() != null) {
            settings.setCheckoutMode(request.getCheckoutMode());
        }
        if (request.getGlobalStyles() != null) {
            settings.setGlobalStyles(request.getGlobalStyles());
        }
        if (request.getSeoDefaults() != null) {
            settings.setSeoDefaults(request.getSeoDefaults());
        }
        if (request.getSocialLinks() != null) {
            settings.setSocialLinks(request.getSocialLinks());
        }
        if (request.getContactEmail() != null) {
            settings.setContactEmail(request.getContactEmail());
        }
        if (request.getCustomDomain() != null) {
            // Validate domain uniqueness
            if (!request.getCustomDomain().isEmpty()) {
                storeSettingsRepository.findByCustomDomain(request.getCustomDomain())
                        .ifPresent(existing -> {
                            if (!existing.getTenantId().equals(tenantId)) {
                                throw new IllegalArgumentException("Custom domain is already in use");
                            }
                        });
            }
            settings.setCustomDomain(request.getCustomDomain().isEmpty() ? null : request.getCustomDomain());
        }
        if (request.getAnnouncementText() != null) {
            settings.setAnnouncementText(request.getAnnouncementText());
        }
        if (request.getAnnouncementEnabled() != null) {
            settings.setAnnouncementEnabled(request.getAnnouncementEnabled());
        }
        if (request.getPublished() != null) {
            settings.setPublished(request.getPublished());
        }

        return mapToResponse(storeSettingsRepository.save(settings), tenant);
    }

    @Override
    @Transactional
    public StoreSettingsResponse publishStore(Long tenantId) {
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant", tenantId));

        StoreSettings settings = getOrCreateStoreSettings(tenantId);
        settings.setPublished(true);
        return mapToResponse(storeSettingsRepository.save(settings), tenant);
    }

    @Override
    @Transactional
    public StoreSettingsResponse unpublishStore(Long tenantId) {
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant", tenantId));

        StoreSettings settings = getOrCreateStoreSettings(tenantId);
        settings.setPublished(false);
        return mapToResponse(storeSettingsRepository.save(settings), tenant);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isStorePublished(Long tenantId) {
        return storeSettingsRepository.findByTenantId(tenantId)
                .map(StoreSettings::isPublished)
                .orElse(false);
    }

    @Override
    @Transactional(readOnly = true)
    public Long resolveTenantIdByDomain(String domain) {
        return storeSettingsRepository.findByCustomDomain(domain)
                .map(StoreSettings::getTenantId)
                .orElse(null);
    }

    private StoreSettings createDefaultSettings(Long tenantId) {
        StoreSettings settings = new StoreSettings();
        settings.setTenantId(tenantId);
        settings.setCheckoutMode(CheckoutMode.BOTH);
        settings.setPublished(false);
        settings.setAnnouncementEnabled(false);
        return settings;
    }

    private StoreSettings createAndSaveDefaultSettings(Long tenantId) {
        StoreSettings settings = createDefaultSettings(tenantId);
        return storeSettingsRepository.save(settings);
    }

    private StoreSettingsResponse mapToResponse(StoreSettings settings, Tenant tenant) {
        StoreSettingsResponse.ThemeSummary themeSummary = null;
        if (settings.getTheme() != null) {
            themeSummary = StoreSettingsResponse.ThemeSummary.builder()
                    .id(settings.getTheme().getId())
                    .name(settings.getTheme().getName())
                    .cssVariables(settings.getTheme().getCssVariables())
                    .build();
        }

        return StoreSettingsResponse.builder()
                .storeName(tenant.getName())
                .storeSlug(tenant.getSlug())
                .checkoutMode(settings.getCheckoutMode())
                .globalStyles(settings.getGlobalStyles())
                .seoDefaults(settings.getSeoDefaults())
                .socialLinks(settings.getSocialLinks())
                .contactEmail(settings.getContactEmail())
                .announcementText(settings.getAnnouncementText())
                .announcementEnabled(settings.isAnnouncementEnabled())
                .theme(themeSummary)
                .build();
    }
}
