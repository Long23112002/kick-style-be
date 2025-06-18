package org.longg.nh.kickstyleecommerce.domain.dtos.responses.orders;

import lombok.*;
import org.longg.nh.kickstyleecommerce.domain.entities.Product;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItemResponse {

  private Long id;
  private Long variantId;
  private String productName;
  private Map<String, Object> variantInfo;
  private Integer quantity;
  private Long productId;
  private BigDecimal unitPrice;
  private BigDecimal totalPrice;
  private Timestamp createdAt;
} 