package org.longg.nh.kickstyleecommerce.domain.persistence;

import com.eps.shared.interfaces.persistence.IBasePersistence;
import com.eps.shared.interfaces.repository.IBaseRepository;
import lombok.RequiredArgsConstructor;
import org.longg.nh.kickstyleecommerce.domain.entities.Sizes;
import org.longg.nh.kickstyleecommerce.domain.repositories.SizeRepository;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class SizePersistence implements IBasePersistence<Sizes, Long> {

  private final SizeRepository sizeRepository;

  @Override
  public IBaseRepository<Sizes, Long> getIBaseRepository() {
    return sizeRepository;
  }
}
