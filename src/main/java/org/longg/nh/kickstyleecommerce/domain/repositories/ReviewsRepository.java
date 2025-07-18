package org.longg.nh.kickstyleecommerce.domain.repositories;

import com.eps.shared.interfaces.repository.IBaseRepository;
import org.longg.nh.kickstyleecommerce.domain.entities.Review;
import org.springframework.data.domain.Page;

public interface ReviewsRepository extends IBaseRepository<Review , Long> {

    boolean existsByUserIdAndOrderId(Long userId, Long orderId);

}
