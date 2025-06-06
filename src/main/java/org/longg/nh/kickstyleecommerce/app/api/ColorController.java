package org.longg.nh.kickstyleecommerce.app.api;

import com.eps.shared.interfaces.api.IBaseApi;
import com.eps.shared.interfaces.services.IBaseService;
import lombok.RequiredArgsConstructor;
import org.longg.nh.kickstyleecommerce.domain.dtos.requests.products.ColorRequest;
import org.longg.nh.kickstyleecommerce.domain.dtos.responses.products.ColorResponse;
import org.longg.nh.kickstyleecommerce.domain.entities.Colors;
import org.longg.nh.kickstyleecommerce.domain.services.products.ColorService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/colors")
@RequiredArgsConstructor
public class ColorController
    implements IBaseApi<Colors, Long, ColorResponse, ColorRequest, ColorResponse> {

  private final ColorService colorService;

  @Override
  public IBaseService<Colors, Long, ColorResponse, ColorRequest, ColorResponse> getService() {
    return colorService;
  }
}
