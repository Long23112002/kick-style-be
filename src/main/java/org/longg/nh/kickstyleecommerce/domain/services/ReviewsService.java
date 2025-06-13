package org.longg.nh.kickstyleecommerce.domain.services;

import com.eps.shared.interfaces.persistence.IBasePersistence;
import com.eps.shared.interfaces.services.IBaseService;
import com.eps.shared.models.HeaderContext;
import com.eps.shared.models.exceptions.ResponseException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.longg.nh.kickstyleecommerce.domain.dtos.requests.review.ReviewRequest;
import org.longg.nh.kickstyleecommerce.domain.dtos.responses.ReviewResponse;
import org.longg.nh.kickstyleecommerce.domain.entities.Order;
import org.longg.nh.kickstyleecommerce.domain.entities.Review;
import org.longg.nh.kickstyleecommerce.domain.entities.User;
import org.longg.nh.kickstyleecommerce.domain.entities.enums.OrderStatus;
import org.longg.nh.kickstyleecommerce.domain.persistence.ReviewsPersistence;
import org.longg.nh.kickstyleecommerce.domain.repositories.ReviewsRepository;
import org.longg.nh.kickstyleecommerce.domain.repositories.UserRepository;
import org.longg.nh.kickstyleecommerce.domain.services.orders.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReviewsService
    implements IBaseService<Review, Long, ReviewResponse, ReviewRequest, ReviewResponse> {

  private final ReviewsPersistence reviewsPersistence;
  private final ReviewsRepository reviewsRepository;
  private final OrderService orderService;
  private final UserRepository userRepository;
  private final ObjectMapper objectMapper;

  @Override
  public IBasePersistence<Review, Long> getPersistence() {
    return reviewsPersistence;
  }

  public ReviewResponse createReview(ReviewRequest reviewRequest)
      throws ResponseException {
    User user = userRepository.findById(reviewRequest.getUserId())
        .orElseThrow(() -> new ResponseException(HttpStatus.NOT_FOUND, "User not found"));

    Order order = orderService.finById(reviewRequest.getOrderId());
    if (order.getStatus() != OrderStatus.DELIVERED) {
      throw new ResponseException(HttpStatus.BAD_REQUEST, "Order must be delivered to leave a review");
    }

    Review review = new Review();
    review.setRating(reviewRequest.getRating());
    review.setComment(reviewRequest.getComment());
    review.setOrder(order);
    review.setUser(user);

    return objectMapper.convertValue(
        reviewsRepository.save(review), ReviewResponse.class);
  }
}
