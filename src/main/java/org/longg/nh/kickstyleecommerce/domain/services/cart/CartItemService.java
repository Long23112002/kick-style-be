package org.longg.nh.kickstyleecommerce.domain.services.cart;

import com.eps.shared.models.exceptions.ResponseException;
import lombok.RequiredArgsConstructor;
import org.longg.nh.kickstyleecommerce.domain.dtos.requests.carts.CartItemRequest;
import org.longg.nh.kickstyleecommerce.domain.entities.Cart;
import org.longg.nh.kickstyleecommerce.domain.entities.CartItem;
import org.longg.nh.kickstyleecommerce.domain.entities.ProductVariant;
import org.longg.nh.kickstyleecommerce.domain.repositories.CartItemRepository;
import org.longg.nh.kickstyleecommerce.domain.repositories.CartRepository;
import org.longg.nh.kickstyleecommerce.domain.repositories.ProductVariantRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class CartItemService {

  private final CartItemRepository cartItemRepository;
  private final CartRepository cartRepository;
  private final ProductVariantRepository productVariantRepository;

  public CartItem create(CartItemRequest request) {
    CartItem cartItem = new CartItem();
    cartItem.setCartId(findById(request.getCartId()).getId());
    cartItem.setVariantId(findVariantById(request.getVariantId()).getId());
    cartItem.setQuantity(request.getQuantity());
    return cartItemRepository.save(cartItem);
  }

  public void delete(Long id) {
    CartItem cartItem =
        cartItemRepository
            .findById(id)
            .orElseThrow(
                () ->
                    new ResponseException(
                        HttpStatus.BAD_REQUEST, "Cart item not found with id: " + id));
    cartItemRepository.delete(cartItem);
  }

  public CartItem update(Long id, CartItemRequest request) {
    CartItem cartItem =
        cartItemRepository
            .findById(id)
            .orElseThrow(
                () ->
                    new ResponseException(
                        HttpStatus.BAD_REQUEST, "Cart item not found with id: " + id));
    cartItem.setQuantity(request.getQuantity());
    return cartItemRepository.save(cartItem);
  }

  public Cart findById(Long id) {
    return cartRepository
        .findById(id)
        .orElseThrow(
            () -> new ResponseException(HttpStatus.BAD_REQUEST, "Cart not found with id: " + id));
  }

  public ProductVariant findVariantById(Long id) {
    return productVariantRepository
        .findById(id)
        .orElseThrow(
            () ->
                new ResponseException(
                    HttpStatus.BAD_REQUEST, "Product variant not found with id: " + id));
  }
}
