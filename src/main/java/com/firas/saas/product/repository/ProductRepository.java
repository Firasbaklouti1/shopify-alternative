package com.firas.saas.product.repository;

import com.firas.saas.common.base.BaseRepository;
import com.firas.saas.product.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends BaseRepository<Product> {
    List<Product> findAllByTenantId(Long tenantId);
    List<Product> findByCategoryIdAndTenantId(Long categoryId, Long tenantId);
    Optional<Product> findBySlugAndTenantId(String slug, Long tenantId);
    boolean existsBySlugAndTenantId(String slug, Long tenantId);

    // For storefront API - find products by category slug
    List<Product> findByCategorySlugAndTenantId(String categorySlug, Long tenantId);

    // Count products in a category
    int countByCategoryIdAndTenantId(Long categoryId, Long tenantId);

    // ============ Paginated queries for Storefront API ============

    /**
     * Get all products for a tenant with proper database pagination
     */
    Page<Product> findByTenantId(Long tenantId, Pageable pageable);

    /**
     * Get products by category slug with proper database pagination
     */
    Page<Product> findByCategorySlugAndTenantId(String categorySlug, Long tenantId, Pageable pageable);

    /**
     * Get product counts grouped by category for a tenant (avoids N+1 selects)
     * Returns List of Object[] where [0]=categoryId, [1]=count
     */
    @Query("SELECT p.category.id, COUNT(p) FROM Product p WHERE p.tenantId = :tenantId GROUP BY p.category.id")
    List<Object[]> countProductsByCategory(@Param("tenantId") Long tenantId);
}
