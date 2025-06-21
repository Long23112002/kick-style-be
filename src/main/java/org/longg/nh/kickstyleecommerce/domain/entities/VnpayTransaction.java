package org.longg.nh.kickstyleecommerce.domain.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

import java.math.BigDecimal;
import java.sql.Timestamp;

@Entity
@Table(name = "vnpay_transactions", schema = "reviews")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VnpayTransaction {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "transaction_code", length = 255)
  private String transactionCode;

  @Column(name = "bank_code", length = 50)
  private String bankCode;

  @Column(name = "payment_method", length = 50)
  private String paymentMethod;

  @Column(name = "card_type", length = 50)
  private String cardType;

  @Column(name = "amount", precision = 18, scale = 2)
  private BigDecimal amount;

  @Column(name = "currency", length = 10)
  private String currency;

  @Column(name = "status", length = 10)
  private String status;

  @Column(name = "order_info", columnDefinition = "TEXT")
  private String orderInfo;

  @Column(name = "pay_date", length = 20)
  private String payDate;

  @Column(name = "response_code", length = 10)
  private String responseCode;

  @Column(name = "tmn_code", length = 50)
  private String tmnCode;

  @Column(name = "secure_hash", columnDefinition = "TEXT")
  private String secureHash;

  @JoinColumn(name = "order_id")
  @ManyToOne(fetch = FetchType.LAZY)
  @NotFound(action = NotFoundAction.IGNORE)
  @JsonIgnoreProperties({"hibernateLazyInitializer", "handler" , "orderItems"})
  private Order order;

  @Column(name = "created_at")
  @CreationTimestamp
  private Timestamp createdAt;
}
