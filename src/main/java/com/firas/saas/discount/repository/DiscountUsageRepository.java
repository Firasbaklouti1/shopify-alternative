package com.firas.saas.discount.repository;

import com.firas.saas.common.base.BaseRepository;
import com.firas.saas.discount.entity.DiscountUsage;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DiscountUsageRepository extends BaseRepository<DiscountUsage> {

    List<DiscountUsage> findAllByDiscountIdAndTenantId(Long discountId, Long tenantId);

    List<DiscountUsage> findAllByCustomerEmailAndTenantId(String customerEmail, Long tenantId);

    int countByDiscountIdAndCustomerEmailAndTenantId(Long discountId, String customerEmail, Long tenantId);

    int countByDiscountIdAndTenantId(Long discountId, Long tenantId);
}

