package org.longg.nh.kickstyleecommerce.domain.dtos.requests.review;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.longg.nh.kickstyleecommerce.domain.entities.Product;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReviewRequest {
  @NotNull(message = "Người dùng không được để trống")
  private Long userId;

  @NotNull(message = "Don Hang không được để trống")
  private Long orderId;

  private Integer rating;

  private Long productId;

  @NotBlank(message = "Comment không được để trống")
  private String comment;

  private List<String> images;
}
