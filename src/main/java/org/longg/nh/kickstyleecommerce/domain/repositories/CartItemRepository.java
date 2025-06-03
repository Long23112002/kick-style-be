package org.longg.nh.kickstyleecommerce.domain.repositories;

import com.eps.shared.interfaces.repository.IBaseRepository;
import org.longg.nh.kickstyleecommerce.domain.entities.CartItem;
import org.springframework.stereotype.Repository;

@Repository
public interface CartItemRepository extends IBaseRepository<CartItem, Long> {}
