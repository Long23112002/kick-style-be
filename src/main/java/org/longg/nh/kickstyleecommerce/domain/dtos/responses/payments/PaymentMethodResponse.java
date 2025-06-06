package org.longg.nh.kickstyleecommerce.domain.dtos.responses.payments;

import lombok.Builder;
import lombok.Data;

import java.sql.Timestamp;

@Data
@Builder
public class PaymentMethodResponse {
  private Long id;
  private String name;
  private String description;
  private Boolean isActive;
  private Timestamp createdAt;
  private Timestamp updatedAt;
} 