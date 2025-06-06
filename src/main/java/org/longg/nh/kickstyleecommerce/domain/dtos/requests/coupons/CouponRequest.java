package org.longg.nh.kickstyleecommerce.domain.dtos.requests.coupons;

import jakarta.validation.constraints.*;
import lombok.*;
import org.longg.nh.kickstyleecommerce.domain.entities.enums.DiscountType;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CouponRequest {

  @NotBlank(message = "Mã coupon không được để trống")
  @Size(max = 50, message = "Mã coupon không được vượt quá 50 ký tự")
  @Pattern(regexp = "^[A-Z0-9]+$", message = "Mã coupon chỉ được chứa chữ in hoa và số")
  private String code;
  
  @Size(max = 255, message = "Tên không được vượt quá 255 ký tự")
  private String name;

  @Size(max = 500, message = "Mô tả không được vượt quá 500 ký tự")
  private String description;

  @NotNull(message = "Loại giảm giá không được để trống")
  private DiscountType discountType;

  @NotNull(message = "Giá trị giảm giá không được để trống")
  @DecimalMin(value = "0.01", message = "Giá trị giảm giá phải lớn hơn 0")
  private BigDecimal discountValue;

  @DecimalMin(value = "0", message = "Số tiền tối thiểu không được âm")
  private BigDecimal minimumAmount;

  @DecimalMin(value = "0.01", message = "Giảm giá tối đa phải lớn hơn 0")
  private BigDecimal maximumDiscount;

  @Min(value = 1, message = "Giới hạn sử dụng phải lớn hơn 0")
  private Integer maxUsageCount;

  @NotNull(message = "Ngày bắt đầu không được để trống")
  private Timestamp validFrom;

  @NotNull(message = "Ngày kết thúc không được để trống")
  private Timestamp validTo;

  private Boolean userSpecific = false;

  private List<Long> userIds;
} 