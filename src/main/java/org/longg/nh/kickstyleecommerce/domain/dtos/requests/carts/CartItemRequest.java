package org.longg.nh.kickstyleecommerce.domain.dtos.requests.carts;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CartItemRequest {

  private Long cartId;

  private Long variantId;

  private Integer quantity;
}
