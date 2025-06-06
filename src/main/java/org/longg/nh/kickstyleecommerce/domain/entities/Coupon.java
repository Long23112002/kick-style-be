package org.longg.nh.kickstyleecommerce.domain.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.Where;
import org.longg.nh.kickstyleecommerce.domain.entities.enums.DiscountType;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;

@Entity
@Table(name = "coupons", schema = "coupons")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Where(clause = "is_deleted = false")
@SQLDelete(sql = "UPDATE coupons.coupons SET is_deleted = true WHERE id = ?")
public class Coupon {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "code", nullable = false, unique = true)
  private String code;

  @Column(name = "name", nullable = false)
  private String name;

  @Column(name = "description")
  private String description;

  @Enumerated(EnumType.STRING)
  @Column(name = "discount_type", nullable = false)
  private DiscountType discountType;

  @Column(name = "discount_value", nullable = false)
  private BigDecimal discountValue;

  @Column(name = "minimum_amount")
  private BigDecimal minimumAmount = BigDecimal.ZERO;

  @Column(name = "maximum_discount")
  private BigDecimal maximumDiscount;

  @Column(name = "usage_limit")
  private Integer usageLimit;
  
  @Column(name = "max_usage_count")
  private Integer maxUsageCount;

  @Column(name = "used_count")
  private Integer usedCount = 0;

  @Column(name = "start_date", nullable = false)
  private Timestamp startDate;
  
  @Column(name = "valid_from", nullable = false)
  private Timestamp validFrom;

  @Column(name = "end_date", nullable = false)
  private Timestamp endDate;
  
  @Column(name = "valid_to", nullable = false)  
  private Timestamp validTo;

  @Column(name = "is_active")
  private Boolean isActive = true;

  @Column(name = "created_at", nullable = false, updatable = false)
  @CreationTimestamp
  private Timestamp createdAt;

  @Column(name = "updated_at", nullable = false)
  @UpdateTimestamp
  private Timestamp updatedAt;

  @Column(name = "is_deleted")
  private Boolean isDeleted = false;

  @Column(name = "user_specific")
  private Boolean userSpecific = false;

  @OneToMany(mappedBy = "coupon", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  private List<CouponUser> allowedUsers;

  // Helper methods
  public boolean isValid() {
    if (!isActive || isDeleted) return false;
    
    Timestamp now = new Timestamp(System.currentTimeMillis());
    if (now.before(startDate) || now.after(endDate)) return false;
    
    if (usageLimit != null && usedCount >= usageLimit) return false;
    
    return true;
  }

  public BigDecimal calculateDiscount(BigDecimal orderAmount) {
    if (!isValid() || orderAmount.compareTo(minimumAmount) < 0) {
      return BigDecimal.ZERO;
    }

    BigDecimal discount;
    if (discountType == DiscountType.PERCENTAGE) {
      discount = orderAmount.multiply(discountValue).divide(BigDecimal.valueOf(100));
      if (maximumDiscount != null && discount.compareTo(maximumDiscount) > 0) {
        discount = maximumDiscount;
      }
    } else {
      discount = discountValue;
    }

    return discount;
  }
} 