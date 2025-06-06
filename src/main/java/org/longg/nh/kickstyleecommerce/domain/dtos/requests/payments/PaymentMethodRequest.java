package org.longg.nh.kickstyleecommerce.domain.dtos.requests.payments;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PaymentMethodRequest {
  
  @NotBlank(message = "Tên phương thức thanh toán không được để trống")
  private String name;
  
  private String description;
} 