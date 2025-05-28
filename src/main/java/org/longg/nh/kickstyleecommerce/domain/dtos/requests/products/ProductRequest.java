package org.longg.nh.kickstyleecommerce.domain.dtos.requests.products;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductRequest {

  @NotBlank(message = "Tên sản phẩm không được để trống")
  private String name;

  @NotNull(message = "Danh mục không được để trống")
  private Long categoryId;

//  @NotBlank(message = "URL hình ảnh không được để trống")
  private List<String> imageUrls;

  @NotNull(message = "ID đội bóng không được để trống")
  private Long teamId;

  @NotNull(message = "ID chất liệu không được để trống")
  private Long materialId;

  private String season;

  private String jerseyType;

  private Boolean isFeatured = false;

  private String description;

  @NotNull(message = "Giá sản phẩm không được để trống")
  @DecimalMin(value = "0.0", inclusive = false, message = "Giá phải lớn hơn 0")
  private BigDecimal price;

  @DecimalMin(value = "0.0", inclusive = true, message = "Giá khuyến mãi không hợp lệ")
  private BigDecimal salePrice;

  @Size(min = 1, message = "Danh sách biến thể sản phẩm không được rỗng")
  private List<ProductVariantRequest> variants;
}
