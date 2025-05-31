package org.longg.nh.kickstyleecommerce.domain.dtos.requests.products;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MaterialRequest {
  @NotBlank(message = "Tên chất liệu không được để trống")
  private String name;

  private String getSlug() {
    return name != null ? name.toLowerCase().replaceAll("[^a-z0-9]+", "-") : null;
  }
}
