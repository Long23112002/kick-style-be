package org.longg.nh.kickstyleecommerce.domain.dtos.filter;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TeamParam {
  private String name;
  private String league;
  private String country;
}
