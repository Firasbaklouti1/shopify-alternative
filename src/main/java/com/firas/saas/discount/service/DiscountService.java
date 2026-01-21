package com.firas.saas.discount.service;

import com.firas.saas.discount.dto.*;

import java.util.List;

public interface DiscountService {

    // CRUD Operations
    DiscountResponse createDiscount(DiscountRequest request, Long tenantId);
    List<DiscountResponse> getAllDiscounts(Long tenantId);
    List<DiscountResponse> getActiveDiscounts(Long tenantId);
    DiscountResponse getDiscountById(Long id, Long tenantId);
    DiscountResponse getDiscountByCode(String code, Long tenantId);
    DiscountResponse updateDiscount(Long id, DiscountRequest request, Long tenantId);
    void deleteDiscount(Long id, Long tenantId);

    // Discount Application
    ApplyDiscountResponse validateDiscount(ApplyDiscountRequest request, Long tenantId);
    ApplyDiscountResponse applyDiscount(ApplyDiscountRequest request, Long orderId, Long tenantId);

    // Activation/Deactivation
    DiscountResponse activateDiscount(Long id, Long tenantId);
    DiscountResponse deactivateDiscount(Long id, Long tenantId);
}

