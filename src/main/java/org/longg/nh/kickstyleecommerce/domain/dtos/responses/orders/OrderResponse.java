package org.longg.nh.kickstyleecommerce.domain.dtos.responses.orders;

import lombok.*;
import org.longg.nh.kickstyleecommerce.domain.entities.PaymentMethod;
import org.longg.nh.kickstyleecommerce.domain.entities.User;
import org.longg.nh.kickstyleecommerce.domain.entities.enums.OrderStatus;
import org.longg.nh.kickstyleecommerce.domain.entities.enums.PaymentStatus;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderResponse {

  private Long id;
  private User user;
  private String code;
  private OrderStatus status;
  private String customerName;
  private String customerEmail;
  private String customerPhone;
  private String shippingAddress;
  private String shippingDistrict;
  private String shippingWard;
  private BigDecimal subtotal;
  private BigDecimal discountAmount;
  private BigDecimal totalAmount;
  private PaymentMethod paymentMethod;
  private PaymentStatus paymentStatus;
  private String couponCode;
  private String note;
  private Timestamp createdAt;
  private Timestamp updatedAt;
  private List<OrderItemResponse> orderItems;
} 