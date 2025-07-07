package org.longg.nh.kickstyleecommerce.domain.services.cart;

import com.eps.shared.models.exceptions.ResponseException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.longg.nh.kickstyleecommerce.domain.dtos.requests.carts.CartItemRequest;
import org.longg.nh.kickstyleecommerce.domain.entities.Cart;
import org.longg.nh.kickstyleecommerce.domain.entities.CartItem;
import org.longg.nh.kickstyleecommerce.domain.entities.ProductVariant;
import org.longg.nh.kickstyleecommerce.domain.repositories.CartItemRepository;
import org.longg.nh.kickstyleecommerce.domain.repositories.CartRepository;
import org.longg.nh.kickstyleecommerce.domain.repositories.ProductVariantRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
@Slf4j
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

  /**
   * Cập nhật số lượng sản phẩm trong các giỏ hàng khi số lượng tồn kho của variant thay đổi
   * @param variantId ID của variant
   * @param newStockQuantity Số lượng tồn kho mới
   */
  @Transactional
  public void updateCartItemsForVariantStockChange(Long variantId, Integer newStockQuantity) {
    log.info("Updating cart items for variant ID {} with new stock quantity {}", variantId, newStockQuantity);
    
    if (newStockQuantity <= 0) {
      // Nếu hết hàng, xóa variant khỏi tất cả giỏ hàng
      deleteAllByVariantId(variantId);
      log.info("Variant ID {} out of stock, removed from all carts", variantId);
      return;
    }
    
    // Lấy tất cả cart items chứa variant này
    List<CartItem> cartItems = cartItemRepository.findByVariantId(variantId);
    
    if (cartItems.isEmpty()) {
      log.info("No cart items found containing variant ID {}", variantId);
      return;
    }
    
    log.info("Found {} cart items containing variant ID {}", cartItems.size(), variantId);
    
    // Cập nhật số lượng trong cart items nếu vượt quá số lượng tồn kho mới
    for (CartItem cartItem : cartItems) {
      if (cartItem.getQuantity() > newStockQuantity) {
        log.info("Adjusting cart item ID {} quantity from {} to {}", 
                 cartItem.getId(), cartItem.getQuantity(), newStockQuantity);
        cartItem.setQuantity(newStockQuantity);
        cartItemRepository.save(cartItem);
      }
    }
    
    log.info("Successfully updated cart items for variant ID {}", variantId);
  }

  /**
   * Xóa tất cả các CartItem có chứa một variant cụ thể khỏi tất cả giỏ hàng
   */
  @Transactional
  public void deleteAllByVariantId(Long variantId) {
    log.info("Deleting all cart items with variant ID: {}", variantId);
    cartItemRepository.deleteAllByVariantId(variantId);
    log.info("Successfully deleted all cart items with variant ID: {}", variantId);
  }

  /**
   * Xóa tất cả các CartItem có chứa sản phẩm cụ thể khỏi tất cả giỏ hàng
   */
  @Transactional
  public void deleteAllByProductId(Long productId) {
    log.info("Deleting all cart items with product ID: {}", productId);
    cartItemRepository.deleteAllByProductId(productId);
    log.info("Successfully deleted all cart items with product ID: {}", productId);
  }
}
