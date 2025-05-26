package org.longg.nh.kickstyleecommerce.domain.repositories;

import com.eps.shared.interfaces.repository.IBaseRepository;
import org.longg.nh.kickstyleecommerce.domain.entities.Category;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends IBaseRepository<Category , Long> {}
