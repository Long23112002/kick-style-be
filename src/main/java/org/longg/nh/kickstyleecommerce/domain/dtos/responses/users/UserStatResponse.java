package org.longg.nh.kickstyleecommerce.domain.dtos.responses.users;

import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserStatResponse {

  private Long userId;
  private String fullName;
  private String email;
  private BigDecimal totalSpent;
  private Long totalOrders;
} 