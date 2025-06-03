package org.longg.nh.kickstyleecommerce.app.api;

import com.eps.shared.interfaces.api.IBaseApi;
import com.eps.shared.interfaces.services.IBaseService;
import lombok.RequiredArgsConstructor;
import org.longg.nh.kickstyleecommerce.domain.dtos.requests.auth.UserRequest;
import org.longg.nh.kickstyleecommerce.domain.dtos.responses.auth.UserResponse;
import org.longg.nh.kickstyleecommerce.domain.entities.User;
import org.longg.nh.kickstyleecommerce.domain.services.UserService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController implements IBaseApi<User ,Long, UserResponse , UserRequest , UserResponse > {

    private final UserService userService;

    @Override
    public IBaseService<User, Long, UserResponse, UserRequest, UserResponse> getService() {
        return userService;
    }
}
