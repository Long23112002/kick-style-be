package org.longg.nh.kickstyleecommerce.app.api;

import com.eps.shared.interfaces.api.IBaseApi;
import com.eps.shared.interfaces.services.IBaseService;
import com.eps.shared.models.HeaderContext;
import lombok.RequiredArgsConstructor;
import org.longg.nh.kickstyleecommerce.domain.dtos.filter.MaterialParam;
import org.longg.nh.kickstyleecommerce.domain.dtos.requests.products.MaterialRequest;
import org.longg.nh.kickstyleecommerce.domain.dtos.responses.products.MaterialResponse;
import org.longg.nh.kickstyleecommerce.domain.entities.Material;
import org.longg.nh.kickstyleecommerce.domain.services.products.MaterialService;
import org.longg.nh.kickstyleecommerce.infrastructure.config.annotation.CheckRole;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/materials")
@RequiredArgsConstructor
public class MaterialController
    implements IBaseApi<Material, Long, MaterialResponse, MaterialRequest, MaterialParam> {

  private final MaterialService materialService;

  @Override
  public IBaseService<Material, Long, MaterialResponse, MaterialRequest, MaterialParam>
      getService() {
    return materialService;
  }

  @Override
  @CheckRole({"ADMIN"})
  public ResponseEntity<MaterialResponse> create(
      HeaderContext context, Map<String, Object> headers, MaterialRequest request) {
    return IBaseApi.super.create(context, headers, request);
  }

  @Override
  @CheckRole({"ADMIN"})
  public ResponseEntity<MaterialResponse> update(
      HeaderContext context, Map<String, Object> headers, Long aLong, MaterialRequest request) {
    return IBaseApi.super.update(context, headers, aLong, request);
  }

  @Override
  @CheckRole({"ADMIN"})
  public ResponseEntity<?> delete(HeaderContext context, Map<String, Object> headers, Long aLong) {
    return IBaseApi.super.delete(context, headers, aLong);
  }
}
