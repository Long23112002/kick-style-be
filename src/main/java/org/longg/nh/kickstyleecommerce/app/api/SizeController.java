package org.longg.nh.kickstyleecommerce.app.api;

import com.eps.shared.interfaces.api.IBaseApi;
import com.eps.shared.interfaces.services.IBaseService;
import lombok.RequiredArgsConstructor;
import org.longg.nh.kickstyleecommerce.domain.dtos.requests.products.SizeRequest;
import org.longg.nh.kickstyleecommerce.domain.dtos.responses.products.SizeResponse;
import org.longg.nh.kickstyleecommerce.domain.entities.Sizes;
import org.longg.nh.kickstyleecommerce.domain.services.products.SizeService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/sizes")
@RequiredArgsConstructor
public class SizeController
    implements IBaseApi<Sizes, Long, SizeResponse, SizeRequest, SizeResponse> {

  private final SizeService sizeService;

  @Override
  public IBaseService<Sizes, Long, SizeResponse, SizeRequest, SizeResponse> getService() {
    return sizeService;
  }
}
