package org.longg.nh.kickstyleecommerce.domain.dtos.responses.orders;

import lombok.*;
import org.longg.nh.kickstyleecommerce.domain.dtos.responses.coupons.CouponResponse;
import org.longg.nh.kickstyleecommerce.domain.entities.Coupon;
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
  private Long userId;
  private String userFullName;
  private String userEmail;
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
  private Long paymentMethodId;
  private String paymentMethodName;
  private PaymentStatus paymentStatus;
  private Double totalDiscount;
  private String note;
  private Timestamp createdAt;
  private Timestamp updatedAt;
  private Boolean isReviewed;
  private CouponResponse coupon;
  private List<OrderItemResponse> orderItems;
} 