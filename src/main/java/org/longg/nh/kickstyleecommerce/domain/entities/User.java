package org.longg.nh.kickstyleecommerce.domain.entities;

import jakarta.persistence.*;
import jakarta.persistence.Table;
import lombok.*;
import org.hibernate.annotations.*;
import org.longg.nh.kickstyleecommerce.domain.entities.enums.Gender;

import java.sql.Timestamp;

@Entity
@Table(name = "user", schema = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Where(clause = "is_deleted = false")
@SQLDelete(sql = "UPDATE users.user SET is_deleted = true WHERE id = ?")
public class User {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "full_name", nullable = false)
  private String fullName;

  @Column(nullable = false, unique = true, name = "email")
  private String email;

  @Column(name = "password", nullable = false)
  private String password;

  @Column(name = "phone", nullable = false, unique = true)
  private String phone;

  @Column(name = "address")
  private String address;

  @Column(name = "district")
  private String district;

  @Column(name = "ward")
  private String ward;

  @ManyToOne(fetch = FetchType.LAZY)
  @NotFound(action = NotFoundAction.IGNORE)
  @JoinColumn(name = "role_id")
  private Role role;

  @Column(name = "avatar_url")
  private String avatarUrl;

  @Column(name = "gender")
  @Enumerated(EnumType.STRING)
  private Gender gender;

  @Column(name = "is_verify")
  private Boolean isVerify = false;

  @Column(name = "is_deleted")
  private Boolean isDeleted = false;

  @Column(name = "created_at", nullable = false, updatable = false)
  @CreationTimestamp
  private Timestamp createdAt;
}
