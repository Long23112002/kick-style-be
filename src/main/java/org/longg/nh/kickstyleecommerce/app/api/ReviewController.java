package org.longg.nh.kickstyleecommerce.app.api;

import com.eps.shared.interfaces.api.IBaseApi;
import com.eps.shared.interfaces.services.IBaseService;
import lombok.RequiredArgsConstructor;
import org.longg.nh.kickstyleecommerce.domain.dtos.requests.review.ReviewRequest;
import org.longg.nh.kickstyleecommerce.domain.dtos.responses.ReviewResponse;
import org.longg.nh.kickstyleecommerce.domain.entities.Review;
import org.longg.nh.kickstyleecommerce.domain.services.ReviewsService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
public class ReviewController
    implements IBaseApi<Review, Long, ReviewResponse, ReviewRequest, ReviewResponse> {

  private final ReviewsService reviewService;

  @Override
  public IBaseService<Review, Long, ReviewResponse, ReviewRequest, ReviewResponse> getService() {
    return reviewService;
  }
}
