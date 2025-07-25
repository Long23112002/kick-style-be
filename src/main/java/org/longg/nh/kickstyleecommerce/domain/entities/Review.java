package org.longg.nh.kickstyleecommerce.domain.entities;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.*;
import org.hibernate.type.SqlTypes;

import java.sql.Timestamp;
import java.util.List;

@Entity
@Table(name = "reviews", schema = "reviews")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Where(clause = "is_deleted = false")
@SQLDelete(sql = "UPDATE reviews.reviews SET is_deleted = true WHERE id = ?")
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Review {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  @NotFound(action = NotFoundAction.IGNORE)
  @JsonIgnoreProperties({"reviews", "orders", "answers"})
  private User user;

  @Column(name = "product_id", nullable = false)
  private Long productId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "order_id", nullable = false)
  @NotFound(action = NotFoundAction.IGNORE)
  @JsonIgnoreProperties({"reviews", "orderItems", "user"})
  private Order order;

  @Column(name = "rating", nullable = false)
  private Integer rating;

  @Column(name = "comment", length = 1000)
  private String comment;

  @Column(name = "images")
  @JdbcTypeCode(SqlTypes.JSON)
  private List<String> images;

  @Column(name = "created_at", nullable = false, updatable = false)
  @CreationTimestamp
  private Timestamp createdAt;

  @Column(name = "updated_at", nullable = false)
  @UpdateTimestamp
  private Timestamp updatedAt;

  @Column(name = "is_deleted")
  private Boolean isDeleted = false;

  private Boolean isAdmin = false;

  @OneToMany(mappedBy = "review", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  @JsonIgnoreProperties({"review", "user"})
  @JsonManagedReference
  private List<Answers> answers;
}
