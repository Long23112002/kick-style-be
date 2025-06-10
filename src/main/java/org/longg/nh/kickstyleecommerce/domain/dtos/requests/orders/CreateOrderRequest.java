package org.longg.nh.kickstyleecommerce.domain.dtos.requests.orders;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateOrderRequest {

  @NotNull(message = "Phương thức thanh toán không được để trống")
  private Long paymentMethodId;

  private Long cartId;

  @NotBlank(message = "Tên khách hàng không được để trống")
  @Size(max = 255, message = "Tên khách hàng không được vượt quá 255 ký tự")
  private String customerName;

  @NotBlank(message = "Email khách hàng không được để trống")
  @Email(message = "Email không hợp lệ")
  private String customerEmail;

  @NotBlank(message = "Số điện thoại không được để trống")
  @Pattern(regexp = "^[0-9+\\-\\s()]+$", message = "Số điện thoại không hợp lệ")
  private String customerPhone;

  @NotBlank(message = "Địa chỉ giao hàng không được để trống")
  private String shippingAddress;

  private String shippingDistrict;

  private String shippingWard;

  @Size(max = 500, message = "Ghi chú không được vượt quá 500 ký tự")
  private String note;

  private String couponCode;

  @NotEmpty(message = "Danh sách sản phẩm không được rỗng")
  @Valid
  private List<OrderItemRequest> items;

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  public static class OrderItemRequest {
    
    @NotNull(message = "ID variant không được để trống")
    private Long variantId;

    @NotNull(message = "Số lượng không được để trống")
    @Min(value = 1, message = "Số lượng phải lớn hơn 0")
    private Integer quantity;
  }
} 