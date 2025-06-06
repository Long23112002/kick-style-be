package org.longg.nh.kickstyleecommerce.domain.persistence;

import com.eps.shared.interfaces.persistence.IBasePersistence;
import com.eps.shared.interfaces.repository.IBaseRepository;
import lombok.RequiredArgsConstructor;
import org.longg.nh.kickstyleecommerce.domain.entities.Order;
import org.longg.nh.kickstyleecommerce.domain.repositories.OrderRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderPersistence implements IBasePersistence<Order, Long> {

  private final OrderRepository orderRepository;

  @Override
  public IBaseRepository<Order, Long> getIBaseRepository() {
    return orderRepository;
  }
} 