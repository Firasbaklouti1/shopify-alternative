package com.firas.saas.product.repository;

import com.firas.saas.common.base.BaseRepository;
import com.firas.saas.product.entity.ProductVariant;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductVariantRepository extends BaseRepository<ProductVariant> {
    Optional<ProductVariant> findBySkuAndTenantId(String sku, Long tenantId);
    boolean existsBySkuAndTenantId(String sku, Long tenantId);
}
