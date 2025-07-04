package org.longg.nh.kickstyleecommerce.app.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.longg.nh.kickstyleecommerce.domain.dtos.requests.carts.CartItemRequest;
import org.longg.nh.kickstyleecommerce.domain.entities.CartItem;
import org.longg.nh.kickstyleecommerce.domain.services.cart.CartItemService;
import org.longg.nh.kickstyleecommerce.infrastructure.config.annotation.CheckRole;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/cart_items")
@RequiredArgsConstructor
public class CartItemController {

  private final CartItemService cartItemService;

  @PostMapping
  public CartItem create(CartItemRequest request) {
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
  
  @DeleteMapping("/product/{productId}")
  @Operation(summary = "Xóa tất cả các cart item có chứa sản phẩm với ID cụ thể")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Xóa thành công"),
      @ApiResponse(responseCode = "400", description = "Lỗi khi xóa")
  })
  @CheckRole({"ADMIN"})
  public ResponseEntity<Void> deleteAllByProductId(@PathVariable Long productId) {
    cartItemService.deleteAllByProductId(productId);
    return ResponseEntity.ok().build();
  }
  
  @DeleteMapping("/variant/{variantId}")
  @Operation(summary = "Xóa tất cả các cart item có chứa biến thể sản phẩm với ID cụ thể")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Xóa thành công"),
      @ApiResponse(responseCode = "400", description = "Lỗi khi xóa")
  })
  @CheckRole({"ADMIN"})
  public ResponseEntity<Void> deleteAllByVariantId(@PathVariable Long variantId) {
    cartItemService.deleteAllByVariantId(variantId);
    return ResponseEntity.ok().build();
  }
}
