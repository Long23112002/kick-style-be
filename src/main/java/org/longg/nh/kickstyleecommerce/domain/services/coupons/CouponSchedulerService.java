package org.longg.nh.kickstyleecommerce.domain.services.coupons;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.longg.nh.kickstyleecommerce.domain.entities.Coupon;
import org.longg.nh.kickstyleecommerce.domain.repositories.CouponRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CouponSchedulerService {

  private final CouponRepository couponRepository;

  @Scheduled(cron = "0 * * * * *")
  @Transactional
  public void deactivateExpiredCoupons() {
    log.info("Starting scheduled task to deactivate expired coupons");

    try {
      Timestamp now = new Timestamp(System.currentTimeMillis());
      List<Coupon> expiredCoupons = couponRepository.findExpiredActiveCoupons(now);

      if (!expiredCoupons.isEmpty()) {
        log.info("Found {} expired coupons to deactivate", expiredCoupons.size());

        for (Coupon coupon : expiredCoupons) {
          coupon.setIsActive(false);
          log.info(
              "Deactivated expired coupon: {} (Code: {}, End Date: {})",
              coupon.getId(),
              coupon.getCode(),
              coupon.getEndDate());
        }

        couponRepository.saveAll(expiredCoupons);
        log.info("Successfully deactivated {} expired coupons", expiredCoupons.size());
      } else {
        log.debug("No expired coupons found");
      }

    } catch (Exception e) {
      log.error("Error while deactivating expired coupons", e);
    }
  }

  /**
   * Run daily at 1:00 AM to deactivate max-used coupons Cron: 0 0 1 * * * means run at 1:00 AM
   * every day
   */
  @Scheduled(cron = "0 * * * * *")
  @Transactional
  public void deactivateMaxUsedCoupons() {
    log.info("Starting scheduled task to deactivate max-used coupons");

    try {
      List<Coupon> maxUsedCoupons = couponRepository.findMaxUsedActiveCoupons();

      if (!maxUsedCoupons.isEmpty()) {
        log.info("Found {} max-used coupons to deactivate", maxUsedCoupons.size());

        for (Coupon coupon : maxUsedCoupons) {
          coupon.setIsActive(false);
          log.info(
              "Deactivated max-used coupon: {} (Code: {}, Used: {}/{}, Max: {})",
              coupon.getId(),
              coupon.getCode(),
              coupon.getUsedCount(),
              coupon.getMaxUsageCount(),
              coupon.getMaxUsageCount());
        }

        couponRepository.saveAll(maxUsedCoupons);
        log.info("Successfully deactivated {} max-used coupons", maxUsedCoupons.size());
      } else {
        log.debug("No max-used coupons found");
      }

    } catch (Exception e) {
      log.error("Error while deactivating max-used coupons", e);
    }
  }

  /** Manual method to force deactivate expired coupons */
  @Transactional
  public int manualDeactivateExpiredCoupons() {
    log.info("Manual deactivation of expired coupons triggered");

    Timestamp now = new Timestamp(System.currentTimeMillis());
    List<Coupon> expiredCoupons = couponRepository.findExpiredActiveCoupons(now);

    if (!expiredCoupons.isEmpty()) {
      for (Coupon coupon : expiredCoupons) {
        coupon.setIsActive(false);
      }
      couponRepository.saveAll(expiredCoupons);
      log.info("Manually deactivated {} expired coupons", expiredCoupons.size());
    }

    return expiredCoupons.size();
  }
}
