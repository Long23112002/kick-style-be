package org.longg.nh.kickstyleecommerce.domain.services;

import com.eps.shared.interfaces.persistence.IBasePersistence;
import com.eps.shared.interfaces.services.IBaseService;
import com.eps.shared.models.HeaderContext;
import com.eps.shared.models.exceptions.ResponseException;
import lombok.RequiredArgsConstructor;
import org.longg.nh.kickstyleecommerce.domain.dtos.requests.review.ReviewRequest;
import org.longg.nh.kickstyleecommerce.domain.dtos.responses.ReviewResponse;
import org.longg.nh.kickstyleecommerce.domain.entities.Order;
import org.longg.nh.kickstyleecommerce.domain.entities.Review;
import org.longg.nh.kickstyleecommerce.domain.entities.enums.OrderStatus;
import org.longg.nh.kickstyleecommerce.domain.persistence.ReviewsPersistence;
import org.longg.nh.kickstyleecommerce.domain.services.orders.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReviewsService
    implements IBaseService<Review, Long, ReviewResponse, ReviewRequest, ReviewResponse> {

  private final ReviewsPersistence reviewsPersistence;
  private final UserService userService;
  private final OrderService orderService;

  @Override
  public IBasePersistence<Review, Long> getPersistence() {
    return reviewsPersistence;
  }

  @Override
  public void postCreateHandler(HeaderContext context, Review entity, ReviewRequest request) {
    Order order = orderService.getEntityById(context, request.getOrderId());
    if (order.getStatus().equals(OrderStatus.DELIVERED)) {
      entity.setUser(userService.getEntityById(context, request.getUserId()));
      entity.setOrder(orderService.getEntityById(context, request.getOrderId()));
    } else {
      throw new ResponseException(
          HttpStatus.BAD_REQUEST, "Ban chua the order nay, khong the danh gia san pham");
    }
    IBaseService.super.postCreateHandler(context, entity, request);
  }
}
