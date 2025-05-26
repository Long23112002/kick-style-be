package org.longg.nh.kickstyleecommerce.domain.repositories;

import com.eps.shared.interfaces.repository.IBaseRepository;
import org.longg.nh.kickstyleecommerce.domain.entities.Material;
import org.springframework.stereotype.Repository;

@Repository
public interface MaterialRepository extends IBaseRepository<Material, Long> {}
