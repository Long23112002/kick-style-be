package org.longg.nh.kickstyleecommerce.domain.dtos.responses.statistics;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class ProductSalesResponse {
  private Long productId;
  private String productName;
  private Long totalQuantitySold;
  private BigDecimal totalRevenue;
  private String period;
  private String periodType; // DAY, MONTH, YEAR
} 