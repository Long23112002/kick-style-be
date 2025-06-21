package org.longg.nh.kickstyleecommerce.domain.services;

import com.eps.shared.interfaces.persistence.IBasePersistence;
import com.eps.shared.interfaces.services.IBaseService;
import com.eps.shared.models.HeaderContext;
import lombok.RequiredArgsConstructor;
import org.longg.nh.kickstyleecommerce.domain.dtos.requests.AnswerRequest;
import org.longg.nh.kickstyleecommerce.domain.dtos.responses.AnswerResponse;
import org.longg.nh.kickstyleecommerce.domain.entities.Answers;
import org.longg.nh.kickstyleecommerce.domain.entities.Review;
import org.longg.nh.kickstyleecommerce.domain.persistence.AnswerPersistence;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AnswerService
    implements IBaseService<Answers, Long, AnswerResponse, AnswerRequest, AnswerResponse> {

  private final AnswerPersistence answersPersistence;
  private final ReviewsService reviewsService;
  private final UserService userService;

  @Override
  public IBasePersistence<Answers, Long> getPersistence() {
    return answersPersistence;
  }

    @Override
    public void postCreateHandler(HeaderContext context, Answers entity, AnswerRequest request) {
        IBaseService.super.postCreateHandler(context, entity, request);
    }

    @Override
    public void validateCreateRequest(HeaderContext context, Answers entity, AnswerRequest request) {
        entity.setReview(reviewsService.getEntityById(context, request.getReviewId()));
        entity.setUser(userService.getEntityById(context, request.getUserId()));
        IBaseService.super.validateCreateRequest(context, entity, request);
    }
}
