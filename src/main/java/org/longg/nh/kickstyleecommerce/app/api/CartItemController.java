package org.longg.nh.kickstyleecommerce.app.api;

import lombok.RequiredArgsConstructor;
import org.longg.nh.kickstyleecommerce.domain.dtos.requests.carts.CartItemRequest;
import org.longg.nh.kickstyleecommerce.domain.entities.CartItem;
import org.longg.nh.kickstyleecommerce.domain.services.cart.CartItemService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/cart_items")
@RequiredArgsConstructor
public class CartItemController {

  private final CartItemService cartItemService;

  @PostMapping
  public CartItem create( CartItemRequest request) {
    return cartItemService.create(request);
  }

  @PutMapping("/{id}")
  public CartItem update(@PathVariable Long id, CartItemRequest request) {
    return cartItemService.update(id, request);
  }

  @DeleteMapping("/{id}")
  public void delete(@PathVariable Long id) {
    cartItemService.delete(id);
  }
}
