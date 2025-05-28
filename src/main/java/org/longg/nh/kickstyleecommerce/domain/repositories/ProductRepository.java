package org.longg.nh.kickstyleecommerce.domain.repositories;

import com.eps.shared.interfaces.repository.IBaseRepository;
import org.longg.nh.kickstyleecommerce.domain.entities.Product;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends IBaseRepository<Product, Long> {

    @Query(value = "SELECT last_value + 1 FROM products.products_id_seq", nativeQuery = true)
    Long getNextSequence();
}
