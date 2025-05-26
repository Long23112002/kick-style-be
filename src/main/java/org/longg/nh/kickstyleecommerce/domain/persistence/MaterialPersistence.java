package org.longg.nh.kickstyleecommerce.domain.persistence;

import com.eps.shared.interfaces.persistence.IBasePersistence;
import com.eps.shared.interfaces.repository.IBaseRepository;
import lombok.RequiredArgsConstructor;
import org.longg.nh.kickstyleecommerce.domain.entities.Material;
import org.longg.nh.kickstyleecommerce.domain.repositories.MaterialRepository;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class MaterialPersistence implements IBasePersistence<Material, Long> {

  private final MaterialRepository materialRepository;

  @Override
  public IBaseRepository<Material, Long> getIBaseRepository() {
    return materialRepository;
  }
}
