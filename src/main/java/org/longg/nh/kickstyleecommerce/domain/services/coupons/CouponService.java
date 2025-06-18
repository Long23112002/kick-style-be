package org.longg.nh.kickstyleecommerce.domain.services.coupons;

import com.eps.shared.interfaces.persistence.IBasePersistence;
import com.eps.shared.interfaces.services.IBaseService;
import com.eps.shared.models.HeaderContext;
import com.eps.shared.models.exceptions.ResponseException;
import lombok.RequiredArgsConstructor;
import org.longg.nh.kickstyleecommerce.domain.dtos.requests.coupons.CouponRequest;
import org.longg.nh.kickstyleecommerce.domain.dtos.responses.coupons.CouponResponse;
import org.longg.nh.kickstyleecommerce.domain.entities.Coupon;
import org.longg.nh.kickstyleecommerce.domain.entities.CouponUser;
import org.longg.nh.kickstyleecommerce.domain.entities.User;
import org.longg.nh.kickstyleecommerce.domain.persistence.CouponPersistence;
import org.longg.nh.kickstyleecommerce.domain.repositories.CouponRepository;
import org.longg.nh.kickstyleecommerce.domain.repositories.CouponUserRepository;
import org.longg.nh.kickstyleecommerce.domain.repositories.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.sql.Timestamp;
import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CouponService
    implements IBaseService<Coupon, Long, CouponResponse, CouponRequest, CouponResponse> {

  private final CouponPersistence couponPersistence;
  private final CouponRepository couponRepository;
  private final CouponUserRepository couponUserRepository;
  private final UserRepository userRepository;

  @Override
  public IBasePersistence<Coupon, Long> getPersistence() {
    return couponPersistence;
  }

  @Transactional
  public CouponResponse createCoupon(HeaderContext context, CouponRequest request) {
    // Kiểm tra code không trùng
    if (couponRepository.existsByCode(request.getCode())) {
      throw new ResponseException(HttpStatus.BAD_REQUEST, "Mã coupon đã tồn tại");
    }

    Coupon coupon =
        Coupon.builder()
            .code(request.getCode())
            .name(request.getName())
            .description(request.getDescription())
            .discountType(request.getDiscountType())
            .discountValue(request.getDiscountValue())
            .minimumAmount(request.getMinimumAmount())
            .maximumDiscount(request.getMaximumDiscount())
            .maxUsageCount(request.getMaxUsageCount())
            .usedCount(0)
            .startDate(request.getValidFrom())
            .endDate(request.getValidTo())
            .validFrom(request.getValidFrom())
            .validTo(request.getValidTo())
            .userSpecific(request.getUserSpecific())
            .isActive(true)
            .isDeleted(false)
            .build();

    Coupon savedCoupon = couponRepository.save(coupon);

    // Nếu là user-specific coupon, tạo relationships
    if (request.getUserSpecific() != null
        && request.getUserSpecific()
        && request.getUserIds() != null
        && !request.getUserIds().isEmpty()) {

      List<User> users = userRepository.findAllById(request.getUserIds());
      if (users.size() != request.getUserIds().size()) {
        throw new ResponseException(HttpStatus.BAD_REQUEST, "Một số user ID không tồn tại");
      }

      List<CouponUser> couponUsers =
          users.stream()
              .map(user -> CouponUser.builder().coupon(savedCoupon).user(user).build())
              .collect(Collectors.toList());

      couponUserRepository.saveAll(couponUsers);
    }

    return mapToCouponResponse(coupon);
  }

  @Transactional
  public CouponResponse updateCoupon(Long id, CouponRequest request) {
    Coupon coupon =
        couponRepository
            .findById(id)
            .orElseThrow(
                () -> new ResponseException(HttpStatus.BAD_REQUEST, "Coupon không tồn tại"));
    coupon.setCode(coupon.getCode());
    coupon.setName(request.getName());
    coupon.setDescription(request.getDescription());
    coupon.setDiscountType(request.getDiscountType());
    coupon.setDiscountValue(request.getDiscountValue());
    coupon.setMinimumAmount(request.getMinimumAmount());
    coupon.setMaximumDiscount(request.getMaximumDiscount());
    coupon.setMaxUsageCount(request.getMaxUsageCount());
    coupon.setStartDate(request.getValidFrom());
    coupon.setEndDate(request.getValidTo());
    coupon.setValidFrom(request.getValidFrom());
    coupon.setValidTo(request.getValidTo());
    coupon.setUserSpecific(request.getUserSpecific());

    coupon = couponRepository.save(coupon);

    // Cập nhật user relationships
    couponUserRepository.deleteByCouponId(id);
    couponUserRepository.flush();

    if (request.getUserSpecific() != null
        && request.getUserSpecific()
        && request.getUserIds() != null
        && !request.getUserIds().isEmpty()) {

      List<User> users = userRepository.findAllById(request.getUserIds());
      if (users.size() != request.getUserIds().size()) {
        throw new ResponseException(HttpStatus.BAD_REQUEST, "Một số user ID không tồn tại");
      }

      Coupon finalCoupon = coupon;
      List<CouponUser> couponUsers =
          users.stream()
              .map(user -> CouponUser.builder().coupon(finalCoupon).user(user).build())
              .collect(Collectors.toList());

      couponUserRepository.saveAll(couponUsers);
    }

    return mapToCouponResponse(coupon);
  }

  public List<CouponResponse> getActiveCoupons() {
    Timestamp now = new Timestamp(System.currentTimeMillis());
    return couponRepository.findActiveCoupons(now).stream()
        .map(this::mapToCouponResponse)
        .collect(Collectors.toList());
  }

  public List<CouponResponse> getValidCouponsForUser(Long userId) {
    Timestamp now = new Timestamp(System.currentTimeMillis());
    return couponRepository.findValidCouponsForUser(userId, now).stream()
        .map(this::mapToCouponResponse)
        .collect(Collectors.toList());
  }

  public CouponResponse activateCoupon(Long id) {
    Coupon coupon =
        couponRepository
            .findById(id)
            .orElseThrow(
                () -> new ResponseException(HttpStatus.BAD_REQUEST, "Coupon không tồn tại"));

    coupon.setIsActive(true);
    coupon = couponRepository.save(coupon);

    return mapToCouponResponse(coupon);
  }

  public CouponResponse deactivateCoupon(Long id) {
    Coupon coupon =
        couponRepository
            .findById(id)
            .orElseThrow(
                () -> new ResponseException(HttpStatus.BAD_REQUEST, "Coupon không tồn tại"));

    coupon.setIsActive(false);
    coupon = couponRepository.save(coupon);

    return mapToCouponResponse(coupon);
  }

  public CouponResponse mapToCouponResponse(Coupon coupon) {
    List<Long> userIds = null;

    if (coupon.getUserSpecific() != null && coupon.getUserSpecific()) {
      userIds = couponUserRepository.findUserIdsByCouponId(coupon.getId());
    }

    return CouponResponse.builder()
        .id(coupon.getId())
        .code(coupon.getCode())
        .name(coupon.getName())
        .description(coupon.getDescription())
        .discountType(coupon.getDiscountType())
        .discountValue(coupon.getDiscountValue())
        .minimumAmount(coupon.getMinimumAmount())
        .maximumDiscount(coupon.getMaximumDiscount())
        .maxUsageCount(coupon.getMaxUsageCount())
        .usedCount(coupon.getUsedCount())
        .startDate(coupon.getStartDate())
        .endDate(coupon.getEndDate())
        .validFrom(coupon.getValidFrom())
        .validTo(coupon.getValidTo())
        .userSpecific(coupon.getUserSpecific())
        .isActive(coupon.getIsActive())
        .userIds(userIds)
        .createdAt(coupon.getCreatedAt())
        .updatedAt(coupon.getUpdatedAt())
        .build();
  }

  // Override để có đúng signature với Controller
  public void deleteById(HeaderContext context, Long id) {
    couponRepository.deleteById(id);
  }

  private BiFunction<HeaderContext, Coupon, CouponResponse> mappingResponseHandler() {
    return (context, coupon) -> mapToCouponResponse(coupon);
  }

  // Paginated methods
  public Page<CouponResponse> getAllCoupons(Pageable pageable) {
    return couponRepository.findAll(pageable).map(this::mapToCouponResponse);
  }

  public Page<CouponResponse> getActiveCoupons(Pageable pageable) {
    Timestamp now = new Timestamp(System.currentTimeMillis());
    return couponRepository.findActiveCoupons(now, pageable).map(this::mapToCouponResponse);
  }

  public Page<CouponResponse> getValidCouponsForUser(Long userId, Pageable pageable) {
    Timestamp now = new Timestamp(System.currentTimeMillis());
    return couponRepository
        .findValidCouponsForUser(userId, now, pageable)
        .map(this::mapToCouponResponse);
  }

  // Search and filter methods
  public Page<CouponResponse> searchCoupons(
      String code, String name, Boolean isActive, Pageable pageable) {
    return couponRepository
        .findCouponsWithFilters(code, name, isActive, pageable)
        .map(this::mapToCouponResponse);
  }

  public Page<CouponResponse> searchActiveCoupons(String code, String name, Pageable pageable) {
    Timestamp now = new Timestamp(System.currentTimeMillis());
    return couponRepository
        .findActiveCouponsWithFilters(code, name, now, pageable)
        .map(this::mapToCouponResponse);
  }
}
