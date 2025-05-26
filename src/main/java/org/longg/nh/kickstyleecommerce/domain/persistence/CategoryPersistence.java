package org.longg.nh.kickstyleecommerce.domain.persistence;

import com.eps.shared.interfaces.persistence.IBasePersistence;
import com.eps.shared.interfaces.repository.IBaseRepository;
import lombok.RequiredArgsConstructor;
import org.longg.nh.kickstyleecommerce.domain.entities.Category;
import org.longg.nh.kickstyleecommerce.domain.repositories.CategoryRepository;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class CategoryPersistence implements IBasePersistence<Category, Long> {

  private final CategoryRepository categoryRepository;

  @Override
  public IBaseRepository<Category, Long> getIBaseRepository() {
    return categoryRepository;
  }
}
