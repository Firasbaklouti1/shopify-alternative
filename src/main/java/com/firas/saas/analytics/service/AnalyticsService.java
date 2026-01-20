package com.firas.saas.analytics.service;

import com.firas.saas.analytics.dto.CustomerAnalytics;
import com.firas.saas.analytics.dto.DashboardStats;
import com.firas.saas.analytics.dto.SalesReport;

import java.time.LocalDate;

public interface AnalyticsService {

    /**
     * Get dashboard overview stats
     */
    DashboardStats getDashboardStats(Long tenantId);

    /**
     * Get dashboard stats for a specific date range
     */
    DashboardStats getDashboardStats(Long tenantId, LocalDate startDate, LocalDate endDate);

    /**
     * Get sales report for date range
     */
    SalesReport getSalesReport(Long tenantId, LocalDate startDate, LocalDate endDate);

    /**
     * Get customer analytics
     */
    CustomerAnalytics getCustomerAnalytics(Long tenantId);

    /**
     * Get customer analytics for date range
     */
    CustomerAnalytics getCustomerAnalytics(Long tenantId, LocalDate startDate, LocalDate endDate);
}

