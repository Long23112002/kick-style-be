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
import org.longg.nh.kickstyleecommerce.domain.entities.Product;
import org.longg.nh.kickstyleecommerce.domain.entities.Review;
import org.longg.nh.kickstyleecommerce.domain.entities.User;
import org.longg.nh.kickstyleecommerce.domain.entities.enums.OrderStatus;
import org.longg.nh.kickstyleecommerce.domain.persistence.ReviewsPersistence;
import org.longg.nh.kickstyleecommerce.domain.repositories.ProductRepository;
import org.longg.nh.kickstyleecommerce.domain.repositories.ReviewsRepository;
import org.longg.nh.kickstyleecommerce.domain.repositories.UserRepository;
import org.longg.nh.kickstyleecommerce.domain.services.orders.OrderService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
  private final ProductRepository productRepository;
  private final ObjectMapper objectMapper;

  @Override
  public IBasePersistence<Review, Long> getPersistence() {
    return reviewsPersistence;
  }

  public ReviewResponse createReview(ReviewRequest reviewRequest) throws ResponseException {
    User user =
        userRepository
            .findById(reviewRequest.getUserId())
            .orElseThrow(() -> new ResponseException(HttpStatus.NOT_FOUND, "User not found"));

    Order order = orderService.finById(reviewRequest.getOrderId());
    if (order.getStatus() != OrderStatus.DELIVERED) {
      throw new ResponseException(
          HttpStatus.BAD_REQUEST, "Order must be delivered to leave a review");
    }

    User reviewUser =
        userRepository
            .findById(reviewRequest.getUserId())
            .orElseThrow(() -> new ResponseException(HttpStatus.NOT_FOUND, "User not found"));

    Review review = new Review();
    if (reviewRequest.getIsAdmin() != null && reviewRequest.getIsAdmin()) {
      review.setIsDeleted(false);
    } else {
      review.setIsDeleted(true);
    }
    review.setUser(reviewUser);
    review.setRating(reviewRequest.getRating());
    review.setComment(reviewRequest.getComment());
    review.setOrder(order);
    review.setProductId(
        productRepository
            .findById(reviewRequest.getProductId())
            .orElseThrow(() -> new ResponseException(HttpStatus.NOT_FOUND, "Product not found"))
            .getId());
    review.setUser(user);
    review.setImages(reviewRequest.getImages());

    return mapToReviewResponse(reviewsRepository.save(review));
  }

  public Page<ReviewResponse> getAll(HeaderContext context, Pageable pageable) {
    Page<Review> reviews = reviewsRepository.findAll(pageable);
    return reviews.map(this::mapToReviewResponse);
  }

  private ReviewResponse mapToReviewResponse(Review review) {
    ReviewResponse response = new ReviewResponse();
    response.setId(review.getId());
    response.setRating(review.getRating());
    response.setComment(review.getComment());
    response.setImages(review.getImages());
    response.setCreatedAt(review.getCreatedAt());
    response.setUpdatedAt(review.getUpdatedAt());
    response.setIsDeleted(review.getIsDeleted());

    // Map user info
    if (review.getUser() != null) {
      User user = review.getUser();
      User userInfo = new User();
      userInfo.setId(user.getId());
      userInfo.setFullName(user.getFullName());
      userInfo.setEmail(user.getEmail());
      userInfo.setAvatarUrl(user.getAvatarUrl());
      response.setUser(userInfo);
    }

    // Map order info
    if (review.getOrder() != null) {
      Order order = review.getOrder();
      Order orderInfo = new Order();
      orderInfo.setId(order.getId());
      orderInfo.setCode(order.getCode());
      response.setOrder(orderInfo);
    }

    return response;
  }
}
