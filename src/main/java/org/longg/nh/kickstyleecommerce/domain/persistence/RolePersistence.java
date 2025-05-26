package org.longg.nh.kickstyleecommerce.domain.persistence;

import com.eps.shared.interfaces.persistence.IBasePersistence;
import com.eps.shared.interfaces.repository.IBaseRepository;
import lombok.RequiredArgsConstructor;
import org.longg.nh.kickstyleecommerce.domain.entities.Role;
import org.longg.nh.kickstyleecommerce.domain.repositories.RoleRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RolePersistence implements IBasePersistence<Role, Long> {

  private final RoleRepository roleRepository;

  @Override
  public IBaseRepository<Role, Long> getIBaseRepository() {
    return roleRepository;
  }
}
