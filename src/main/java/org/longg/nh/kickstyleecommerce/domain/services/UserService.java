package org.longg.nh.kickstyleecommerce.domain.services;

import com.eps.shared.interfaces.persistence.IBasePersistence;
import com.eps.shared.interfaces.services.IBaseService;
import lombok.RequiredArgsConstructor;
import org.longg.nh.kickstyleecommerce.domain.dtos.requests.auth.UserRequest;
import org.longg.nh.kickstyleecommerce.domain.dtos.responses.auth.UserResponse;
import org.longg.nh.kickstyleecommerce.domain.entities.User;
import org.longg.nh.kickstyleecommerce.domain.persistence.UserPersistence;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService implements IBaseService<User , Long , UserResponse , UserRequest , UserResponse> {

    private final UserPersistence userPersistence;

    @Override
    public IBasePersistence<User, Long> getPersistence() {
        return userPersistence;
    }


}
