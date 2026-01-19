package com.firas.saas.tenant.service;

import com.firas.saas.tenant.dto.TenantCreateRequest;
import com.firas.saas.tenant.dto.TenantResponse;
import com.firas.saas.security.dto.MerchantSignupRequest;

import java.util.List;

public interface TenantService {
    TenantResponse createTenant(TenantCreateRequest request);
    TenantResponse getTenantBySlug(String slug);
    List<TenantResponse> getAllTenants();
    TenantResponse registerMerchant(MerchantSignupRequest request);
}
