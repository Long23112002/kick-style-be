package org.longg.nh.kickstyleecommerce.app.api;

import com.eps.shared.interfaces.api.IBaseApi;
import com.eps.shared.interfaces.services.IBaseService;
import com.eps.shared.models.HeaderContext;
import lombok.RequiredArgsConstructor;
import org.longg.nh.kickstyleecommerce.domain.dtos.requests.products.TeamRequest;
import org.longg.nh.kickstyleecommerce.domain.dtos.responses.products.TeamResponse;
import org.longg.nh.kickstyleecommerce.domain.entities.Team;
import org.longg.nh.kickstyleecommerce.domain.services.products.TeamService;
import org.longg.nh.kickstyleecommerce.infrastructure.config.annotation.CheckRole;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/teams")
@RequiredArgsConstructor
public class TeamController implements IBaseApi<Team, Long, TeamResponse, TeamRequest, TeamResponse> {

  private final TeamService teamService;

  @Override
  public IBaseService<Team, Long, TeamResponse, TeamRequest, TeamResponse> getService() {
    return teamService;
  }

  @Override
  @CheckRole({"ADMIN"})
  public ResponseEntity<TeamResponse> create(
      HeaderContext context, Map<String, Object> headers, TeamRequest request) {
    return IBaseApi.super.create(context, headers, request);
  }

  @Override
  @CheckRole({"ADMIN"})
  public ResponseEntity<TeamResponse> update(
      HeaderContext context, Map<String, Object> headers, Long aLong, TeamRequest request) {
    return IBaseApi.super.update(context, headers, aLong, request);
  }

  @Override
  @CheckRole({"ADMIN"})
  public ResponseEntity<?> delete(HeaderContext context, Map<String, Object> headers, Long aLong) {
    return IBaseApi.super.delete(context, headers, aLong);
  }
}
