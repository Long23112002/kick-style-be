package org.longg.nh.kickstyleecommerce.app.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;
import org.longg.nh.kickstyleecommerce.domain.dtos.responses.statistics.ProductSalesResponse;
import org.longg.nh.kickstyleecommerce.domain.dtos.responses.statistics.RevenueStatResponse;
import org.longg.nh.kickstyleecommerce.domain.dtos.responses.users.UserStatResponse;
import org.longg.nh.kickstyleecommerce.domain.services.statistics.StatisticsService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/statistics")
@RequiredArgsConstructor
@Tag(name = "Báo cáo và Thống kê", description = "API báo cáo doanh thu, thống kê bán hàng và phân tích dữ liệu")
public class StatisticsController {

  private final StatisticsService statisticsService;

  @Operation(summary = "Thống kê doanh thu theo ngày", description = "Lấy thông tin doanh thu và số đơn hàng trong một ngày cụ thể")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Lấy thống kê doanh thu thành công",
                  content = @Content(schema = @Schema(implementation = RevenueStatResponse.class)))
  })
  @GetMapping("/revenue/daily")
  public ResponseEntity<RevenueStatResponse> getDailyRevenue(
      @RequestParam @Parameter(description = "Ngày cần thống kê (yyyy-MM-dd)") LocalDate date) {
    RevenueStatResponse response = statisticsService.getTotalRevenueByDate(date);
    return ResponseEntity.ok(response);
  }

  @Operation(summary = "Thống kê doanh thu theo tháng", description = "Lấy thông tin doanh thu và số đơn hàng trong một tháng cụ thể")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Lấy thống kê doanh thu thành công",
                  content = @Content(schema = @Schema(implementation = RevenueStatResponse.class)))
  })
  @GetMapping("/revenue/monthly")
  public ResponseEntity<RevenueStatResponse> getMonthlyRevenue(
      @RequestParam @Parameter(description = "Năm") int year,
      @RequestParam @Parameter(description = "Tháng (1-12)") int month) {
    RevenueStatResponse response = statisticsService.getTotalRevenueByMonth(year, month);
    return ResponseEntity.ok(response);
  }

  @Operation(summary = "Thống kê doanh thu theo năm", description = "Lấy thông tin tổng doanh thu và số đơn hàng trong một năm")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Lấy thống kê doanh thu thành công",
                  content = @Content(schema = @Schema(implementation = RevenueStatResponse.class)))
  })
  @GetMapping("/revenue/yearly")
  public ResponseEntity<RevenueStatResponse> getYearlyRevenue(
      @RequestParam @Parameter(description = "Năm cần thống kê") int year) {
    RevenueStatResponse response = statisticsService.getTotalRevenueByYear(year);
    return ResponseEntity.ok(response);
  }

  @GetMapping("/products/top-selling/daily")
  public ResponseEntity<List<ProductSalesResponse>> getTopSellingProductsDaily(
          @RequestParam(required = false) LocalDate date,
          @RequestParam(required = false) Integer year,
          @RequestParam(required = false) Integer month,
          @RequestParam(required = false) Integer day,
          @RequestParam(defaultValue = "10") int limit) {

    if (date == null && year != null && month != null && day != null) {
      try {
        date = LocalDate.of(year, month, day);
      } catch (DateTimeException e) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ngày không hợp lệ");
      }
    }

    if (date == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Thiếu tham số 'date' hoặc 'year/month/day'");
    }

    return ResponseEntity.ok(statisticsService.getTopSellingProductsByDate(date, limit));
  }


  @Operation(summary = "Top sản phẩm bán chạy theo tháng", description = "Lấy danh sách sản phẩm bán chạy nhất trong một tháng")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Lấy danh sách sản phẩm bán chạy thành công")
  })
  @GetMapping("/products/top-selling/monthly")
  public ResponseEntity<List<ProductSalesResponse>> getTopSellingProductsMonthly(
      @RequestParam @Parameter(description = "Năm") int year,
      @RequestParam @Parameter(description = "Tháng (1-12)") int month,
      @RequestParam(defaultValue = "10") @Parameter(description = "Số lượng sản phẩm cần lấy") int limit) {
    List<ProductSalesResponse> responses = statisticsService.getTopSellingProductsByMonth(year, month, limit);
    return ResponseEntity.ok(responses);
  }

  @Operation(summary = "Top sản phẩm bán chạy theo năm", description = "Lấy danh sách sản phẩm bán chạy nhất trong một năm")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Lấy danh sách sản phẩm bán chạy thành công")
  })
  @GetMapping("/products/top-selling/yearly")
  public ResponseEntity<List<ProductSalesResponse>> getTopSellingProductsYearly(
      @RequestParam @Parameter(description = "Năm cần thống kê") int year,
      @RequestParam(defaultValue = "10") @Parameter(description = "Số lượng sản phẩm cần lấy") int limit) {
    List<ProductSalesResponse> responses = statisticsService.getTopSellingProductsByYear(year, limit);
    return ResponseEntity.ok(responses);
  }

  @Operation(summary = "Thống kê tổng chi tiêu của người dùng", description = "Lấy danh sách tất cả người dùng với tổng số tiền đã chi tiêu")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Lấy thống kê người dùng thành công")
  })
  @GetMapping("/users/spending")
  public ResponseEntity<List<UserStatResponse>> getUsersWithTotalSpent() {
    List<UserStatResponse> responses = statisticsService.getUsersWithTotalSpent();
    return ResponseEntity.ok(responses);
  }

  @Operation(summary = "Top khách hàng chi tiêu nhiều nhất", description = "Lấy danh sách khách hàng có tổng chi tiêu cao nhất")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Lấy danh sách top khách hàng thành công")
  })
  @GetMapping("/users/top-customers")
  public ResponseEntity<List<UserStatResponse>> getTopCustomers(
      @RequestParam(defaultValue = "10") @Parameter(description = "Số lượng khách hàng cần lấy") int limit) {
    List<UserStatResponse> responses = statisticsService.getTopCustomers(limit);
    return ResponseEntity.ok(responses);
  }

  @Operation(summary = "Dashboard tổng quan", description = "Lấy thông tin tổng quan cho dashboard quản trị bao gồm doanh thu và top sản phẩm. Có thể lọc theo ngày, tháng, năm.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Lấy thông tin dashboard thành công",
                  content = @Content(schema = @Schema(implementation = StatisticsService.DashboardSummaryResponse.class)))
  })
  @GetMapping("/dashboard")
  public ResponseEntity<StatisticsService.DashboardSummaryResponse> getDashboardSummary(
          @RequestParam(required = false) @Parameter(description = "Năm cần thống kê (yyyy)") Integer year,
          @RequestParam(required = false) @Parameter(description = "Tháng cần thống kê (1-12)") Integer month,
          @RequestParam(required = false) @Parameter(description = "Ngày cần thống kê (yyyy-MM-dd)") 
          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        
        StatisticsService.DashboardSummaryResponse response = statisticsService.getDashboardSummary(year, month, date);
        return ResponseEntity.ok(response);
    }
} 