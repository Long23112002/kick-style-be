package org.longg.nh.kickstyleecommerce.domain.persistence;

import com.eps.shared.interfaces.persistence.IBasePersistence;
import com.eps.shared.interfaces.repository.IBaseRepository;
import lombok.RequiredArgsConstructor;
import org.longg.nh.kickstyleecommerce.domain.entities.Product;
import org.longg.nh.kickstyleecommerce.domain.repositories.ProductRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductPersistence implements IBasePersistence<Product, Long> {

  private final ProductRepository productRepository;

  @Override
  public IBaseRepository<Product, Long> getIBaseRepository() {
    return productRepository;
  }



}
