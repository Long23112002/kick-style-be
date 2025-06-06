package org.longg.nh.kickstyleecommerce.domain.services.statistics;

import lombok.RequiredArgsConstructor;
import org.longg.nh.kickstyleecommerce.domain.dtos.responses.statistics.ProductSalesResponse;
import org.longg.nh.kickstyleecommerce.domain.dtos.responses.statistics.RevenueStatResponse;
import org.longg.nh.kickstyleecommerce.domain.dtos.responses.users.UserStatResponse;
import org.longg.nh.kickstyleecommerce.domain.entities.enums.PaymentStatus;
import org.longg.nh.kickstyleecommerce.domain.repositories.OrderItemRepository;
import org.longg.nh.kickstyleecommerce.domain.repositories.OrderRepository;
import org.longg.nh.kickstyleecommerce.domain.repositories.UserRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StatisticsService {

  private final OrderRepository orderRepository;
  private final OrderItemRepository orderItemRepository;
  private final UserRepository userRepository;

  // Revenue Statistics
  public RevenueStatResponse getTotalRevenueByDate(LocalDate date) {
    Timestamp timestamp = Timestamp.valueOf(date.atStartOfDay());
    BigDecimal revenue = orderRepository.getTotalRevenueByDate(PaymentStatus.PAID, timestamp);
    Long orderCount = orderRepository.getTotalOrdersByDate(PaymentStatus.PAID, timestamp);
    
    return RevenueStatResponse.builder()
        .period(date.toString())
        .totalRevenue(revenue != null ? revenue : BigDecimal.ZERO)
        .totalOrders(orderCount != null ? orderCount : 0L)
        .periodType("DAY")
        .build();
  }

  public RevenueStatResponse getTotalRevenueByMonth(int year, int month) {
    BigDecimal revenue = orderRepository.getTotalRevenueByMonth(PaymentStatus.PAID, year, month);
    Long orderCount = orderRepository.getTotalOrdersByMonth(PaymentStatus.PAID, year, month);
    
    return RevenueStatResponse.builder()
        .period(year + "-" + String.format("%02d", month))
        .totalRevenue(revenue != null ? revenue : BigDecimal.ZERO)
        .totalOrders(orderCount != null ? orderCount : 0L)
        .periodType("MONTH")
        .build();
  }

  public RevenueStatResponse getTotalRevenueByYear(int year) {
    BigDecimal revenue = orderRepository.getTotalRevenueByYear(PaymentStatus.PAID, year);
    Long orderCount = orderRepository.getTotalOrdersByYear(PaymentStatus.PAID, year);
    
    return RevenueStatResponse.builder()
        .period(String.valueOf(year))
        .totalRevenue(revenue != null ? revenue : BigDecimal.ZERO)
        .totalOrders(orderCount != null ? orderCount : 0L)
        .periodType("YEAR")
        .build();
  }

  // Product Sales Statistics
  public List<ProductSalesResponse> getTopSellingProductsByDate(LocalDate date, int limit) {
    Timestamp timestamp = Timestamp.valueOf(date.atStartOfDay());
    return orderItemRepository.getTopSellingProductsByDate(timestamp)
        .stream()
        .map(result -> ProductSalesResponse.builder()
            .productId((Long) result[0])
            .productName((String) result[1])
            .totalQuantitySold(((Number) result[2]).longValue())
            .totalRevenue((BigDecimal) result[3])
            .period(date.toString())
            .periodType("DAY")
            .build())
        .collect(Collectors.toList());
  }

  public List<ProductSalesResponse> getTopSellingProductsByMonth(int year, int month, int limit) {
    return orderItemRepository.getTopSellingProductsByMonth(year, month)
        .stream()
        .map(result -> ProductSalesResponse.builder()
            .productId((Long) result[0])
            .productName((String) result[1])
            .totalQuantitySold(((Number) result[2]).longValue())
            .totalRevenue((BigDecimal) result[3])
            .period(year + "-" + String.format("%02d", month))
            .periodType("MONTH")
            .build())
        .collect(Collectors.toList());
  }

  public List<ProductSalesResponse> getTopSellingProductsByYear(int year, int limit) {
    return orderItemRepository.getTopSellingProductsByYear(year)
        .stream()
        .map(result -> ProductSalesResponse.builder()
            .productId((Long) result[0])
            .productName((String) result[1])
            .totalQuantitySold(((Number) result[2]).longValue())
            .totalRevenue((BigDecimal) result[3])
            .period(String.valueOf(year))
            .periodType("YEAR")
            .build())
        .collect(Collectors.toList());
  }

  // User Statistics
  public List<UserStatResponse> getUsersWithTotalSpent() {
    return userRepository.getUsersWithTotalSpent()
        .stream()
        .map(result -> UserStatResponse.builder()
            .userId((Long) result[0])
            .fullName((String) result[1])
            .email((String) result[2])
            .totalSpent((BigDecimal) result[3])
            .totalOrders(((Number) result[4]).longValue())
            .build())
        .collect(Collectors.toList());
  }

  public List<UserStatResponse> getTopCustomers(int limit) {
    return userRepository.getTopCustomers(limit)
        .stream()
        .map(result -> UserStatResponse.builder()
            .userId((Long) result[0])
            .fullName((String) result[1])
            .email((String) result[2])
            .totalSpent((BigDecimal) result[3])
            .totalOrders(((Number) result[4]).longValue())
            .build())
        .collect(Collectors.toList());
  }

  // Dashboard Summary
  public DashboardSummaryResponse getDashboardSummary() {
    LocalDate today = LocalDate.now();
    LocalDate thisMonth = today.withDayOfMonth(1);
    int currentYear = today.getYear();
    int currentMonth = today.getMonthValue();

    // Today's stats
    RevenueStatResponse todayStats = getTotalRevenueByDate(today);
    
    // This month's stats
    RevenueStatResponse monthStats = getTotalRevenueByMonth(currentYear, currentMonth);
    
    // This year's stats
    RevenueStatResponse yearStats = getTotalRevenueByYear(currentYear);
    
    // Top products this month
    List<ProductSalesResponse> topProducts = getTopSellingProductsByMonth(currentYear, currentMonth, 5);
    
    // Top customers
    List<UserStatResponse> topCustomers = getTopCustomers(5);

    return DashboardSummaryResponse.builder()
        .todayRevenue(todayStats.getTotalRevenue())
        .todayOrders(todayStats.getTotalOrders())
        .monthRevenue(monthStats.getTotalRevenue())
        .monthOrders(monthStats.getTotalOrders())
        .yearRevenue(yearStats.getTotalRevenue())
        .yearOrders(yearStats.getTotalOrders())
        .topProducts(topProducts)
        .topCustomers(topCustomers)
        .build();
  }

  // Inner class for dashboard summary
  public static class DashboardSummaryResponse {
    private BigDecimal todayRevenue;
    private Long todayOrders;
    private BigDecimal monthRevenue;
    private Long monthOrders;
    private BigDecimal yearRevenue;
    private Long yearOrders;
    private List<ProductSalesResponse> topProducts;
    private List<UserStatResponse> topCustomers;

    public static DashboardSummaryResponseBuilder builder() {
      return new DashboardSummaryResponseBuilder();
    }

    public static class DashboardSummaryResponseBuilder {
      private BigDecimal todayRevenue;
      private Long todayOrders;
      private BigDecimal monthRevenue;
      private Long monthOrders;
      private BigDecimal yearRevenue;
      private Long yearOrders;
      private List<ProductSalesResponse> topProducts;
      private List<UserStatResponse> topCustomers;

      public DashboardSummaryResponseBuilder todayRevenue(BigDecimal todayRevenue) {
        this.todayRevenue = todayRevenue;
        return this;
      }

      public DashboardSummaryResponseBuilder todayOrders(Long todayOrders) {
        this.todayOrders = todayOrders;
        return this;
      }

      public DashboardSummaryResponseBuilder monthRevenue(BigDecimal monthRevenue) {
        this.monthRevenue = monthRevenue;
        return this;
      }

      public DashboardSummaryResponseBuilder monthOrders(Long monthOrders) {
        this.monthOrders = monthOrders;
        return this;
      }

      public DashboardSummaryResponseBuilder yearRevenue(BigDecimal yearRevenue) {
        this.yearRevenue = yearRevenue;
        return this;
      }

      public DashboardSummaryResponseBuilder yearOrders(Long yearOrders) {
        this.yearOrders = yearOrders;
        return this;
      }

      public DashboardSummaryResponseBuilder topProducts(List<ProductSalesResponse> topProducts) {
        this.topProducts = topProducts;
        return this;
      }

      public DashboardSummaryResponseBuilder topCustomers(List<UserStatResponse> topCustomers) {
        this.topCustomers = topCustomers;
        return this;
      }

      public DashboardSummaryResponse build() {
        DashboardSummaryResponse response = new DashboardSummaryResponse();
        response.todayRevenue = this.todayRevenue;
        response.todayOrders = this.todayOrders;
        response.monthRevenue = this.monthRevenue;
        response.monthOrders = this.monthOrders;
        response.yearRevenue = this.yearRevenue;
        response.yearOrders = this.yearOrders;
        response.topProducts = this.topProducts;
        response.topCustomers = this.topCustomers;
        return response;
      }
    }

    // Getters
    public BigDecimal getTodayRevenue() { return todayRevenue; }
    public Long getTodayOrders() { return todayOrders; }
    public BigDecimal getMonthRevenue() { return monthRevenue; }
    public Long getMonthOrders() { return monthOrders; }
    public BigDecimal getYearRevenue() { return yearRevenue; }
    public Long getYearOrders() { return yearOrders; }
    public List<ProductSalesResponse> getTopProducts() { return topProducts; }
    public List<UserStatResponse> getTopCustomers() { return topCustomers; }
  }
} 