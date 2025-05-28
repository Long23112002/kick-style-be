package org.longg.nh.kickstyleecommerce.domain.dtos.responses.products;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;
import org.hibernate.annotations.UpdateTimestamp;
import org.longg.nh.kickstyleecommerce.domain.entities.Category;
import org.longg.nh.kickstyleecommerce.domain.entities.Material;
import org.longg.nh.kickstyleecommerce.domain.entities.ProductVariant;
import org.longg.nh.kickstyleecommerce.domain.entities.Team;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductResponse {
  private Long id;

  private String name;

  private String slug;

  private Category category;

  private List<String> imageUrls;

  private Team team;

  private Material material;

  private String season;

  private String jerseyType;

  private Boolean isFeatured;

  private String code;

  private String description;

  private BigDecimal price;

  private BigDecimal salePrice;

  private Timestamp createdAt;

  private Timestamp updatedAt;

  private Boolean isDeleted;

  private List<ProductVariant> variants;
}
