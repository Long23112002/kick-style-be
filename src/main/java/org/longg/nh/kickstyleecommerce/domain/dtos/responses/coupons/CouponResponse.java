package org.longg.nh.kickstyleecommerce.domain.dtos.responses.coupons;

import lombok.*;
import org.longg.nh.kickstyleecommerce.domain.entities.enums.DiscountType;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CouponResponse {

  private Long id;
  private String code;
  private String name;
  private String description;
  private DiscountType discountType;
  private BigDecimal discountValue;
  private BigDecimal minimumAmount;
  private BigDecimal maximumDiscount;
  private Integer usageLimit;
  private Integer maxUsageCount;
  private Integer usedCount;
  private Timestamp startDate;
  private Timestamp endDate;
  private Timestamp validFrom;
  private Timestamp validTo;
  private Boolean isActive;
  private Boolean userSpecific;
  private List<Long> userIds;
  private Timestamp createdAt;
  private Timestamp updatedAt;
} 