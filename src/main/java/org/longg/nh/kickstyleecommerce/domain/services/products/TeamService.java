package org.longg.nh.kickstyleecommerce.domain.services.products;

import com.eps.shared.interfaces.persistence.IBasePersistence;
import com.eps.shared.interfaces.services.IBaseService;
import lombok.RequiredArgsConstructor;
import org.longg.nh.kickstyleecommerce.domain.dtos.requests.products.TeamRequest;
import org.longg.nh.kickstyleecommerce.domain.dtos.responses.products.TeamResponse;
import org.longg.nh.kickstyleecommerce.domain.entities.Team;
import org.longg.nh.kickstyleecommerce.domain.persistence.TeamPersistence;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TeamService implements IBaseService<Team, Long, TeamResponse, TeamRequest, TeamResponse> {

  private final TeamPersistence teamPersistence;

  @Override
  public IBasePersistence<Team, Long> getPersistence() {
    return teamPersistence;
  }
}
