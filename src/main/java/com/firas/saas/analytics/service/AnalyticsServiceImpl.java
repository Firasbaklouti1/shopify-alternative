package com.firas.saas.analytics.service;

import com.firas.saas.analytics.dto.CustomerAnalytics;
import com.firas.saas.analytics.dto.DashboardStats;
import com.firas.saas.analytics.dto.SalesReport;
import com.firas.saas.customer.repository.CustomerRepository;
import com.firas.saas.order.entity.Order;
import com.firas.saas.order.entity.OrderStatus;
import com.firas.saas.order.repository.OrderRepository;
import com.firas.saas.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnalyticsServiceImpl implements AnalyticsService {

    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;

    @Override
    @Transactional(readOnly = true)
    public DashboardStats getDashboardStats(Long tenantId) {
        // Default to last 30 days
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(30);
        return getDashboardStats(tenantId, startDate, endDate);
    }

    @Override
    @Transactional(readOnly = true)
    public DashboardStats getDashboardStats(Long tenantId, LocalDate startDate, LocalDate endDate) {
        List<Order> orders = orderRepository.findAllByTenantId(tenantId);

        // Filter orders by date range
        List<Order> periodOrders = orders.stream()
                .filter(o -> {
                    LocalDate orderDate = o.getCreatedAt().toLocalDate();
                    return !orderDate.isBefore(startDate) && !orderDate.isAfter(endDate);
                })
                .collect(Collectors.toList());

        // Calculate total revenue (only from completed/paid orders)
        BigDecimal totalRevenue = periodOrders.stream()
                .filter(o -> o.getStatus() != OrderStatus.CANCELLED)
                .map(Order::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Total orders
        long totalOrders = periodOrders.size();

        // Total customers
        long totalCustomers = customerRepository.findAllByTenantId(tenantId).size();

        // Total products
        long totalProducts = productRepository.findAllByTenantId(tenantId).size();

        // Calculate previous period for comparison
        long daysDiff = java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate);
        LocalDate prevStartDate = startDate.minusDays(daysDiff);
        LocalDate prevEndDate = startDate.minusDays(1);

        List<Order> prevPeriodOrders = orders.stream()
                .filter(o -> {
                    LocalDate orderDate = o.getCreatedAt().toLocalDate();
                    return !orderDate.isBefore(prevStartDate) && !orderDate.isAfter(prevEndDate);
                })
                .collect(Collectors.toList());

        BigDecimal prevRevenue = prevPeriodOrders.stream()
                .filter(o -> o.getStatus() != OrderStatus.CANCELLED)
                .map(Order::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal revenueChange = BigDecimal.ZERO;
        if (prevRevenue.compareTo(BigDecimal.ZERO) > 0) {
            revenueChange = totalRevenue.subtract(prevRevenue)
                    .divide(prevRevenue, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
        }

        // Order status distribution
        Map<String, Long> ordersByStatus = periodOrders.stream()
                .collect(Collectors.groupingBy(
                        o -> o.getStatus().name(),
                        Collectors.counting()
                ));

        // Recent orders (last 5)
        List<DashboardStats.RecentOrder> recentOrders = periodOrders.stream()
                .sorted(Comparator.comparing(Order::getCreatedAt).reversed())
                .limit(5)
                .map(o -> DashboardStats.RecentOrder.builder()
                        .orderId(o.getId())
                        .orderNumber(o.getOrderNumber())
                        .customerEmail(o.getCustomerEmail())
                        .totalPrice(o.getTotalPrice())
                        .status(o.getStatus().name())
                        .createdAt(o.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                        .build())
                .collect(Collectors.toList());

        // Daily revenue chart
        List<DashboardStats.DailyRevenue> revenueChart = generateDailyRevenue(periodOrders, startDate, endDate);

        return DashboardStats.builder()
                .totalRevenue(totalRevenue)
                .totalOrders(totalOrders)
                .totalCustomers(totalCustomers)
                .totalProducts(totalProducts)
                .revenueChange(revenueChange)
                .ordersChange((long) periodOrders.size() - prevPeriodOrders.size())
                .ordersByStatus(ordersByStatus)
                .recentOrders(recentOrders)
                .revenueChart(revenueChart)
                .topProducts(new ArrayList<>()) // Would require OrderItem analysis
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public SalesReport getSalesReport(Long tenantId, LocalDate startDate, LocalDate endDate) {
        List<Order> orders = orderRepository.findAllByTenantId(tenantId).stream()
                .filter(o -> {
                    LocalDate orderDate = o.getCreatedAt().toLocalDate();
                    return !orderDate.isBefore(startDate) && !orderDate.isAfter(endDate);
                })
                .filter(o -> o.getStatus() != OrderStatus.CANCELLED)
                .collect(Collectors.toList());

        BigDecimal totalRevenue = orders.stream()
                .map(Order::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long totalOrders = orders.size();

        BigDecimal avgOrderValue = totalOrders > 0
                ? totalRevenue.divide(BigDecimal.valueOf(totalOrders), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        // Daily breakdown
        List<SalesReport.PeriodSales> salesByPeriod = orders.stream()
                .collect(Collectors.groupingBy(
                        o -> o.getCreatedAt().toLocalDate().toString(),
                        Collectors.toList()
                ))
                .entrySet().stream()
                .map(entry -> SalesReport.PeriodSales.builder()
                        .period(entry.getKey())
                        .orders((long) entry.getValue().size())
                        .revenue(entry.getValue().stream()
                                .map(Order::getTotalPrice)
                                .reduce(BigDecimal.ZERO, BigDecimal::add))
                        .build())
                .sorted(Comparator.comparing(SalesReport.PeriodSales::getPeriod))
                .collect(Collectors.toList());

        return SalesReport.builder()
                .periodStart(startDate.toString())
                .periodEnd(endDate.toString())
                .totalRevenue(totalRevenue)
                .totalOrders(totalOrders)
                .averageOrderValue(avgOrderValue)
                .totalItemsSold(0L) // Would require OrderItem analysis
                .salesByPeriod(salesByPeriod)
                .salesByCategory(new ArrayList<>())
                .salesByProduct(new ArrayList<>())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerAnalytics getCustomerAnalytics(Long tenantId) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(30);
        return getCustomerAnalytics(tenantId, startDate, endDate);
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerAnalytics getCustomerAnalytics(Long tenantId, LocalDate startDate, LocalDate endDate) {
        var customers = customerRepository.findAllByTenantId(tenantId);
        long totalCustomers = customers.size();

        // New customers in period
        long newCustomers = customers.stream()
                .filter(c -> {
                    LocalDate createdDate = c.getCreatedAt().toLocalDate();
                    return !createdDate.isBefore(startDate) && !createdDate.isAfter(endDate);
                })
                .count();

        List<Order> orders = orderRepository.findAllByTenantId(tenantId);

        // Calculate customer spending
        Map<String, BigDecimal> customerSpending = orders.stream()
                .filter(o -> o.getStatus() != OrderStatus.CANCELLED)
                .collect(Collectors.groupingBy(
                        Order::getCustomerEmail,
                        Collectors.reducing(
                                BigDecimal.ZERO,
                                Order::getTotalPrice,
                                BigDecimal::add
                        )
                ));

        Map<String, Long> customerOrderCount = orders.stream()
                .collect(Collectors.groupingBy(
                        Order::getCustomerEmail,
                        Collectors.counting()
                ));

        // Average customer value
        BigDecimal avgCustomerValue = BigDecimal.ZERO;
        if (!customerSpending.isEmpty()) {
            avgCustomerValue = customerSpending.values().stream()
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .divide(BigDecimal.valueOf(customerSpending.size()), 2, RoundingMode.HALF_UP);
        }

        // Top customers
        List<CustomerAnalytics.TopCustomer> topCustomers = customerSpending.entrySet().stream()
                .sorted(Map.Entry.<String, BigDecimal>comparingByValue().reversed())
                .limit(10)
                .map(entry -> CustomerAnalytics.TopCustomer.builder()
                        .email(entry.getKey())
                        .totalSpent(entry.getValue())
                        .totalOrders(customerOrderCount.getOrDefault(entry.getKey(), 0L))
                        .build())
                .collect(Collectors.toList());

        // Returning customers (more than 1 order)
        long returningCustomers = customerOrderCount.values().stream()
                .filter(count -> count > 1)
                .count();

        double retentionRate = totalCustomers > 0
                ? (double) returningCustomers / totalCustomers * 100
                : 0;

        return CustomerAnalytics.builder()
                .totalCustomers(totalCustomers)
                .newCustomersThisPeriod(newCustomers)
                .returningCustomers(returningCustomers)
                .retentionRate(retentionRate)
                .averageCustomerValue(avgCustomerValue)
                .topCustomers(topCustomers)
                .customerGrowth(new ArrayList<>())
                .build();
    }

    // ==================== HELPER METHODS ====================

    private List<DashboardStats.DailyRevenue> generateDailyRevenue(List<Order> orders,
                                                                    LocalDate startDate,
                                                                    LocalDate endDate) {
        Map<LocalDate, List<Order>> ordersByDate = orders.stream()
                .collect(Collectors.groupingBy(o -> o.getCreatedAt().toLocalDate()));

        List<DashboardStats.DailyRevenue> result = new ArrayList<>();
        LocalDate current = startDate;

        while (!current.isAfter(endDate)) {
            List<Order> dayOrders = ordersByDate.getOrDefault(current, new ArrayList<>());
            BigDecimal dayRevenue = dayOrders.stream()
                    .filter(o -> o.getStatus() != OrderStatus.CANCELLED)
                    .map(Order::getTotalPrice)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            result.add(DashboardStats.DailyRevenue.builder()
                    .date(current)
                    .revenue(dayRevenue)
                    .orders((long) dayOrders.size())
                    .build());

            current = current.plusDays(1);
        }

        return result;
    }
}

