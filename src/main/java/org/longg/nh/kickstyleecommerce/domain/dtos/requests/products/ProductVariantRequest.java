package org.longg.nh.kickstyleecommerce.domain.dtos.requests.products;

import jakarta.validation.constraints.*;
import lombok.*;
import org.longg.nh.kickstyleecommerce.domain.entities.enums.Status;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductVariantRequest {

  @DecimalMin(value = "0.0", inclusive = true, message = "Giá điều chỉnh không hợp lệ")
  private BigDecimal priceAdjustment;

  @NotBlank(message = "Mã sản phẩm biến thể không được để trống")
  private String code;

  @NotNull(message = "Số lượng trong kho không được để trống")
  @Min(value = 0, message = "Số lượng trong kho phải >= 0")
  private Integer stockQuantity;

  @NotNull(message = "ID kích thước không được để trống")
  private Long sizeId;

  // Status sẽ được tự động tính toán dựa trên stockQuantity (hết hàng -> OUT_OF_STOCK, còn hàng -> ACTIVE)
  private Status status;

  @NotNull(message = "ID màu sắc không được để trống")
  private Long colorId;

  private Boolean isEnabled = true;
}
