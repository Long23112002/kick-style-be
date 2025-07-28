package org.longg.nh.kickstyleecommerce.domain.services.products;

import com.eps.shared.interfaces.persistence.IBasePersistence;
import com.eps.shared.interfaces.services.IBaseService;
import com.eps.shared.models.HeaderContext;
import com.eps.shared.models.exceptions.ResponseException;
import com.eps.shared.utils.functions.PentaConsumer;
import com.eps.shared.utils.functions.QuadConsumer;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.util.TriConsumer;
import org.longg.nh.kickstyleecommerce.domain.dtos.requests.products.MaterialRequest;
import org.longg.nh.kickstyleecommerce.domain.dtos.responses.products.MaterialResponse;
import org.longg.nh.kickstyleecommerce.domain.entities.Material;
import org.longg.nh.kickstyleecommerce.domain.persistence.MaterialPersistence;
import org.longg.nh.kickstyleecommerce.domain.repositories.ProductRepository;
import org.longg.nh.kickstyleecommerce.domain.utils.SlugUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.function.BiFunction;

@Service
@RequiredArgsConstructor
public class MaterialService
    implements IBaseService<Material, Long, MaterialResponse, MaterialRequest, MaterialResponse> {

  private final MaterialPersistence materialPersistence;

  @Override
  public IBasePersistence<Material, Long> getPersistence() {
    return materialPersistence;
  }

  @Override
  public MaterialResponse create(
      HeaderContext context,
      MaterialRequest request,
      TriConsumer<HeaderContext, Material, MaterialRequest> validationCreateHandler,
      TriConsumer<HeaderContext, Material, MaterialRequest> mappingEntityHandler,
      TriConsumer<HeaderContext, Material, MaterialRequest> postHandler,
      BiFunction<HeaderContext, Material, MaterialResponse> mappingResponseHandler) {

    return IBaseService.super.create(
        context,
        request,
        validationCreateHandler,
        wrapMappingHandlerWithSlug(mappingEntityHandler),
        postHandler,
        materialResponseMapper());
  }

  @Override
  public MaterialResponse update(
      HeaderContext context,
      Long aLong,
      MaterialRequest request,
      QuadConsumer<HeaderContext, Long, Material, MaterialRequest> validationHandler,
      TriConsumer<HeaderContext, Material, MaterialRequest> mappingHandler,
      PentaConsumer<HeaderContext, Material, Material, Long, MaterialRequest> postHandler,
      BiFunction<HeaderContext, Material, MaterialResponse> mappingResponseHandler) {
    return IBaseService.super.update(
        context,
        aLong,
        request,
        validationHandler,
        mappingHandler,
        postHandler,
        mappingResponseHandler);
  }

  private BiFunction<HeaderContext, Material, MaterialResponse> materialResponseMapper() {
    return (context, entity) ->
        MaterialResponse.builder()
            .id(entity.getId())
            .name(entity.getName())
            .slug(entity.getSlug())
            .createdAt(entity.getCreatedAt())
            .isDeleted(entity.getIsDeleted())
            .build();
  }


  private final ProductRepository productRepository;

  @Override
  public void delete(HeaderContext context, Long aLong) {
    if (productRepository.existsByMaterialId(aLong)) {
      throw new ResponseException(HttpStatus.BAD_REQUEST,
              "Không thể xóa chất liệu này vì đang có sản phẩm sử dụng");
    }
    IBaseService.super.delete(context, aLong);
  }

  private TriConsumer<HeaderContext, Material, MaterialRequest> wrapMappingHandlerWithSlug(
      TriConsumer<HeaderContext, Material, MaterialRequest> originalHandler) {

    return (ctx, material, req) -> {
      originalHandler.accept(ctx, material, req);

      if (material.getName() != null
          && (material.getSlug() == null || material.getSlug().isBlank())) {
        material.setSlug(SlugUtils.toSlug(material.getName()));
      }
    };
  }
}
