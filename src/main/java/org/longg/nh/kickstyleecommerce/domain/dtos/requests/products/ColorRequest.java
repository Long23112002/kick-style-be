package org.longg.nh.kickstyleecommerce.domain.dtos.requests.products;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ColorRequest {
    @NotBlank(message = "Tên màu sắc không được để trống")
    private String name;

    private String hexColor;
}
