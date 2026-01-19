package com.firas.saas.customer.controller;

import com.firas.saas.customer.dto.CustomerRequest;
import com.firas.saas.customer.dto.CustomerResponse;
import com.firas.saas.customer.service.CustomerService;
import com.firas.saas.security.service.UserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    @PostMapping
    @PreAuthorize("hasRole('MERCHANT')")
    public ResponseEntity<CustomerResponse> createCustomer(
            @Valid @RequestBody CustomerRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        return new ResponseEntity<>(customerService.createCustomer(request, principal.getTenantId()), HttpStatus.CREATED);
    }

    @GetMapping("/{email}")
    @PreAuthorize("hasRole('MERCHANT')")
    public ResponseEntity<CustomerResponse> getCustomer(
            @PathVariable String email,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(customerService.getCustomerByEmail(email, principal.getTenantId()));
    }

    @GetMapping
    @PreAuthorize("hasRole('MERCHANT')")
    public ResponseEntity<List<CustomerResponse>> getAllCustomers(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(customerService.getAllCustomers(principal.getTenantId()));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('MERCHANT')")
    public ResponseEntity<CustomerResponse> updateCustomer(
            @PathVariable Long id,
            @Valid @RequestBody CustomerRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(customerService.updateCustomer(id, request, principal.getTenantId()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('MERCHANT')")
    public ResponseEntity<Void> deleteCustomer(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        customerService.deleteCustomer(id, principal.getTenantId());
        return ResponseEntity.noContent().build();
    }
}
