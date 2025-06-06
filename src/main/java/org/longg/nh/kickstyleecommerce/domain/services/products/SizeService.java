package org.longg.nh.kickstyleecommerce.domain.services.products;

import com.eps.shared.interfaces.persistence.IBasePersistence;
import com.eps.shared.interfaces.services.IBaseService;
import lombok.RequiredArgsConstructor;
import org.longg.nh.kickstyleecommerce.domain.dtos.requests.products.SizeRequest;
import org.longg.nh.kickstyleecommerce.domain.dtos.responses.products.SizeResponse;
import org.longg.nh.kickstyleecommerce.domain.entities.Sizes;
import org.longg.nh.kickstyleecommerce.domain.persistence.SizePersistence;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SizeService
    implements IBaseService<Sizes, Long, SizeResponse, SizeRequest, SizeResponse> {

  private final SizePersistence sizePersistence;

  @Override
  public IBasePersistence<Sizes, Long> getPersistence() {
    return sizePersistence;
  }
}
