package org.longg.nh.kickstyleecommerce.app.api;

import com.eps.shared.interfaces.api.IBaseApi;
import com.eps.shared.interfaces.services.IBaseService;
import lombok.RequiredArgsConstructor;
import org.longg.nh.kickstyleecommerce.domain.dtos.requests.AnswerRequest;
import org.longg.nh.kickstyleecommerce.domain.dtos.responses.AnswerResponse;
import org.longg.nh.kickstyleecommerce.domain.entities.Answers;
import org.longg.nh.kickstyleecommerce.domain.services.AnswerService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/answers")
@RequiredArgsConstructor
public class AnswerController
    implements IBaseApi<Answers, Long, AnswerResponse, AnswerRequest, AnswerResponse> {

  private final AnswerService answerService;

  @Override
  public IBaseService<Answers, Long, AnswerResponse, AnswerRequest, AnswerResponse> getService() {
    return answerService;
  }
}
