package com.firas.saas.billing.controller;

import com.firas.saas.billing.entity.Invoice;
import com.firas.saas.billing.repository.InvoiceRepository;
import com.firas.saas.security.service.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/invoices")
@RequiredArgsConstructor
public class InvoiceController {

    private final InvoiceRepository invoiceRepository;

    @GetMapping
    @PreAuthorize("hasRole('MERCHANT')")
    public ResponseEntity<List<Invoice>> getMyInvoices(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(invoiceRepository.findByTenantId(principal.getTenantId()));
    }
}
