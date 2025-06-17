package org.longg.nh.kickstyleecommerce.domain.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import lombok.*;
import jakarta.persistence.*;
import org.hibernate.annotations.*;
import org.longg.nh.kickstyleecommerce.domain.entities.enums.Status;

import java.math.BigDecimal;
import java.sql.Timestamp;

@Entity
@Table(name = "product_variants", schema = "products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Where(clause = "is_deleted = false")
@SQLDelete(sql = "UPDATE products.product_variants SET is_deleted = true WHERE id = ?")
public class ProductVariant {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "product_id", nullable = false)
  @NotFound(action = NotFoundAction.IGNORE)
  private Product product;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "size_id", nullable = false)
  @NotFound(action = NotFoundAction.IGNORE)
  private Sizes size;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "color_id", nullable = false)
  @NotFound(action = NotFoundAction.IGNORE)
  private Colors color;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false)
  private Status status;

  @Column(name = "price_adjustment")
  private BigDecimal priceAdjustment;

  @Column(name = "stock_quantity")
  private Integer stockQuantity;

  @Column(name = "created_at", nullable = false, updatable = false)
  @CreationTimestamp
  private Timestamp createdAt;

  @Column(name = "updated_at", nullable = false)
  @UpdateTimestamp
  private Timestamp updatedAt;

  @Column(name = "is_deleted")
  private Boolean isDeleted = false;
}
