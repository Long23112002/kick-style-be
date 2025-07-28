package org.longg.nh.kickstyleecommerce.domain.services.products;

import com.eps.shared.interfaces.persistence.IBasePersistence;
import com.eps.shared.interfaces.services.IBaseService;
import com.eps.shared.models.HeaderContext;
import com.eps.shared.models.exceptions.ResponseException;
import lombok.RequiredArgsConstructor;
import org.longg.nh.kickstyleecommerce.domain.dtos.requests.products.TeamRequest;
import org.longg.nh.kickstyleecommerce.domain.dtos.responses.products.TeamResponse;
import org.longg.nh.kickstyleecommerce.domain.entities.Team;
import org.longg.nh.kickstyleecommerce.domain.persistence.TeamPersistence;
import org.longg.nh.kickstyleecommerce.domain.repositories.ProductRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TeamService implements IBaseService<Team, Long, TeamResponse, TeamRequest, TeamResponse> {

  private final TeamPersistence teamPersistence;

  @Override
  public IBasePersistence<Team, Long> getPersistence() {
    return teamPersistence;
  }

  private final ProductRepository productRepository;

  @Override
  public void delete(HeaderContext context, Long aLong) {
    if (productRepository.existsByTeamId(aLong)) {
      throw new ResponseException(HttpStatus.BAD_REQUEST,
              "Không thể xóa đội bóng này vì đang có sản phẩm sử dụng");
    }
    IBaseService.super.delete(context, aLong);
  }
}
