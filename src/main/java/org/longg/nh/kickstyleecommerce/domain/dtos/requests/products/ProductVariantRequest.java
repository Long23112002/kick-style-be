package org.longg.nh.kickstyleecommerce.domain.dtos.requests.products;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductVariantRequest {

  @NotBlank(message = "Kích thước không được để trống")
  private String size;

  @DecimalMin(value = "0.0", inclusive = true, message = "Giá điều chỉnh không hợp lệ")
  private BigDecimal priceAdjustment;

  @NotBlank(message = "Mã sản phẩm biến thể không được để trống")
  private String code;

  @NotNull(message = "Số lượng trong kho không được để trống")
  @Min(value = 0, message = "Số lượng trong kho phải >= 0")
  private Integer stockQuantity;
}
