package com.firas.saas.analytics.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SalesReport {

    private String periodStart;
    private String periodEnd;

    // Summary
    private BigDecimal totalRevenue;
    private Long totalOrders;
    private BigDecimal averageOrderValue;
    private Long totalItemsSold;

    // Breakdown by Category
    private List<CategorySales> salesByCategory;

    // Breakdown by Product
    private List<ProductSales> salesByProduct;

    // Daily/Weekly/Monthly breakdown
    private List<PeriodSales> salesByPeriod;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategorySales {
        private Long categoryId;
        private String categoryName;
        private Long itemsSold;
        private BigDecimal revenue;
        private Double percentage; // Percentage of total
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductSales {
        private Long productId;
        private String productName;
        private String sku;
        private Long quantitySold;
        private BigDecimal revenue;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PeriodSales {
        private String period; // Date or week/month label
        private Long orders;
        private BigDecimal revenue;
    }
}

