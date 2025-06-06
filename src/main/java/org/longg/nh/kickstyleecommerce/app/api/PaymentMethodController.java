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
import org.longg.nh.kickstyleecommerce.domain.dtos.requests.payments.PaymentMethodRequest;
import org.longg.nh.kickstyleecommerce.domain.dtos.responses.payments.PaymentMethodResponse;
import org.longg.nh.kickstyleecommerce.domain.services.payments.PaymentMethodService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/payment-methods")
@RequiredArgsConstructor
@Tag(name = "Quản lý Phương thức thanh toán", description = "API quản lý các phương thức thanh toán")
public class PaymentMethodController {

  private final PaymentMethodService paymentMethodService;

  @Operation(summary = "Tạo phương thức thanh toán mới", description = "Tạo một phương thức thanh toán mới trong hệ thống")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Tạo phương thức thanh toán thành công",
                  content = @Content(schema = @Schema(implementation = PaymentMethodResponse.class))),
      @ApiResponse(responseCode = "400", description = "Dữ liệu đầu vào không hợp lệ"),
      @ApiResponse(responseCode = "409", description = "Tên phương thức thanh toán đã tồn tại")
  })
  @PostMapping
  public ResponseEntity<PaymentMethodResponse> createPaymentMethod(
      @Parameter(hidden = true) HeaderContext context,
      @Valid @RequestBody @Parameter(description = "Thông tin phương thức thanh toán cần tạo") PaymentMethodRequest request) {
    PaymentMethodResponse response = paymentMethodService.createPaymentMethod(context, request);
    return ResponseEntity.ok(response);
  }

  @Operation(summary = "Lấy thông tin phương thức thanh toán theo ID", description = "Lấy chi tiết thông tin của một phương thức thanh toán")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Lấy thông tin phương thức thanh toán thành công",
                  content = @Content(schema = @Schema(implementation = PaymentMethodResponse.class))),
      @ApiResponse(responseCode = "404", description = "Không tìm thấy phương thức thanh toán")
  })
  @GetMapping("/{id}")
  public ResponseEntity<PaymentMethodResponse> getPaymentMethodById(
      @Parameter(hidden = true) HeaderContext context,
      @PathVariable @Parameter(description = "ID phương thức thanh toán") Long id) {
    PaymentMethodResponse response = paymentMethodService.getById(context, id);
    return ResponseEntity.ok(response);
  }

  @Operation(summary = "Lấy danh sách tất cả phương thức thanh toán", description = "Lấy danh sách tất cả phương thức thanh toán với phân trang")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Lấy danh sách phương thức thanh toán thành công")
  })
  @GetMapping
  public ResponseEntity<Page<PaymentMethodResponse>> getAllPaymentMethods(
      @Parameter(hidden = true) HeaderContext context,
      @Parameter(description = "Thông tin phân trang") Pageable pageable) {
    Page<PaymentMethodResponse> responses = paymentMethodService.getAll(context, null, null, null, null, null, 
        (ctx, paymentMethod) -> paymentMethodService.mapToPaymentMethodResponse(paymentMethod));
    return ResponseEntity.ok(responses);
  }

  @Operation(summary = "Lấy danh sách phương thức thanh toán đang hoạt động", description = "Lấy danh sách các phương thức thanh toán đang có thể sử dụng")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Lấy danh sách phương thức thanh toán hoạt động thành công")
  })
  @GetMapping("/active")
  public ResponseEntity<List<PaymentMethodResponse>> getActivePaymentMethods(@Parameter(hidden = true) HeaderContext context) {
    List<PaymentMethodResponse> responses = paymentMethodService.getActivePaymentMethods();
    return ResponseEntity.ok(responses);
  }

  @Operation(summary = "Cập nhật phương thức thanh toán", description = "Cập nhật thông tin của một phương thức thanh toán")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Cập nhật phương thức thanh toán thành công",
                  content = @Content(schema = @Schema(implementation = PaymentMethodResponse.class))),
      @ApiResponse(responseCode = "400", description = "Dữ liệu đầu vào không hợp lệ"),
      @ApiResponse(responseCode = "404", description = "Không tìm thấy phương thức thanh toán")
  })
  @PutMapping("/{id}")
  public ResponseEntity<PaymentMethodResponse> updatePaymentMethod(
      @Parameter(hidden = true) HeaderContext context,
      @PathVariable @Parameter(description = "ID phương thức thanh toán cần cập nhật") Long id,
      @Valid @RequestBody @Parameter(description = "Thông tin phương thức thanh toán mới") PaymentMethodRequest request) {
    PaymentMethodResponse response = paymentMethodService.updatePaymentMethod(id, request);
    return ResponseEntity.ok(response);
  }

  @Operation(summary = "Kích hoạt phương thức thanh toán", description = "Kích hoạt một phương thức thanh toán để có thể sử dụng")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Kích hoạt phương thức thanh toán thành công",
                  content = @Content(schema = @Schema(implementation = PaymentMethodResponse.class))),
      @ApiResponse(responseCode = "404", description = "Không tìm thấy phương thức thanh toán")
  })
  @PutMapping("/{id}/activate")
  public ResponseEntity<PaymentMethodResponse> activatePaymentMethod(
      @Parameter(hidden = true) HeaderContext context,
      @PathVariable @Parameter(description = "ID phương thức thanh toán cần kích hoạt") Long id) {
    PaymentMethodResponse response = paymentMethodService.activatePaymentMethod(id);
    return ResponseEntity.ok(response);
  }

  @Operation(summary = "Vô hiệu hóa phương thức thanh toán", description = "Vô hiệu hóa một phương thức thanh toán để ngừng sử dụng")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Vô hiệu hóa phương thức thanh toán thành công",
                  content = @Content(schema = @Schema(implementation = PaymentMethodResponse.class))),
      @ApiResponse(responseCode = "404", description = "Không tìm thấy phương thức thanh toán")
  })
  @PutMapping("/{id}/deactivate")
  public ResponseEntity<PaymentMethodResponse> deactivatePaymentMethod(
      @Parameter(hidden = true) HeaderContext context,
      @PathVariable @Parameter(description = "ID phương thức thanh toán cần vô hiệu hóa") Long id) {
    PaymentMethodResponse response = paymentMethodService.deactivatePaymentMethod(id);
    return ResponseEntity.ok(response);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deletePaymentMethod(
      HeaderContext context,
      @PathVariable Long id) {
    paymentMethodService.deletePaymentMethod(id);
    return ResponseEntity.ok().build();
  }
} 