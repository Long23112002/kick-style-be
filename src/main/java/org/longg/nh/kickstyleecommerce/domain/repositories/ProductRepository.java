package org.longg.nh.kickstyleecommerce.domain.repositories;

import com.eps.shared.interfaces.repository.IBaseRepository;
import org.longg.nh.kickstyleecommerce.domain.entities.Product;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends IBaseRepository<Product, Long> {

    @Query(value = "SELECT last_value + 1 FROM products.products_id_seq", nativeQuery = true)
    Long getNextSequence();


    Optional<Product> findByCode(String code);

    /**
     * Kiểm tra xem slug có tồn tại không (trừ product có ID cụ thể)
     */
    boolean existsBySlugAndIdNot(String slug, Long excludeId);

    /**
     * Kiểm tra xem slug có tồn tại không
     */
    boolean existsBySlug(String slug);
    
    /**
     * Tìm product theo ID bao gồm cả sản phẩm đã bị soft-delete
     * Dùng riêng cho Orders để đảm bảo orders vẫn hiển thị được sản phẩm đã xóa
     * Sử dụng nativeQuery để bỏ qua @Where(clause = "is_deleted = false") của entity
     */
    @Query(value = "SELECT * FROM products.products WHERE id = :id", nativeQuery = true)
    Optional<Product> findProductByIdIncludingDeleted(@Param("id") Long id);
    
    /**
     * Tìm product theo code bao gồm cả sản phẩm đã bị soft-delete
     * Dùng riêng cho Orders để đảm bảo orders vẫn hiển thị được sản phẩm đã xóa
     * Sử dụng nativeQuery để bỏ qua @Where(clause = "is_deleted = false") của entity
     */
    @Query(value = "SELECT * FROM products.products WHERE code = :code", nativeQuery = true)
    Optional<Product> findByCodeIncludingDeleted(@Param("code") String code);

    /**
     * Get products by their codes, including soft-deleted ones
     * This is especially useful for historical orders where we need product data
     * @param codes List of product codes
     * @return List of products matching the codes, including soft-deleted ones
     */
    @Query(value = "SELECT * FROM products.products WHERE code IN :codes", nativeQuery = true)
    List<Product> findProductsByCodesIncludingDeleted(@Param("codes") List<String> codes);
}
