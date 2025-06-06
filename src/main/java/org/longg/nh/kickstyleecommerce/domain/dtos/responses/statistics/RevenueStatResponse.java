package org.longg.nh.kickstyleecommerce.domain.dtos.responses.statistics;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class RevenueStatResponse {
  private String period;
  private BigDecimal totalRevenue;
  private Long totalOrders;
  private String periodType; // DAY, MONTH, YEAR
} 