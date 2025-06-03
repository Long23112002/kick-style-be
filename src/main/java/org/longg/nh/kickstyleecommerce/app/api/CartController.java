package org.longg.nh.kickstyleecommerce.app.api;


import lombok.RequiredArgsConstructor;
import org.longg.nh.kickstyleecommerce.domain.entities.Cart;
import org.longg.nh.kickstyleecommerce.domain.services.cart.CartService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/carts")
@RequiredArgsConstructor
public class CartController {


    private final CartService cartService;

    @GetMapping("/{userId}")
    public Cart getCartByUserId(@PathVariable Long userId) {
        return cartService.getCartByUserId(userId);
    }

}
