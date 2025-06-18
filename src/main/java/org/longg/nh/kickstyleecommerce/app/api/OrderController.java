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
import org.longg.nh.kickstyleecommerce.domain.dtos.requests.orders.CreateOrderRequest;
import org.longg.nh.kickstyleecommerce.domain.dtos.responses.orders.OrderResponse;
import org.longg.nh.kickstyleecommerce.domain.entities.Order;
import org.longg.nh.kickstyleecommerce.domain.entities.enums.OrderStatus;
import org.longg.nh.kickstyleecommerce.domain.entities.enums.PaymentStatus;
import org.longg.nh.kickstyleecommerce.domain.services.orders.OrderService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Tag(name = "Quản lý Đơn hàng", description = "API quản lý đơn hàng và xử lý thanh toán")
public class OrderController {

  private final OrderService orderService;

  @Operation(
      summary = "Tạo đơn hàng mới",
      description = "Tạo một đơn hàng mới cho khách hàng với danh sách sản phẩm")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Tạo đơn hàng thành công",
            content = @Content(schema = @Schema(implementation = OrderResponse.class))),
        @ApiResponse(responseCode = "400", description = "Dữ liệu đầu vào không hợp lệ"),
        @ApiResponse(responseCode = "404", description = "Không tìm thấy sản phẩm hoặc người dùng")
      })
  @PostMapping
  public ResponseEntity<OrderResponse> createOrder(
      @Parameter(hidden = true) HeaderContext context,
      @Valid @RequestBody @Parameter(description = "Thông tin đơn hàng cần tạo")
          CreateOrderRequest request,
      @RequestParam @Parameter(description = "ID người dùng đặt hàng") Long userId) {
    OrderResponse response = orderService.createOrder(context, request, userId);
    return ResponseEntity.ok(response);
  }

  @Operation(
      summary = "Lấy thông tin đơn hàng theo ID",
      description = "Lấy chi tiết thông tin của một đơn hàng dựa vào ID")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Lấy thông tin đơn hàng thành công",
            content = @Content(schema = @Schema(implementation = OrderResponse.class))),
        @ApiResponse(responseCode = "404", description = "Không tìm thấy đơn hàng")
      })
  @GetMapping("/{id}")
  public ResponseEntity<Order> getOrderById(
      @Parameter(hidden = true) HeaderContext context,
      @PathVariable @Parameter(description = "ID của đơn hàng cần lấy") Long id) {
    Order response = orderService.finById(id);
    return ResponseEntity.ok(response);
  }

  @Operation(
      summary = "Lấy thông tin đơn hàng theo mã",
      description = "Lấy chi tiết thông tin của một đơn hàng dựa vào mã đơn hàng")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Lấy thông tin đơn hàng thành công",
            content = @Content(schema = @Schema(implementation = OrderResponse.class))),
        @ApiResponse(responseCode = "404", description = "Không tìm thấy đơn hàng")
      })
  @GetMapping("/code/{code}")
  public ResponseEntity<OrderResponse> getOrderByCode(
      @Parameter(hidden = true) HeaderContext context,
      @PathVariable @Parameter(description = "Mã đơn hàng") String code) {
    OrderResponse response = orderService.getOrderByCode(code);
    return ResponseEntity.ok(response);
  }

  @Operation(
      summary = "Lấy danh sách đơn hàng của người dùng",
      description = "Lấy tất cả đơn hàng của một người dùng cụ thể")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Lấy danh sách đơn hàng thành công"),
        @ApiResponse(responseCode = "404", description = "Không tìm thấy người dùng")
      })
  @GetMapping("/user/{userId}")
  public ResponseEntity<List<OrderResponse>> getOrdersByUser(
      @Parameter(hidden = true) HeaderContext context,
      @PathVariable @Parameter(description = "ID người dùng") Long userId) {
    List<OrderResponse> responses = orderService.getOrdersByUser(userId);
    return ResponseEntity.ok(responses);
  }


  @GetMapping("/export-pdf/{orderId}")
  public ResponseEntity<byte[]> generateOrderPdf( @PathVariable Long orderId) {
    byte[] pdfBytes = orderService.generateOrderPdf(orderId);

    return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=order_" + orderId + ".pdf")
            .contentType(MediaType.APPLICATION_PDF)
            .body(pdfBytes);
  }

  @Operation(
      summary = "Lấy danh sách tất cả đơn hàng",
      description = "Lấy danh sách tất cả đơn hàng với phân trang")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Lấy danh sách đơn hàng thành công")
      })
  @GetMapping
  public ResponseEntity<Page<OrderResponse>> getAllOrders(
      @Parameter(hidden = true) HeaderContext context,
      @Parameter(description = "Thông tin phân trang") Pageable pageable) {
    Page<OrderResponse> page = orderService.getAllOrders(pageable);
    return ResponseEntity.ok(page);
  }

  @Operation(
      summary = "Cập nhật trạng thái đơn hàng",
      description = "Cập nhật trạng thái xử lý của đơn hàng")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Cập nhật trạng thái thành công",
            content = @Content(schema = @Schema(implementation = OrderResponse.class))),
        @ApiResponse(responseCode = "404", description = "Không tìm thấy đơn hàng")
      })
  @PutMapping("/{id}/status")
  public ResponseEntity<OrderResponse> updateOrderStatus(
      @Parameter(hidden = true) HeaderContext context,
      @PathVariable @Parameter(description = "ID đơn hàng") Long id,
      @RequestParam @Parameter(description = "Trạng thái đơn hàng mới") OrderStatus status) {
    OrderResponse response = orderService.updateOrderStatus(id, status);
    return ResponseEntity.ok(response);
  }

  @Operation(
      summary = "Cập nhật trạng thái thanh toán",
      description = "Cập nhật trạng thái thanh toán của đơn hàng")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Cập nhật trạng thái thanh toán thành công",
            content = @Content(schema = @Schema(implementation = OrderResponse.class))),
        @ApiResponse(responseCode = "404", description = "Không tìm thấy đơn hàng")
      })
  @PutMapping("/{id}/payment-status")
  public ResponseEntity<OrderResponse> updatePaymentStatus(
      @Parameter(hidden = true) HeaderContext context,
      @PathVariable @Parameter(description = "ID đơn hàng") Long id,
      @RequestParam @Parameter(description = "Trạng thái thanh toán mới")
          PaymentStatus paymentStatus) {
    OrderResponse response = orderService.updatePaymentStatus(id, paymentStatus);
    return ResponseEntity.ok(response);
  }

  @Operation(summary = "Xóa đơn hàng", description = "Xóa vĩnh viễn một đơn hàng khỏi hệ thống")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Xóa đơn hàng thành công"),
        @ApiResponse(responseCode = "404", description = "Không tìm thấy đơn hàng")
      })
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteOrder(
      @Parameter(hidden = true) HeaderContext context,
      @PathVariable @Parameter(description = "ID đơn hàng cần xóa") Long id) {
    orderService.deleteById(context, id);
    return ResponseEntity.ok().build();
  }
}
