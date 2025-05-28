package org.longg.nh.kickstyleecommerce.domain.repositories;

import com.eps.shared.interfaces.repository.IBaseRepository;
import org.longg.nh.kickstyleecommerce.domain.entities.ProductVariant;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface ProductVariantRepository extends IBaseRepository<ProductVariant, Long> {

    List<ProductVariant> findByProductId(Long id);

    /**
     * Xóa tất cả variants của một product
     */
    @Transactional
    void deleteByProductId(Long productId);
}
