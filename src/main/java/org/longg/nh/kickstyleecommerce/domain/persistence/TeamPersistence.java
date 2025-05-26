package org.longg.nh.kickstyleecommerce.domain.persistence;

import com.eps.shared.interfaces.persistence.IBasePersistence;
import com.eps.shared.interfaces.repository.IBaseRepository;
import lombok.RequiredArgsConstructor;
import org.longg.nh.kickstyleecommerce.domain.entities.Team;
import org.longg.nh.kickstyleecommerce.domain.repositories.TeamRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TeamPersistence implements IBasePersistence<Team, Long> {

  private final TeamRepository teamRepository;

  @Override
  public IBaseRepository<Team, Long> getIBaseRepository() {
    return teamRepository;
  }
}
