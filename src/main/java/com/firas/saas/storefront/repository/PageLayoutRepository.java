package com.firas.saas.storefront.repository;

import com.firas.saas.storefront.entity.PageLayout;
import com.firas.saas.storefront.entity.PageType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PageLayoutRepository extends JpaRepository<PageLayout, Long> {

    /**
     * Find all page layouts for a tenant
     */
    List<PageLayout> findByTenantId(Long tenantId);

    /**
     * Find all published page layouts for a tenant (used by storefront)
     */
    List<PageLayout> findByTenantIdAndPublishedTrue(Long tenantId);

    /**
     * Find specific page layout by tenant and page type (for standard pages)
     */
    Optional<PageLayout> findFirstByTenantIdAndPageType(Long tenantId, PageType pageType);

    /**
     * Find custom page by tenant and handle
     */
    Optional<PageLayout> findByTenantIdAndPageTypeAndHandle(Long tenantId, PageType pageType, String handle);

    /**
     * Find all pages of a specific type for a tenant (useful for listing all custom pages)
     */
    List<PageLayout> findAllByTenantIdAndPageType(Long tenantId, PageType pageType);

    /**
     * Check if page layout exists for tenant and page type
     */
    boolean existsByTenantIdAndPageType(Long tenantId, PageType pageType);

    /**
     * Check if custom page handle exists for tenant
     */
    boolean existsByTenantIdAndPageTypeAndHandle(Long tenantId, PageType pageType, String handle);
}
