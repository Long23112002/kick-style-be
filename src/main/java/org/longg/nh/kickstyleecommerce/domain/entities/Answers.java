package org.longg.nh.kickstyleecommerce.domain.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
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
@Table(name = "answers", schema = "reviews")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Where(clause = "is_deleted = false")
@SQLDelete(sql = "UPDATE reviews.answers SET is_deleted = true WHERE id = ?")
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Answers {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  @NotFound(action = NotFoundAction.IGNORE)
  @JsonIgnoreProperties({"reviews", "orders", "answers"})
  private User user;

  @Column(name = "images")
  @JdbcTypeCode(SqlTypes.JSON)
  private List<String> images;

  @Column(name = "answer", nullable = false)
  private String answer;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "review_id", nullable = false)
  @NotFound(action = NotFoundAction.IGNORE)
  @JsonBackReference
  private Review review;

  @Column(name = "created_at", nullable = false, updatable = false)
  @CreationTimestamp
  private Timestamp createdAt;

  @Column(name = "updated_at", nullable = false)
  @UpdateTimestamp
  private Timestamp updatedAt;

  @Column(name = "is_deleted")
  private Boolean isDeleted = false;
}
