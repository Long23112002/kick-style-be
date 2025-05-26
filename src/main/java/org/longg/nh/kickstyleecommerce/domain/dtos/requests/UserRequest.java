package org.longg.nh.kickstyleecommerce.domain.dtos.requests;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;
import org.longg.nh.kickstyleecommerce.domain.entities.Role;
import org.longg.nh.kickstyleecommerce.domain.entities.enums.Gender;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserRequest {
  private String fullName;

  private String email;

  private String password;

  private String phone;

  private String address;

  private String district;

  private String ward;

  private Long roleId;

  private String avatarUrl;

  private Gender gender;
}
