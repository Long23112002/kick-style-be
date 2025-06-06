package org.longg.nh.kickstyleecommerce.domain.dtos.requests.products;

import jakarta.validation.constraints.NotBlank;

public class ColorRequest {
    @NotBlank(message = "Tên màu sắc không được để trống")
    private String name;
}
