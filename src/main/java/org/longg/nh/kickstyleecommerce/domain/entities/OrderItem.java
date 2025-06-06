package org.longg.nh.kickstyleecommerce.domain.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Map;

@Entity
@Table(name = "order_items", schema = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItem {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "order_id", nullable = false)
  @NotFound(action = NotFoundAction.IGNORE)
  private Order order;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "variant_id", nullable = false)
  @NotFound(action = NotFoundAction.IGNORE)
  private ProductVariant variant;

  @Column(name = "product_name", nullable = false)
  private String productName;

  @Column(name = "variant_info")
  @JdbcTypeCode(SqlTypes.JSON)
  private Map<String, Object> variantInfo;

  @Column(name = "quantity", nullable = false)
  private Integer quantity;

  @Column(name = "unit_price", nullable = false)
  private BigDecimal unitPrice;

  @Column(name = "created_at", nullable = false, updatable = false)
  @CreationTimestamp
  private Timestamp createdAt;

  // Helper method to calculate total price
  public BigDecimal getTotalPrice() {
    return unitPrice.multiply(BigDecimal.valueOf(quantity));
  }
} 