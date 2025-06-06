package org.longg.nh.kickstyleecommerce.domain.persistence;

import com.eps.shared.interfaces.persistence.IBasePersistence;
import com.eps.shared.interfaces.repository.IBaseRepository;
import lombok.RequiredArgsConstructor;
import org.longg.nh.kickstyleecommerce.domain.entities.Colors;
import org.longg.nh.kickstyleecommerce.domain.repositories.ColorRepository;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class ColorPersistence implements IBasePersistence<Colors, Long> {

  private final ColorRepository colorRepository;

  @Override
  public IBaseRepository<Colors, Long> getIBaseRepository() {
    return colorRepository;
  }
}
