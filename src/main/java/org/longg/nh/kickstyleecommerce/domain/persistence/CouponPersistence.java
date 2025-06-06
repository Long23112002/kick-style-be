package org.longg.nh.kickstyleecommerce.domain.persistence;

import com.eps.shared.interfaces.persistence.IBasePersistence;
import com.eps.shared.interfaces.repository.IBaseRepository;
import lombok.RequiredArgsConstructor;
import org.longg.nh.kickstyleecommerce.domain.entities.Coupon;
import org.longg.nh.kickstyleecommerce.domain.repositories.CouponRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CouponPersistence implements IBasePersistence<Coupon, Long> {

  private final CouponRepository couponRepository;

  @Override
  public IBaseRepository<Coupon, Long> getIBaseRepository() {
    return couponRepository;
  }
} 