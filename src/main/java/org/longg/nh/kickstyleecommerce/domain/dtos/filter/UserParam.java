package org.longg.nh.kickstyleecommerce.domain.dtos.filter;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.longg.nh.kickstyleecommerce.domain.entities.enums.Gender;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserParam {
  private String fullName;

  private String email;

  private String password;

  private String phone;

  private String district;

  private String ward;

  private Long roleId;

  private Gender gender;
}
