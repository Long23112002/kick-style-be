package org.longg.nh.kickstyleecommerce.domain.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.Where;
import org.longg.nh.kickstyleecommerce.domain.entities.enums.OrderStatus;
import org.longg.nh.kickstyleecommerce.domain.entities.enums.PaymentStatus;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;

@Entity
@Table(name = "orders", schema = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Where(clause = "is_deleted = false")
@SQLDelete(sql = "UPDATE orders.orders SET is_deleted = true WHERE id = ?")
public class Order {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  @NotFound(action = NotFoundAction.IGNORE)
  private User user;

  @Column(name = "code", nullable = false, unique = true)
  private String code;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false)
  private OrderStatus status = OrderStatus.PENDING;

  @Column(name = "customer_name", nullable = false)
  private String customerName;

  @Column(name = "customer_email", nullable = false)
  private String customerEmail;

  @Column(name = "customer_phone", nullable = false)
  private String customerPhone;

  @Column(name = "shipping_address", nullable = false, columnDefinition = "TEXT")
  private String shippingAddress;

  @Column(name = "shipping_district")
  private String shippingDistrict;

  @Column(name = "shipping_ward")
  private String shippingWard;

  @Column(name = "subtotal", nullable = false)
  private BigDecimal subtotal = BigDecimal.ZERO;

  @Column(name = "discount_amount")
  private BigDecimal discountAmount = BigDecimal.ZERO;

  @Column(name = "total_amount", nullable = false)
  private BigDecimal totalAmount = BigDecimal.ZERO;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "payment_method_id", nullable = false)
  @NotFound(action = NotFoundAction.IGNORE)
  private PaymentMethod paymentMethod;

  @Enumerated(EnumType.STRING)
  @Column(name = "payment_status", nullable = false)
  private PaymentStatus paymentStatus = PaymentStatus.PENDING;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "coupon_id")
  @NotFound(action = NotFoundAction.IGNORE)
  private Coupon coupon;

  @Column(name = "coupon_code")
  private String couponCode;

  @Column(name = "note", columnDefinition = "TEXT")
  private String note;

  @Column(name = "created_at", nullable = false, updatable = false)
  @CreationTimestamp
  private Timestamp createdAt;

  @Column(name = "updated_at", nullable = false)
  @UpdateTimestamp
  private Timestamp updatedAt;

  @Column(name = "is_deleted")
  private Boolean isDeleted = false;

  @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  private List<OrderItem> orderItems;
} 