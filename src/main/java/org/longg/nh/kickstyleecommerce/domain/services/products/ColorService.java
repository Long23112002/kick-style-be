package org.longg.nh.kickstyleecommerce.domain.services.products;

import com.eps.shared.interfaces.persistence.IBasePersistence;
import com.eps.shared.interfaces.services.IBaseService;
import com.eps.shared.models.HeaderContext;
import com.eps.shared.models.exceptions.ResponseException;
import lombok.RequiredArgsConstructor;
import org.longg.nh.kickstyleecommerce.domain.dtos.requests.products.ColorRequest;
import org.longg.nh.kickstyleecommerce.domain.dtos.responses.products.ColorResponse;
import org.longg.nh.kickstyleecommerce.domain.entities.Colors;
import org.longg.nh.kickstyleecommerce.domain.persistence.ColorPersistence;
import org.longg.nh.kickstyleecommerce.domain.repositories.ProductVariantRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ColorService implements IBaseService<Colors , Long , ColorResponse , ColorRequest , ColorResponse> {

    private final ColorPersistence colorPersistence;
    private final ProductVariantRepository productVariantRepository;

    @Override
    public IBasePersistence<Colors, Long> getPersistence() {
        return colorPersistence;
    }

    @Override
    public void delete(HeaderContext context, Long aLong) {
        if (productVariantRepository.existsByColorId(aLong)) {
            throw new ResponseException(HttpStatus.BAD_REQUEST,
                    "Không thể xóa màu sắc này vì đang có biến thể sản phẩm sử dụng");
        }
        IBaseService.super.delete(context, aLong);
    }
}
