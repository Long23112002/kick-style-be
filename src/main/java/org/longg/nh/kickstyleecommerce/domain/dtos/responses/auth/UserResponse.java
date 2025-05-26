package org.longg.nh.kickstyleecommerce.domain.dtos.responses.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.longg.nh.kickstyleecommerce.domain.entities.enums.Gender;

import java.sql.Timestamp;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserResponse {

  private Long id;
  private String fullName;
  private String email;
  private String phone;
  private String address;
  private String district;
  private String ward;
  private String roleName;
  private String avatarUrl;
  private Gender gender;
  private Boolean isVerify;
  private Timestamp createdAt;
  private Timestamp updatedAt;
}
