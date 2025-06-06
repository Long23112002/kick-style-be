package org.longg.nh.kickstyleecommerce.app.api;

import com.eps.shared.models.HeaderContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.longg.nh.kickstyleecommerce.domain.dtos.requests.coupons.CouponRequest;
import org.longg.nh.kickstyleecommerce.domain.dtos.responses.coupons.CouponResponse;
import org.longg.nh.kickstyleecommerce.domain.services.coupons.CouponService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/coupons")
@RequiredArgsConstructor
@Tag(name = "Quản lý Coupon", description = "API quản lý mã giảm giá và khuyến mãi")
public class CouponController {

  private final CouponService couponService;

  @Operation(summary = "Tạo mã coupon mới", description = "Tạo một mã giảm giá mới với các thông tin chi tiết")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Tạo coupon thành công",
                  content = @Content(schema = @Schema(implementation = CouponResponse.class))),
      @ApiResponse(responseCode = "400", description = "Dữ liệu đầu vào không hợp lệ"),
      @ApiResponse(responseCode = "409", description = "Mã coupon đã tồn tại")
  })
  @PostMapping
  public ResponseEntity<CouponResponse> createCoupon(
      @Parameter(hidden = true) HeaderContext context,
      @Valid @RequestBody @Parameter(description = "Thông tin coupon cần tạo") CouponRequest request) {
    CouponResponse response = couponService.createCoupon(context, request);
    return ResponseEntity.ok(response);
  }

  @Operation(summary = "Lấy thông tin coupon theo ID", description = "Lấy chi tiết thông tin của một mã coupon dựa vào ID")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Lấy thông tin coupon thành công",
                  content = @Content(schema = @Schema(implementation = CouponResponse.class))),
      @ApiResponse(responseCode = "404", description = "Không tìm thấy coupon")
  })
  @GetMapping("/{id}")
  public ResponseEntity<CouponResponse> getCouponById(
      @Parameter(hidden = true) HeaderContext context,
      @PathVariable @Parameter(description = "ID của coupon cần lấy") Long id) {
    CouponResponse response = couponService.getById(context, id);
    return ResponseEntity.ok(response);
  }

  @Operation(summary = "Lấy danh sách tất cả coupon", description = "Lấy danh sách tất cả mã coupon với phân trang")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Lấy danh sách coupon thành công")
  })
  @GetMapping
  public ResponseEntity<Page<CouponResponse>> getAllCoupons(
      @Parameter(hidden = true) HeaderContext context,
      @Parameter(description = "Thông tin phân trang") Pageable pageable) {
    Page<CouponResponse> responses = couponService.getAll(context, null, null, null, null, null, 
        (ctx, coupon) -> couponService.mapToCouponResponse(coupon));
    return ResponseEntity.ok(responses);
  }

  @Operation(summary = "Lấy danh sách coupon đang hoạt động", description = "Lấy danh sách các mã coupon đang có hiệu lực")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Lấy danh sách coupon hoạt động thành công")
  })
  @GetMapping("/active")
  public ResponseEntity<List<CouponResponse>> getActiveCoupons(@Parameter(hidden = true) HeaderContext context) {
    List<CouponResponse> responses = couponService.getActiveCoupons();
    return ResponseEntity.ok(responses);
  }

  @Operation(summary = "Lấy coupon khả dụng cho người dùng", description = "Lấy danh sách mã coupon mà người dùng có thể sử dụng")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Lấy danh sách coupon khả dụng thành công"),
      @ApiResponse(responseCode = "404", description = "Không tìm thấy người dùng")
  })
  @GetMapping("/user/{userId}")
  public ResponseEntity<List<CouponResponse>> getValidCouponsForUser(
      @Parameter(hidden = true) HeaderContext context,
      @PathVariable @Parameter(description = "ID người dùng") Long userId) {
    List<CouponResponse> responses = couponService.getValidCouponsForUser(userId);
    return ResponseEntity.ok(responses);
  }

  @Operation(summary = "Cập nhật thông tin coupon", description = "Cập nhật thông tin chi tiết của một mã coupon")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Cập nhật coupon thành công",
                  content = @Content(schema = @Schema(implementation = CouponResponse.class))),
      @ApiResponse(responseCode = "400", description = "Dữ liệu đầu vào không hợp lệ"),
      @ApiResponse(responseCode = "404", description = "Không tìm thấy coupon")
  })
  @PutMapping("/{id}")
  public ResponseEntity<CouponResponse> updateCoupon(
      @Parameter(hidden = true) HeaderContext context,
      @PathVariable @Parameter(description = "ID coupon cần cập nhật") Long id,
      @Valid @RequestBody @Parameter(description = "Thông tin coupon mới") CouponRequest request) {
    CouponResponse response = couponService.updateCoupon(id, request);
    return ResponseEntity.ok(response);
  }

  @Operation(summary = "Kích hoạt coupon", description = "Kích hoạt một mã coupon để có thể sử dụng")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Kích hoạt coupon thành công",
                  content = @Content(schema = @Schema(implementation = CouponResponse.class))),
      @ApiResponse(responseCode = "404", description = "Không tìm thấy coupon")
  })
  @PutMapping("/{id}/activate")
  public ResponseEntity<CouponResponse> activateCoupon(
      @Parameter(hidden = true) HeaderContext context,
      @PathVariable @Parameter(description = "ID coupon cần kích hoạt") Long id) {
    CouponResponse response = couponService.activateCoupon(id);
    return ResponseEntity.ok(response);
  }

  @Operation(summary = "Vô hiệu hóa coupon", description = "Vô hiệu hóa một mã coupon để ngừng sử dụng")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Vô hiệu hóa coupon thành công",
                  content = @Content(schema = @Schema(implementation = CouponResponse.class))),
      @ApiResponse(responseCode = "404", description = "Không tìm thấy coupon")
  })
  @PutMapping("/{id}/deactivate")
  public ResponseEntity<CouponResponse> deactivateCoupon(
      @Parameter(hidden = true) HeaderContext context,
      @PathVariable @Parameter(description = "ID coupon cần vô hiệu hóa") Long id) {
    CouponResponse response = couponService.deactivateCoupon(id);
    return ResponseEntity.ok(response);
  }

  @Operation(summary = "Xóa coupon", description = "Xóa vĩnh viễn một mã coupon khỏi hệ thống")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Xóa coupon thành công"),
      @ApiResponse(responseCode = "404", description = "Không tìm thấy coupon")
  })
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteCoupon(
      @Parameter(hidden = true) HeaderContext context,
      @PathVariable @Parameter(description = "ID coupon cần xóa") Long id) {
    couponService.deleteById(context, id);
    return ResponseEntity.ok().build();
  }
} 