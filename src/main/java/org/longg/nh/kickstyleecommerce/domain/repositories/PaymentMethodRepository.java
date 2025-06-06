package org.longg.nh.kickstyleecommerce.domain.repositories;

import com.eps.shared.interfaces.repository.IBaseRepository;
import org.longg.nh.kickstyleecommerce.domain.entities.PaymentMethod;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentMethodRepository extends IBaseRepository<PaymentMethod, Long> {
    
    Optional<PaymentMethod> findBySlug(String slug);
    
    boolean existsBySlug(String slug);
    
    boolean existsBySlugAndIdNot(String slug, Long excludeId);
    
    boolean existsByName(String name);
    
    List<PaymentMethod> findByIsActiveTrue();
    
    @Query("SELECT CASE WHEN COUNT(o) > 0 THEN true ELSE false END FROM Order o WHERE o.paymentMethod.id = :paymentMethodId")
    boolean hasActiveOrders(@Param("paymentMethodId") Long paymentMethodId);
} 