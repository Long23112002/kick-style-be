package org.longg.nh.kickstyleecommerce.domain.repositories;

import com.eps.shared.interfaces.repository.IBaseRepository;
import org.longg.nh.kickstyleecommerce.domain.entities.Coupon;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

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

    // Queries for scheduler service
    @Query("SELECT c FROM Coupon c WHERE c.isActive = true AND c.endDate < :now")
    List<Coupon> findExpiredActiveCoupons(@Param("now") Timestamp now);
    
    @Query("SELECT c FROM Coupon c WHERE c.isActive = true AND c.maxUsageCount IS NOT NULL AND c.usedCount >= c.maxUsageCount")
    List<Coupon> findMaxUsedActiveCoupons();

    // Paginated queries
    @Query("SELECT c FROM Coupon c WHERE c.isActive = true AND c.startDate <= :now AND c.endDate >= :now")
    Page<Coupon> findActiveCoupons(@Param("now") Timestamp now, Pageable pageable);
    
    @Query("SELECT c FROM Coupon c WHERE c.isActive = true AND c.startDate <= :now AND c.endDate >= :now " +
           "AND (c.userSpecific = false OR EXISTS (SELECT cu FROM CouponUser cu WHERE cu.coupon.id = c.id AND cu.user.id = :userId))")
    Page<Coupon> findValidCouponsForUser(@Param("userId") Long userId, @Param("now") Timestamp now, Pageable pageable);

    // Search and filter queries
    @Query("SELECT c FROM Coupon c WHERE " +
           "(:code IS NULL OR UPPER(c.code) LIKE UPPER(CONCAT('%', :code, '%'))) AND " +
           "(:name IS NULL OR UPPER(c.name) LIKE UPPER(CONCAT('%', :name, '%'))) AND " +
           "(:isActive IS NULL OR c.isActive = :isActive)")
    Page<Coupon> findCouponsWithFilters(@Param("code") String code, 
                                       @Param("name") String name, 
                                       @Param("isActive") Boolean isActive, 
                                       Pageable pageable);
                                       
    @Query("SELECT c FROM Coupon c WHERE " +
           "c.isActive = true AND " +
           "c.startDate <= :now AND c.endDate >= :now AND " +
           "(:code IS NULL OR UPPER(c.code) LIKE UPPER(CONCAT('%', :code, '%'))) AND " +
           "(:name IS NULL OR UPPER(c.name) LIKE UPPER(CONCAT('%', :name, '%')))")
    Page<Coupon> findActiveCouponsWithFilters(@Param("code") String code,
                                             @Param("name") String name,
                                             @Param("now") Timestamp now,
                                             Pageable pageable);
} 