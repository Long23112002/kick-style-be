package org.longg.nh.kickstyleecommerce.domain.services.cart;

import com.eps.shared.models.exceptions.ResponseException;
import lombok.RequiredArgsConstructor;
import org.longg.nh.kickstyleecommerce.domain.entities.Cart;
import org.longg.nh.kickstyleecommerce.domain.repositories.CartRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class CartService {

  private final CartRepository cartRepository;

  public Cart getCartByUserId(Long userId) {
    return cartRepository
        .findByUserId(userId)
        .orElseThrow(
            () -> new ResponseException(HttpStatus.BAD_REQUEST, "Không tìm thấy giỏ hàng"));
  }
}
