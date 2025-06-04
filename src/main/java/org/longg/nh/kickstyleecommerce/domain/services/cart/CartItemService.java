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

import java.util.Optional;

@RequiredArgsConstructor
@Service
public class CartItemService {

  private final CartItemRepository cartItemRepository;
  private final CartRepository cartRepository;
  private final ProductVariantRepository productVariantRepository;

  public CartItem create(CartItemRequest request) {
    ProductVariant productVariant = findVariantById(request.getVariantId());

    if (productVariant.getStockQuantity() < request.getQuantity()) {
      throw new ResponseException(
              HttpStatus.BAD_REQUEST, "Không đủ số lượng");
    }

    Optional<CartItem> existingItemOpt = cartItemRepository
            .findByCartIdAndVariantId(request.getCartId(), request.getVariantId());

    if (existingItemOpt.isPresent()) {
      CartItem existingItem = existingItemOpt.get();
      int newQuantity = existingItem.getQuantity() + request.getQuantity();

      if (productVariant.getStockQuantity() < newQuantity) {
        throw new ResponseException(
                HttpStatus.BAD_REQUEST, "Không đủ số lượng sau khi cộng dồn");
      }

      existingItem.setQuantity(newQuantity);
      return cartItemRepository.save(existingItem);
    }

    CartItem cartItem = new CartItem();
    cartItem.setCartId(request.getCartId());
    cartItem.setVariant(productVariant);
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
    ProductVariant productVariant = findVariantById(request.getVariantId());

    if (productVariant.getStockQuantity() < request.getQuantity()) {
      throw new ResponseException(
              HttpStatus.BAD_REQUEST, "Không đủ số lượng: " + request.getVariantId());
    }

    CartItem cartItem = cartItemRepository.findById(id)
            .orElseThrow(() -> new ResponseException(
                    HttpStatus.BAD_REQUEST, "Cart item not found with id: " + id));

    // Nếu variantId không đổi → chỉ update quantity
    if (cartItem.getVariant().getId().equals(request.getVariantId())) {
      cartItem.setQuantity(request.getQuantity());
      return cartItemRepository.save(cartItem);
    }

    // Nếu variantId thay đổi → kiểm tra xem đã tồn tại item khác chưa
    Optional<CartItem> existingOpt = cartItemRepository
            .findByCartIdAndVariantId(cartItem.getCartId(), request.getVariantId());

    if (existingOpt.isPresent()) {
      CartItem existing = existingOpt.get();
      int newQuantity = existing.getQuantity() + request.getQuantity();

      if (productVariant.getStockQuantity() < newQuantity) {
        throw new ResponseException(
                HttpStatus.BAD_REQUEST, "Không đủ số lượng sau khi gộp");
      }

      // Cập nhật item đã tồn tại với tổng số lượng
      existing.setQuantity(newQuantity);
      cartItemRepository.delete(cartItem); // xóa item cũ (ID cũ)
      return cartItemRepository.save(existing);
    }

    // Nếu chưa tồn tại item với variantId mới → cập nhật variant và quantity
    cartItem.setVariant(productVariant);
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
