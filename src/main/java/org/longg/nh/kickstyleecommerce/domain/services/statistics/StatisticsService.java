package org.longg.nh.kickstyleecommerce.domain.services.statistics;

import lombok.RequiredArgsConstructor;
import org.longg.nh.kickstyleecommerce.domain.dtos.responses.statistics.ProductSalesResponse;
import org.longg.nh.kickstyleecommerce.domain.dtos.responses.statistics.RevenueStatResponse;
import org.longg.nh.kickstyleecommerce.domain.dtos.responses.users.UserStatResponse;
import org.longg.nh.kickstyleecommerce.domain.entities.enums.OrderStatus;
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
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StatisticsService {

  private final OrderRepository orderRepository;
  private final OrderItemRepository orderItemRepository;
  private final UserRepository userRepository;

  // Revenue Statistics
  public RevenueStatResponse getTotalRevenueByDate(LocalDate date) {
    List<OrderStatus> orderStatuses = List.of(OrderStatus.DELIVERED, OrderStatus.RECEIVED);
    Timestamp timestamp = Timestamp.valueOf(date.atStartOfDay());
    BigDecimal revenue = orderRepository.getTotalRevenueByDate(orderStatuses, timestamp);
    Long orderCount = orderRepository.getTotalOrdersByDate(orderStatuses, timestamp);
    
    return RevenueStatResponse.builder()
        .period(date.toString())
        .totalRevenue(revenue != null ? revenue : BigDecimal.ZERO)
        .totalOrders(orderCount != null ? orderCount : 0L)
        .periodType("DAY")
        .build();
  }

  public RevenueStatResponse getTotalRevenueByMonth(int year, int month) {
    List<OrderStatus> orderStatuses = List.of(OrderStatus.DELIVERED, OrderStatus.RECEIVED);
    BigDecimal revenue = orderRepository.getTotalRevenueByMonth(orderStatuses, year, month);
    Long orderCount = orderRepository.getTotalOrdersByMonth(orderStatuses, year, month);
    
    return RevenueStatResponse.builder()
        .period(year + "-" + String.format("%02d", month))
        .totalRevenue(revenue != null ? revenue : BigDecimal.ZERO)
        .totalOrders(orderCount != null ? orderCount : 0L)
        .periodType("MONTH")
        .build();
  }

  public RevenueStatResponse getTotalRevenueByYear(int year) {
    List<OrderStatus> orderStatuses = List.of(OrderStatus.DELIVERED, OrderStatus.RECEIVED);
    BigDecimal revenue = orderRepository.getTotalRevenueByYear(orderStatuses, year);
    Long orderCount = orderRepository.getTotalOrdersByYear(orderStatuses, year);

    return RevenueStatResponse.builder()
        .period(String.valueOf(year))
        .totalRevenue(revenue != null ? revenue : BigDecimal.ZERO)
        .totalOrders(orderCount != null ? orderCount : 0L)
        .periodType("YEAR")
        .build();
  }

  // Revenue Statistics by Date Range
  public RevenueStatResponse getTotalRevenueByDateRange(LocalDate startDate, LocalDate endDate) {
    List<OrderStatus> orderStatuses = List.of(OrderStatus.DELIVERED, OrderStatus.RECEIVED);
    Timestamp startTimestamp = Timestamp.valueOf(startDate.atStartOfDay());
    Timestamp endTimestamp = Timestamp.valueOf(endDate.atTime(23, 59, 59));

    BigDecimal revenue = orderRepository.getTotalRevenueByDateRange(orderStatuses, startTimestamp, endTimestamp);
    Long orderCount = orderRepository.getTotalOrdersByDateRange(orderStatuses, startTimestamp, endTimestamp);

    return RevenueStatResponse.builder()
        .period(startDate.toString() + " to " + endDate.toString())
        .totalRevenue(revenue != null ? revenue : BigDecimal.ZERO)
        .totalOrders(orderCount != null ? orderCount : 0L)
        .periodType("RANGE")
        .build();
  }

  // Product Sales Statistics
  public List<ProductSalesResponse> getTopSellingProductsByDate(LocalDate date, int limit) {
    Timestamp timestamp = Timestamp.valueOf(date.atStartOfDay());
    return orderItemRepository.getTopSellingProductsByDate(timestamp )
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

  // Product Sales Statistics by Date Range
  public List<ProductSalesResponse> getTopSellingProductsByDateRange(LocalDate startDate, LocalDate endDate, int limit) {
    Timestamp startTimestamp = Timestamp.valueOf(startDate.atStartOfDay());
    Timestamp endTimestamp = Timestamp.valueOf(endDate.atTime(23, 59, 59));

    return orderItemRepository.getTopSellingProductsByDateRange(startTimestamp, endTimestamp, limit)
        .stream()
        .map(result -> ProductSalesResponse.builder()
            .productId((Long) result[0])
            .productName((String) result[1])
            .totalQuantitySold(((Number) result[2]).longValue())
            .totalRevenue((BigDecimal) result[3])
            .period(startDate.toString() + " to " + endDate.toString())
            .periodType("RANGE")
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
  public DashboardSummaryResponse getDashboardSummary(Integer year, Integer month, LocalDate date) {
    LocalDate today = LocalDate.now();
    int currentYear = Optional.ofNullable(year).orElse(today.getYear());
    int currentMonth = Optional.ofNullable(month).orElse(today.getMonthValue());
    LocalDate targetDate = Optional.ofNullable(date).orElse(today);

    // Nếu có ngày cụ thể, chỉ lấy thống kê theo ngày
    if (date != null) {
      RevenueStatResponse dateStats = getTotalRevenueByDate(targetDate);
      List<ProductSalesResponse> topProducts = getTopSellingProductsByDate(targetDate, 5);
      List<UserStatResponse> topCustomers = getTopCustomers(5);

      return DashboardSummaryResponse.builder()
          .todayRevenue(dateStats.getTotalRevenue())
          .todayOrders(dateStats.getTotalOrders())
          .monthRevenue(BigDecimal.ZERO)
          .monthOrders(0L)
          .yearRevenue(BigDecimal.ZERO)
          .yearOrders(0L)
          .topProducts(topProducts)
          .topCustomers(topCustomers)
          .build();
    }

    // Nếu có tháng cụ thể, lấy thống kê theo tháng và năm
    if (month != null) {
      RevenueStatResponse monthStats = getTotalRevenueByMonth(currentYear, currentMonth);
      RevenueStatResponse yearStats = getTotalRevenueByYear(currentYear);
      List<ProductSalesResponse> topProducts = getTopSellingProductsByMonth(currentYear, currentMonth, 5);
      List<UserStatResponse> topCustomers = getTopCustomers(5);

      return DashboardSummaryResponse.builder()
          .todayRevenue(BigDecimal.ZERO)
          .todayOrders(0L)
          .monthRevenue(monthStats.getTotalRevenue())
          .monthOrders(monthStats.getTotalOrders())
          .yearRevenue(yearStats.getTotalRevenue())
          .yearOrders(yearStats.getTotalOrders())
          .topProducts(topProducts)
          .topCustomers(topCustomers)
          .build();
    }

    // Nếu chỉ có năm, lấy thống kê theo năm
    if (year != null) {
      RevenueStatResponse yearStats = getTotalRevenueByYear(currentYear);
      List<ProductSalesResponse> topProducts = getTopSellingProductsByYear(currentYear, 5);
      List<UserStatResponse> topCustomers = getTopCustomers(5);

      return DashboardSummaryResponse.builder()
          .todayRevenue(BigDecimal.ZERO)
          .todayOrders(0L)
          .monthRevenue(BigDecimal.ZERO)
          .monthOrders(0L)
          .yearRevenue(yearStats.getTotalRevenue())
          .yearOrders(yearStats.getTotalOrders())
          .topProducts(topProducts)
          .topCustomers(topCustomers)
          .build();
    }

    // Mặc định lấy thống kê của ngày hiện tại
    RevenueStatResponse todayStats = getTotalRevenueByDate(today);
    RevenueStatResponse monthStats = getTotalRevenueByMonth(currentYear, currentMonth);
    RevenueStatResponse yearStats = getTotalRevenueByYear(currentYear);
    List<ProductSalesResponse> topProducts = getTopSellingProductsByMonth(currentYear, currentMonth, 5);
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