package org.longg.nh.kickstyleecommerce.domain.persistence;

import com.eps.shared.interfaces.persistence.IBasePersistence;
import com.eps.shared.interfaces.repository.IBaseRepository;
import lombok.RequiredArgsConstructor;
import org.longg.nh.kickstyleecommerce.domain.entities.Answers;
import org.longg.nh.kickstyleecommerce.domain.repositories.AnswersRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AnswerPersistence implements IBasePersistence<Answers, Long> {

  private final AnswersRepository answersRepository;

  @Override
  public IBaseRepository<Answers, Long> getIBaseRepository() {
    return answersRepository;
  }
}
