package com.firas.saas.analytics.controller;

import com.firas.saas.analytics.dto.CustomerAnalytics;
import com.firas.saas.analytics.dto.DashboardStats;
import com.firas.saas.analytics.dto.SalesReport;
import com.firas.saas.analytics.service.AnalyticsService;
import com.firas.saas.security.service.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/analytics")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('MERCHANT', 'STAFF')")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    /**
     * Get dashboard overview stats (default: last 30 days)
     */
    @GetMapping("/dashboard")
    public ResponseEntity<DashboardStats> getDashboardStats(
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(analyticsService.getDashboardStats(principal.getTenantId()));
    }

    /**
     * Get dashboard stats for specific date range
     */
    @GetMapping("/dashboard/range")
    public ResponseEntity<DashboardStats> getDashboardStatsForRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(analyticsService.getDashboardStats(principal.getTenantId(), startDate, endDate));
    }

    /**
     * Get sales report for date range
     */
    @GetMapping("/sales")
    public ResponseEntity<SalesReport> getSalesReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(analyticsService.getSalesReport(principal.getTenantId(), startDate, endDate));
    }

    /**
     * Get customer analytics (default: last 30 days)
     */
    @GetMapping("/customers")
    public ResponseEntity<CustomerAnalytics> getCustomerAnalytics(
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(analyticsService.getCustomerAnalytics(principal.getTenantId()));
    }

    /**
     * Get customer analytics for date range
     */
    @GetMapping("/customers/range")
    public ResponseEntity<CustomerAnalytics> getCustomerAnalyticsForRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(analyticsService.getCustomerAnalytics(principal.getTenantId(), startDate, endDate));
    }
}
