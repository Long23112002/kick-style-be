package org.longg.nh.kickstyleecommerce.domain.services.products;

import com.eps.shared.interfaces.persistence.IBasePersistence;
import com.eps.shared.interfaces.services.IBaseService;
import lombok.RequiredArgsConstructor;
import org.longg.nh.kickstyleecommerce.domain.dtos.requests.products.ColorRequest;
import org.longg.nh.kickstyleecommerce.domain.dtos.responses.products.ColorResponse;
import org.longg.nh.kickstyleecommerce.domain.entities.Colors;
import org.longg.nh.kickstyleecommerce.domain.persistence.ColorPersistence;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ColorService implements IBaseService<Colors , Long , ColorResponse , ColorRequest , ColorResponse> {

    private final ColorPersistence colorPersistence;

    @Override
    public IBasePersistence<Colors, Long> getPersistence() {
        return colorPersistence;
    }
}
