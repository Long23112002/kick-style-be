package org.longg.nh.kickstyleecommerce.domain.persistence;

import com.eps.shared.interfaces.persistence.IBasePersistence;
import com.eps.shared.interfaces.repository.IBaseRepository;
import lombok.RequiredArgsConstructor;
import org.longg.nh.kickstyleecommerce.domain.entities.PaymentMethod;
import org.longg.nh.kickstyleecommerce.domain.repositories.PaymentMethodRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentMethodPersistence implements IBasePersistence<PaymentMethod, Long> {

  private final PaymentMethodRepository paymentMethodRepository;

  @Override
  public IBaseRepository<PaymentMethod, Long> getIBaseRepository() {
    return paymentMethodRepository;
  }
} 