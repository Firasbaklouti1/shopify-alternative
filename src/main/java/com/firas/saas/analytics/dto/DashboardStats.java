package com.firas.saas.analytics.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStats {

    // Overview Stats
    private BigDecimal totalRevenue;
    private Long totalOrders;
    private Long totalCustomers;
    private Long totalProducts;

    // Period Comparison
    private BigDecimal revenueChange; // Percentage change from previous period
    private Long ordersChange;
    private Long customersChange;

    // Top Products
    private List<TopProduct> topProducts;

    // Recent Orders
    private List<RecentOrder> recentOrders;

    // Revenue Chart Data
    private List<DailyRevenue> revenueChart;

    // Order Status Distribution
    private Map<String, Long> ordersByStatus;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopProduct {
        private Long productId;
        private String productName;
        private Long totalSold;
        private BigDecimal totalRevenue;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecentOrder {
        private Long orderId;
        private String orderNumber;
        private String customerEmail;
        private BigDecimal totalPrice;
        private String status;
        private String createdAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DailyRevenue {
        private LocalDate date;
        private BigDecimal revenue;
        private Long orders;
    }
}

