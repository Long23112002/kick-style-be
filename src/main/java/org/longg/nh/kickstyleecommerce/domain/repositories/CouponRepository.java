package org.longg.nh.kickstyleecommerce.domain.repositories;

import com.eps.shared.interfaces.repository.IBaseRepository;
import org.longg.nh.kickstyleecommerce.domain.entities.Coupon;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@Repository
public interface CouponRepository extends IBaseRepository<Coupon, Long> {
    
    Optional<Coupon> findByCode(String code);
    
    boolean existsByCode(String code);
    
    boolean existsByCodeAndIdNot(String code, Long excludeId);
    
    @Query("SELECT c FROM Coupon c WHERE c.isActive = true AND c.startDate <= :now AND c.endDate >= :now")
    List<Coupon> findActiveCoupons(@Param("now") Timestamp now);
    
    @Query("SELECT c FROM Coupon c WHERE c.code = :code AND c.isActive = true AND c.startDate <= :now AND c.endDate >= :now")
    Optional<Coupon> findValidCouponByCode(@Param("code") String code, @Param("now") Timestamp now);
    
    @Modifying
    @Transactional
    @Query("UPDATE Coupon c SET c.usedCount = c.usedCount + 1 WHERE c.id = :couponId")
    void incrementUsedCount(@Param("couponId") Long couponId);
    
    @Query("SELECT c FROM Coupon c WHERE c.isActive = true AND c.startDate <= :now AND c.endDate >= :now " +
           "AND (c.userSpecific = false OR EXISTS (SELECT cu FROM CouponUser cu WHERE cu.coupon.id = c.id AND cu.user.id = :userId))")
    List<Coupon> findValidCouponsForUser(@Param("userId") Long userId, @Param("now") Timestamp now);
} 