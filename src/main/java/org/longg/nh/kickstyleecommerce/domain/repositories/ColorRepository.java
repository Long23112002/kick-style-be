package org.longg.nh.kickstyleecommerce.domain.repositories;

import com.eps.shared.interfaces.repository.IBaseRepository;
import org.longg.nh.kickstyleecommerce.domain.entities.Colors;
import org.springframework.stereotype.Repository;

@Repository
public interface ColorRepository extends IBaseRepository<Colors , Long> {}
