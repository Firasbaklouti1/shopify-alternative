package com.firas.saas.tenant.controller;

import com.firas.saas.tenant.dto.TenantCreateRequest;
import com.firas.saas.tenant.dto.TenantResponse;
import com.firas.saas.tenant.service.TenantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tenants")
@RequiredArgsConstructor
public class TenantController {

    private final TenantService tenantService;

    @PostMapping
    public ResponseEntity<TenantResponse> createTenant(@Valid @RequestBody TenantCreateRequest request) {
        return new ResponseEntity<>(tenantService.createTenant(request), HttpStatus.CREATED);
    }

    @GetMapping("/{slug}")
    public ResponseEntity<TenantResponse> getTenant(@PathVariable String slug) {
        return ResponseEntity.ok(tenantService.getTenantBySlug(slug));
    }

    @GetMapping
    public ResponseEntity<List<TenantResponse>> getAllTenants() {
        return ResponseEntity.ok(tenantService.getAllTenants());
    }
}
