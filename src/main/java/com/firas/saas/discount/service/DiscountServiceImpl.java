package com.firas.saas.discount.service;

import com.firas.saas.common.exception.ResourceNotFoundException;
import com.firas.saas.discount.dto.*;
import com.firas.saas.discount.entity.Discount;
import com.firas.saas.discount.entity.DiscountUsage;
import com.firas.saas.discount.repository.DiscountRepository;
import com.firas.saas.discount.repository.DiscountUsageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DiscountServiceImpl implements DiscountService {

    private final DiscountRepository discountRepository;
    private final DiscountUsageRepository usageRepository;

    @Override
    @Transactional
    public DiscountResponse createDiscount(DiscountRequest request, Long tenantId) {
        String code = request.getCode().toUpperCase();

        if (discountRepository.existsByCodeAndTenantId(code, tenantId)) {
            throw new RuntimeException("Discount code already exists");
        }

        // Validate percentage is between 0-100
        if (request.getType() == Discount.DiscountType.PERCENTAGE &&
            (request.getValue().compareTo(BigDecimal.ZERO) <= 0 || request.getValue().compareTo(BigDecimal.valueOf(100)) > 0)) {
            throw new RuntimeException("Percentage must be between 0 and 100");
        }

        Discount discount = Discount.builder()
                .code(code)
                .description(request.getDescription())
                .type(request.getType())
                .value(request.getValue())
                .minOrderAmount(request.getMinOrderAmount())
                .maxDiscountAmount(request.getMaxDiscountAmount())
                .usageLimit(request.getUsageLimit())
                .usageLimitPerCustomer(request.getUsageLimitPerCustomer())
                .startsAt(request.getStartsAt())
                .expiresAt(request.getExpiresAt())
                .active(request.isActive())
                .timesUsed(0)
                .build();
        discount.setTenantId(tenantId);

        return mapToResponse(discountRepository.save(discount));
    }

    @Override
    @Transactional(readOnly = true)
    public List<DiscountResponse> getAllDiscounts(Long tenantId) {
        return discountRepository.findAllByTenantId(tenantId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<DiscountResponse> getActiveDiscounts(Long tenantId) {
        return discountRepository.findAllByTenantIdAndActiveTrue(tenantId).stream()
                .filter(Discount::isValid)
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public DiscountResponse getDiscountById(Long id, Long tenantId) {
        return discountRepository.findByIdAndTenantId(id, tenantId)
                .map(this::mapToResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Discount", id));
    }

    @Override
    @Transactional(readOnly = true)
    public DiscountResponse getDiscountByCode(String code, Long tenantId) {
        return discountRepository.findByCodeAndTenantId(code.toUpperCase(), tenantId)
                .map(this::mapToResponse)
                .orElseThrow(() -> new RuntimeException("Discount not found"));
    }

    @Override
    @Transactional
    public DiscountResponse updateDiscount(Long id, DiscountRequest request, Long tenantId) {
        Discount discount = discountRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new RuntimeException("Discount not found"));

        String newCode = request.getCode().toUpperCase();
        if (!discount.getCode().equals(newCode) && discountRepository.existsByCodeAndTenantId(newCode, tenantId)) {
            throw new RuntimeException("Discount code already exists");
        }

        discount.setCode(newCode);
        discount.setDescription(request.getDescription());
        discount.setType(request.getType());
        discount.setValue(request.getValue());
        discount.setMinOrderAmount(request.getMinOrderAmount());
        discount.setMaxDiscountAmount(request.getMaxDiscountAmount());
        discount.setUsageLimit(request.getUsageLimit());
        discount.setUsageLimitPerCustomer(request.getUsageLimitPerCustomer());
        discount.setStartsAt(request.getStartsAt());
        discount.setExpiresAt(request.getExpiresAt());
        discount.setActive(request.isActive());

        return mapToResponse(discountRepository.save(discount));
    }

    @Override
    @Transactional
    public void deleteDiscount(Long id, Long tenantId) {
        Discount discount = discountRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new RuntimeException("Discount not found"));
        discountRepository.delete(discount);
    }

    @Override
    @Transactional(readOnly = true)
    public ApplyDiscountResponse validateDiscount(ApplyDiscountRequest request, Long tenantId) {
        return checkDiscount(request, tenantId, false, null);
    }

    @Override
    @Transactional
    public ApplyDiscountResponse applyDiscount(ApplyDiscountRequest request, Long orderId, Long tenantId) {
        ApplyDiscountResponse response = checkDiscount(request, tenantId, true, orderId);

        if (response.isApplicable()) {
            Discount discount = discountRepository.findByCodeAndTenantId(request.getCode().toUpperCase(), tenantId)
                    .orElseThrow();

            // Record usage
            DiscountUsage usage = DiscountUsage.builder()
                    .discount(discount)
                    .customerEmail(request.getCustomerEmail())
                    .orderId(orderId)
                    .usedAt(LocalDateTime.now())
                    .build();
            usage.setTenantId(tenantId);
            usageRepository.save(usage);

            // Increment usage count
            discount.setTimesUsed(discount.getTimesUsed() + 1);
            discountRepository.save(discount);
        }

        return response;
    }

    @Override
    @Transactional
    public DiscountResponse activateDiscount(Long id, Long tenantId) {
        Discount discount = discountRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new RuntimeException("Discount not found"));
        discount.setActive(true);
        return mapToResponse(discountRepository.save(discount));
    }

    @Override
    @Transactional
    public DiscountResponse deactivateDiscount(Long id, Long tenantId) {
        Discount discount = discountRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new RuntimeException("Discount not found"));
        discount.setActive(false);
        return mapToResponse(discountRepository.save(discount));
    }

    // ==================== HELPER METHODS ====================

    private ApplyDiscountResponse checkDiscount(ApplyDiscountRequest request, Long tenantId,
                                                 boolean checkCustomerLimit, Long orderId) {
        String code = request.getCode().toUpperCase();

        Discount discount = discountRepository.findByCodeAndTenantId(code, tenantId).orElse(null);

        if (discount == null) {
            return ApplyDiscountResponse.builder()
                    .applicable(false)
                    .code(code)
                    .message("Invalid discount code")
                    .originalTotal(request.getOrderTotal())
                    .discountAmount(BigDecimal.ZERO)
                    .finalTotal(request.getOrderTotal())
                    .build();
        }

        if (!discount.isValid()) {
            String reason = "Discount is not valid";
            if (!discount.isActive()) reason = "Discount is inactive";
            else if (discount.getExpiresAt() != null && LocalDateTime.now().isAfter(discount.getExpiresAt()))
                reason = "Discount has expired";
            else if (discount.getStartsAt() != null && LocalDateTime.now().isBefore(discount.getStartsAt()))
                reason = "Discount is not yet active";
            else if (discount.getUsageLimit() != null && discount.getTimesUsed() >= discount.getUsageLimit())
                reason = "Discount usage limit reached";

            return ApplyDiscountResponse.builder()
                    .applicable(false)
                    .code(code)
                    .message(reason)
                    .originalTotal(request.getOrderTotal())
                    .discountAmount(BigDecimal.ZERO)
                    .finalTotal(request.getOrderTotal())
                    .build();
        }

        // Check minimum order amount
        if (discount.getMinOrderAmount() != null &&
            request.getOrderTotal().compareTo(discount.getMinOrderAmount()) < 0) {
            return ApplyDiscountResponse.builder()
                    .applicable(false)
                    .code(code)
                    .message("Order total must be at least " + discount.getMinOrderAmount())
                    .originalTotal(request.getOrderTotal())
                    .discountAmount(BigDecimal.ZERO)
                    .finalTotal(request.getOrderTotal())
                    .build();
        }

        // Check per-customer usage limit
        if (checkCustomerLimit && request.getCustomerEmail() != null &&
            discount.getUsageLimitPerCustomer() != null) {
            int customerUsage = usageRepository.countByDiscountIdAndCustomerEmailAndTenantId(
                    discount.getId(), request.getCustomerEmail(), tenantId);
            if (customerUsage >= discount.getUsageLimitPerCustomer()) {
                return ApplyDiscountResponse.builder()
                        .applicable(false)
                        .code(code)
                        .message("You have already used this discount the maximum number of times")
                        .originalTotal(request.getOrderTotal())
                        .discountAmount(BigDecimal.ZERO)
                        .finalTotal(request.getOrderTotal())
                        .build();
            }
        }

        BigDecimal discountAmount = discount.calculateDiscount(request.getOrderTotal());
        BigDecimal finalTotal = request.getOrderTotal().subtract(discountAmount);

        return ApplyDiscountResponse.builder()
                .applicable(true)
                .code(code)
                .message("Discount applied successfully")
                .originalTotal(request.getOrderTotal())
                .discountAmount(discountAmount)
                .finalTotal(finalTotal)
                .build();
    }

    private DiscountResponse mapToResponse(Discount discount) {
        return DiscountResponse.builder()
                .id(discount.getId())
                .code(discount.getCode())
                .description(discount.getDescription())
                .type(discount.getType())
                .value(discount.getValue())
                .minOrderAmount(discount.getMinOrderAmount())
                .maxDiscountAmount(discount.getMaxDiscountAmount())
                .usageLimit(discount.getUsageLimit())
                .usageLimitPerCustomer(discount.getUsageLimitPerCustomer())
                .timesUsed(discount.getTimesUsed())
                .startsAt(discount.getStartsAt())
                .expiresAt(discount.getExpiresAt())
                .active(discount.isActive())
                .valid(discount.isValid())
                .createdAt(discount.getCreatedAt())
                .build();
    }
}

