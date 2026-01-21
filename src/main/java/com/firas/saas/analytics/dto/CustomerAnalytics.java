package com.firas.saas.analytics.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerAnalytics {

    // Overview
    private Long totalCustomers;
    private Long newCustomersThisPeriod;
    private Long returningCustomers;
    private Double retentionRate;

    // Customer Value
    private BigDecimal averageCustomerValue;
    private BigDecimal customerLifetimeValue;

    // Top Customers
    private List<TopCustomer> topCustomers;

    // Customer Acquisition
    private List<CustomerGrowth> customerGrowth;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopCustomer {
        private Long customerId;
        private String email;
        private String name;
        private Long totalOrders;
        private BigDecimal totalSpent;
        private String lastOrderDate;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CustomerGrowth {
        private String period;
        private Long newCustomers;
        private Long totalCustomers;
    }
}

