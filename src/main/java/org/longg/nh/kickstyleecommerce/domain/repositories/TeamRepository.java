package org.longg.nh.kickstyleecommerce.domain.repositories;

import com.eps.shared.interfaces.repository.IBaseRepository;
import org.longg.nh.kickstyleecommerce.domain.entities.Team;
import org.springframework.stereotype.Repository;

@Repository
public interface TeamRepository extends IBaseRepository<Team, Long> {}
