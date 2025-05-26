package org.longg.nh.kickstyleecommerce.app.api;

import com.eps.shared.interfaces.api.IBaseApi;
import com.eps.shared.interfaces.services.IBaseService;
import lombok.RequiredArgsConstructor;
import org.longg.nh.kickstyleecommerce.domain.dtos.filter.RoleParam;
import org.longg.nh.kickstyleecommerce.domain.dtos.requests.RoleRequest;
import org.longg.nh.kickstyleecommerce.domain.dtos.responses.RoleResponse;
import org.longg.nh.kickstyleecommerce.domain.entities.Role;
import org.longg.nh.kickstyleecommerce.domain.services.RoleService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/roles")
@RequiredArgsConstructor
public class RoleController implements IBaseApi<Role, Long, RoleResponse, RoleRequest, RoleParam> {

  private final RoleService roleService;

  @Override
  public IBaseService<Role, Long, RoleResponse, RoleRequest, RoleParam> getService() {
    return roleService;
  }
}
