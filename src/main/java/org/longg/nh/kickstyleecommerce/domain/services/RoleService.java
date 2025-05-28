package org.longg.nh.kickstyleecommerce.domain.services;

import com.eps.shared.interfaces.persistence.IBasePersistence;
import com.eps.shared.interfaces.services.IBaseService;
import lombok.RequiredArgsConstructor;
import org.longg.nh.kickstyleecommerce.domain.dtos.requests.auth.RoleRequest;
import org.longg.nh.kickstyleecommerce.domain.dtos.responses.auth.RoleResponse;
import org.longg.nh.kickstyleecommerce.domain.entities.Role;
import org.longg.nh.kickstyleecommerce.domain.persistence.RolePersistence;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RoleService implements IBaseService<Role, Long, RoleResponse, RoleRequest, RoleResponse> {

  private final RolePersistence rolePersistence;

  @Override
  public IBasePersistence<Role, Long> getPersistence() {
    return rolePersistence;
  }
}
