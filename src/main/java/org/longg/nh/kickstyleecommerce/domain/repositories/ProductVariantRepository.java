package org.longg.nh.kickstyleecommerce.domain.repositories;

import com.eps.shared.interfaces.repository.IBaseRepository;
import org.longg.nh.kickstyleecommerce.domain.entities.ProductVariant;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface ProductVariantRepository extends IBaseRepository<ProductVariant, Long> {
    boolean existsBySizeId(Long sizeId);
    boolean existsByColorId(Long colorId);

    List<ProductVariant> findByProductId(Long id);
    
    /**
     * Tìm variants theo product ID bao gồm cả variants đã bị soft-delete
     * Dùng riêng cho Orders để đảm bảo orders vẫn hiển thị được variants của sản phẩm đã xóa
     * Sử dụng nativeQuery để bỏ qua @Where(clause = "is_deleted = false") của entity
     */
    @Query(value = "SELECT * FROM products.product_variants WHERE product_id = :productId", nativeQuery = true)
    List<ProductVariant> findByProductIdIncludingDeleted(@Param("productId") Long productId);

    /**
     * Xóa tất cả variants của một product
     */
    @Transactional
    void deleteByProductId(Long productId);
}
