package org.longg.nh.kickstyleecommerce.domain.services.products;

import com.eps.shared.interfaces.persistence.IBasePersistence;
import com.eps.shared.interfaces.services.IBaseService;
import com.eps.shared.models.HeaderContext;
import com.eps.shared.models.exceptions.ResponseException;
import lombok.RequiredArgsConstructor;
import org.longg.nh.kickstyleecommerce.domain.dtos.requests.products.SizeRequest;
import org.longg.nh.kickstyleecommerce.domain.dtos.responses.products.SizeResponse;
import org.longg.nh.kickstyleecommerce.domain.entities.Sizes;
import org.longg.nh.kickstyleecommerce.domain.persistence.SizePersistence;
import org.longg.nh.kickstyleecommerce.domain.repositories.ProductVariantRepository;
import org.springframework.http.HttpStatus;
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

  private final ProductVariantRepository productVariantRepository;

  @Override
  public void delete(HeaderContext context, Long aLong) {
    if (productVariantRepository.existsBySizeId(aLong)) {
      throw new ResponseException(HttpStatus.BAD_REQUEST,
              "Không thể xóa kích thước này vì đang có biến thể sản phẩm sử dụng");
    }
    IBaseService.super.delete(context, aLong);
  }
}
