package org.longg.nh.kickstyleecommerce.domain.dtos.requests.products;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TeamRequest {

  @NotBlank(message = "Tên đội không được để trống")
  private String name;

  @NotBlank(message = "Giải đấu không được để trống")
  private String league;

  @NotBlank(message = "Quốc gia không được để trống")
  private String country;

  @NotBlank(message = "URL logo không được để trống")
  private String logoUrl;
}
