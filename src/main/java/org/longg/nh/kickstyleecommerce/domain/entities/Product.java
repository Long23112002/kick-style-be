package org.longg.nh.kickstyleecommerce.domain.entities;

import jakarta.persistence.Table;
import lombok.*;
import jakarta.persistence.*;
import org.hibernate.annotations.*;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;

@Entity
@Table(name = "products", schema = "products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Where(clause = "is_deleted = false")
@SQLDelete(sql = "UPDATE products SET is_deleted = true WHERE id = ?")
public class Product {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "name", nullable = false)
  private String name;

  @Column(name = "slug", unique = true, nullable = false)
  private String slug;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "category_id", nullable = false)
  @NotFound(action = NotFoundAction.IGNORE)
  private Category category;

  @Column(name = "image_urls")
  @JdbcTypeCode(SqlTypes.JSON)
  private List<String> imageUrls;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "team_id", nullable = false)
  @NotFound(action = NotFoundAction.IGNORE)
  private Team team;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "material_id", nullable = false)
  @NotFound(action = NotFoundAction.IGNORE)
  private Material material;

  private String season;

  @Column(name = "jersey_type")
  private String jerseyType;

  @Column(name = "is_featured")
  private Boolean isFeatured = false;

  private String code;

  private String description;

  private BigDecimal price;

  @Column(name = "sale_price")
  private BigDecimal salePrice;

  @Column(name = "created_at", nullable = false, updatable = false)
  @CreationTimestamp
  private Timestamp createdAt;

  @Column(name = "updated_at", nullable = false)
  @UpdateTimestamp
  private Timestamp updatedAt;

  @Column(name = "is_deleted")
  private Boolean isDeleted = false;

}
