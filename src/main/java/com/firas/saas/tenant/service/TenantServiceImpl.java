package com.firas.saas.tenant.service;

import com.firas.saas.tenant.dto.TenantCreateRequest;
import com.firas.saas.tenant.dto.TenantResponse;
import com.firas.saas.tenant.entity.Tenant;
import com.firas.saas.tenant.exception.TenantNotFoundException;
import com.firas.saas.tenant.repository.TenantRepository;
import com.firas.saas.security.dto.MerchantSignupRequest;
import com.firas.saas.user.dto.UserCreateRequest;
import com.firas.saas.user.entity.Role;
import com.firas.saas.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TenantServiceImpl implements TenantService {

    private final TenantRepository tenantRepository;
    private final UserService userService;

    @Override
    @Transactional
    public TenantResponse createTenant(TenantCreateRequest request) {
        if (tenantRepository.existsBySlug(request.getSlug())) {
            throw new RuntimeException("Tenant with slug " + request.getSlug() + " already exists");
        }
        if (tenantRepository.existsByName(request.getName())) {
            throw new RuntimeException("Tenant with name " + request.getName() + " already exists");
        }

        Tenant tenant = Tenant.builder()
                .name(request.getName())
                .slug(request.getSlug())
                .ownerEmail(request.getOwnerEmail())
                .active(true)
                .build();

        Tenant savedTenant = tenantRepository.save(tenant);
        return mapToResponse(savedTenant);
    }

    @Override
    @Transactional(readOnly = true)
    public TenantResponse getTenantBySlug(String slug) {
        return tenantRepository.findBySlug(slug)
                .map(this::mapToResponse)
                .orElseThrow(() -> new TenantNotFoundException("Tenant not found with slug: " + slug));
    }

    @Override
    @Transactional(readOnly = true)
    public List<TenantResponse> getAllTenants() {
        return tenantRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public TenantResponse registerMerchant(MerchantSignupRequest request) {
        if (tenantRepository.existsBySlug(request.getStoreSlug())) {
            throw new RuntimeException("Store with slug '" + request.getStoreSlug() + "' already exists");
        }
        if (tenantRepository.existsByName(request.getStoreName())) {
            throw new RuntimeException("Store with name '" + request.getStoreName() + "' already exists");
        }

        // 1. Create Tenant
        Tenant tenant = Tenant.builder()
                .name(request.getStoreName())
                .slug(request.getStoreSlug())
                .ownerEmail(request.getEmail())
                .active(true)
                .build();
        
        Tenant savedTenant = tenantRepository.save(tenant);

        // 2. Create Merchant User
        UserCreateRequest userRequest = new UserCreateRequest();
        userRequest.setEmail(request.getEmail());
        userRequest.setPassword(request.getPassword());
        userRequest.setFullName(request.getFullName());
        userRequest.setRole(Role.MERCHANT);
        userRequest.setTenantId(savedTenant.getId());

        userService.createUser(userRequest);

        return mapToResponse(savedTenant);
    }

    private TenantResponse mapToResponse(Tenant tenant) {
        return TenantResponse.builder()
                .id(tenant.getId())
                .name(tenant.getName())
                .slug(tenant.getSlug())
                .ownerEmail(tenant.getOwnerEmail())
                .active(tenant.isActive())
                .createdAt(tenant.getCreatedAt())
                .build();
    }
}
