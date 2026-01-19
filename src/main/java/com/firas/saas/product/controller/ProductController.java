package com.firas.saas.product.controller;

import com.firas.saas.product.dto.CategoryRequest;
import com.firas.saas.product.dto.CategoryResponse;
import com.firas.saas.product.dto.ProductRequest;
import com.firas.saas.product.dto.ProductResponse;
import com.firas.saas.product.service.ProductService;
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
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @PostMapping("/categories")
    @PreAuthorize("hasRole('MERCHANT')")
    public ResponseEntity<CategoryResponse> createCategory(
            @Valid @RequestBody CategoryRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        return new ResponseEntity<>(productService.createCategory(request, principal.getTenantId()), HttpStatus.CREATED);
    }

    @GetMapping("/categories")
    public ResponseEntity<List<CategoryResponse>> getAllCategories(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(productService.getAllCategories(principal.getTenantId()));
    }

    @PostMapping
    @PreAuthorize("hasRole('MERCHANT')")
    public ResponseEntity<ProductResponse> createProduct(
            @Valid @RequestBody ProductRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        return new ResponseEntity<>(productService.createProduct(request, principal.getTenantId()), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('MERCHANT')")
    public ResponseEntity<ProductResponse> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(productService.updateProduct(id, request, principal.getTenantId()));
    }

    @GetMapping("/{slug}")
    public ResponseEntity<ProductResponse> getProduct(
            @PathVariable String slug,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(productService.getProductBySlug(slug, principal.getTenantId()));
    }

    @GetMapping
    public ResponseEntity<List<ProductResponse>> getAllProducts(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(productService.getAllProducts(principal.getTenantId()));
    }

    @GetMapping("/id/{id}")
    public ResponseEntity<ProductResponse> getProductById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(productService.getProductById(id, principal.getTenantId()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('MERCHANT')")
    public ResponseEntity<Void> deleteProduct(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        productService.deleteProduct(id, principal.getTenantId());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/categories/{id}")
    @PreAuthorize("hasRole('MERCHANT')")
    public ResponseEntity<Void> deleteCategory(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        productService.deleteCategory(id, principal.getTenantId());
        return ResponseEntity.noContent().build();
    }
}
