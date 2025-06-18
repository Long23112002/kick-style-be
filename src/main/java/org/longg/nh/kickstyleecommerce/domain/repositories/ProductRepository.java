package org.longg.nh.kickstyleecommerce.domain.repositories;

import com.eps.shared.interfaces.repository.IBaseRepository;
import org.longg.nh.kickstyleecommerce.domain.entities.Product;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

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
}
