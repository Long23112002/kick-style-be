package org.longg.nh.kickstyleecommerce.domain.repositories;

import com.eps.shared.interfaces.repository.IBaseRepository;
import org.longg.nh.kickstyleecommerce.domain.entities.CouponUser;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CouponUserRepository extends IBaseRepository<CouponUser, Long> {
    
    List<CouponUser> findByCouponId(Long couponId);
    
    List<CouponUser> findByUserId(Long userId);
    
    boolean existsByCouponIdAndUserId(Long couponId, Long userId);
    
    void deleteByCouponId(Long couponId);
    
    @Query("SELECT cu.user.id FROM CouponUser cu WHERE cu.coupon.id = :couponId")
    List<Long> findUserIdsByCouponId(@Param("couponId") Long couponId);
} 