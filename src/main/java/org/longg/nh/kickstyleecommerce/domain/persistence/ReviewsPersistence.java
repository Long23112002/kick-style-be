package org.longg.nh.kickstyleecommerce.domain.persistence;

import com.eps.shared.interfaces.persistence.IBasePersistence;
import com.eps.shared.interfaces.repository.IBaseRepository;
import lombok.RequiredArgsConstructor;
import org.longg.nh.kickstyleecommerce.domain.entities.Review;
import org.longg.nh.kickstyleecommerce.domain.repositories.ReviewsRepository;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReviewsPersistence implements IBasePersistence<Review, Long> {

  private final ReviewsRepository reviewsRepository;

  @Override
  public IBaseRepository<Review, Long> getIBaseRepository() {
    return reviewsRepository;
  }


}
