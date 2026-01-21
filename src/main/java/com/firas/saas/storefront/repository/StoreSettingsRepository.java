package com.firas.saas.storefront.repository;

import com.firas.saas.storefront.entity.StoreSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StoreSettingsRepository extends JpaRepository<StoreSettings, Long> {

    /**
     * Find store settings by tenant ID
     */
    Optional<StoreSettings> findByTenantId(Long tenantId);

    /**
     * Check if store settings exist for tenant
     */
    boolean existsByTenantId(Long tenantId);

    /**
     * Find store settings by custom domain
     */
    Optional<StoreSettings> findByCustomDomain(String customDomain);

    /**
     * Check if custom domain is already in use
     */
    boolean existsByCustomDomain(String customDomain);
}
