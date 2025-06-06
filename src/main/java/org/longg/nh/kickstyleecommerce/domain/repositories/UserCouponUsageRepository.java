package org.longg.nh.kickstyleecommerce.domain.repositories;

import com.eps.shared.interfaces.repository.IBaseRepository;
import org.longg.nh.kickstyleecommerce.domain.entities.UserCouponUsage;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserCouponUsageRepository extends IBaseRepository<UserCouponUsage, Long> {
    
    boolean existsByCouponIdAndUserId(Long couponId, Long userId);
    
    List<UserCouponUsage> findByUserId(Long userId);
    
    List<UserCouponUsage> findByCouponId(Long couponId);
    
    List<UserCouponUsage> findByOrderId(Long orderId);
} 